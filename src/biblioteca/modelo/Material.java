package biblioteca.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Titulo de la biblioteca (libro o revista) con varios ejemplares.
 * Los ejemplares prestados se calculan a partir de los prestamos activos.
 */
public abstract class Material {

    private final String codigo;
    private String titulo;
    private int ejemplares;
    private int prestados;

    protected Material(String codigo, String titulo, int ejemplares) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }
        this.codigo = codigo.trim().toUpperCase();
        setTitulo(titulo);
        setEjemplares(ejemplares);
        this.prestados = 0;
    }

    /** Dias que dura el prestamo de este tipo de material. */
    public abstract int diasPrestamo();

    /** Valor de la multa por cada dia de retraso. */
    public abstract BigDecimal multaPorDia();

    /** Nombre del tipo de material, usado tambien al guardar en archivo. */
    public abstract String getTipo();

    /** Atributo propio de cada subclase en formato texto (para archivo). */
    public abstract String valorEspecifico();

    /** Atributo propio de cada subclase en formato legible. */
    public abstract String descripcionEspecifica();

    public BigDecimal calcularMulta(long diasRetraso) {
        if (diasRetraso <= 0) return BigDecimal.ZERO.setScale(2);
        return multaPorDia().multiply(BigDecimal.valueOf(diasRetraso)).setScale(2, RoundingMode.HALF_UP);
    }

    public int getDisponibles() { return ejemplares - prestados; }

    public boolean hayDisponibles() { return getDisponibles() > 0; }

    public void prestarEjemplar() {
        if (!hayDisponibles()) {
            throw new IllegalStateException("No hay ejemplares disponibles de " + codigo + " - " + titulo);
        }
        prestados++;
    }

    public void devolverEjemplar() {
        if (prestados == 0) {
            throw new IllegalStateException("No hay ejemplares prestados de " + codigo);
        }
        prestados--;
    }

    public String getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public int getEjemplares() { return ejemplares; }
    public int getPrestados() { return prestados; }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El titulo es obligatorio");
        }
        this.titulo = titulo.trim();
    }

    /** No permite dejar menos ejemplares que los que estan prestados. */
    public void setEjemplares(int ejemplares) {
        if (ejemplares < 1) {
            throw new IllegalArgumentException("Debe existir al menos 1 ejemplar");
        }
        if (ejemplares < prestados) {
            throw new IllegalArgumentException("Hay " + prestados + " ejemplares prestados; no puede dejar "
                    + ejemplares);
        }
        this.ejemplares = ejemplares;
    }

    @Override
    public String toString() {
        return String.format("%-8s %-7s %-32s %-26s ejemplares:%3d  disponibles:%3d",
                codigo, getTipo(), titulo, descripcionEspecifica(), ejemplares, getDisponibles());
    }
}
