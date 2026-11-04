package biblioteca.persistencia;

import biblioteca.modelo.Libro;
import biblioteca.modelo.Material;
import biblioteca.modelo.Revista;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MaterialCsv {

    private final Path archivo;

    public MaterialCsv(Path archivo) {
        this.archivo = archivo;
    }

    public List<Material> cargar() throws IOException {
        List<Material> materiales = new ArrayList<>();
        for (String[] c : Csv.leer(archivo)) {
            try {
                materiales.add(convertir(c));
            } catch (RuntimeException e) {
                throw new IOException("Registro invalido en " + archivo + " (" + String.join(";", c) + "): "
                        + e.getMessage());
            }
        }
        return materiales;
    }

    private Material convertir(String[] c) {
        if (c.length < 5) throw new IllegalArgumentException("faltan campos");
        String tipo = c[0];
        int ejemplares = Integer.parseInt(c[3]);
        if (tipo.equals(Libro.TIPO)) return new Libro(c[1], c[2], ejemplares, c[4]);
        if (tipo.equals(Revista.TIPO)) return new Revista(c[1], c[2], ejemplares, c[4]);
        throw new IllegalArgumentException("tipo desconocido: " + tipo);
    }
}
