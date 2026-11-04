package biblioteca.modelo;

import java.math.BigDecimal;

public class Libro extends Material {

    public static final String TIPO = "LIBRO";
    public static final int DIAS_PRESTAMO = 7;
    public static final BigDecimal MULTA_POR_DIA = new BigDecimal("0.25");

    private String autor;

    public Libro(String codigo, String titulo, int ejemplares, String autor) {
        super(codigo, titulo, ejemplares);
        setAutor(autor);
    }

    @Override
    public int diasPrestamo() { return DIAS_PRESTAMO; }

    @Override
    public BigDecimal multaPorDia() { return MULTA_POR_DIA; }

    @Override
    public String getTipo() { return TIPO; }

    @Override
    public String valorEspecifico() { return autor; }

    @Override
    public String descripcionEspecifica() { return "autor: " + autor; }

    public String getAutor() { return autor; }

    public void setAutor(String autor) {
        if (autor == null || autor.isBlank()) {
            throw new IllegalArgumentException("El autor es obligatorio");
        }
        this.autor = autor.trim();
    }
}
