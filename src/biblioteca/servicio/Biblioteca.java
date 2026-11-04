package biblioteca.servicio;

import biblioteca.modelo.Material;
import biblioteca.modelo.Prestamo;
import biblioteca.modelo.Usuario;
import biblioteca.persistencia.MaterialRepositorio;
import biblioteca.persistencia.PrestamoRepositorio;
import biblioteca.persistencia.UsuarioRepositorio;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Logica del negocio. Toda operacion pasa por aqui, que valida las reglas,
 * guarda en archivo y luego actualiza la memoria.
 */
public class Biblioteca {

    public static final int MAXIMO_PRESTAMOS_ACTIVOS = 3;

    private final MaterialRepositorio materialRepo;
    private final UsuarioRepositorio usuarioRepo;
    private final PrestamoRepositorio prestamoRepo;
    private final Map<String, Material> materiales = new LinkedHashMap<>();
    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();
    private final List<Prestamo> prestamos = new ArrayList<>();
    private long ultimoNumero = 0;

    public Biblioteca(MaterialRepositorio materialRepo, UsuarioRepositorio usuarioRepo,
                      PrestamoRepositorio prestamoRepo) throws BibliotecaException {
        this.materialRepo = materialRepo;
        this.usuarioRepo = usuarioRepo;
        this.prestamoRepo = prestamoRepo;
        cargarDatos();
    }

    /** Lee los archivos y reconstruye los ejemplares prestados a partir de los prestamos activos. */
    private void cargarDatos() throws BibliotecaException {
        try {
            for (Material m : materialRepo.cargar()) materiales.put(m.getCodigo(), m);
            for (Usuario u : usuarioRepo.cargar()) usuarios.put(u.getCedula(), u);
            for (Prestamo p : prestamoRepo.cargar()) {
                Material m = materiales.get(p.getCodigoMaterial());
                if (m == null || !usuarios.containsKey(p.getCedulaUsuario())) {
                    throw new BibliotecaException("El prestamo #" + p.getNumero()
                            + " hace referencia a un material o usuario que no existe. Archivos inconsistentes.");
                }
                if (p.estaActivo()) m.prestarEjemplar();
                prestamos.add(p);
                ultimoNumero = Math.max(ultimoNumero, p.getNumero());
            }
        } catch (IOException e) {
            throw new BibliotecaException("No se pudieron leer los datos: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new BibliotecaException("Prestamos inconsistentes: " + e.getMessage(), e);
        }
    }

    // ============================================================ MATERIALES

    public void registrarMaterial(Material material) throws BibliotecaException {
        if (materiales.containsKey(material.getCodigo())) {
            throw new BibliotecaException("Ya existe un material con codigo " + material.getCodigo());
        }
        materiales.put(material.getCodigo(), material);
        try {
            materialRepo.guardarTodos(materiales.values());
        } catch (IOException e) {
            materiales.remove(material.getCodigo());
            throw new BibliotecaException("No se pudo guardar: " + e.getMessage(), e);
        }
    }

    /** Agrega (cantidad positiva) o da de baja (cantidad negativa) ejemplares de un titulo. */
    public void ajustarEjemplares(String codigo, int cantidad) throws BibliotecaException {
        Material m = obtenerMaterial(codigo);
        int anterior = m.getEjemplares();
        try {
            m.setEjemplares(anterior + cantidad);
            materialRepo.guardarTodos(materiales.values());
        } catch (IllegalArgumentException e) {
            throw new BibliotecaException(e.getMessage());
        } catch (IOException e) {
            m.setEjemplares(anterior);
            throw new BibliotecaException("No se pudo guardar: " + e.getMessage(), e);
        }
    }

    public Material obtenerMaterial(String codigo) throws BibliotecaException {
        Material m = materiales.get(codigo.trim().toUpperCase());
        if (m == null) throw new BibliotecaException("No existe el material " + codigo);
        return m;
    }

    public List<Material> listarMateriales() {
        return new ArrayList<>(materiales.values());
    }

    public List<Material> buscarMateriales(String texto) {
        String t = texto.trim().toLowerCase();
        List<Material> resultado = new ArrayList<>();
        for (Material m : materiales.values()) {
            if (m.getTitulo().toLowerCase().contains(t) || m.descripcionEspecifica().toLowerCase().contains(t)) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    // ============================================================ USUARIOS

    public void registrarUsuario(Usuario usuario) throws BibliotecaException {
        if (usuarios.containsKey(usuario.getCedula())) {
            throw new BibliotecaException("Ya existe un usuario con cedula " + usuario.getCedula());
        }
        usuarios.put(usuario.getCedula(), usuario);
        try {
            usuarioRepo.guardarTodos(usuarios.values());
        } catch (IOException e) {
            usuarios.remove(usuario.getCedula());
            throw new BibliotecaException("No se pudo guardar: " + e.getMessage(), e);
        }
    }

    public Usuario obtenerUsuario(String cedula) throws BibliotecaException {
        Usuario u = usuarios.get(cedula.trim());
        if (u == null) throw new BibliotecaException("No existe el usuario con cedula " + cedula);
        return u;
    }

    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios.values());
    }

    // ============================================================ PRESTAMOS

    /**
     * Presta un ejemplar validando: ejemplares disponibles, maximo de prestamos activos,
     * multas pendientes y prestamos vencidos sin devolver.
     */
    public Prestamo prestar(String cedula, String codigoMaterial, LocalDate fecha) throws BibliotecaException {
        Usuario usuario = obtenerUsuario(cedula);
        Material material = obtenerMaterial(codigoMaterial);

        BigDecimal pendiente = multaPendiente(usuario.getCedula());
        if (pendiente.signum() > 0) {
            throw new BibliotecaException(usuario.getNombre() + " tiene multas pendientes por $" + pendiente
                    + ". Debe pagarlas antes de solicitar un nuevo prestamo.");
        }
        for (Prestamo p : prestamosActivos(usuario.getCedula())) {
            if (p.estaVencido(fecha)) {
                throw new BibliotecaException(usuario.getNombre() + " tiene el prestamo #" + p.getNumero()
                        + " vencido (multa en curso). Debe devolverlo primero.");
            }
        }
        if (prestamosActivos(usuario.getCedula()).size() >= MAXIMO_PRESTAMOS_ACTIVOS) {
            throw new BibliotecaException(usuario.getNombre() + " ya tiene " + MAXIMO_PRESTAMOS_ACTIVOS
                    + " prestamos activos (maximo permitido).");
        }
        if (!material.hayDisponibles()) {
            throw new BibliotecaException("No hay ejemplares disponibles de " + material.getTitulo());
        }

        Prestamo prestamo = new Prestamo(ultimoNumero + 1, usuario, material, fecha);
        material.prestarEjemplar();
        prestamos.add(prestamo);
        try {
            guardarPrestamos();
        } catch (BibliotecaException e) {
            prestamos.remove(prestamo);
            material.devolverEjemplar();
            throw e;
        }
        ultimoNumero = prestamo.getNumero();
        return prestamo;
    }

    /** Registra la devolucion; si hay retraso genera la multa. */
    public Prestamo devolver(long numero, LocalDate fecha) throws BibliotecaException {
        Prestamo prestamo = obtenerPrestamo(numero);
        Material material = materiales.get(prestamo.getCodigoMaterial());
        try {
            prestamo.devolver(fecha, material);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new BibliotecaException(e.getMessage());
        }
        material.devolverEjemplar();
        guardarPrestamos();
        return prestamo;
    }

    public void pagarMulta(long numero) throws BibliotecaException {
        Prestamo prestamo = obtenerPrestamo(numero);
        try {
            prestamo.pagarMulta();
        } catch (IllegalStateException e) {
            throw new BibliotecaException(e.getMessage());
        }
        guardarPrestamos();
    }

    public Prestamo obtenerPrestamo(long numero) throws BibliotecaException {
        for (Prestamo p : prestamos) {
            if (p.getNumero() == numero) return p;
        }
        throw new BibliotecaException("No existe el prestamo #" + numero);
    }

    public List<Prestamo> prestamosActivos(String cedula) {
        List<Prestamo> resultado = new ArrayList<>();
        for (Prestamo p : prestamos) {
            if (p.estaActivo() && p.getCedulaUsuario().equals(cedula)) resultado.add(p);
        }
        return resultado;
    }

    public BigDecimal multaPendiente(String cedula) {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Prestamo p : prestamos) {
            if (p.getCedulaUsuario().equals(cedula) && p.tieneMultaPendiente()) total = total.add(p.getMulta());
        }
        return total;
    }

    /** Multa que se generaria si el prestamo activo se devolviera en la fecha indicada. */
    public BigDecimal multaEstimada(Prestamo prestamo, LocalDate fecha) {
        return materiales.get(prestamo.getCodigoMaterial()).calcularMulta(prestamo.diasRetraso(fecha));
    }

    // ============================================================ REPORTES

    /** Prestamos sin devolver cuya fecha de vencimiento ya paso, del mas atrasado al menos atrasado. */
    public List<Prestamo> prestamosVencidos(LocalDate fecha) {
        List<Prestamo> resultado = new ArrayList<>();
        for (Prestamo p : prestamos) {
            if (p.estaVencido(fecha)) resultado.add(p);
        }
        resultado.sort(Comparator.comparing(Prestamo::getFechaVencimiento));
        return resultado;
    }

    public List<Prestamo> historialUsuario(String cedula) throws BibliotecaException {
        Usuario u = obtenerUsuario(cedula);
        List<Prestamo> resultado = new ArrayList<>();
        for (Prestamo p : prestamos) {
            if (p.getCedulaUsuario().equals(u.getCedula())) resultado.add(p);
        }
        return resultado;
    }

    /** Materiales ordenados por cantidad de prestamos (solo los que se prestaron al menos una vez). */
    public List<MaterialPrestado> materialesMasPrestados(int limite) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Prestamo p : prestamos) {
            conteo.merge(p.getCodigoMaterial(), 1, Integer::sum);
        }
        List<MaterialPrestado> resultado = new ArrayList<>();
        for (Map.Entry<String, Integer> e : conteo.entrySet()) {
            resultado.add(new MaterialPrestado(materiales.get(e.getKey()), e.getValue()));
        }
        resultado.sort(Comparator.comparingInt(MaterialPrestado::getVecesPrestado).reversed());
        return resultado.size() > limite ? resultado.subList(0, limite) : resultado;
    }

    /** Prestamos con multa cuya devolucion (momento en que se genera la multa) cae en el periodo. */
    public List<Prestamo> multasDelPeriodo(LocalDate desde, LocalDate hasta) throws BibliotecaException {
        if (hasta.isBefore(desde)) {
            throw new BibliotecaException("La fecha final no puede ser anterior a la inicial");
        }
        List<Prestamo> resultado = new ArrayList<>();
        for (Prestamo p : prestamos) {
            LocalDate d = p.getFechaDevolucion();
            if (d != null && p.getMulta().signum() > 0 && !d.isBefore(desde) && !d.isAfter(hasta)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public static BigDecimal totalMultas(List<Prestamo> lista) {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Prestamo p : lista) total = total.add(p.getMulta());
        return total;
    }

    public List<LineaAuditoria> auditoriaEjemplares() {
        Map<String, Integer> activos = new LinkedHashMap<>();
        for (Prestamo p : prestamos) {
            if (p.estaActivo()) activos.merge(p.getCodigoMaterial(), 1, Integer::sum);
        }
        List<LineaAuditoria> resultado = new ArrayList<>();
        for (Material m : materiales.values()) {
            resultado.add(new LineaAuditoria(m, activos.getOrDefault(m.getCodigo(), 0)));
        }
        return resultado;
    }

    private void guardarPrestamos() throws BibliotecaException {
        try {
            prestamoRepo.guardarTodos(prestamos);
        } catch (IOException e) {
            throw new BibliotecaException("No se pudo guardar los prestamos: " + e.getMessage(), e);
        }
    }
}
