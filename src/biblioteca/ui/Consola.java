package biblioteca.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Lectura validada de datos desde el teclado. */
public class Consola {

    private final Scanner scanner = new Scanner(System.in);

    public String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = leerLinea().trim();
            if (!valor.isEmpty()) return valor;
            System.out.println("  ! El valor es obligatorio.");
        }
    }

    public int leerEntero(String mensaje, int minimo) {
        while (true) {
            System.out.print(mensaje);
            try {
                int valor = Integer.parseInt(leerLinea().trim());
                if (valor >= minimo) return valor;
                System.out.println("  ! Debe ser un numero mayor o igual a " + minimo + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ! Ingrese un numero entero valido.");
            }
        }
    }

    /** Si el usuario presiona Enter sin escribir, devuelve el valor por defecto. */
    public int leerEntero(String mensaje, int minimo, int porDefecto) {
        while (true) {
            System.out.print(mensaje + " [" + porDefecto + "]: ");
            String texto = leerLinea().trim();
            if (texto.isEmpty()) return porDefecto;
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= minimo) return valor;
                System.out.println("  ! Debe ser un numero mayor o igual a " + minimo + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ! Ingrese un numero entero valido.");
            }
        }
    }

    public LocalDate leerFecha(String mensaje, LocalDate porDefecto) {
        while (true) {
            System.out.print(mensaje + " (aaaa-mm-dd) [" + porDefecto + "]: ");
            String texto = leerLinea().trim();
            if (texto.isEmpty()) return porDefecto;
            try {
                return LocalDate.parse(texto);
            } catch (DateTimeParseException e) {
                System.out.println("  ! Fecha invalida. Use el formato aaaa-mm-dd, ej. 2026-11-03.");
            }
        }
    }

    public boolean confirmar(String mensaje) {
        System.out.print(mensaje + " (s/n): ");
        return leerLinea().trim().equalsIgnoreCase("s");
    }

    /** Lee una linea; si se cierra la entrada estandar, termina el programa ordenadamente. */
    private String leerLinea() {
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("Entrada finalizada. Hasta pronto.");
            System.exit(0);
        }
        return scanner.nextLine();
    }

    public void pausa() {
        System.out.print("\nPresione Enter para continuar...");
        leerLinea();
    }
}
