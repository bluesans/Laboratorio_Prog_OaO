package com.mazmorra.model;

public class Aventurero implements Danable {

    private int id;
    private String nombre;
    private int nivel;
    private int vida;
    private int vidaMaxima;
    private int ataqueBase;
    private int oro;
    private int pisoActual;

    public Aventurero(int id, String nombre, int nivel, int vida, int vidaMaxima,
            int ataqueBase, int oro, int pisoActual) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del aventurero no puede ser nulo ni vacío.");
        }
        if (nivel < 1) {
            throw new IllegalArgumentException("El nivel debe ser al menos 1.");
        }
        if (vidaMaxima <= 0) {
            throw new IllegalArgumentException("La vida máxima debe ser mayor que cero.");
        }
        if (vida < 0 || vida > vidaMaxima) {
            throw new IllegalArgumentException("La vida debe estar entre 0 y la vida máxima.");
        }
        if (ataqueBase <= 0) {
            throw new IllegalArgumentException("El ataque base debe ser mayor que cero.");
        }
        if (oro < 0) {
            throw new IllegalArgumentException("El oro no puede ser negativo.");
        }
        if (pisoActual < 1) {
            throw new IllegalArgumentException("El piso actual debe ser al menos 1.");
        }
        this.id = id;
        this.nombre = nombre;
        this.nivel = nivel;
        this.vida = vida;
        this.vidaMaxima = vidaMaxima;
        this.ataqueBase = ataqueBase;
        this.oro = oro;
        this.pisoActual = pisoActual;
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

    public int getNivel() {
        return nivel;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public int getAtaqueBase() {
        return ataqueBase;
    }

    public int getOro() {
        return oro;
    }

    public void setOro(int oro) {
        if (oro < 0) {
            throw new IllegalArgumentException("El oro no puede ser negativo.");
        }
        this.oro = oro;
    }

    public int getPisoActual() {
        return pisoActual;
    }

    public void setPisoActual(int pisoActual) {
        if (pisoActual < 1) {
            throw new IllegalArgumentException("El piso actual debe ser al menos 1.");
        }
        this.pisoActual = pisoActual;
    }

    @Override
    public void recibirDanio(int cantidad) {
        if (cantidad <= 0) {
            return;
        }
        // Sin resistencias: pierde la vida completa del golpe.
        this.vida = Math.max(0, this.vida - cantidad);
    }

    public void curar(int cantidad) {
        if (cantidad <= 0) {
            return;
        }
        // La curación no supera la vida máxima ni desborda el int.
        int curado = Math.min(cantidad, getVidaMaxima() - getVida());
        this.vida += curado;
    }

    @Override
    public boolean estaDerrotado() {
        return vida <= 0;
    }

    public boolean estaVivo() {
        return !estaDerrotado();
    }

    public int getPorcentajeVida() {
        return (int) Math.round(vida * 100.0 / vidaMaxima);
    }

    @Override
    public String toString() {
        return String.format(
                "%s[id=%d, nombre=%s, nivel=%d, vida=%d/%d, ataque=%d, oro=%d, piso=%d, derrotado=%b]",
                getClass().getSimpleName(), id, nombre, nivel, vida, vidaMaxima,
                ataqueBase, oro, pisoActual, estaDerrotado());
    }
}
