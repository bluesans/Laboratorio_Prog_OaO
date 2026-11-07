package com.mazmorra.dao;

import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Partida;

import java.util.List;

public interface PartidaDAO {

    int insertar(Partida partida) throws PersistenciaException;

    List<Partida> listarPorPersonaje(int personajeId) throws PersistenciaException;
}
