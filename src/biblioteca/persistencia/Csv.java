package biblioteca.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Lectura de los archivos CSV antiguos (solo para la importacion inicial a PostgreSQL). */
final class Csv {

    static final String SEPARADOR = ";";

    private Csv() { }

    /** Lineas de datos ya separadas en campos (sin cabecera ni lineas vacias). */
    static List<String[]> leer(Path archivo) throws IOException {
        List<String[]> filas = new ArrayList<>();
        if (!Files.exists(archivo)) return filas;
        List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        for (int i = 1; i < lineas.size(); i++) {
            if (!lineas.get(i).isBlank()) filas.add(lineas.get(i).split(SEPARADOR, -1));
        }
        return filas;
    }
}
