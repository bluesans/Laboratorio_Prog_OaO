package com.mazmorra.service;

import com.mazmorra.dao.CriaturaDAO;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Criatura;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria del DAO de criaturas para las pruebas.
 */
class InMemoryCriaturaDAO implements CriaturaDAO {

    private final Map<Integer, Criatura> criaturas = new LinkedHashMap<>();
    private int siguienteId = 1;

    @Override
    public int insertar(Criatura criatura) throws PersistenciaException {
        int id = siguienteId++;
        criatura.setId(id);
        criaturas.put(id, criatura);
        return id;
    }

    @Override
    public boolean actualizar(Criatura criatura) throws PersistenciaException {
        if (criatura == null || !criaturas.containsKey(criatura.getId())) {
            return false;
        }
        criaturas.put(criatura.getId(), criatura);
        return true;
    }

    @Override
    public Optional<Criatura> buscarPorId(int id) throws PersistenciaException {
        return Optional.ofNullable(criaturas.get(id));
    }

    @Override
    public List<Criatura> listarPorPiso(int piso) throws PersistenciaException {
        List<Criatura> resultado = new ArrayList<>();
        for (Criatura criatura : criaturas.values()) {
            if (criatura.getPisoMazmorra() == piso) {
                resultado.add(criatura);
            }
        }
        return resultado;
    }

    @Override
    public List<Criatura> listarTodas() throws PersistenciaException {
        return new ArrayList<>(criaturas.values());
    }

    @Override
    public boolean eliminar(int id) throws PersistenciaException {
        return criaturas.remove(id) != null;
    }

    @Override
    public int eliminarTodas() throws PersistenciaException {
        int cantidad = criaturas.size();
        criaturas.clear();
        return cantidad;
    }
}
