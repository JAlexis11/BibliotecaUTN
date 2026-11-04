# Biblioteca UTN - Prestamos y multas

Aplicacion de consola en Java (17 o superior) para controlar los prestamos de libros y revistas.

## Ejecutar
Requisitos: Java 17 o superior y PostgreSQL con una base llamada `Biblio`.

- Windows: doble clic en `ejecutar.bat`
- Manual:
  ```
  javac --release 17 -encoding UTF-8 -d out src/biblioteca/*.java src/biblioteca/*/*.java
  java -cp "out;lib\postgresql-42.7.12.jar" biblioteca.App
  ```

## Base de datos (PostgreSQL)
Los datos se guardan en la base `Biblio` de PostgreSQL (la que se administra con pgAdmin).
- `db.properties`: url, usuario y contrasena de la conexion. No se sube al repositorio:
  copie `db.properties.example` como `db.properties` y ponga su contrasena.
- `lib/postgresql-42.7.12.jar`: driver JDBC.
- `sql/biblio.sql`: esquema (tablas `usuario`, `material`, `prestamo`). La aplicacion crea las
  tablas sola al iniciar si no existen.
- Si la base esta vacia, en el primer arranque se importan los archivos de la carpeta `datos/`
  (`materiales.csv`, `usuarios.csv`, `prestamos.csv`). Despues ya no se usan.

Los ejemplares prestados de cada titulo se calculan al iniciar a partir de los prestamos activos,
por eso los disponibles siempre coinciden con los prestamos registrados.

## Reglas de negocio
| Material | Dias de prestamo | Multa por dia de retraso |
|----------|------------------|--------------------------|
| Libro    | 7                | $0.25                    |
| Revista  | 3                | $0.50                    |

- Un usuario puede tener como maximo 3 prestamos activos.
- No se presta si no hay ejemplares disponibles.
- Un prestamo no se puede devolver dos veces.
- Un usuario con multas pendientes no puede solicitar nuevos prestamos.
  Tampoco si tiene un prestamo vencido sin devolver (su multa ya esta corriendo).
- La multa se genera al devolver: dias de retraso x multa por dia del material.
- Al prestar y devolver se puede indicar la fecha (por defecto hoy), util para probar retrasos.

## Reportes del administrador
1. Prestamos vencidos a una fecha de corte (con la multa acumulada).
2. Historial de un usuario.
3. Materiales mas prestados.
4. Total de multas generadas en un periodo (generado, cobrado y por cobrar).
5. Auditoria de ejemplares: total, prestados y disponibles por titulo, verificando que el
   contador coincida con los prestamos activos.

## Estructura (POO)
- `modelo`: `Material` (abstracta) -> `Libro`, `Revista`. Polimorfismo en `diasPrestamo()`
  y `multaPorDia()`. `Usuario`, `Prestamo` (devolucion, calculo de retraso y multa).
- `persistencia`: `ConexionBD`, `MaterialRepositorio`, `UsuarioRepositorio`, `PrestamoRepositorio`
  (PostgreSQL via JDBC) y `MaterialCsv`, `UsuarioCsv`, `PrestamoCsv` (importacion inicial).
- `servicio`: `Biblioteca` (reglas de negocio y reportes), `MaterialPrestado`, `LineaAuditoria`.
- `ui`: `Consola` (lectura validada) y `MenuPrincipal`.
