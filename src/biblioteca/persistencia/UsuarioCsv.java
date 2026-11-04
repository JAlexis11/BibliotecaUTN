package biblioteca.persistencia;

import biblioteca.modelo.Usuario;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UsuarioCsv {

    private final Path archivo;

    public UsuarioCsv(Path archivo) {
        this.archivo = archivo;
    }

    public List<Usuario> cargar() throws IOException {
        List<Usuario> usuarios = new ArrayList<>();
        for (String[] c : Csv.leer(archivo)) {
            try {
                if (c.length < 2) throw new IllegalArgumentException("faltan campos");
                usuarios.add(new Usuario(c[0], c[1]));
            } catch (RuntimeException e) {
                throw new IOException("Registro invalido en " + archivo + " (" + String.join(";", c) + "): "
                        + e.getMessage());
            }
        }
        return usuarios;
    }
}
