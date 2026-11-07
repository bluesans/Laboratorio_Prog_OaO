package com.mazmorra.service.impl;

import com.mazmorra.Reglas;
import com.mazmorra.dao.AventureroDAO;
import com.mazmorra.dao.CriaturaDAO;
import com.mazmorra.dao.PartidaDAO;
import com.mazmorra.dao.PersonajeDAO;
import com.mazmorra.db.Transaccion;
import com.mazmorra.exception.AventureroDerrotadoException;
import com.mazmorra.exception.CriaturaEvadidaException;
import com.mazmorra.exception.CriaturaYaDerrotadaException;
import com.mazmorra.exception.CriaturasPendientesException;
import com.mazmorra.exception.DanioInvalidoException;
import com.mazmorra.exception.HuidaProhibidaException;
import com.mazmorra.exception.JuegoException;
import com.mazmorra.exception.OroInsuficienteException;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.exception.PersonajeDuplicadoException;
import com.mazmorra.exception.PisoFinalException;
import com.mazmorra.exception.VidaCompletaException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.Danable;
import com.mazmorra.model.Partida;
import com.mazmorra.model.Personaje;
import com.mazmorra.model.TipoAtaque;
import com.mazmorra.model.TipoCriatura;
import com.mazmorra.service.InicioPartida;
import com.mazmorra.service.JuegoService;
import com.mazmorra.service.ResultadoPocion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JuegoServiceImpl implements JuegoService {

    private final CriaturaDAO criaturaDAO;
    private final AventureroDAO aventureroDAO;
    private final PersonajeDAO personajeDAO;
    private final PartidaDAO partidaDAO;
    private final Transaccion transaccion;

    public JuegoServiceImpl(CriaturaDAO criaturaDAO, AventureroDAO aventureroDAO,
            PersonajeDAO personajeDAO, PartidaDAO partidaDAO, Transaccion transaccion) {
        this.criaturaDAO = Objects.requireNonNull(
                criaturaDAO, "El DAO de criaturas no puede ser nulo.");
        this.aventureroDAO = Objects.requireNonNull(
                aventureroDAO, "El DAO de aventureros no puede ser nulo.");
        this.personajeDAO = Objects.requireNonNull(
                personajeDAO, "El DAO de personajes no puede ser nulo.");
        this.partidaDAO = Objects.requireNonNull(
                partidaDAO, "El DAO de partidas no puede ser nulo.");
        this.transaccion = Objects.requireNonNull(
                transaccion, "La transacción no puede ser nula.");
    }

    @Override
    public List<Aventurero> listarAventureros() throws PersistenciaException {
        return aventureroDAO.listarTodos();
    }

    @Override
    public int resolverAtaqueEnemigo(Aventurero aventurero, Criatura criatura)
            throws AventureroDerrotadoException, PersistenciaException {
        if (aventurero == null || criatura == null) {
            throw new IllegalArgumentException("El aventurero y la criatura no pueden ser nulos.");
        }
        if (aventurero.estaDerrotado()) {
            throw new AventureroDerrotadoException(
                    "El aventurero está derrotado y no puede recibir otro ataque.");
        }
        if (!criatura.estaViva()) {
            return 0;
        }
        int vidaAntes = aventurero.getVida();
        aventurero.recibirDanio(criatura.getDanioAtaque());
        if (!aventureroDAO.actualizar(aventurero)) {
            throw new PersistenciaException(
                    "Sin filas actualizadas para aventurero id " + aventurero.getId());
        }
        return vidaAntes - aventurero.getVida();
    }

    @Override
    public boolean esVictoria(int idAventurero) throws PersistenciaException {
        Aventurero aventurero = aventureroDAO.buscarPorId(idAventurero)
                .orElseThrow(() -> new PersistenciaException(
                        "No existe un aventurero con id " + idAventurero));
        if (aventurero.getPisoActual() < Reglas.PISO_FINAL) {
            return false;
        }
        boolean hayDragon = false;
        for (Criatura criatura : criaturaDAO.listarPorPiso(Reglas.PISO_FINAL)) {
            if (criatura.getTipo() == TipoCriatura.DRAGON_JEFE) {
                hayDragon = true;
                if (!criatura.estaDerrotado()) {
                    return false;
                }
            }
        }
        return hayDragon;
    }

    @Override
    public List<Criatura> listarCriaturasPorPiso(int piso) throws PersistenciaException {
        if (piso < 1) {
            throw new IllegalArgumentException("El piso debe ser al menos 1.");
        }
        return criaturaDAO.listarPorPiso(piso);
    }

    @Override
    public void resolverTurno(Aventurero aventurero, Criatura criatura, TipoAtaque tipoAtaque)
            throws AventureroDerrotadoException, CriaturaYaDerrotadaException,
            CriaturaEvadidaException, DanioInvalidoException,
            PersistenciaException {
        if (aventurero == null || criatura == null) {
            throw new IllegalArgumentException("El aventurero y la criatura no pueden ser nulos.");
        }
        if (aventurero.estaDerrotado()) {
            throw new AventureroDerrotadoException(
                    "El aventurero está derrotado y no puede atacar: " + aventurero.getNombre());
        }
        // Validación y normalización antes de abrir la transacción.
        TipoAtaque tipo = tipoAtaque == null ? TipoAtaque.FISICO : tipoAtaque;

        try {
            transaccion.ejecutar((criaturas, aventureros, personajes, partidas) -> {
                if (!aplicarAtaque(criatura, aventurero.getAtaqueBase(), tipo, criaturas, aventureros)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para criatura id " + criatura.getId());
                }

                if (criatura.estaViva()) {
                    aplicarTurnoEnemigo(aventurero, criatura, aventureros);
                }

                if (criatura.estaDerrotado()) {
                    // La muerte la produjo el ataque del jugador: suma al contador.
                    aventurero.incrementarEnemigosDerrotados();
                    if (!aventureros.actualizar(aventurero)) {
                        throw new PersistenciaException(
                                "Sin filas actualizadas para aventurero id " + aventurero.getId());
                    }
                    pagarRecompensaEnTransaccion(aventurero, criatura, criaturas, aventureros);
                }
                return null;
            });
        } catch (JuegoException e) {
            relanzarAtaque(e);
        }
    }

    @Override
    public void avanzarPiso(Aventurero aventurero)
            throws CriaturasPendientesException, AventureroDerrotadoException,
            PisoFinalException, PersistenciaException {
        if (aventurero == null) {
            throw new IllegalArgumentException("El aventurero no puede ser nulo.");
        }
        if (aventurero.estaDerrotado()) {
            throw new AventureroDerrotadoException(
                    "El aventurero está derrotado y no puede avanzar de piso: " + aventurero.getNombre());
        }
        if (aventurero.getPisoActual() >= Reglas.PISO_FINAL) {
            throw new PisoFinalException(
                    "El aventurero ya está en el último piso (" + Reglas.PISO_FINAL + ").");
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
        if (!aventureroDAO.actualizar(aventurero)) {
            throw new PersistenciaException(
                    "Sin filas actualizadas para aventurero id " + aventurero.getId());
        }
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

        try {
            transaccion.ejecutar((criaturas, aventureros, personajes, partidas) -> {
                // Huir cuesta hasta el costo de huida, la criatura golpea al escapar y queda atrás.
                aventurero.setOro(Math.max(0, aventurero.getOro() - Reglas.COSTO_HUIDA));
                aventurero.recibirDanio(criatura.getDanioAtaque());
                criatura.marcarEvadida();

                // Las dos filas se actualizan en una sola transacción: o se guardan ambas o ninguna.
                if (!aventureros.actualizar(aventurero)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para aventurero id " + aventurero.getId());
                }
                if (!criaturas.actualizar(criatura)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para criatura id " + criatura.getId());
                }
                return null;
            });
        } catch (JuegoException e) {
            relanzarHuida(e);
        }
    }

    @Override
    public ResultadoPocion usarPocion(Aventurero aventurero, int cantidad, Criatura enemigo)
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
        int costo = (int) Math.ceil((double) Reglas.COSTO_POCION * curado / Reglas.TAMANO_POCION);
        if (aventurero.getOro() < costo) {
            throw new OroInsuficienteException(
                    "La poción cuesta " + costo + " de oro y " + aventurero.getNombre()
                    + " solo tiene " + aventurero.getOro() + ".");
        }

        try {
            return transaccion.ejecutar((criaturas, aventureros, personajes, partidas) -> {
                // Pago, curación y golpe del enemigo se guardan en una sola transacción.
                aventurero.setOro(aventurero.getOro() - costo);
                aventurero.curar(curado);
                if (!aventureros.actualizar(aventurero)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para aventurero id " + aventurero.getId());
                }
                int danioRecibido = 0;
                if (enemigo != null && !enemigo.estaDerrotado()) {
                    int vidaAntesDelGolpe = aventurero.getVida();
                    aplicarTurnoEnemigo(aventurero, enemigo, aventureros);
                    danioRecibido = vidaAntesDelGolpe - aventurero.getVida();
                }
                return new ResultadoPocion(curado, costo, enemigo, danioRecibido);
            });
        } catch (JuegoException e) {
            return relanzarPocion(e);
        }
    }

    @Override
    public Aventurero obtenerEstadoAventurero(int idAventurero) throws PersistenciaException {
        return aventureroDAO.buscarPorId(idAventurero)
                .orElseThrow(() -> new PersistenciaException(
                        "No existe un aventurero con id " + idAventurero));
    }

    @Override
    public Personaje obtenerPersonaje(int id) throws PersistenciaException {
        return personajeDAO.buscarPorId(id)
                .orElseThrow(() -> new PersistenciaException(
                        "No existe un personaje con id " + id));
    }

    @Override
    public List<Personaje> listarRanking(int limite) throws PersistenciaException {
        return personajeDAO.ranking(limite);
    }

    @Override
    public List<Partida> listarPartidas(int personajeId) throws PersistenciaException {
        return partidaDAO.listarPorPersonaje(personajeId);
    }

    @Override
    public InicioPartida iniciarPartidaNueva(String nombre, int ronda, List<Criatura> escenario)
            throws PersistenciaException, PersonajeDuplicadoException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del personaje no puede ser nulo ni vacío.");
        }
        String limpio = nombre.trim();
        if (limpio.length() > 50) {
            throw new IllegalArgumentException("El nombre no puede superar los 50 caracteres.");
        }
        if (ronda < 1) {
            throw new IllegalArgumentException("La ronda debe ser al menos 1.");
        }
        if (escenario == null) {
            throw new IllegalArgumentException("El escenario inicial no puede ser nulo.");
        }

        try {
            return transaccion.ejecutar((criaturas, aventureros, personajes, partidas) -> {
                if (personajes.buscarPorNombre(limpio).isPresent()) {
                    throw new PersonajeDuplicadoException(
                            "Ya existe un personaje con el nombre " + limpio);
                }
                Personaje personaje = new Personaje(0, limpio, 0, 0, 0);
                int idPersonaje = personajes.insertar(personaje);
                personaje.setId(idPersonaje);
                Aventurero aventurero = sembrarPartida(criaturas, aventureros, personaje, ronda,
                        escenario);
                return new InicioPartida(personaje, aventurero);
            });
        } catch (PersonajeDuplicadoException e) {
            throw e;
        } catch (JuegoException e) {
            throw comoPersistencia(e);
        }
    }

    @Override
    public Aventurero iniciarPartida(Personaje personaje, int ronda, List<Criatura> escenario)
            throws PersistenciaException {
        if (personaje == null) {
            throw new IllegalArgumentException("El personaje no puede ser nulo.");
        }
        if (ronda < 1) {
            throw new IllegalArgumentException("La ronda debe ser al menos 1.");
        }
        if (escenario == null) {
            throw new IllegalArgumentException("El escenario inicial no puede ser nulo.");
        }

        try {
            return transaccion.ejecutar((criaturas, aventureros, personajes, partidas) ->
                    sembrarPartida(criaturas, aventureros, personaje, ronda, escenario));
        } catch (JuegoException e) {
            throw comoPersistencia(e);
        }
    }

    private Aventurero sembrarPartida(CriaturaDAO criaturas, AventureroDAO aventureros,
            Personaje personaje, int ronda, List<Criatura> escenario) throws PersistenciaException {
        criaturas.eliminarTodas();
        aventureros.eliminarTodos();
        Aventurero aventurero = new Aventurero(0, personaje.getNombre(), 1,
                Reglas.VIDA_INICIAL, Reglas.VIDA_INICIAL, Reglas.ATAQUE_INICIAL,
                Reglas.ORO_INICIAL, Reglas.PISO_INICIAL, personaje.getId(), ronda, 0);
        int id = aventureros.insertar(aventurero);
        aventurero.setId(id);
        for (Criatura criatura : escenario) {
            criaturas.insertar(criatura);
        }
        return aventurero;
    }

    @Override
    public int registrarResultado(Personaje personaje, Aventurero aventurero, String resultado)
            throws PersistenciaException {
        if (personaje == null || aventurero == null) {
            throw new IllegalArgumentException("El personaje y el aventurero no pueden ser nulos.");
        }
        if (resultado == null || resultado.isBlank()) {
            throw new IllegalArgumentException("El resultado no puede ser nulo ni vacío.");
        }

        try {
            return transaccion.ejecutar((criaturas, aventureros, personajes, partidas) -> {
                int puntos = aventurero.getRonda() * Reglas.PUNTOS_RONDA
                        + aventurero.getPisoActual() * Reglas.PUNTOS_PISO
                        + aventurero.getOro()
                        + aventurero.getEnemigosDerrotados() * Reglas.PUNTOS_ENEMIGO;
                Partida partida = new Partida(0, personaje.getId(), aventurero.getRonda(),
                        aventurero.getPisoActual(), aventurero.getOro(),
                        aventurero.getEnemigosDerrotados(), puntos, resultado, LocalDateTime.now());
                partidas.insertar(partida);
                personaje.registrarResultado(puntos, aventurero.getRonda());
                if (!personajes.actualizar(personaje)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para personaje id " + personaje.getId());
                }
                return puntos;
            });
        } catch (JuegoException e) {
            throw comoPersistencia(e);
        }
    }

    private static PersistenciaException comoPersistencia(JuegoException e) {
        if (e instanceof PersistenciaException ex) {
            return ex;
        }
        return new PersistenciaException(e.getMessage(), e);
    }

    private boolean aplicarAtaque(Danable objetivo, int danio, TipoAtaque tipoAtaque,
            CriaturaDAO criaturas, AventureroDAO aventureros)
            throws DanioInvalidoException, CriaturaYaDerrotadaException,
            CriaturaEvadidaException, AventureroDerrotadoException,
            PersistenciaException {
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
            throw new CriaturaEvadidaException(
                    criaturaEvadida.getNombre() + " quedó atrás al huir y ya no puede pelear.");
        }

        TipoAtaque tipo = tipoAtaque == null ? TipoAtaque.FISICO : tipoAtaque;

        objetivo.recibirDanio(danio, tipo);
        if (objetivo instanceof Criatura criatura) {
            return criaturas.actualizar(criatura);
        }
        if (objetivo instanceof Aventurero aventurero) {
            return aventureros.actualizar(aventurero);
        }
        return true;
    }

    private void aplicarTurnoEnemigo(Aventurero aventurero, Criatura criatura, AventureroDAO aventureros)
            throws AventureroDerrotadoException, PersistenciaException {
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
        if (!aventureros.actualizar(aventurero)) {
            throw new PersistenciaException(
                    "Sin filas actualizadas para aventurero id " + aventurero.getId());
        }
    }

    private int pagarRecompensaEnTransaccion(Aventurero aventurero, Criatura criatura,
            CriaturaDAO criaturas, AventureroDAO aventureros)
            throws PersistenciaException {
        // Si otra ejecución ya pagó, no se vuelve a cobrar.
        if (criatura.isRecompensaPagada()) {
            return 0;
        }
        int recompensa = criatura.getNivelAmenaza() * Reglas.ORO_POR_NIVEL_AMENAZA;
        criatura.marcarRecompensaPagada();
        if (!criaturas.actualizar(criatura)) {
            throw new PersistenciaException(
                    "Sin filas actualizadas para criatura id " + criatura.getId());
        }
        aventurero.setOro(aventurero.getOro() + recompensa);
        if (!aventureros.actualizar(aventurero)) {
            throw new PersistenciaException(
                    "Sin filas actualizadas para aventurero id " + aventurero.getId());
        }
        return recompensa;
    }

    private static void relanzarAtaque(JuegoException e)
            throws DanioInvalidoException, CriaturaYaDerrotadaException,
            CriaturaEvadidaException, AventureroDerrotadoException,
            PersistenciaException {
        if (e instanceof DanioInvalidoException ex) {
            throw ex;
        }
        if (e instanceof CriaturaYaDerrotadaException ex) {
            throw ex;
        }
        if (e instanceof CriaturaEvadidaException ex) {
            throw ex;
        }
        if (e instanceof AventureroDerrotadoException ex) {
            throw ex;
        }
        if (e instanceof PersistenciaException ex) {
            throw ex;
        }
        throw new PersistenciaException(e.getMessage(), e);
    }

    private static ResultadoPocion relanzarPocion(JuegoException e)
            throws VidaCompletaException, AventureroDerrotadoException,
            OroInsuficienteException, PersistenciaException {
        if (e instanceof VidaCompletaException ex) {
            throw ex;
        }
        if (e instanceof AventureroDerrotadoException ex) {
            throw ex;
        }
        if (e instanceof OroInsuficienteException ex) {
            throw ex;
        }
        if (e instanceof PersistenciaException ex) {
            throw ex;
        }
        throw new PersistenciaException(e.getMessage(), e);
    }

    private static void relanzarHuida(JuegoException e)
            throws HuidaProhibidaException, AventureroDerrotadoException,
            PersistenciaException {
        if (e instanceof HuidaProhibidaException ex) {
            throw ex;
        }
        if (e instanceof AventureroDerrotadoException ex) {
            throw ex;
        }
        if (e instanceof PersistenciaException ex) {
            throw ex;
        }
        throw new PersistenciaException(e.getMessage(), e);
    }
}
