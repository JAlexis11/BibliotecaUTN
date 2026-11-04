package biblioteca.servicio;

import biblioteca.modelo.Material;

/**
 * Fila de la auditoria de ejemplares: compara el contador del material con
 * los prestamos activos registrados.
 */
public class LineaAuditoria {

    private final Material material;
    private final int prestamosActivos;

    public LineaAuditoria(Material material, int prestamosActivos) {
        this.material = material;
        this.prestamosActivos = prestamosActivos;
    }

    public Material getMaterial() { return material; }
    public int getPrestamosActivos() { return prestamosActivos; }

    public int getDisponiblesEsperados() {
        return material.getEjemplares() - prestamosActivos;
    }

    public boolean esConsistente() {
        return material.getPrestados() == prestamosActivos && getDisponiblesEsperados() >= 0;
    }
}
