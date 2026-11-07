package com.mazmorra.db.impl;

import com.mazmorra.dao.AventureroDAO;
import com.mazmorra.dao.CriaturaDAO;
import com.mazmorra.dao.PartidaDAO;
import com.mazmorra.dao.PersonajeDAO;
import com.mazmorra.dao.impl.AventureroDAOMySQL;
import com.mazmorra.dao.impl.CriaturaDAOMySQL;
import com.mazmorra.dao.impl.PartidaDAOMySQL;
import com.mazmorra.dao.impl.PersonajeDAOMySQL;
import com.mazmorra.db.Conexion;
import com.mazmorra.db.Transaccion;
import com.mazmorra.exception.JuegoException;
import com.mazmorra.exception.PersistenciaException;

import java.sql.Connection;
import java.sql.SQLException;

public final class TransaccionMySQL implements Transaccion {

    @Override
    public <T> T ejecutar(Trabajo<T> trabajo) throws JuegoException {
        // La unidad de trabajo comparte una única conexión.
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            CriaturaDAO criaturas = new CriaturaDAOMySQL(cn);
            AventureroDAO aventureros = new AventureroDAOMySQL(cn);
            PersonajeDAO personajes = new PersonajeDAOMySQL(cn);
            PartidaDAO partidas = new PartidaDAOMySQL(cn);
            try {
                T resultado = trabajo.ejecutar(criaturas, aventureros, personajes, partidas);
                cn.commit();
                return resultado;
            } catch (Throwable e) {
                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                if (e instanceof Error err) {
                    throw err;
                }
                if (e instanceof JuegoException juegoException) {
                    throw juegoException;
                }
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new PersistenciaException("Error en la transacción: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error en la transacción: " + e.getMessage(), e);
        }
    }
}
