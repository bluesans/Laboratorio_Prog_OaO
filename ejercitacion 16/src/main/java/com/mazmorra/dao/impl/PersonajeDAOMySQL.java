package com.mazmorra.dao.impl;

import com.mazmorra.dao.PersonajeDAO;
import com.mazmorra.db.Conexion;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Personaje;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonajeDAOMySQL implements PersonajeDAO {

    private static final String COLUMNAS = "ID, Nombre, MejorPuntaje, Rondas, Partidas";

    private static final String SQL_INSERTAR = "INSERT INTO personaje (Nombre, MejorPuntaje, "
            + "Rondas, Partidas) VALUES (?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR = "UPDATE personaje SET Nombre = ?, MejorPuntaje = ?, "
            + "Rondas = ?, Partidas = ? WHERE ID = ?";

    private static final String SQL_BUSCAR_POR_ID = "SELECT " + COLUMNAS + " FROM personaje WHERE ID = ?";
    private static final String SQL_BUSCAR_POR_NOMBRE = "SELECT " + COLUMNAS
            + " FROM personaje WHERE Nombre = ?";
    private static final String SQL_RANKING = "SELECT " + COLUMNAS + " FROM personaje "
            + "WHERE Partidas > 0 ORDER BY MejorPuntaje DESC, Rondas DESC, Nombre ASC LIMIT ?";
    private static final String SQL_LISTAR_TODOS = "SELECT " + COLUMNAS + " FROM personaje ORDER BY ID";

    private final Connection conexionInyectada;

    public PersonajeDAOMySQL() {
        this.conexionInyectada = null;
    }

    public PersonajeDAOMySQL(Connection conexion) {
        this.conexionInyectada = conexion;
    }

    @Override
    public int insertar(Personaje personaje) throws PersistenciaException {
        if (personaje == null) {
            throw new PersistenciaException("No se puede insertar un personaje nulo.");
        }
        if (conexionInyectada != null) {
            return insertarConConexion(personaje, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return insertarConConexion(personaje, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al insertar el personaje '" + personaje.getNombre() + "' en la base de datos.", e);
        }
    }

    private int insertarConConexion(Personaje personaje, Connection conexion) throws PersistenciaException {
        if (personaje == null) {
            throw new PersistenciaException("No se puede insertar un personaje nulo.");
        }
        try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, personaje.getNombre());
            ps.setInt(2, personaje.getMejorPuntaje());
            ps.setInt(3, personaje.getRondas());
            ps.setInt(4, personaje.getPartidas());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new PersistenciaException(
                            "La inserción del personaje '" + personaje.getNombre()
                            + "' no devolvió una clave generada.");
                }
                int id = claves.getInt(1);
                personaje.setId(id);
                return id;
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al insertar el personaje '" + personaje.getNombre() + "' en la base de datos.", e);
        }
    }

    @Override
    public boolean actualizar(Personaje personaje) throws PersistenciaException {
        if (personaje == null) {
            throw new PersistenciaException("No se puede actualizar un personaje nulo.");
        }
        if (conexionInyectada != null) {
            return actualizarConConexion(personaje, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return actualizarConConexion(personaje, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al actualizar el personaje con ID " + personaje.getId() + ".", e);
        }
    }

    private boolean actualizarConConexion(Personaje personaje, Connection conexion)
            throws PersistenciaException {
        if (personaje == null) {
            throw new PersistenciaException("No se puede actualizar un personaje nulo.");
        }
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, personaje.getNombre());
            ps.setInt(2, personaje.getMejorPuntaje());
            ps.setInt(3, personaje.getRondas());
            ps.setInt(4, personaje.getPartidas());
            ps.setInt(5, personaje.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al actualizar el personaje con ID " + personaje.getId() + ".", e);
        }
    }

    @Override
    public Optional<Personaje> buscarPorId(int id) throws PersistenciaException {
        if (conexionInyectada != null) {
            return buscarPorIdConConexion(id, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return buscarPorIdConConexion(id, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al buscar el personaje con ID " + id + " en la base de datos.", e);
        }
    }

    private Optional<Personaje> buscarPorIdConConexion(int id, Connection conexion)
            throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearPersonaje(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al buscar el personaje con ID " + id + " en la base de datos.", e);
        }
    }

    @Override
    public Optional<Personaje> buscarPorNombre(String nombre) throws PersistenciaException {
        if (conexionInyectada != null) {
            return buscarPorNombreConConexion(nombre, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return buscarPorNombreConConexion(nombre, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al buscar el personaje '" + nombre + "' en la base de datos.", e);
        }
    }

    private Optional<Personaje> buscarPorNombreConConexion(String nombre, Connection conexion)
            throws PersistenciaException {
        // La colación de la base compara sin distinguir mayúsculas.
        try (PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearPersonaje(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al buscar el personaje '" + nombre + "' en la base de datos.", e);
        }
    }

    @Override
    public List<Personaje> ranking(int limite) throws PersistenciaException {
        if (conexionInyectada != null) {
            return rankingConConexion(limite, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return rankingConConexion(limite, cn);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar el ranking de personajes.", e);
        }
    }

    private List<Personaje> rankingConConexion(int limite, Connection conexion)
            throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_RANKING)) {
            ps.setInt(1, Math.max(0, limite));
            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar el ranking de personajes.", e);
        }
    }

    @Override
    public List<Personaje> listarTodos() throws PersistenciaException {
        if (conexionInyectada != null) {
            return listarTodosConConexion(conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return listarTodosConConexion(cn);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar todos los personajes.", e);
        }
    }

    private List<Personaje> listarTodosConConexion(Connection conexion) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR_TODOS)) {
            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar todos los personajes.", e);
        }
    }

    private List<Personaje> mapearLista(ResultSet rs) throws SQLException, PersistenciaException {
        List<Personaje> personajes = new ArrayList<>();
        while (rs.next()) {
            personajes.add(mapearPersonaje(rs));
        }
        return personajes;
    }

    private Personaje mapearPersonaje(ResultSet rs) throws SQLException, PersistenciaException {
        int id = rs.getInt("ID");
        String nombre = rs.getString("Nombre");
        int mejorPuntaje = rs.getInt("MejorPuntaje");
        int rondas = rs.getInt("Rondas");
        int partidas = rs.getInt("Partidas");
        try {
            return new Personaje(id, nombre, mejorPuntaje, rondas, partidas);
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Fila corrupta en personaje con id " + id, e);
        }
    }
}
