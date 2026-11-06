package com.mazmorra.service;

import com.mazmorra.dao.AventureroDAO;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Aventurero;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria del DAO de aventureros para las pruebas.
 */
class AventureroDAOEnMemoria implements AventureroDAO {

    private final Map<Integer, Aventurero> aventureros = new LinkedHashMap<>();
    private int siguienteId = 1;

    @Override
    public int insertar(Aventurero aventurero) throws PersistenciaException {
        int id = siguienteId++;
        aventurero.setId(id);
        aventureros.put(id, aventurero);
        return id;
    }

    @Override
    public boolean actualizar(Aventurero aventurero) throws PersistenciaException {
        if (aventurero == null || !aventureros.containsKey(aventurero.getId())) {
            return false;
        }
        aventureros.put(aventurero.getId(), aventurero);
        return true;
    }

    @Override
    public Optional<Aventurero> buscarPorId(int id) throws PersistenciaException {
        return Optional.ofNullable(aventureros.get(id));
    }

    @Override
    public List<Aventurero> listarTodos() throws PersistenciaException {
        return new ArrayList<>(aventureros.values());
    }

    @Override
    public boolean eliminar(int id) throws PersistenciaException {
        return aventureros.remove(id) != null;
    }

    @Override
    public int eliminarTodos() throws PersistenciaException {
        int cantidad = aventureros.size();
        aventureros.clear();
        return cantidad;
    }
}
