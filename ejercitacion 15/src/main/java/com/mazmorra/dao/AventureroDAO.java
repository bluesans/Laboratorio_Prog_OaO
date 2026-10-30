package com.mazmorra.dao;

import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Aventurero;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface AventureroDAO {

    int insertar(Aventurero aventurero) throws PersistenciaException;

    int insertar(Aventurero aventurero, Connection conexion) throws PersistenciaException;

    boolean actualizar(Aventurero aventurero) throws PersistenciaException;

    boolean actualizar(Aventurero aventurero, Connection conexion) throws PersistenciaException;

    Optional<Aventurero> buscarPorId(int id) throws PersistenciaException;

    List<Aventurero> listarTodos() throws PersistenciaException;

    boolean eliminar(int id) throws PersistenciaException;

    int eliminarTodos() throws PersistenciaException;

    int eliminarTodos(Connection conexion) throws PersistenciaException;
}
