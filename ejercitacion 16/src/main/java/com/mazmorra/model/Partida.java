package com.mazmorra.model;

import java.time.LocalDateTime;

/**
 * Historial de una partida terminada de un personaje.
 */
public record Partida(int id, int personajeId, int ronda, int piso, int oro,
        int enemigosDerrotados, int puntos, String resultado, LocalDateTime fecha) {
}
