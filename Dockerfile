# ===========================================================================
# Mesa 2.0 — imagen de producción: la SPA y la API en un único jar.
#
#   1. Node compila el frontend (Vite + TypeScript).
#   2. Maven empaqueta el backend con el dist dentro de src/main/resources/static.
#   3. La imagen final solo lleva el JRE y el jar.
#
#   Construcción:  docker build -t mesa2 .
#   Prueba:        docker run --rm -p 8080:8080 mesa2
#                  (perfil `local`: H2 de fichero, datos demo, sin configurar nada)
#   Producción:    con SPRING_PROFILES_ACTIVE=produccion y las variables
#                  SPRING_DATASOURCE_* / MESA2_JWT_SECRET / MESA2_COOKIE_SECURE
#                  apuntando a PostgreSQL (ver README).
# ===========================================================================

# ---------------------------------------------------------- 1) SPA
FROM node:24-alpine AS spa
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---------------------------------------------------------- 2) Backend + SPA
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app/backend
# El pom primero: así se cachean las dependencias entre builds.
COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
# La SPA compilada entra en el jar como recursos estáticos.
COPY --from=spa /app/frontend/dist ./src/main/resources/static
RUN mvn -B -q -DskipTests package

# ---------------------------------------------------------- 3) Imagen final
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home --shell /usr/sbin/nologin mesa2
COPY --from=backend /app/backend/target/mesa2-*.jar app.jar
# El usuario sin privilegios debe poder crear ./datos (H2 del perfil local).
RUN chown -R mesa2:mesa2 /app
USER mesa2

# Salida "de fábrica": perfil local (H2 + datos demo). En el hosting se
# sobreescribe con SPRING_PROFILES_ACTIVE + las variables del entorno.
ENV SPRING_PROFILES_ACTIVE=local
ENV JAVA_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar \"$@\"", "mesa2"]
