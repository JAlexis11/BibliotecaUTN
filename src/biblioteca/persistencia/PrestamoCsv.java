package biblioteca.persistencia;

import biblioteca.modelo.Prestamo;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoCsv {

    private final Path archivo;

    public PrestamoCsv(Path archivo) {
        this.archivo = archivo;
    }

    public List<Prestamo> cargar() throws IOException {
        List<Prestamo> prestamos = new ArrayList<>();
        for (String[] c : Csv.leer(archivo)) {
            try {
                prestamos.add(convertir(c));
            } catch (RuntimeException e) {
                throw new IOException("Registro invalido en " + archivo + " (" + String.join(";", c) + "): "
                        + e.getMessage());
            }
        }
        return prestamos;
    }

    private Prestamo convertir(String[] c) {
        if (c.length < 8) throw new IllegalArgumentException("faltan campos");
        LocalDate devolucion = c[5].isBlank() ? null : LocalDate.parse(c[5]);
        return new Prestamo(Long.parseLong(c[0]), c[1], c[2], LocalDate.parse(c[3]), LocalDate.parse(c[4]),
                devolucion, new BigDecimal(c[6]), Boolean.parseBoolean(c[7]));
    }
}
