-- Esquema completo de la base de datos del juego de mazmorra.
-- Este script es destructivo: elimina y vuelve a crear las tablas.

CREATE DATABASE IF NOT EXISTS mazmorra CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mazmorra;

DROP TABLE IF EXISTS partida;
DROP TABLE IF EXISTS criatura;
DROP TABLE IF EXISTS aventurero;
DROP TABLE IF EXISTS personaje;

CREATE TABLE personaje (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(50) NOT NULL UNIQUE,
    MejorPuntaje INT NOT NULL DEFAULT 0,
    Rondas INT NOT NULL DEFAULT 0,
    Partidas INT NOT NULL DEFAULT 0,
    FechaCreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE criatura (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(50) NOT NULL,
    Tipo VARCHAR(20) NOT NULL,           -- ESQUELETO, FANTASMA, ORCO, DRAGON_JEFE, ARANA, GOLEM
    Vida INT NOT NULL,
    VidaMaxima INT NOT NULL,
    NivelAmenaza INT NOT NULL,
    PisoMazmorra INT NOT NULL,
    Derrotada BOOLEAN NOT NULL DEFAULT FALSE,
    Evadida BOOLEAN NOT NULL DEFAULT FALSE,
    RecompensaPagada BOOLEAN NOT NULL DEFAULT FALSE,
    DanioAtaque INT NOT NULL,
    ResistenciaFisica INT NULL,          -- Esqueleto y Gólem
    EsIncorporeo BOOLEAN NULL,           -- solo Fantasma
    ResistenciaMagica INT NULL,          -- solo Orco
    FaseActual INT NULL,                 -- solo DragonJefe
    CantidadFases INT NULL,              -- solo DragonJefe
    AlientoDeFuego BOOLEAN NULL          -- solo DragonJefe
);

CREATE TABLE aventurero (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(50) NOT NULL,
    Nivel INT NOT NULL DEFAULT 1,
    Vida INT NOT NULL,
    VidaMaxima INT NOT NULL,
    AtaqueBase INT NOT NULL,
    Oro INT NOT NULL DEFAULT 0,
    PisoActual INT NOT NULL DEFAULT 1,
    PersonajeID INT NULL,
    Ronda INT NOT NULL DEFAULT 1,
    EnemigosDerrotados INT NOT NULL DEFAULT 0,
    CONSTRAINT FK_Aventurero_Personaje FOREIGN KEY (PersonajeID) REFERENCES personaje(ID)
);

CREATE TABLE partida (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    PersonajeID INT NOT NULL,
    Ronda INT NOT NULL,
    Piso INT NOT NULL,
    Oro INT NOT NULL,
    EnemigosDerrotados INT NOT NULL,
    Puntos INT NOT NULL,
    Resultado VARCHAR(20) NOT NULL,
    Fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_Partida_Personaje FOREIGN KEY (PersonajeID) REFERENCES personaje(ID)
);
