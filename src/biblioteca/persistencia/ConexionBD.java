package biblioteca.persistencia;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/** Datos de conexion a PostgreSQL leidos de db.properties y creacion del esquema. */
public class ConexionBD {

    private static final String[] ESQUEMA = {
        "CREATE TABLE IF NOT EXISTS usuario ("
            + " cedula CHAR(10) PRIMARY KEY CHECK (cedula ~ '^[0-9]{10}$'),"
            + " nombre VARCHAR(100) NOT NULL)",
        "CREATE TABLE IF NOT EXISTS material ("
            + " codigo VARCHAR(20) PRIMARY KEY,"
            + " tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('LIBRO', 'REVISTA')),"
            + " titulo VARCHAR(200) NOT NULL,"
            + " ejemplares INTEGER NOT NULL CHECK (ejemplares >= 1),"
            + " especifico VARCHAR(150) NOT NULL)",
        "CREATE TABLE IF NOT EXISTS prestamo ("
            + " numero BIGINT PRIMARY KEY,"
            + " cedula CHAR(10) NOT NULL REFERENCES usuario (cedula),"
            + " codigo_material VARCHAR(20) NOT NULL REFERENCES material (codigo),"
            + " fecha_prestamo DATE NOT NULL,"
            + " fecha_vencimiento DATE NOT NULL,"
            + " fecha_devolucion DATE,"
            + " multa NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (multa >= 0),"
            + " multa_pagada BOOLEAN NOT NULL DEFAULT FALSE,"
            + " CHECK (fecha_vencimiento >= fecha_prestamo),"
            + " CHECK (fecha_devolucion IS NULL OR fecha_devolucion >= fecha_prestamo))"
    };

    private final String url;
    private final String usuario;
    private final String contrasena;

    public ConexionBD(Path archivoPropiedades) throws IOException {
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(archivoPropiedades, StandardCharsets.UTF_8)) {
            p.load(r);
        }
        this.url = p.getProperty("url", "jdbc:postgresql://localhost:5432/Biblio");
        this.usuario = p.getProperty("usuario", "postgres");
        this.contrasena = p.getProperty("contrasena", "");
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    /** Crea las tablas si no existen. */
    public void crearEsquema() throws SQLException {
        try (Connection c = abrir(); Statement st = c.createStatement()) {
            for (String sql : ESQUEMA) st.execute(sql);
        }
    }

    /** true si las tablas de la biblioteca no tienen ningun registro. */
    public boolean estaVacia() throws SQLException {
        try (Connection c = abrir(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT (SELECT COUNT(*) FROM material) + (SELECT COUNT(*) FROM usuario)"
                     + " + (SELECT COUNT(*) FROM prestamo)")) {
            rs.next();
            return rs.getLong(1) == 0;
        }
    }

    static IOException error(String accion, SQLException e) {
        return new IOException("Error de base de datos al " + accion + ": " + e.getMessage(), e);
    }
}
