package com.mazmorra.service;

import com.mazmorra.dao.AventureroDAO;
import com.mazmorra.dao.CriaturaDAO;
import com.mazmorra.db.Transaccion;
import com.mazmorra.exception.JuegoException;

/**
 * Transacción de prueba que ejecuta el trabajo directamente sobre los DAO en memoria.
 */
class TransaccionDirecta implements Transaccion {

    private final CriaturaDAO criaturas;
    private final AventureroDAO aventureros;

    TransaccionDirecta(CriaturaDAO criaturas, AventureroDAO aventureros) {
        this.criaturas = criaturas;
        this.aventureros = aventureros;
    }

    @Override
    public <T> T ejecutar(Trabajo<T> trabajo) throws JuegoException {
        // No hay JDBC: se delega en los mismos DAO en memoria y no se atrapa nada.
        return trabajo.ejecutar(criaturas, aventureros);
    }
}
