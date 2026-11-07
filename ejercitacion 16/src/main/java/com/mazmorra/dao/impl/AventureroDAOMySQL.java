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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AventureroDAOMySQL implements AventureroDAO {

    private static final String COLUMNAS = "ID, Nombre, Nivel, Vida, VidaMaxima, AtaqueBase, "
            + "Oro, PisoActual, PersonajeID, Ronda, EnemigosDerrotados";

    private static final String SQL_INSERTAR = "INSERT INTO aventurero (Nombre, Nivel, Vida, "
            + "VidaMaxima, AtaqueBase, Oro, PisoActual, PersonajeID, Ronda, EnemigosDerrotados) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR = "UPDATE aventurero SET Nombre = ?, Nivel = ?, Vida = ?, "
            + "VidaMaxima = ?, AtaqueBase = ?, Oro = ?, PisoActual = ?, PersonajeID = ?, Ronda = ?, "
            + "EnemigosDerrotados = ? WHERE ID = ?";

    private static final String SQL_BUSCAR_POR_ID = "SELECT " + COLUMNAS + " FROM aventurero WHERE ID = ?";
    private static final String SQL_LISTAR_TODOS = "SELECT " + COLUMNAS + " FROM aventurero ORDER BY ID";
    private static final String SQL_ELIMINAR = "DELETE FROM aventurero WHERE ID = ?";
    private static final String SQL_ELIMINAR_TODOS = "DELETE FROM aventurero";

    private final Connection conexionInyectada;

    public AventureroDAOMySQL() {
        this.conexionInyectada = null;
    }

    public AventureroDAOMySQL(Connection conexion) {
        this.conexionInyectada = conexion;
    }

    @Override
    public int insertar(Aventurero aventurero) throws PersistenciaException {
        if (aventurero == null) {
            throw new PersistenciaException("No se puede insertar un aventurero nulo.");
        }
        if (conexionInyectada != null) {
            return insertarConConexion(aventurero, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return insertarConConexion(aventurero, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al insertar el aventurero '" + aventurero.getNombre() + "' en la base de datos.", e);
        }
    }

    private int insertarConConexion(Aventurero aventurero, Connection conexion)
            throws PersistenciaException {
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
            escribirPersonajeId(ps, 8, aventurero);
            ps.setInt(9, aventurero.getRonda());
            ps.setInt(10, aventurero.getEnemigosDerrotados());
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
        if (conexionInyectada != null) {
            return actualizarConConexion(aventurero, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return actualizarConConexion(aventurero, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al actualizar el aventurero con ID " + aventurero.getId() + ".", e);
        }
    }

    private boolean actualizarConConexion(Aventurero aventurero, Connection conexion)
            throws PersistenciaException {
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
            escribirPersonajeId(ps, 8, aventurero);
            ps.setInt(9, aventurero.getRonda());
            ps.setInt(10, aventurero.getEnemigosDerrotados());
            ps.setInt(11, aventurero.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al actualizar el aventurero con ID " + aventurero.getId() + ".", e);
        }
    }

    @Override
    public Optional<Aventurero> buscarPorId(int id) throws PersistenciaException {
        if (conexionInyectada != null) {
            return buscarPorIdConConexion(id, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return buscarPorIdConConexion(id, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al buscar el aventurero con ID " + id + " en la base de datos.", e);
        }
    }

    private Optional<Aventurero> buscarPorIdConConexion(int id, Connection conexion)
            throws PersistenciaException {
        // Sin fila devuelve un Optional vacío, no null.
        try (PreparedStatement ps = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
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
        if (conexionInyectada != null) {
            return listarTodosConConexion(conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return listarTodosConConexion(cn);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar todos los aventureros.", e);
        }
    }

    private List<Aventurero> listarTodosConConexion(Connection conexion) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR_TODOS)) {
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
        if (conexionInyectada != null) {
            return eliminarConConexion(id, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return eliminarConConexion(id, cn);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar el aventurero con ID " + id + ".", e);
        }
    }

    private boolean eliminarConConexion(int id, Connection conexion) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar el aventurero con ID " + id + ".", e);
        }
    }

    @Override
    public int eliminarTodos() throws PersistenciaException {
        if (conexionInyectada != null) {
            return eliminarTodosConConexion(conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return eliminarTodosConConexion(cn);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar todos los aventureros.", e);
        }
    }

    private int eliminarTodosConConexion(Connection conexion) throws PersistenciaException {
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
        int personajeId = rs.getInt("PersonajeID");
        if (rs.wasNull()) {
            // Una partida sin personaje asociado se representa con 0.
            personajeId = 0;
        }
        int ronda = rs.getInt("Ronda");
        int enemigosDerrotados = rs.getInt("EnemigosDerrotados");
        try {
            return new Aventurero(id, nombre, nivel, vida, vidaMaxima, ataqueBase, oro, pisoActual,
                    personajeId, ronda, enemigosDerrotados);
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Fila corrupta en aventurero con id " + id, e);
        }
    }

    private void escribirPersonajeId(PreparedStatement ps, int posicion, Aventurero aventurero)
            throws SQLException {
        // Sin personaje asociado la columna queda NULL, no en cero.
        if (aventurero.getPersonajeId() > 0) {
            ps.setInt(posicion, aventurero.getPersonajeId());
        } else {
            ps.setNull(posicion, Types.INTEGER);
        }
    }
}
