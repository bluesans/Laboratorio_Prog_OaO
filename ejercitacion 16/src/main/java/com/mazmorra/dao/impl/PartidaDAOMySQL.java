package com.mazmorra.dao.impl;

import com.mazmorra.dao.PartidaDAO;
import com.mazmorra.db.Conexion;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Partida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PartidaDAOMySQL implements PartidaDAO {

    private static final String COLUMNAS = "ID, PersonajeID, Ronda, Piso, Oro, "
            + "EnemigosDerrotados, Puntos, Resultado, Fecha";

    private static final String SQL_INSERTAR = "INSERT INTO partida (PersonajeID, Ronda, Piso, "
            + "Oro, EnemigosDerrotados, Puntos, Resultado, Fecha) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_LISTAR_POR_PERSONAJE = "SELECT " + COLUMNAS
            + " FROM partida WHERE PersonajeID = ? ORDER BY ID";

    private final Connection conexionInyectada;

    public PartidaDAOMySQL() {
        this.conexionInyectada = null;
    }

    public PartidaDAOMySQL(Connection conexion) {
        this.conexionInyectada = conexion;
    }

    @Override
    public int insertar(Partida partida) throws PersistenciaException {
        if (partida == null) {
            throw new PersistenciaException("No se puede insertar una partida nula.");
        }
        if (conexionInyectada != null) {
            return insertarConConexion(partida, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return insertarConConexion(partida, cn);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al insertar la partida en la base de datos.", e);
        }
    }

    private int insertarConConexion(Partida partida, Connection conexion) throws PersistenciaException {
        if (partida == null) {
            throw new PersistenciaException("No se puede insertar una partida nula.");
        }
        try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, partida.personajeId());
            ps.setInt(2, partida.ronda());
            ps.setInt(3, partida.piso());
            ps.setInt(4, partida.oro());
            ps.setInt(5, partida.enemigosDerrotados());
            ps.setInt(6, partida.puntos());
            ps.setString(7, partida.resultado());
            if (partida.fecha() == null) {
                ps.setNull(8, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(8, Timestamp.valueOf(partida.fecha()));
            }
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new PersistenciaException(
                            "La inserción de la partida no devolvió una clave generada.");
                }
                int id = claves.getInt(1);
                return id;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al insertar la partida en la base de datos.", e);
        }
    }

    @Override
    public List<Partida> listarPorPersonaje(int personajeId) throws PersistenciaException {
        if (conexionInyectada != null) {
            return listarPorPersonajeConConexion(personajeId, conexionInyectada);
        }
        try (Connection cn = Conexion.getConexion()) {
            return listarPorPersonajeConConexion(personajeId, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al listar las partidas del personaje con ID " + personajeId + ".", e);
        }
    }

    private List<Partida> listarPorPersonajeConConexion(int personajeId, Connection conexion)
            throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_LISTAR_POR_PERSONAJE)) {
            ps.setInt(1, personajeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Partida> partidas = new ArrayList<>();
                while (rs.next()) {
                    partidas.add(mapearPartida(rs));
                }
                return partidas;
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al listar las partidas del personaje con ID " + personajeId + ".", e);
        }
    }

    private Partida mapearPartida(ResultSet rs) throws SQLException, PersistenciaException {
        int id = rs.getInt("ID");
        int personajeId = rs.getInt("PersonajeID");
        int ronda = rs.getInt("Ronda");
        int piso = rs.getInt("Piso");
        int oro = rs.getInt("Oro");
        int enemigosDerrotados = rs.getInt("EnemigosDerrotados");
        int puntos = rs.getInt("Puntos");
        String resultado = rs.getString("Resultado");
        Timestamp marca = rs.getTimestamp("Fecha");
        LocalDateTime fecha = marca == null ? null : marca.toLocalDateTime();
        return new Partida(id, personajeId, ronda, piso, oro, enemigosDerrotados,
                puntos, resultado, fecha);
    }
}
