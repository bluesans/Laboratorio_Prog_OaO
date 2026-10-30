package com.mazmorra.service.impl;

import com.mazmorra.dao.AventureroDAO;
import com.mazmorra.dao.CriaturaDAO;
import com.mazmorra.dao.impl.AventureroDAOMySQL;
import com.mazmorra.dao.impl.CriaturaDAOMySQL;
import com.mazmorra.db.Conexion;
import com.mazmorra.exception.AventureroDerrotadoException;
import com.mazmorra.exception.CriaturaYaDerrotadaException;
import com.mazmorra.exception.CriaturasPendientesException;
import com.mazmorra.exception.DanioInvalidoException;
import com.mazmorra.exception.HuidaProhibidaException;
import com.mazmorra.exception.JuegoException;
import com.mazmorra.exception.OroInsuficienteException;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.exception.VidaCompletaException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.Danable;
import com.mazmorra.model.Orco;
import com.mazmorra.model.TipoAtaque;
import com.mazmorra.service.JuegoService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JuegoServiceImpl implements JuegoService {

    private static final int ORO_POR_NIVEL_AMENAZA = 5;

    private final CriaturaDAO criaturaDAO;
    private final AventureroDAO aventureroDAO;

    public JuegoServiceImpl() {
        this(new CriaturaDAOMySQL(), new AventureroDAOMySQL());
    }

    public JuegoServiceImpl(CriaturaDAO criaturaDAO, AventureroDAO aventureroDAO) {
        this.criaturaDAO = criaturaDAO;
        this.aventureroDAO = aventureroDAO;
    }

    @Override
    public Aventurero registrarAventurero(String nombre, int vidaMaxima, int ataqueBase)
            throws PersistenciaException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del aventurero no puede ser nulo ni vacío.");
        }
        if (vidaMaxima <= 0) {
            throw new IllegalArgumentException("La vida máxima debe ser mayor que cero.");
        }
        if (ataqueBase <= 0) {
            throw new IllegalArgumentException("El ataque base debe ser mayor que cero.");
        }

        // Arranca en el piso 1 con 50 de oro.
        Aventurero aventurero = new Aventurero(0, nombre, 1, vidaMaxima, vidaMaxima, ataqueBase, 50, 1);
        int id = aventureroDAO.insertar(aventurero);
        aventurero.setId(id);
        return aventurero;
    }

    @Override
    public Criatura registrarCriatura(Criatura criatura) throws PersistenciaException {
        if (criatura == null) {
            throw new IllegalArgumentException("La criatura a registrar no puede ser nula.");
        }
        int id = criaturaDAO.insertar(criatura);
        criatura.setId(id);
        return criatura;
    }

    @Override
    public List<Criatura> listarCriaturasPorPiso(int piso) throws PersistenciaException {
        if (piso < 1) {
            throw new IllegalArgumentException("El piso debe ser al menos 1.");
        }
        return criaturaDAO.listarPorPiso(piso);
    }

    @Override
    public List<Criatura> listarCriaturas() throws PersistenciaException {
        return criaturaDAO.listarTodas();
    }

    @Override
    public void resolverAtaque(Danable objetivo, int danio)
            throws DanioInvalidoException, CriaturaYaDerrotadaException,
            AventureroDerrotadoException, PersistenciaException {
        resolverAtaque(objetivo, danio, TipoAtaque.FISICO);
    }

    @Override
    public void resolverAtaque(Danable objetivo, int danio, TipoAtaque tipoAtaque)
            throws DanioInvalidoException, CriaturaYaDerrotadaException,
            AventureroDerrotadoException, PersistenciaException {
        if (objetivo == null) {
            throw new DanioInvalidoException("No se puede atacar a un objetivo nulo.");
        }
        if (danio <= 0) {
            throw new DanioInvalidoException("El daño debe ser mayor que cero: " + danio);
        }
        if (objetivo.estaDerrotado()) {
            if (objetivo instanceof Criatura criaturaDerrotada) {
                throw new CriaturaYaDerrotadaException(
                        "La criatura ya estaba derrotada: " + criaturaDerrotada.getNombre());
            }
            if (objetivo instanceof Aventurero aventureroDerrotado) {
                throw new AventureroDerrotadoException(
                        "El aventurero ya estaba derrotado: " + aventureroDerrotado.getNombre());
            }
        }

        if (objetivo instanceof Criatura criaturaEvadida && criaturaEvadida.isEvadida()) {
            throw new IllegalArgumentException(
                    criaturaEvadida.getNombre() + " quedó atrás al huir y ya no puede pelear.");
        }

        TipoAtaque tipo = tipoAtaque == null ? TipoAtaque.FISICO : tipoAtaque;

        if (objetivo instanceof Criatura criatura) {
            aplicarAtaqueCriatura(criatura, danio, tipo);
            criaturaDAO.actualizar(criatura);
        } else {
            // Se resuelve por la interfaz, no por la clase concreta.
            objetivo.recibirDanio(danio);
            if (objetivo instanceof Aventurero aventurero) {
                aventureroDAO.actualizar(aventurero);
            }
        }
    }

    private void aplicarAtaqueCriatura(Criatura criatura, int danio, TipoAtaque tipoAtaque)
            throws DanioInvalidoException, CriaturaYaDerrotadaException {
        if (danio <= 0) {
            throw new DanioInvalidoException("El daño debe ser mayor que cero: " + danio);
        }
        if (criatura.estaDerrotado()) {
            throw new CriaturaYaDerrotadaException(
                    "La criatura ya estaba derrotada: " + criatura.getNombre());
        }
        if (criatura.isEvadida()) {
            throw new IllegalArgumentException(
                    criatura.getNombre() + " quedó atrás al huir y ya no puede pelear.");
        }
        criatura.recibirDanio(danio, tipoAtaque == null ? TipoAtaque.FISICO : tipoAtaque);
    }

    @Override
    public void contraatacarSiCorresponde(Orco orco, Danable objetivo)
            throws AventureroDerrotadoException, PersistenciaException {
        if (orco == null || objetivo == null) {
            return;
        }
        aplicarContraataque(orco, objetivo);
        if (objetivo instanceof Aventurero aventurero) {
            aventureroDAO.actualizar(aventurero);
        } else if (objetivo instanceof Criatura criatura) {
            criaturaDAO.actualizar(criatura);
        }
    }

    private void aplicarContraataque(Orco orco, Danable objetivo)
            throws AventureroDerrotadoException {
        if (orco.estaDerrotado()) {
            // Un orco muerto ya no devuelve el golpe.
            return;
        }
        if (!orco.estaViva()) {
            return;
        }
        if (objetivo instanceof Aventurero aventurero && aventurero.estaDerrotado()) {
            throw new AventureroDerrotadoException(
                    "El aventurero ya estaba derrotado: " + aventurero.getNombre());
        }
        objetivo.recibirDanio(orco.getDanioAtaque());
    }

    @Override
    public void turnoEnemigo(Aventurero aventurero, Criatura criatura)
            throws AventureroDerrotadoException, PersistenciaException {
        if (aventurero == null || criatura == null) {
            throw new IllegalArgumentException("El aventurero y la criatura no pueden ser nulos.");
        }
        aplicarTurnoEnemigo(aventurero, criatura);
        aventureroDAO.actualizar(aventurero);
    }

    private void aplicarTurnoEnemigo(Aventurero aventurero, Criatura criatura)
            throws AventureroDerrotadoException {
        if (aventurero.estaDerrotado()) {
            throw new AventureroDerrotadoException(
                    "El aventurero ya estaba derrotado: " + aventurero.getNombre());
        }
        if (criatura.estaDerrotado()) {
            return;
        }
        if (!criatura.estaViva()) {
            return;
        }
        aventurero.recibirDanio(criatura.getDanioAtaque());
    }

    @Override
    public void resolverTurno(Aventurero aventurero, Criatura criatura, TipoAtaque tipoAtaque)
            throws AventureroDerrotadoException, CriaturaYaDerrotadaException,
            DanioInvalidoException, PersistenciaException {
        if (aventurero == null || criatura == null) {
            throw new IllegalArgumentException("El aventurero y la criatura no pueden ser nulos.");
        }
        // Validación y normalización antes de abrir la conexión.
        TipoAtaque tipo = tipoAtaque == null ? TipoAtaque.FISICO : tipoAtaque;

        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                aplicarAtaqueCriatura(criatura, aventurero.getAtaqueBase(), tipo);
                if (!criaturaDAO.actualizar(criatura, cn)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para criatura id " + criatura.getId());
                }

                if (criatura.estaViva()) {
                    if (criatura instanceof Orco orco) {
                        aplicarContraataque(orco, aventurero);
                    } else {
                        aplicarTurnoEnemigo(aventurero, criatura);
                    }
                    if (!aventureroDAO.actualizar(aventurero, cn)) {
                        throw new PersistenciaException(
                                "Sin filas actualizadas para aventurero id " + aventurero.getId());
                    }
                }

                if (criatura.estaDerrotado()) {
                    pagarRecompensaEnTransaccion(aventurero, criatura, cn);
                }
                cn.commit();
            } catch (SQLException e) {
                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw new PersistenciaException(
                        "Error al resolver el turno de " + aventurero.getNombre() + ".", e);
            } catch (PersistenciaException e) {
                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al resolver el turno de " + aventurero.getNombre() + ".", e);
        }
    }

    @Override
    public void avanzarPiso(Aventurero aventurero)
            throws CriaturasPendientesException, AventureroDerrotadoException,
            PersistenciaException {
        if (aventurero == null) {
            throw new IllegalArgumentException("El aventurero no puede ser nulo.");
        }
        if (aventurero.estaDerrotado()) {
            throw new AventureroDerrotadoException(
                    "El aventurero está derrotado y no puede avanzar de piso: " + aventurero.getNombre());
        }

        // Solo se baja de piso si no quedan criaturas vivas en el actual.
        List<Criatura> criaturas = criaturaDAO.listarPorPiso(aventurero.getPisoActual());
        List<Criatura> vivas = new ArrayList<>();
        for (Criatura criatura : criaturas) {
            if (criatura.estaViva()) {
                vivas.add(criatura);
            }
        }

        if (!vivas.isEmpty()) {
            List<String> nombres = new ArrayList<>();
            for (Criatura viva : vivas) {
                nombres.add(viva.getNombre());
            }
            throw new CriaturasPendientesException(
                    "No se puede bajar de piso: quedan " + vivas.size()
                    + " criaturas vivas (" + String.join(", ", nombres) + ").");
        }

        aventurero.setPisoActual(aventurero.getPisoActual() + 1);
        aventureroDAO.actualizar(aventurero);
    }

    @Override
    public void huirDeCombate(Aventurero aventurero, Criatura criatura)
            throws HuidaProhibidaException, AventureroDerrotadoException,
            PersistenciaException {
        if (aventurero == null) {
            throw new IllegalArgumentException("El aventurero no puede ser nulo.");
        }
        if (criatura == null) {
            throw new IllegalArgumentException("La criatura no puede ser nula.");
        }
        if (aventurero.estaDerrotado()) {
            throw new AventureroDerrotadoException(
                    "El aventurero está derrotado y no puede huir: " + aventurero.getNombre());
        }
        if (!criatura.permiteHuida()) {
            throw new HuidaProhibidaException(
                    "No se puede huir de " + criatura.getNombre()
                    + ": es el jefe final de la mazmorra.");
        }
        if (criatura.isEvadida()) {
            throw new IllegalArgumentException(
                    criatura.getNombre() + " quedó atrás al huir y ya no puede pelear.");
        }
        if (criatura.estaDerrotado()) {
            throw new IllegalArgumentException("No hace falta huir de una criatura ya derrotada.");
        }

        // Huir cuesta hasta 10 de oro, la criatura golpea al escapar y queda atrás.
        aventurero.setOro(Math.max(0, aventurero.getOro() - COSTO_HUIDA));
        aventurero.recibirDanio(criatura.getDanioAtaque());
        criatura.marcarEvadida();

        // Las dos filas se actualizan en una sola transacción: o se guardan ambas o ninguna.
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                if (!aventureroDAO.actualizar(aventurero, cn)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para aventurero id " + aventurero.getId());
                }
                if (!criaturaDAO.actualizar(criatura, cn)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para criatura id " + criatura.getId());
                }
                cn.commit();
            } catch (SQLException e) {
                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw new PersistenciaException(
                        "Error al persistir la huida de " + aventurero.getNombre() + ".", e);
            } catch (PersistenciaException e) {
                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al persistir la huida de " + aventurero.getNombre() + ".", e);
        }
    }

    @Override
    public void usarPocion(Aventurero aventurero, int cantidad)
            throws VidaCompletaException, AventureroDerrotadoException,
            OroInsuficienteException, PersistenciaException {
        usarPocion(aventurero, cantidad, null);
    }

    @Override
    public void usarPocion(Aventurero aventurero, int cantidad, Criatura enemigo)
            throws VidaCompletaException, AventureroDerrotadoException,
            OroInsuficienteException, PersistenciaException {
        if (aventurero == null) {
            throw new IllegalArgumentException("El aventurero no puede ser nulo.");
        }
        if (aventurero.estaDerrotado()) {
            throw new AventureroDerrotadoException(
                    "El aventurero está derrotado y no puede usar pociones: " + aventurero.getNombre());
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de curación debe ser mayor que cero.");
        }
        if (aventurero.getVida() >= aventurero.getVidaMaxima()) {
            throw new VidaCompletaException(
                    "El aventurero " + aventurero.getNombre() + " ya tiene la vida al máximo ("
                    + aventurero.getVida() + "/" + aventurero.getVidaMaxima() + ").");
        }
        int faltante = aventurero.getVidaMaxima() - aventurero.getVida();
        int curado = Math.min(cantidad, faltante);
        int costo = (int) Math.ceil((double) COSTO_POCION * curado / TAMANO_POCION);
        if (aventurero.getOro() < costo) {
            throw new OroInsuficienteException(
                    "La poción cuesta " + costo + " de oro y " + aventurero.getNombre()
                    + " solo tiene " + aventurero.getOro() + ".");
        }

        // Pago, curación y golpe del enemigo se guardan en una sola transacción.
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                aventurero.setOro(aventurero.getOro() - costo);
                aventurero.curar(curado);
                if (!aventureroDAO.actualizar(aventurero, cn)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para aventurero id " + aventurero.getId());
                }
                if (enemigo != null && enemigo.estaViva()) {
                    aplicarTurnoEnemigo(aventurero, enemigo);
                    if (!aventureroDAO.actualizar(aventurero, cn)) {
                        throw new PersistenciaException(
                                "Sin filas actualizadas para aventurero id " + aventurero.getId());
                    }
                }
                cn.commit();
            } catch (SQLException e) {
                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw new PersistenciaException(
                        "Error al usar la poción de " + aventurero.getNombre() + ".", e);
            } catch (PersistenciaException e) {
                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "Error al usar la poción de " + aventurero.getNombre() + ".", e);
        }
    }

    @Override
    public int otorgarRecompensa(Aventurero aventurero, Criatura criatura)
            throws AventureroDerrotadoException, PersistenciaException {
        if (aventurero == null || criatura == null) {
            throw new IllegalArgumentException("El aventurero y la criatura no pueden ser nulos.");
        }
        int recompensa = aplicarRecompensa(aventurero, criatura);
        criaturaDAO.actualizar(criatura);
        aventureroDAO.actualizar(aventurero);
        return recompensa;
    }

    private int aplicarRecompensa(Aventurero aventurero, Criatura criatura)
            throws AventureroDerrotadoException {
        if (aventurero.estaDerrotado()) {
            throw new AventureroDerrotadoException(
                    "El aventurero está derrotado y no puede cobrar recompensas: " + aventurero.getNombre());
        }
        if (criatura.isEvadida()) {
            throw new IllegalArgumentException(
                    criatura.getNombre() + " quedó atrás al huir y no paga recompensa.");
        }
        if (!criatura.estaDerrotado()) {
            throw new IllegalArgumentException(
                    "No hay recompensa: " + criatura.getNombre() + " sigue en pie.");
        }
        if (criatura.isRecompensaPagada()) {
            throw new IllegalArgumentException(
                    "La recompensa de " + criatura.getNombre() + " ya fue cobrada.");
        }

        // La recompensa escala con el nivel de amenaza.
        int recompensa = criatura.getNivelAmenaza() * ORO_POR_NIVEL_AMENAZA;
        criatura.marcarRecompensaPagada();
        aventurero.setOro(aventurero.getOro() + recompensa);
        return recompensa;
    }

    private int pagarRecompensaEnTransaccion(Aventurero aventurero, Criatura criatura, Connection cn)
            throws PersistenciaException {
        // Si otra ejecución ya pagó, no se vuelve a cobrar.
        if (criatura.isRecompensaPagada()) {
            return 0;
        }
        int recompensa = criatura.getNivelAmenaza() * ORO_POR_NIVEL_AMENAZA;
        criatura.marcarRecompensaPagada();
        if (!criaturaDAO.actualizar(criatura, cn)) {
            throw new PersistenciaException(
                    "Sin filas actualizadas para criatura id " + criatura.getId());
        }
        aventurero.setOro(aventurero.getOro() + recompensa);
        if (!aventureroDAO.actualizar(aventurero, cn)) {
            throw new PersistenciaException(
                    "Sin filas actualizadas para aventurero id " + aventurero.getId());
        }
        return recompensa;
    }

    @Override
    public Aventurero obtenerEstadoAventurero(int idAventurero) throws PersistenciaException {
        return aventureroDAO.buscarPorId(idAventurero)
                .orElseThrow(() -> new PersistenciaException(
                        "No existe un aventurero con id " + idAventurero));
    }

    @Override
    public void reiniciarPartida() throws PersistenciaException {
        criaturaDAO.eliminarTodas();
        aventureroDAO.eliminarTodos();
    }

    @Override
    public int iniciarNuevaPartida(String nombre, int vidaMaxima, int ataqueBase,
            List<Criatura> escenario) throws JuegoException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del aventurero no puede ser nulo ni vacío.");
        }
        if (vidaMaxima <= 0) {
            throw new IllegalArgumentException("La vida máxima debe ser mayor que cero.");
        }
        if (ataqueBase <= 0) {
            throw new IllegalArgumentException("El ataque base debe ser mayor que cero.");
        }
        if (escenario == null) {
            throw new IllegalArgumentException("El escenario inicial no puede ser nulo.");
        }

        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                criaturaDAO.eliminarTodas(cn);
                aventureroDAO.eliminarTodos(cn);
                Aventurero aventurero = new Aventurero(
                        0, nombre, 1, vidaMaxima, vidaMaxima, ataqueBase, 50, 1);
                int id = aventureroDAO.insertar(aventurero, cn);
                for (Criatura criatura : escenario) {
                    criaturaDAO.insertar(criatura, cn);
                }
                cn.commit();
                return id;
            } catch (SQLException e) {
                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw new PersistenciaException("Error al iniciar la nueva partida.", e);
            } catch (PersistenciaException e) {
                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al iniciar la nueva partida.", e);
        }
    }
}
