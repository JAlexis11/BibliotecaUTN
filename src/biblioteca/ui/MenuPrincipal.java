package biblioteca.ui;

import biblioteca.modelo.Libro;
import biblioteca.modelo.Material;
import biblioteca.modelo.Prestamo;
import biblioteca.modelo.Revista;
import biblioteca.modelo.Usuario;
import biblioteca.servicio.Biblioteca;
import biblioteca.servicio.BibliotecaException;
import biblioteca.servicio.LineaAuditoria;
import biblioteca.servicio.MaterialPrestado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Menus de la aplicacion. Solo muestra y pide datos; las reglas estan en Biblioteca. */
public class MenuPrincipal {

    private final Biblioteca biblioteca;
    private final Consola consola;

    public MenuPrincipal(Biblioteca biblioteca, Consola consola) {
        this.biblioteca = biblioteca;
        this.consola = consola;
    }

    public void iniciar() {
        while (true) {
            System.out.println();
            System.out.println("==========================================");
            System.out.println("   BIBLIOTECA UTN");
            System.out.println("==========================================");
            System.out.println(" 1. Materiales (libros y revistas)");
            System.out.println(" 2. Usuarios");
            System.out.println(" 3. Prestar material");
            System.out.println(" 4. Devolver material");
            System.out.println(" 5. Pagar multa");
            System.out.println(" 6. Reportes del administrador");
            System.out.println(" 0. Salir");
            int opcion = consola.leerEntero("Opcion: ", 0);
            try {
                switch (opcion) {
                    case 1:
                        menuMateriales();
                        break;
                    case 2:
                        menuUsuarios();
                        break;
                    case 3:
                        prestar();
                        break;
                    case 4:
                        devolver();
                        break;
                    case 5:
                        pagarMulta();
                        break;
                    case 6:
                        menuReportes();
                        break;
                    case 0:
                        System.out.println("Hasta pronto.");
                        return;
                    default:
                        System.out.println("Opcion no valida.");
                }
            } catch (BibliotecaException e) {
                System.out.println("\n  ERROR: " + e.getMessage());
                consola.pausa();
            }
        }
    }

    // ============================================================ MATERIALES

    private void menuMateriales() {
        while (true) {
            System.out.println("\n--- MATERIALES ---");
            System.out.println(" 1. Registrar material");
            System.out.println(" 2. Listar todos");
            System.out.println(" 3. Buscar por titulo, autor o edicion");
            System.out.println(" 4. Agregar o dar de baja ejemplares");
            System.out.println(" 0. Volver");
            int opcion = consola.leerEntero("Opcion: ", 0);
            try {
                switch (opcion) {
                    case 1:
                        registrarMaterial();
                        break;
                    case 2:
                        mostrarMateriales(biblioteca.listarMateriales());
                        break;
                    case 3:
                        mostrarMateriales(biblioteca.buscarMateriales(consola.leerTexto("Texto a buscar: ")));
                        break;
                    case 4:
                        ajustarEjemplares();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Opcion no valida.");
                }
            } catch (BibliotecaException e) {
                System.out.println("\n  ERROR: " + e.getMessage());
            }
            if (opcion != 0) consola.pausa();
        }
    }

    private void registrarMaterial() throws BibliotecaException {
        System.out.println("\nTipo de material:");
        System.out.println(" 1. Libro   (" + Libro.DIAS_PRESTAMO + " dias, multa $" + Libro.MULTA_POR_DIA + "/dia)");
        System.out.println(" 2. Revista (" + Revista.DIAS_PRESTAMO + " dias, multa $" + Revista.MULTA_POR_DIA + "/dia)");
        int tipo = consola.leerEntero("Tipo: ", 1);
        if (tipo > 2) {
            System.out.println("Tipo no valido.");
            return;
        }
        String codigo = consola.leerTexto("Codigo: ");
        String titulo = consola.leerTexto("Titulo: ");
        String especifico = consola.leerTexto(tipo == 1 ? "Autor: " : "Edicion / numero: ");
        int ejemplares = consola.leerEntero("Numero de ejemplares: ", 1);

        Material material;
        try {
            material = tipo == 1
                    ? new Libro(codigo, titulo, ejemplares, especifico)
                    : new Revista(codigo, titulo, ejemplares, especifico);
        } catch (IllegalArgumentException e) {
            throw new BibliotecaException(e.getMessage());
        }
        biblioteca.registrarMaterial(material);
        System.out.println("Material registrado.");
    }

    private void ajustarEjemplares() throws BibliotecaException {
        Material m = biblioteca.obtenerMaterial(consola.leerTexto("Codigo del material: "));
        System.out.println(m);
        System.out.println("Ingrese un numero positivo para agregar ejemplares o negativo para darlos de baja.");
        int cantidad = consola.leerEntero("Cantidad: ", -m.getEjemplares());
        if (cantidad == 0) return;
        biblioteca.ajustarEjemplares(m.getCodigo(), cantidad);
        System.out.println("Ahora hay " + m.getEjemplares() + " ejemplares (" + m.getDisponibles() + " disponibles).");
    }

    private void mostrarMateriales(List<Material> lista) {
        if (lista.isEmpty()) {
            System.out.println("No hay materiales para mostrar.");
            return;
        }
        System.out.println();
        for (Material m : lista) System.out.println(m);
        System.out.println("Total: " + lista.size());
    }

    // ============================================================ USUARIOS

    private void menuUsuarios() {
        while (true) {
            System.out.println("\n--- USUARIOS ---");
            System.out.println(" 1. Registrar usuario");
            System.out.println(" 2. Listar usuarios");
            System.out.println(" 3. Consultar estado de un usuario");
            System.out.println(" 0. Volver");
            int opcion = consola.leerEntero("Opcion: ", 0);
            try {
                switch (opcion) {
                    case 1:
                        registrarUsuario();
                        break;
                    case 2:
                        listarUsuarios();
                        break;
                    case 3:
                        estadoUsuario();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Opcion no valida.");
                }
            } catch (BibliotecaException e) {
                System.out.println("\n  ERROR: " + e.getMessage());
            }
            if (opcion != 0) consola.pausa();
        }
    }

    private void registrarUsuario() throws BibliotecaException {
        String cedula = consola.leerTexto("Cedula (10 digitos): ");
        String nombre = consola.leerTexto("Nombre completo: ");
        try {
            biblioteca.registrarUsuario(new Usuario(cedula, nombre));
        } catch (IllegalArgumentException e) {
            throw new BibliotecaException(e.getMessage());
        }
        System.out.println("Usuario registrado.");
    }

    private void listarUsuarios() {
        List<Usuario> lista = biblioteca.listarUsuarios();
        if (lista.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
            return;
        }
        System.out.println();
        System.out.printf("%-10s  %-30s %8s %10s%n", "CEDULA", "NOMBRE", "ACTIVOS", "MULTA");
        for (Usuario u : lista) {
            System.out.printf("%-10s  %-30s %8d %10s%n", u.getCedula(), u.getNombre(),
                    biblioteca.prestamosActivos(u.getCedula()).size(),
                    "$" + biblioteca.multaPendiente(u.getCedula()));
        }
    }

    private void estadoUsuario() throws BibliotecaException {
        Usuario u = biblioteca.obtenerUsuario(consola.leerTexto("Cedula: "));
        List<Prestamo> activos = biblioteca.prestamosActivos(u.getCedula());
        System.out.println("\n" + u);
        System.out.println("Prestamos activos: " + activos.size() + " de " + Biblioteca.MAXIMO_PRESTAMOS_ACTIVOS);
        System.out.println("Multas pendientes: $" + biblioteca.multaPendiente(u.getCedula()));
        if (!activos.isEmpty()) mostrarPrestamos(activos, LocalDate.now());
    }

    // ============================================================ PRESTAMOS

    private void prestar() throws BibliotecaException {
        Usuario u = biblioteca.obtenerUsuario(consola.leerTexto("Cedula del usuario: "));
        System.out.println("Usuario: " + u.getNombre());
        Material m = biblioteca.obtenerMaterial(consola.leerTexto("Codigo del material: "));
        System.out.println("Material: " + m);
        LocalDate fecha = consola.leerFecha("Fecha del prestamo", LocalDate.now());

        Prestamo p = biblioteca.prestar(u.getCedula(), m.getCodigo(), fecha);
        System.out.println("\nPrestamo #" + p.getNumero() + " registrado.");
        System.out.println("Debe devolverse hasta el " + p.getFechaVencimiento()
                + ". Multa por retraso: $" + m.multaPorDia() + " por dia.");
        consola.pausa();
    }

    private void devolver() throws BibliotecaException {
        String cedula = consola.leerTexto("Cedula del usuario: ");
        Usuario u = biblioteca.obtenerUsuario(cedula);
        List<Prestamo> activos = biblioteca.prestamosActivos(u.getCedula());
        if (activos.isEmpty()) {
            System.out.println(u.getNombre() + " no tiene prestamos activos.");
            consola.pausa();
            return;
        }
        mostrarPrestamos(activos, LocalDate.now());
        long numero = consola.leerEntero("Numero de prestamo a devolver: ", 1);
        LocalDate fecha = consola.leerFecha("Fecha de devolucion", LocalDate.now());

        Prestamo p = biblioteca.devolver(numero, fecha);
        System.out.println("\nDevolucion registrada.");
        if (p.getMulta().signum() > 0) {
            System.out.println("Retraso de " + p.diasRetraso(fecha) + " dias. Multa generada: $" + p.getMulta());
            if (consola.confirmar("Desea pagar la multa ahora?")) {
                biblioteca.pagarMulta(p.getNumero());
                System.out.println("Multa pagada.");
            }
        } else {
            System.out.println("Devuelto a tiempo, sin multa.");
        }
        consola.pausa();
    }

    private void pagarMulta() throws BibliotecaException {
        Usuario u = biblioteca.obtenerUsuario(consola.leerTexto("Cedula del usuario: "));
        BigDecimal total = biblioteca.multaPendiente(u.getCedula());
        if (total.signum() == 0) {
            System.out.println(u.getNombre() + " no tiene multas pendientes.");
            consola.pausa();
            return;
        }
        System.out.println("\nMultas pendientes de " + u.getNombre() + ":");
        for (Prestamo p : biblioteca.historialUsuario(u.getCedula())) {
            if (p.tieneMultaPendiente()) {
                System.out.printf("  Prestamo #%d  %-8s  devuelto %s  $%.2f%n", p.getNumero(),
                        p.getCodigoMaterial(), p.getFechaDevolucion(), p.getMulta());
            }
        }
        System.out.println("  Total: $" + total);
        if (consola.confirmar("Registrar el pago de TODAS las multas?")) {
            for (Prestamo p : biblioteca.historialUsuario(u.getCedula())) {
                if (p.tieneMultaPendiente()) biblioteca.pagarMulta(p.getNumero());
            }
            System.out.println("Pago registrado por $" + total + ".");
        }
        consola.pausa();
    }

    private void mostrarPrestamos(List<Prestamo> lista, LocalDate fecha) {
        System.out.println();
        System.out.printf("%5s  %-10s %-8s %-28s %-10s %-10s %-10s %8s  %s%n", "N", "CEDULA", "CODIGO", "TITULO",
                "PRESTADO", "VENCE", "DEVUELTO", "MULTA", "ESTADO");
        for (Prestamo p : lista) {
            String titulo;
            try {
                titulo = biblioteca.obtenerMaterial(p.getCodigoMaterial()).getTitulo();
            } catch (BibliotecaException e) {
                titulo = "?";
            }
            BigDecimal multa = p.estaActivo() ? biblioteca.multaEstimada(p, fecha) : p.getMulta();
            System.out.printf("%5d  %-10s %-8s %-28s %-10s %-10s %-10s %8s  %s%n", p.getNumero(),
                    p.getCedulaUsuario(), p.getCodigoMaterial(), recortar(titulo, 28), p.getFechaPrestamo(),
                    p.getFechaVencimiento(), p.getFechaDevolucion() == null ? "-" : p.getFechaDevolucion(),
                    "$" + multa, p.estado(fecha));
        }
    }

    private static String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 3) + "...";
    }

    // ============================================================ REPORTES

    private void menuReportes() {
        while (true) {
            System.out.println("\n--- REPORTES ---");
            System.out.println(" 1. Prestamos vencidos a la fecha");
            System.out.println(" 2. Historial de un usuario");
            System.out.println(" 3. Materiales mas prestados");
            System.out.println(" 4. Total de multas generadas en un periodo");
            System.out.println(" 5. Auditoria de ejemplares disponibles");
            System.out.println(" 0. Volver");
            int opcion = consola.leerEntero("Opcion: ", 0);
            try {
                switch (opcion) {
                    case 1:
                        reporteVencidos();
                        break;
                    case 2:
                        reporteHistorial();
                        break;
                    case 3:
                        reporteMasPrestados();
                        break;
                    case 4:
                        reporteMultas();
                        break;
                    case 5:
                        reporteAuditoria();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Opcion no valida.");
                }
            } catch (BibliotecaException e) {
                System.out.println("\n  ERROR: " + e.getMessage());
            }
            if (opcion != 0) consola.pausa();
        }
    }

    private void reporteVencidos() {
        LocalDate fecha = consola.leerFecha("Fecha de corte", LocalDate.now());
        List<Prestamo> vencidos = biblioteca.prestamosVencidos(fecha);
        System.out.println("\nPRESTAMOS VENCIDOS AL " + fecha);
        if (vencidos.isEmpty()) {
            System.out.println("No hay prestamos vencidos.");
            return;
        }
        mostrarPrestamos(vencidos, fecha);
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Prestamo p : vencidos) total = total.add(biblioteca.multaEstimada(p, fecha));
        System.out.println("Total: " + vencidos.size() + " prestamos. Multa acumulada si se devolvieran hoy: $"
                + total);
    }

    private void reporteHistorial() throws BibliotecaException {
        Usuario u = biblioteca.obtenerUsuario(consola.leerTexto("Cedula: "));
        List<Prestamo> historial = biblioteca.historialUsuario(u.getCedula());
        System.out.println("\nHISTORIAL DE " + u.getNombre().toUpperCase() + " (" + u.getCedula() + ")");
        if (historial.isEmpty()) {
            System.out.println("El usuario no tiene prestamos.");
            return;
        }
        mostrarPrestamos(historial, LocalDate.now());
        System.out.println("Total prestamos: " + historial.size()
                + "  |  Activos: " + biblioteca.prestamosActivos(u.getCedula()).size()
                + "  |  Multas generadas: $" + Biblioteca.totalMultas(historial)
                + "  |  Pendientes: $" + biblioteca.multaPendiente(u.getCedula()));
    }

    private void reporteMasPrestados() {
        int limite = consola.leerEntero("Cuantos mostrar", 1, 10);
        List<MaterialPrestado> lista = biblioteca.materialesMasPrestados(limite);
        System.out.println("\nMATERIALES MAS PRESTADOS");
        if (lista.isEmpty()) {
            System.out.println("Todavia no hay prestamos.");
            return;
        }
        System.out.printf("%3s  %-8s %-8s %-32s %8s%n", "#", "CODIGO", "TIPO", "TITULO", "VECES");
        int posicion = 1;
        for (MaterialPrestado mp : lista) {
            Material m = mp.getMaterial();
            System.out.printf("%3d  %-8s %-8s %-32s %8d%n", posicion++, m.getCodigo(), m.getTipo(),
                    recortar(m.getTitulo(), 32), mp.getVecesPrestado());
        }
    }

    private void reporteMultas() throws BibliotecaException {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = consola.leerFecha("Desde", hoy.withDayOfMonth(1));
        LocalDate hasta = consola.leerFecha("Hasta", hoy);
        List<Prestamo> multas = biblioteca.multasDelPeriodo(desde, hasta);
        System.out.println("\nMULTAS GENERADAS DEL " + desde + " AL " + hasta);
        if (multas.isEmpty()) {
            System.out.println("No se generaron multas en el periodo.");
            return;
        }
        mostrarPrestamos(multas, hasta);
        BigDecimal cobrado = BigDecimal.ZERO.setScale(2);
        for (Prestamo p : multas) {
            if (p.isMultaPagada()) cobrado = cobrado.add(p.getMulta());
        }
        BigDecimal total = Biblioteca.totalMultas(multas);
        System.out.println("Total generado: $" + total + "  |  Cobrado: $" + cobrado
                + "  |  Por cobrar: $" + total.subtract(cobrado));
    }

    private void reporteAuditoria() {
        List<LineaAuditoria> lineas = biblioteca.auditoriaEjemplares();
        System.out.println("\nAUDITORIA DE EJEMPLARES AL " + LocalDate.now());
        System.out.printf("%-8s %-8s %-32s %6s %9s %12s  %s%n", "CODIGO", "TIPO", "TITULO", "TOTAL",
                "PRESTADOS", "DISPONIBLES", "ESTADO");
        int total = 0;
        int prestados = 0;
        int inconsistentes = 0;
        for (LineaAuditoria l : lineas) {
            Material m = l.getMaterial();
            String estado = l.esConsistente() ? "OK" : "REVISAR (contador: " + m.getPrestados() + ")";
            if (!l.esConsistente()) inconsistentes++;
            System.out.printf("%-8s %-8s %-32s %6d %9d %12d  %s%n", m.getCodigo(), m.getTipo(),
                    recortar(m.getTitulo(), 32), m.getEjemplares(), l.getPrestamosActivos(),
                    l.getDisponiblesEsperados(), estado);
            total += m.getEjemplares();
            prestados += l.getPrestamosActivos();
        }
        System.out.println("Ejemplares: " + total + "  |  Prestados: " + prestados
                + "  |  Disponibles: " + (total - prestados)
                + "  |  " + (inconsistentes == 0 ? "Sin inconsistencias" : inconsistentes + " inconsistencias"));
    }
}
