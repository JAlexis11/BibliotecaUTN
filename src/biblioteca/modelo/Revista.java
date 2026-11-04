package biblioteca.modelo;

import java.math.BigDecimal;

public class Revista extends Material {

    public static final String TIPO = "REVISTA";
    public static final int DIAS_PRESTAMO = 3;
    public static final BigDecimal MULTA_POR_DIA = new BigDecimal("0.50");

    private String edicion;

    public Revista(String codigo, String titulo, int ejemplares, String edicion) {
        super(codigo, titulo, ejemplares);
        setEdicion(edicion);
    }

    @Override
    public int diasPrestamo() { return DIAS_PRESTAMO; }

    @Override
    public BigDecimal multaPorDia() { return MULTA_POR_DIA; }

    @Override
    public String getTipo() { return TIPO; }

    @Override
    public String valorEspecifico() { return edicion; }

    @Override
    public String descripcionEspecifica() { return "edicion: " + edicion; }

    public String getEdicion() { return edicion; }

    public void setEdicion(String edicion) {
        if (edicion == null || edicion.isBlank()) {
            throw new IllegalArgumentException("La edicion es obligatoria");
        }
        this.edicion = edicion.trim();
    }
}
