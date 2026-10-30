package com.mazmorra.model;

public class DragonJefe extends Criatura {

    private int faseActual;
    private int cantidadFases;
    private boolean alientoDeFuego;

    public DragonJefe(int id, String nombre, int vida, int vidaMaxima,
            int nivelAmenaza, int danioAtaque, int pisoMazmorra, boolean derrotada,
            int faseActual, int cantidadFases, boolean alientoDeFuego) {
        super(id, nombre, vida, vidaMaxima, nivelAmenaza, danioAtaque, pisoMazmorra, derrotada);
        if (cantidadFases < 1) {
            throw new IllegalArgumentException("La cantidad de fases debe ser al menos 1.");
        }
        if (faseActual < 1 || faseActual > cantidadFases) {
            throw new IllegalArgumentException("La fase actual debe estar entre 1 y la cantidad de fases.");
        }
        this.faseActual = faseActual;
        this.cantidadFases = cantidadFases;
        this.alientoDeFuego = alientoDeFuego;
    }

    public int getFaseActual() {
        return faseActual;
    }

    public int getCantidadFases() {
        return cantidadFases;
    }

    public boolean isAlientoDeFuego() {
        return alientoDeFuego;
    }

    @Override
    public TipoCriatura getTipo() {
        return TipoCriatura.DRAGON_JEFE;
    }

    @Override
    public int getDanioAtaque() {
        return super.getDanioAtaque() + (isAlientoDeFuego() ? 5 : 0)
                + 5 * (getFaseActual() - 1);
    }

    @Override
    public boolean permiteHuida() {
        // Del jefe final no se puede huir.
        return false;
    }

    @Override
    protected int calcularDanioEfectivo(int cantidad, TipoAtaque tipoAtaque) {
        return cantidad;
    }

    @Override
    protected void alRecibirDanio() {
        // La fase depende de la vida restante.
        int fase = 1 + (int) ((1.0 - (double) getVida() / getVidaMaxima()) * getCantidadFases());
        fase = Math.max(1, Math.min(getCantidadFases(), fase));
        this.faseActual = fase;
        this.alientoDeFuego = this.faseActual >= 2;
    }

    public int getPorcentajeVida() {
        return (int) Math.round(getVida() * 100.0 / getVidaMaxima());
    }
}
