package com.royaltown.mesa.servicio;

import com.royaltown.mesa.dto.InformeDtos.EstadoTotal;
import com.royaltown.mesa.dto.InformeDtos.KpisDto;
import com.royaltown.mesa.dto.InformeDtos.TecnicoRendimiento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Los KPI salen directamente de las vistas SQL del esquema (vw_tickets_abiertos,
 * vw_sla_incumplido, vw_tickets_por_estado, vw_resolucion_por_tecnico): la web no
 * inventa métricas propias, mide lo mismo que el CLI de ticketing.
 */
@Service
public class InformeServicio {

    private final JdbcTemplate jdbc;

    public InformeServicio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public KpisDto kpis() {
        Long abiertos = jdbc.queryForObject("SELECT COUNT(*) FROM vw_tickets_abiertos", Long.class);
        Long slaIncumplidos = jdbc.queryForObject("SELECT COUNT(*) FROM vw_sla_incumplido", Long.class);

        // Acceso por índice: los nombres de columna cambian según el motor.
        List<EstadoTotal> porEstado = jdbc.query(
                "SELECT nombre, total FROM vw_tickets_por_estado ORDER BY orden",
                (rs, i) -> new EstadoTotal(rs.getString(1), rs.getLong(2)));

        List<TecnicoRendimiento> porTecnico = jdbc.query(
                "SELECT nombre_completo, tickets_resueltos, minutos_medios "
                        + "FROM vw_resolucion_por_tecnico ORDER BY tickets_resueltos DESC, nombre_completo",
                (rs, i) -> new TecnicoRendimiento(rs.getString(1), rs.getLong(2),
                        redondear(rs.getBigDecimal(3))));

        return new KpisDto(abiertos == null ? 0 : abiertos,
                slaIncumplidos == null ? 0 : slaIncumplidos,
                porEstado, porTecnico);
    }

    private static Long redondear(BigDecimal valor) {
        return valor == null ? null : valor.setScale(0, RoundingMode.HALF_UP).longValue();
    }
}
