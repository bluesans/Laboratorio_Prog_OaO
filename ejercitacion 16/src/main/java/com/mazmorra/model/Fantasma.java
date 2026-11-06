package com.mazmorra.model;

public class Fantasma extends Criatura {

    private boolean esIncorporeo;

    public Fantasma(int id, String nombre, int vida, int vidaMaxima,
            int nivelAmenaza, int danioAtaque, int pisoMazmorra, boolean derrotada,
            boolean esIncorporeo) {
        super(id, nombre, vida, vidaMaxima, nivelAmenaza, danioAtaque, pisoMazmorra, derrotada);
        this.esIncorporeo = esIncorporeo;
    }

    public boolean isEsIncorporeo() {
        return esIncorporeo;
    }

    @Override
    public TipoCriatura getTipo() {
        return TipoCriatura.FANTASMA;
    }

    @Override
    protected int calcularDanioEfectivo(int cantidad, TipoAtaque tipoAtaque) {
        if (tipoAtaque == TipoAtaque.FISICO && esIncorporeo) {
            // Incorpóreo: recibe la mitad del daño físico.
            return cantidad / 2;
        }
        return cantidad;
    }
}
