package com.mazmorra.model;

/**
 * Registro acumulado de un personaje entre partidas y rondas.
 */
public class Personaje {

    private int id;
    private String nombre;
    private int mejorPuntaje;
    private int rondas;
    private int partidas;

    public Personaje(int id, String nombre, int mejorPuntaje, int rondas, int partidas) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del personaje no puede ser nulo ni vacío.");
        }
        if (nombre.length() > 50) {
            throw new IllegalArgumentException("El nombre del personaje no puede superar los 50 caracteres.");
        }
        if (mejorPuntaje < 0) {
            throw new IllegalArgumentException("El mejor puntaje no puede ser negativo.");
        }
        if (rondas < 0) {
            throw new IllegalArgumentException("Las rondas no pueden ser negativas.");
        }
        if (partidas < 0) {
            throw new IllegalArgumentException("La cantidad de partidas no puede ser negativa.");
        }
        this.id = id;
        this.nombre = nombre;
        this.mejorPuntaje = mejorPuntaje;
        this.rondas = rondas;
        this.partidas = partidas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id no puede ser negativo.");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getMejorPuntaje() {
        return mejorPuntaje;
    }

    public int getRondas() {
        return rondas;
    }

    public int getPartidas() {
        return partidas;
    }

    // Cada partida terminada suma al historial y actualiza los máximos.
    public void registrarResultado(int puntos, int ronda) {
        this.partidas++;
        this.mejorPuntaje = Math.max(this.mejorPuntaje, puntos);
        this.rondas = Math.max(this.rondas, ronda);
    }

    @Override
    public String toString() {
        return String.format(
                "%s[id=%d, nombre=%s, mejorPuntaje=%d, rondas=%d, partidas=%d]",
                getClass().getSimpleName(), id, nombre, mejorPuntaje, rondas, partidas);
    }
}
