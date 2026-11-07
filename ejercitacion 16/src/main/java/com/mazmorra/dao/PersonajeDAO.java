package com.mazmorra.dao;

import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Personaje;

import java.util.List;
import java.util.Optional;

public interface PersonajeDAO {

    int insertar(Personaje personaje) throws PersistenciaException;

    Optional<Personaje> buscarPorId(int id) throws PersistenciaException;

    Optional<Personaje> buscarPorNombre(String nombre) throws PersistenciaException;

    boolean actualizar(Personaje personaje) throws PersistenciaException;

    List<Personaje> ranking(int limite) throws PersistenciaException;

    List<Personaje> listarTodos() throws PersistenciaException;
}
