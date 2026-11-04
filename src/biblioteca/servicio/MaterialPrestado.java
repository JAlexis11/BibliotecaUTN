package biblioteca.servicio;

import biblioteca.modelo.Material;

/** Fila del reporte de materiales mas prestados. */
public class MaterialPrestado {

    private final Material material;
    private final int vecesPrestado;

    public MaterialPrestado(Material material, int vecesPrestado) {
        this.material = material;
        this.vecesPrestado = vecesPrestado;
    }

    public Material getMaterial() { return material; }
    public int getVecesPrestado() { return vecesPrestado; }
}
