package com.mazmorra.model;

public interface Danable {

    // Criaturas y aventurero comparten este contrato.
    void recibirDanio(int cantidad);

    default void recibirDanio(int cantidad, TipoAtaque tipoAtaque) {
        recibirDanio(cantidad);
    }

    boolean estaDerrotado();
}
