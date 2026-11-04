package biblioteca.persistencia;

import biblioteca.modelo.Libro;
import biblioteca.modelo.Material;
import biblioteca.modelo.Revista;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Materiales guardados en la tabla material de PostgreSQL. */
public class MaterialRepositorio {

    private static final String SELECT =
            "SELECT tipo, codigo, titulo, ejemplares, especifico FROM material ORDER BY codigo";
    private static final String UPSERT =
            "INSERT INTO material (codigo, tipo, titulo, ejemplares, especifico) VALUES (?, ?, ?, ?, ?)"
            + " ON CONFLICT (codigo) DO UPDATE SET tipo = EXCLUDED.tipo, titulo = EXCLUDED.titulo,"
            + " ejemplares = EXCLUDED.ejemplares, especifico = EXCLUDED.especifico";

    private final ConexionBD bd;

    public MaterialRepositorio(ConexionBD bd) {
        this.bd = bd;
    }

    public List<Material> cargar() throws IOException {
        List<Material> materiales = new ArrayList<>();
        try (Connection c = bd.abrir(); PreparedStatement ps = c.prepareStatement(SELECT);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tipo = rs.getString("tipo");
                String codigo = rs.getString("codigo");
                String titulo = rs.getString("titulo");
                int ejemplares = rs.getInt("ejemplares");
                String especifico = rs.getString("especifico");
                try {
                    if (tipo.equals(Libro.TIPO)) materiales.add(new Libro(codigo, titulo, ejemplares, especifico));
                    else if (tipo.equals(Revista.TIPO)) materiales.add(new Revista(codigo, titulo, ejemplares, especifico));
                    else throw new IllegalArgumentException("tipo desconocido: " + tipo);
                } catch (RuntimeException e) {
                    throw new IOException("Material invalido en la base (" + codigo + "): " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw ConexionBD.error("leer materiales", e);
        }
        return materiales;
    }

    /** Inserta o actualiza todos los materiales en una sola transaccion. */
    public void guardarTodos(Collection<Material> materiales) throws IOException {
        try (Connection c = bd.abrir()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(UPSERT)) {
                for (Material m : materiales) {
                    ps.setString(1, m.getCodigo());
                    ps.setString(2, m.getTipo());
                    ps.setString(3, m.getTitulo());
                    ps.setInt(4, m.getEjemplares());
                    ps.setString(5, m.valorEspecifico());
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ConexionBD.error("guardar materiales", e);
        }
    }
}
