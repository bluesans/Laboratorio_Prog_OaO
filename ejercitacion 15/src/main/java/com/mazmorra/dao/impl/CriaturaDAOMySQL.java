package com.mazmorra.dao.impl;

import com.mazmorra.dao.CriaturaDAO;
import com.mazmorra.db.Conexion;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.model.Fantasma;
import com.mazmorra.model.Orco;
import com.mazmorra.model.TipoCriatura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CriaturaDAOMySQL implements CriaturaDAO {

    private static final String COLUMNAS = "ID, Nombre, Tipo, Vida, VidaMaxima, NivelAmenaza, "
            + "PisoMazmorra, Derrotada, Evadida, RecompensaPagada, DanioAtaque, ResistenciaFisica, "
            + "EsIncorporeo, ResistenciaMagica, FaseActual, CantidadFases, AlientoDeFuego";

    private static final String SQL_INSERTAR = "INSERT INTO criatura (Nombre, Tipo, Vida, VidaMaxima, "
            + "NivelAmenaza, PisoMazmorra, Derrotada, Evadida, RecompensaPagada, DanioAtaque, "
            + "ResistenciaFisica, EsIncorporeo, ResistenciaMagica, FaseActual, "
            + "CantidadFases, AlientoDeFuego) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR = "UPDATE criatura SET Nombre = ?, Tipo = ?, Vida = ?, "
            + "VidaMaxima = ?, NivelAmenaza = ?, PisoMazmorra = ?, Derrotada = ?, Evadida = ?, "
            + "RecompensaPagada = ?, DanioAtaque = ?, ResistenciaFisica = ?, EsIncorporeo = ?, "
            + "ResistenciaMagica = ?, FaseActual = ?, CantidadFases = ?, "
            + "AlientoDeFuego = ? WHERE ID = ?";

    private static final String SQL_BUSCAR_POR_ID = "SELECT " + COLUMNAS + " FROM criatura WHERE ID = ?";
    private static final String SQL_LISTAR_POR_PISO = "SELECT " + COLUMNAS
            + " FROM criatura WHERE PisoMazmorra = ? ORDER BY ID";
    private static final String SQL_LISTAR_TODAS = "SELECT " + COLUMNAS + " FROM criatura ORDER BY ID";
    private static final String SQL_ELIMINAR = "DELETE FROM criatura WHERE ID = ?";
    private static final String SQL_ELIMINAR_TODAS = "DELETE FROM criatura";

    private static final int PARAM_RECOMPENSA_PAGADA = 9;
    private static final int PARAM_DANIO_ATAQUE = 10;
    private static final int PARAM_RESISTENCIA_FISICA = 11;
    private static final int PARAM_ES_INCORPOREO = 12;
    private static final int PARAM_RESISTENCIA_MAGICA = 13;
    private static final int PARAM_FASE_ACTUAL = 14;
    private static final int PARAM_CANTIDAD_FASES = 15;
    private static final int PARAM_ALIENTO_DE_FUEGO = 16;

    public CriaturaDAOMySQL() {
    }

    @Override
    public int insertar(Criatura criatura) throws PersistenciaException {
        if (criatura == null) {
            throw new PersistenciaException("No se puede insertar una criatura nula.");
        }
        try (Connection cn = Conexion.getConexion()) {
            return insertar(criatura, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al insertar la criatura '" + criatura.getNombre() + "' en la base de datos.", e);
        }
    }

    @Override
    public int insertar(Criatura criatura, Connection conexion) throws PersistenciaException {
        if (criatura == null) {
            throw new PersistenciaException("No se puede insertar una criatura nula.");
        }
        // RETURN_GENERATED_KEYS permite leer el id que asigna la base.
        try (PreparedStatement ps = conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, criatura.getNombre());
            ps.setString(2, criatura.getTipo().name());
            ps.setInt(3, criatura.getVida());
            ps.setInt(4, criatura.getVidaMaxima());
            ps.setInt(5, criatura.getNivelAmenaza());
            ps.setInt(6, criatura.getPisoMazmorra());
            ps.setBoolean(7, criatura.isDerrotada());
            ps.setBoolean(8, criatura.isEvadida());
            ps.setBoolean(PARAM_RECOMPENSA_PAGADA, criatura.isRecompensaPagada());
            ps.setInt(PARAM_DANIO_ATAQUE, criatura.getDanioAtaqueBase());
            escribirColumnasEspecificas(ps, criatura);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new PersistenciaException(
                            "La inserción de la criatura '" + criatura.getNombre()
                            + "' no devolvió una clave generada.");
                }
                int id = claves.getInt(1);
                criatura.setId(id);
                return id;
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al insertar la criatura '" + criatura.getNombre() + "' en la base de datos.", e);
        }
    }

    @Override
    public boolean actualizar(Criatura criatura) throws PersistenciaException {
        if (criatura == null) {
            throw new PersistenciaException("No se puede actualizar una criatura nula.");
        }
        try (Connection cn = Conexion.getConexion()) {
            return actualizar(criatura, cn);
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al actualizar la criatura con ID " + criatura.getId() + ".", e);
        }
    }

    @Override
    public boolean actualizar(Criatura criatura, Connection conexion) throws PersistenciaException {
        if (criatura == null) {
            throw new PersistenciaException("No se puede actualizar una criatura nula.");
        }
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, criatura.getNombre());
            ps.setString(2, criatura.getTipo().name());
            ps.setInt(3, criatura.getVida());
            ps.setInt(4, criatura.getVidaMaxima());
            ps.setInt(5, criatura.getNivelAmenaza());
            ps.setInt(6, criatura.getPisoMazmorra());
            ps.setBoolean(7, criatura.isDerrotada());
            ps.setBoolean(8, criatura.isEvadida());
            ps.setBoolean(PARAM_RECOMPENSA_PAGADA, criatura.isRecompensaPagada());
            ps.setInt(PARAM_DANIO_ATAQUE, criatura.getDanioAtaqueBase());
            escribirColumnasEspecificas(ps, criatura);
            ps.setInt(17, criatura.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al actualizar la criatura con ID " + criatura.getId() + ".", e);
        }
    }

    @Override
    public Optional<Criatura> buscarPorId(int id) throws PersistenciaException {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearCriatura(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al buscar la criatura con ID " + id + " en la base de datos.", e);
        }
    }

    @Override
    public List<Criatura> listarPorPiso(int piso) throws PersistenciaException {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR_POR_PISO)) {
            ps.setInt(1, piso);
            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al listar las criaturas del piso " + piso + ".", e);
        }
    }

    @Override
    public List<Criatura> listarTodas() throws PersistenciaException {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR_TODAS)) {
            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar todas las criaturas.", e);
        }
    }

    @Override
    public boolean eliminar(int id) throws PersistenciaException {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar la criatura con ID " + id + ".", e);
        }
    }

    @Override
    public int eliminarTodas() throws PersistenciaException {
        try (Connection cn = Conexion.getConexion()) {
            return eliminarTodas(cn);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar todas las criaturas.", e);
        }
    }

    @Override
    public int eliminarTodas(Connection conexion) throws PersistenciaException {
        try (PreparedStatement ps = conexion.prepareStatement(SQL_ELIMINAR_TODAS)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al eliminar todas las criaturas.", e);
        }
    }

    private void escribirColumnasEspecificas(PreparedStatement ps, Criatura criatura)
            throws SQLException, PersistenciaException {
        ps.setNull(PARAM_RESISTENCIA_FISICA, Types.INTEGER);
        ps.setNull(PARAM_ES_INCORPOREO, Types.BOOLEAN);
        ps.setNull(PARAM_RESISTENCIA_MAGICA, Types.INTEGER);
        ps.setNull(PARAM_FASE_ACTUAL, Types.INTEGER);
        ps.setNull(PARAM_CANTIDAD_FASES, Types.INTEGER);
        ps.setNull(PARAM_ALIENTO_DE_FUEGO, Types.BOOLEAN);
        if (criatura instanceof Esqueleto esqueleto) {
            ps.setInt(PARAM_RESISTENCIA_FISICA, esqueleto.getResistenciaFisica());
        } else if (criatura instanceof Fantasma fantasma) {
            ps.setBoolean(PARAM_ES_INCORPOREO, fantasma.isEsIncorporeo());
        } else if (criatura instanceof Orco orco) {
            ps.setInt(PARAM_RESISTENCIA_MAGICA, orco.getResistenciaMagica());
        } else if (criatura instanceof DragonJefe dragon) {
            ps.setInt(PARAM_FASE_ACTUAL, dragon.getFaseActual());
            ps.setInt(PARAM_CANTIDAD_FASES, dragon.getCantidadFases());
            ps.setBoolean(PARAM_ALIENTO_DE_FUEGO, dragon.isAlientoDeFuego());
        } else {
            throw new PersistenciaException(
                    "Tipo de criatura no soportado para persistir: " + criatura.getTipo());
        }
    }

    private List<Criatura> mapearLista(ResultSet rs) throws SQLException, PersistenciaException {
        List<Criatura> criaturas = new ArrayList<>();
        while (rs.next()) {
            criaturas.add(mapearCriatura(rs));
        }
        return criaturas;
    }

    private Criatura mapearCriatura(ResultSet rs) throws SQLException, PersistenciaException {
        int id = rs.getInt("ID");
        String nombre = rs.getString("Nombre");
        String tipoLeido = rs.getString("Tipo");
        int vida = rs.getInt("Vida");
        int vidaMaxima = rs.getInt("VidaMaxima");
        int nivelAmenaza = rs.getInt("NivelAmenaza");
        int pisoMazmorra = rs.getInt("PisoMazmorra");
        boolean derrotada = rs.getBoolean("Derrotada");
        boolean evadida = rs.getBoolean("Evadida");
        boolean recompensaPagada = rs.getBoolean("RecompensaPagada");
        int danioAtaque = rs.getInt("DanioAtaque");

        if (tipoLeido == null) {
            throw new PersistenciaException("Tipo de criatura desconocido en la base: null");
        }
        TipoCriatura tipo;
        try {
            tipo = TipoCriatura.valueOf(tipoLeido);
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException(
                    "Tipo de criatura desconocido en la base: " + tipoLeido, e);
        }
        Criatura criatura;
        try {
            // El Tipo decide la subclase concreta.
            criatura = switch (tipo) {
                case ESQUELETO -> {
                    int resistenciaFisica = rs.getInt("ResistenciaFisica");
                    if (rs.wasNull()) {
                        throw new PersistenciaException(
                                "Esqueleto sin ResistenciaFisica en la base (id " + id + ")");
                    }
                    yield new Esqueleto(id, nombre, vida, vidaMaxima, nivelAmenaza, danioAtaque,
                            pisoMazmorra, derrotada, resistenciaFisica);
                }
                case FANTASMA -> {
                    boolean esIncorporeo = rs.getBoolean("EsIncorporeo");
                    if (rs.wasNull()) {
                        throw new PersistenciaException(
                                "Fantasma sin EsIncorporeo en la base (id " + id + ")");
                    }
                    yield new Fantasma(id, nombre, vida, vidaMaxima, nivelAmenaza, danioAtaque,
                            pisoMazmorra, derrotada, esIncorporeo);
                }
                case ORCO -> {
                    int resistenciaMagica = rs.getInt("ResistenciaMagica");
                    if (rs.wasNull()) {
                        throw new PersistenciaException(
                                "Orco sin ResistenciaMagica en la base (id " + id + ")");
                    }
                    yield new Orco(id, nombre, vida, vidaMaxima, nivelAmenaza, danioAtaque,
                            pisoMazmorra, derrotada, resistenciaMagica);
                }
                case DRAGON_JEFE -> {
                    int faseActual = rs.getInt("FaseActual");
                    boolean faseNula = rs.wasNull();
                    int cantidadFases = rs.getInt("CantidadFases");
                    boolean cantidadNula = rs.wasNull();
                    boolean alientoDeFuego = rs.getBoolean("AlientoDeFuego");
                    boolean alientoNulo = rs.wasNull();
                    if (faseNula || cantidadNula || alientoNulo) {
                        throw new PersistenciaException(
                                "DragonJefe con datos de fase incompletos en la base (id " + id + ")");
                    }
                    yield new DragonJefe(id, nombre, vida, vidaMaxima, nivelAmenaza, danioAtaque,
                            pisoMazmorra, derrotada, faseActual, cantidadFases, alientoDeFuego);
                }
            };
        } catch (IllegalArgumentException e) {
            throw new PersistenciaException("Fila corrupta en criatura con id " + id, e);
        }

        if (evadida) {
            criatura.marcarEvadida();
        }
        if (recompensaPagada) {
            criatura.marcarRecompensaPagada();
        }
        return criatura;
    }
}
