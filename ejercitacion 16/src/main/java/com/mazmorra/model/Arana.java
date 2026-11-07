package com.mazmorra.model;

public class Arana extends Criatura {

    public Arana(int id, String nombre, int vida, int vidaMaxima,
            int nivelAmenaza, int danioAtaque, int pisoMazmorra, boolean derrotada) {
        super(id, nombre, vida, vidaMaxima, nivelAmenaza, danioAtaque, pisoMazmorra, derrotada);
    }

    @Override
    public TipoCriatura getTipo() {
        return TipoCriatura.ARANA;
    }

    @Override
    protected int calcularDanioEfectivo(int cantidad, TipoAtaque tipoAtaque) {
        // La araña no tiene resistencias: recibe el daño completo.
        return cantidad;
    }
}
