package com.mazmorra;

/**
 * Constantes con los valores de las reglas del juego.
 */
public final class Reglas {

    public static final int VIDA_INICIAL = 100;
    public static final int ATAQUE_INICIAL = 20;
    public static final int ORO_INICIAL = 50;
    public static final int PISO_INICIAL = 1;
    public static final int PISO_FINAL = 3;

    public static final int TAMANO_POCION = 30;
    public static final int COSTO_POCION = 20;
    public static final int COSTO_HUIDA = 10;

    public static final int ORO_POR_NIVEL_AMENAZA = 5;

    public static final int DANIO_ESQUELETO = 15;
    public static final int DANIO_FANTASMA = 18;
    public static final int DANIO_ORCO = 15;
    public static final int DANIO_DRAGON = 13;

    public static final int RESISTENCIA_ESQUELETO = 40;
    public static final int RESISTENCIA_MAGICA_ORCO = 50;
    public static final int FASES_DRAGON = 3;

    public static final double FACTOR_INICIAL = 1.15;
    public static final double ESCALA_VIDA_RONDA = 0.35;
    public static final double ESCALA_DANIO_RONDA = 0.25;
    public static final int ENEMIGOS_EXTRA_POR_PISO = 2;
    public static final int PUNTOS_RONDA = 1000;
    public static final int PUNTOS_PISO = 100;
    public static final int PUNTOS_ENEMIGO = 10;
    public static final int RANKING_LIMITE = 5;

    public static final int VIDA_ARANA = 18;
    public static final int DANIO_ARANA = 10;
    public static final int AMENAZA_ARANA = 3;

    public static final int VIDA_GOLEM = 55;
    public static final int DANIO_GOLEM = 18;
    public static final int AMENAZA_GOLEM = 6;
    public static final int RESISTENCIA_GOLEM = 40;

    private Reglas() {
        throw new UnsupportedOperationException("Clase de utilidad: no se instancia.");
    }
}
