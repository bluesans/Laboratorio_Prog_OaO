package com.mazmorra.model;

public class Esqueleto extends Criatura {

    private int resistenciaFisica;

    public Esqueleto(int id, String nombre, int vida, int vidaMaxima,
            int nivelAmenaza, int danioAtaque, int pisoMazmorra, boolean derrotada,
            int resistenciaFisica) {
        super(id, nombre, vida, vidaMaxima, nivelAmenaza, danioAtaque, pisoMazmorra, derrotada);
        if (resistenciaFisica < 0 || resistenciaFisica > 100) {
            throw new IllegalArgumentException("La resistencia física debe estar entre 0 y 100.");
        }
        this.resistenciaFisica = resistenciaFisica;
    }

    public int getResistenciaFisica() {
        return resistenciaFisica;
    }

    @Override
    public TipoCriatura getTipo() {
        return TipoCriatura.ESQUELETO;
    }

    @Override
    protected int calcularDanioEfectivo(int cantidad, TipoAtaque tipoAtaque) {
        if (tipoAtaque == TipoAtaque.FISICO) {
            // La resistencia recorta el daño físico.
            int reducido = cantidad
                    - (int) Math.floor(cantidad * (double) getResistenciaFisica() / 100.0);
            return Math.max(0, reducido);
        }
        return cantidad;
    }
}
