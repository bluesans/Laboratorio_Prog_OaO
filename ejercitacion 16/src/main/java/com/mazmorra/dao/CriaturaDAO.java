package com.mazmorra.dao;

import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Criatura;

import java.util.List;
import java.util.Optional;

public interface CriaturaDAO {

    // Devuelve el id que asigna la base.
    int insertar(Criatura criatura) throws PersistenciaException;

    boolean actualizar(Criatura criatura) throws PersistenciaException;

    Optional<Criatura> buscarPorId(int id) throws PersistenciaException;

    List<Criatura> listarPorPiso(int piso) throws PersistenciaException;

    List<Criatura> listarTodas() throws PersistenciaException;

    boolean eliminar(int id) throws PersistenciaException;

    int eliminarTodas() throws PersistenciaException;
}
