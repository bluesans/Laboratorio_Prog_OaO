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

    public static final int DANIO_ESQUELETO = 12;
    public static final int DANIO_FANTASMA = 15;
    public static final int DANIO_ORCO = 12;
    public static final int DANIO_DRAGON = 10;

    public static final int RESISTENCIA_ESQUELETO = 40;
    public static final int RESISTENCIA_MAGICA_ORCO = 50;
    public static final int FASES_DRAGON = 3;

    private Reglas() {
        throw new UnsupportedOperationException("Clase de utilidad: no se instancia.");
    }
}
