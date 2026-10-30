package com.mazmorra.model;

public interface Danable {

    // Criaturas y aventurero comparten este contrato.
    void recibirDanio(int cantidad);

    boolean estaDerrotado();
}
