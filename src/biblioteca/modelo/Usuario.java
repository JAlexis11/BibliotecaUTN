package biblioteca.modelo;

public class Usuario {

    private final String cedula;
    private String nombre;

    public Usuario(String cedula, String nombre) {
        if (cedula == null || !cedula.trim().matches("\\d{10}")) {
            throw new IllegalArgumentException("La cedula debe tener 10 digitos");
        }
        this.cedula = cedula.trim();
        setNombre(nombre);
    }

    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    @Override
    public String toString() {
        return String.format("%-10s  %s", cedula, nombre);
    }
}
