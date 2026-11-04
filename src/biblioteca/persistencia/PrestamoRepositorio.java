package biblioteca.persistencia;

import biblioteca.modelo.Prestamo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Prestamos guardados en la tabla prestamo de PostgreSQL. */
public class PrestamoRepositorio {

    private static final String SELECT = "SELECT numero, cedula, codigo_material, fecha_prestamo,"
            + " fecha_vencimiento, fecha_devolucion, multa, multa_pagada FROM prestamo ORDER BY numero";
    private static final String UPSERT = "INSERT INTO prestamo (numero, cedula, codigo_material, fecha_prestamo,"
            + " fecha_vencimiento, fecha_devolucion, multa, multa_pagada) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            + " ON CONFLICT (numero) DO UPDATE SET fecha_devolucion = EXCLUDED.fecha_devolucion,"
            + " multa = EXCLUDED.multa, multa_pagada = EXCLUDED.multa_pagada";

    private final ConexionBD bd;

    public PrestamoRepositorio(ConexionBD bd) {
        this.bd = bd;
    }

    public List<Prestamo> cargar() throws IOException {
        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection c = bd.abrir(); PreparedStatement ps = c.prepareStatement(SELECT);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long numero = rs.getLong("numero");
                Date devolucion = rs.getDate("fecha_devolucion");
                try {
                    prestamos.add(new Prestamo(numero, rs.getString("cedula"), rs.getString("codigo_material"),
                            rs.getDate("fecha_prestamo").toLocalDate(), rs.getDate("fecha_vencimiento").toLocalDate(),
                            devolucion == null ? null : devolucion.toLocalDate(),
                            rs.getBigDecimal("multa"), rs.getBoolean("multa_pagada")));
                } catch (RuntimeException e) {
                    throw new IOException("Prestamo invalido en la base (#" + numero + "): " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw ConexionBD.error("leer prestamos", e);
        }
        return prestamos;
    }

    /** Inserta o actualiza todos los prestamos en una sola transaccion. */
    public void guardarTodos(Collection<Prestamo> prestamos) throws IOException {
        try (Connection c = bd.abrir()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(UPSERT)) {
                for (Prestamo p : prestamos) {
                    ps.setLong(1, p.getNumero());
                    ps.setString(2, p.getCedulaUsuario());
                    ps.setString(3, p.getCodigoMaterial());
                    ps.setDate(4, Date.valueOf(p.getFechaPrestamo()));
                    ps.setDate(5, Date.valueOf(p.getFechaVencimiento()));
                    if (p.getFechaDevolucion() == null) ps.setNull(6, Types.DATE);
                    else ps.setDate(6, Date.valueOf(p.getFechaDevolucion()));
                    ps.setBigDecimal(7, p.getMulta());
                    ps.setBoolean(8, p.isMultaPagada());
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ConexionBD.error("guardar prestamos", e);
        }
    }
}
