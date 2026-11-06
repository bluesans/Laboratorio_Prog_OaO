package com.mazmorra.db;

import com.mazmorra.dao.AventureroDAO;
import com.mazmorra.dao.CriaturaDAO;
import com.mazmorra.exception.JuegoException;

public interface Transaccion {
    <T> T ejecutar(Trabajo<T> trabajo) throws JuegoException;

    interface Trabajo<T> {
        T ejecutar(CriaturaDAO criaturas, AventureroDAO aventureros) throws JuegoException;
    }
}
