package com.royaltown.mesa.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas del puente entre las variables de Neon ({@code DATABASE_URL} /
 * {@code DATABASE_URL_UNPOOLED}) y la configuración JDBC de Spring.
 */
class NeonVariablesTest {

    private static final String POOLED = "postgresql://neondb_owner:npg_Clave1"
            + "@ep-ejemplo-pooler.c-7.us-east-2.aws.neon.tech/neondb"
            + "?sslmode=require&channel_binding=require";
    private static final String DIRECTA = "postgresql://neondb_owner:npg_Clave1"
            + "@ep-ejemplo.c-7.us-east-2.aws.neon.tech/neondb"
            + "?sslmode=require&channel_binding=require";

    @Test
    void mapeaLasDosCadenasDeNeon() {
        Map<String, String> propiedades = NeonVariables.mapear(
                Map.of("DATABASE_URL", POOLED, "DATABASE_URL_UNPOOLED", DIRECTA), List.of());

        assertThat(propiedades).containsEntry("spring.datasource.url",
                "jdbc:postgresql://ep-ejemplo-pooler.c-7.us-east-2.aws.neon.tech/neondb?sslmode=require");
        assertThat(propiedades).containsEntry("spring.datasource.username", "neondb_owner");
        assertThat(propiedades).containsEntry("spring.datasource.password", "npg_Clave1");
        assertThat(propiedades).containsEntry("spring.flyway.url",
                "jdbc:postgresql://ep-ejemplo.c-7.us-east-2.aws.neon.tech/neondb?sslmode=require");
        assertThat(propiedades).containsEntry("spring.flyway.user", "neondb_owner");
        assertThat(propiedades).containsEntry("spring.flyway.password", "npg_Clave1");
    }

    @Test
    void sinCadenaDirectaFlywayReutilizaLaPooled() {
        Map<String, String> propiedades = NeonVariables.mapear(
                Map.of("DATABASE_URL", POOLED), List.of());

        assertThat(propiedades).containsEntry("spring.flyway.url",
                propiedades.get("spring.datasource.url"));
    }

    @Test
    void laConfiguracionExplicitaDeSpringTienePrioridad() {
        Map<String, String> propiedades = NeonVariables.mapear(
                Map.of("DATABASE_URL", POOLED,
                        "SPRING_DATASOURCE_URL", "jdbc:postgresql://otro:5432/mesa2"),
                List.of());

        assertThat(propiedades).isEmpty();
    }

    @Test
    void elPerfilLocalConservaH2() {
        assertThat(NeonVariables.mapear(Map.of("DATABASE_URL", POOLED),
                List.of("--spring.profiles.active=local"))).isEmpty();

        assertThat(NeonVariables.mapear(
                Map.of("DATABASE_URL", POOLED, "SPRING_PROFILES_ACTIVE", "produccion,local"),
                List.of())).isEmpty();
    }

    @Test
    void sinVariablesDeNeonNoHaceNada() {
        assertThat(NeonVariables.mapear(Map.of(), List.of())).isEmpty();
    }

    @Test
    void anadeSslmodeCuandoLaCadenaNoLoTiene() {
        assertThat(NeonVariables.aJdbc("postgresql://usuario:clave@host.neon.tech/neondb"))
                .isEqualTo("jdbc:postgresql://host.neon.tech/neondb?sslmode=require");
    }

    @Test
    void conservaPuertoYParametrosUtiles() {
        assertThat(NeonVariables.aJdbc(
                "postgres://usuario:clave@host.neon.tech:5432/neondb?application_name=mesa2"))
                .isEqualTo("jdbc:postgresql://host.neon.tech:5432/neondb?application_name=mesa2&sslmode=require");
    }

    @Test
    void aceptaCadenasJdbcYaPreparadas() {
        Map<String, String> propiedades = NeonVariables.mapear(
                Map.of("DATABASE_URL", "jdbc:postgresql://host/neondb?sslmode=require"), List.of());

        assertThat(propiedades).containsEntry("spring.datasource.url",
                "jdbc:postgresql://host/neondb?sslmode=require");
        assertThat(propiedades).doesNotContainKey("spring.datasource.username");
    }

    @Test
    void ignoraCadenasDeOtrosMotores() {
        assertThat(NeonVariables.mapear(
                Map.of("DATABASE_URL", "mysql://usuario:clave@host/mesa2"), List.of()))
                .isEmpty();
    }

    @Test
    void decodificaLasCredencialesEscapadas() {
        Map<String, String> propiedades = NeonVariables.mapear(
                Map.of("DATABASE_URL",
                        "postgresql://usuario:cl%40ve%2Bab@host/neondb?sslmode=require"),
                List.of());

        assertThat(propiedades).containsEntry("spring.datasource.password", "cl@ve+ab");
    }
}
