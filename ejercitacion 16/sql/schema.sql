-- Esquema de la base de datos del juego de mazmorra.
-- Ejecutar este script en MySQL antes de iniciar la aplicacion.

CREATE DATABASE IF NOT EXISTS mazmorra CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mazmorra;

-- Recrea las tablas para que el script sirva tambien sobre una base ya creada.
DROP TABLE IF EXISTS criatura;
DROP TABLE IF EXISTS aventurero;

CREATE TABLE criatura (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(50) NOT NULL,
    Tipo VARCHAR(20) NOT NULL,           -- ESQUELETO, FANTASMA, ORCO, DRAGON_JEFE
    Vida INT NOT NULL,
    VidaMaxima INT NOT NULL,
    NivelAmenaza INT NOT NULL,
    PisoMazmorra INT NOT NULL,
    Derrotada BOOLEAN NOT NULL DEFAULT FALSE,
    Evadida BOOLEAN NOT NULL DEFAULT FALSE,
    RecompensaPagada BOOLEAN NOT NULL DEFAULT FALSE,
    DanioAtaque INT NOT NULL,
    ResistenciaFisica INT NULL,          -- solo Esqueleto
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
    PisoActual INT NOT NULL DEFAULT 1
);
