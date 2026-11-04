package biblioteca.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Prestamo de un ejemplar a un usuario. La fecha de vencimiento se fija al prestar
 * segun el tipo de material; la multa se genera al devolver.
 */
public class Prestamo {

    private final long numero;
    private final String cedulaUsuario;
    private final String codigoMaterial;
    private final LocalDate fechaPrestamo;
    private final LocalDate fechaVencimiento;
    private LocalDate fechaDevolucion;
    private BigDecimal multa;
    private boolean multaPagada;

    /** Nuevo prestamo. */
    public Prestamo(long numero, Usuario usuario, Material material, LocalDate fechaPrestamo) {
        this(numero, usuario.getCedula(), material.getCodigo(), fechaPrestamo,
                fechaPrestamo.plusDays(material.diasPrestamo()), null, BigDecimal.ZERO.setScale(2), false);
    }

    /** Prestamo leido desde archivo. */
    public Prestamo(long numero, String cedulaUsuario, String codigoMaterial, LocalDate fechaPrestamo,
                    LocalDate fechaVencimiento, LocalDate fechaDevolucion, BigDecimal multa, boolean multaPagada) {
        if (numero <= 0) throw new IllegalArgumentException("Numero de prestamo invalido");
        if (fechaPrestamo == null || fechaVencimiento == null || fechaVencimiento.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("Fechas del prestamo invalidas");
        }
        if (fechaDevolucion != null && fechaDevolucion.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La devolucion no puede ser anterior al prestamo");
        }
        if (multa == null || multa.signum() < 0) throw new IllegalArgumentException("Multa invalida");
        this.numero = numero;
        this.cedulaUsuario = cedulaUsuario;
        this.codigoMaterial = codigoMaterial;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaDevolucion = fechaDevolucion;
        this.multa = multa.setScale(2);
        this.multaPagada = multaPagada;
    }

    public boolean estaActivo() { return fechaDevolucion == null; }

    public boolean estaVencido(LocalDate fecha) {
        return estaActivo() && fecha.isAfter(fechaVencimiento);
    }

    /** Dias de retraso a una fecha (si esta activo) o a la fecha en que se devolvio. */
    public long diasRetraso(LocalDate fecha) {
        LocalDate fin = estaActivo() ? fecha : fechaDevolucion;
        return Math.max(0, ChronoUnit.DAYS.between(fechaVencimiento, fin));
    }

    /** Registra la devolucion y genera la multa. Un prestamo no se puede devolver dos veces. */
    public void devolver(LocalDate fecha, Material material) {
        if (!estaActivo()) {
            throw new IllegalStateException("El prestamo #" + numero + " ya fue devuelto el " + fechaDevolucion);
        }
        if (!material.getCodigo().equals(codigoMaterial)) {
            throw new IllegalArgumentException("El material no corresponde a este prestamo");
        }
        if (fecha.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La devolucion no puede ser anterior al prestamo ("
                    + fechaPrestamo + ")");
        }
        this.fechaDevolucion = fecha;
        this.multa = material.calcularMulta(diasRetraso(fecha));
        this.multaPagada = multa.signum() == 0;
    }

    public boolean tieneMultaPendiente() {
        return multa.signum() > 0 && !multaPagada;
    }

    public void pagarMulta() {
        if (!tieneMultaPendiente()) {
            throw new IllegalStateException("El prestamo #" + numero + " no tiene multa pendiente");
        }
        multaPagada = true;
    }

    public String estado(LocalDate fecha) {
        if (estaVencido(fecha)) return "VENCIDO (" + diasRetraso(fecha) + " dias)";
        if (estaActivo()) return "ACTIVO";
        if (tieneMultaPendiente()) return "DEVUELTO - MULTA PENDIENTE";
        if (multa.signum() > 0) return "DEVUELTO - MULTA PAGADA";
        return "DEVUELTO";
    }

    public long getNumero() { return numero; }
    public String getCedulaUsuario() { return cedulaUsuario; }
    public String getCodigoMaterial() { return codigoMaterial; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public BigDecimal getMulta() { return multa; }
    public boolean isMultaPagada() { return multaPagada; }
}
