package com.mazmorra.model;

public class Orco extends Criatura {

    private int resistenciaMagica;

    public Orco(int id, String nombre, int vida, int vidaMaxima,
            int nivelAmenaza, int danioAtaque, int pisoMazmorra, boolean derrotada,
            int resistenciaMagica) {
        super(id, nombre, vida, vidaMaxima, nivelAmenaza, danioAtaque, pisoMazmorra, derrotada);
        if (resistenciaMagica < 0 || resistenciaMagica > 100) {
            throw new IllegalArgumentException("La resistencia mágica debe estar entre 0 y 100.");
        }
        this.resistenciaMagica = resistenciaMagica;
    }

    public int getResistenciaMagica() {
        return resistenciaMagica;
    }

    @Override
    public TipoCriatura getTipo() {
        return TipoCriatura.ORCO;
    }

    @Override
    protected int calcularDanioEfectivo(int cantidad, TipoAtaque tipoAtaque) {
        if (tipoAtaque == TipoAtaque.MAGICO) {
            // La piel del orco absorbe parte del hechizo; multiplica en double.
            return cantidad - (int) Math.floor(cantidad * (double) resistenciaMagica / 100.0);
        }
        return cantidad;
    }
}
