package com.mazmorra.dao.impl;

import com.mazmorra.dao.AventureroDAO;
import com.mazmorra.db.Conexion;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Aventurero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AventureroDAOMySQL implements AventureroDAO {

    private static final String COLUMNAS = "ID, Nombre, Nivel, Vida, VidaMaxima, AtaqueBase, "
            + "Oro, PisoActual";

    private static final String SQL_INSERTAR = "INSERT INTO aventurero (Nombre, Nivel, Vida, "
            + "VidaMaxima, AtaqueBase, Oro, PisoActual) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR = "UPDATE aventurero SET Nombre = ?, Nivel = ?, Vida = ?, "
            + "VidaMaxima = ?, AtaqueBase = ?, Oro = ?, PisoActual = ? WHERE ID = ?";

    private static final String SQL_BUSCAR_POR_ID = "SELECT " + COLUMNAS + " FROM aventurero WHERE ID = ?";
    private static final String SQL_LISTAR_TODOS = "SELECT " + COLUMNAS + " FROM aventurero ORDER BY ID";
    private static final String SQL_ELIMINAR = "DELETE FROM aventurero WHERE ID = ?";
    private static final String SQL_ELIMINAR_TODOS = "DELETE FROM aventurero";

    public AventureroDAOMySQL() {
    }

    @Override
    public int insertar(Aventurero aventurero) throws PersistenciaException {
        if (aventurero == null) {
            throw new PersistenciaException("No se puede insertar un aventurero nulo.");
        }
        try (Connection cn = Conexion.getConexion()) {
            return insertar(aventurero, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al insertar el aventurero '" + aventurero.getNombre() + "' en la base de datos.", e);
        }
    }

    @Override
    public int insertar(Aventurero aventurero, Connection conexion) throws PersistenciaException {
        if (aventurero == null) {
            throw new PersistenciaException("No se puede insertar un aventurero nulo.");
        }
        try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, aventurero.getNombre());
            ps.setInt(2, aventurero.getNivel());
            ps.setInt(3, aventurero.getVida());
            ps.setInt(4, aventurero.getVidaMaxima());
            ps.setInt(5, aventurero.getAtaqueBase());
            ps.setInt(6, aventurero.getOro());
            ps.setInt(7, aventurero.getPisoActual());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new PersistenciaException(
                            "La inserción del aventurero '" + aventurero.getNombre()
                            + "' no devolvió una clave generada.");
                }
                int id = claves.getInt(1);
                aventurero.setId(id);
                return id;
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al insertar el aventurero '" + aventurero.getNombre() + "' en la base de datos.", e);
        }
    }

    @Override
    public boolean actualizar(Aventurero aventurero) throws PersistenciaException {
        if (aventurero == null) {
            throw new PersistenciaException("No se puede actualizar un aventurero nulo.");
        }
        try (Connection cn = Conexion.getConexion()) {
            return actualizar(aventurero, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al actualizar el aventurero con ID " + aventurero.getId() + ".", e);
        }
    }

    @Override
    public boolean actualizar(Aventurero aventurero, Connection conexion) throws PersistenciaException {
        if (aventurero == null) {
            throw new PersistenciaException("No se puede actualizar un aventurero nulo.");
        }
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, aventurero.getNombre());
            ps.setInt(2, aventurero.getNivel());
            ps.setInt(3, aventurero.getVida());
            ps.setInt(4, aventurero.getVidaMaxima());
            ps.setInt(5, aventurero.getAtaqueBase());
            ps.setInt(6, aventurero.getOro());
            ps.setInt(7, aventurero.getPisoActual());
            ps.setInt(8, aventurero.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al actualizar el aventurero con ID " + aventurero.getId() + ".", e);
        }
    }

    @Override
    public Optional<Aventurero> buscarPorId(int id) throws PersistenciaException {
        // Sin fila devuelve un Optional vacío, no null.
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearAventurero(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al buscar el aventurero con ID " + id + " en la base de datos.", e);
        }
    }

    @Override
    public List<Aventurero> listarTodos() throws PersistenciaException {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR_TODOS)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<Aventurero> aventureros = new ArrayList<>();
                while (rs.next()) {
                    aventureros.add(mapearAventurero(rs));
                }
                return aventureros;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar todos los aventureros.", e);
        }
    }

    @Override
    public boolean eliminar(int id) throws PersistenciaException {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar el aventurero con ID " + id + ".", e);
        }
    }

    @Override
    public int eliminarTodos() throws PersistenciaException {
        try (Connection cn = Conexion.getConexion()) {
            return eliminarTodos(cn);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar todos los aventureros.", e);
        }
    }

    @Override
    public int eliminarTodos(Connection conexion) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR_TODOS)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar todos los aventureros.", e);
        }
    }

    private Aventurero mapearAventurero(ResultSet rs) throws SQLException, PersistenciaException {
        int id = rs.getInt("ID");
        String nombre = rs.getString("Nombre");
        int nivel = rs.getInt("Nivel");
        int vida = rs.getInt("Vida");
        int vidaMaxima = rs.getInt("VidaMaxima");
        int ataqueBase = rs.getInt("AtaqueBase");
        int oro = rs.getInt("Oro");
        int pisoActual = rs.getInt("PisoActual");
        try {
            return new Aventurero(id, nombre, nivel, vida, vidaMaxima, ataqueBase, oro, pisoActual);
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Fila corrupta en aventurero con id " + id, e);
        }
    }
}
