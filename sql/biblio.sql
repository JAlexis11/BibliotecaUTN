-- Esquema de la base Biblio (PostgreSQL).
-- La aplicacion lo crea sola al iniciar; este script sirve para revisarlo o ejecutarlo desde pgAdmin.

CREATE TABLE IF NOT EXISTS usuario (
    cedula  CHAR(10)     PRIMARY KEY CHECK (cedula ~ '^[0-9]{10}$'),
    nombre  VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS material (
    codigo      VARCHAR(20)  PRIMARY KEY,
    tipo        VARCHAR(10)  NOT NULL CHECK (tipo IN ('LIBRO', 'REVISTA')),
    titulo      VARCHAR(200) NOT NULL,
    ejemplares  INTEGER      NOT NULL CHECK (ejemplares >= 1),
    especifico  VARCHAR(150) NOT NULL  -- autor (libro) o edicion (revista)
);

CREATE TABLE IF NOT EXISTS prestamo (
    numero             BIGINT       PRIMARY KEY,
    cedula             CHAR(10)     NOT NULL REFERENCES usuario (cedula),
    codigo_material    VARCHAR(20)  NOT NULL REFERENCES material (codigo),
    fecha_prestamo     DATE         NOT NULL,
    fecha_vencimiento  DATE         NOT NULL,
    fecha_devolucion   DATE,
    multa              NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (multa >= 0),
    multa_pagada       BOOLEAN      NOT NULL DEFAULT FALSE,
    CHECK (fecha_vencimiento >= fecha_prestamo),
    CHECK (fecha_devolucion IS NULL OR fecha_devolucion >= fecha_prestamo)
);
