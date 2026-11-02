package com.mazmorra.service.impl;

import com.mazmorra.Reglas;
import com.mazmorra.dao.AventureroDAO;
import com.mazmorra.dao.CriaturaDAO;
import com.mazmorra.dao.impl.AventureroDAOMySQL;
import com.mazmorra.dao.impl.CriaturaDAOMySQL;
import com.mazmorra.db.Transaccion;
import com.mazmorra.db.impl.TransaccionMySQL;
import com.mazmorra.exception.AventureroDerrotadoException;
import com.mazmorra.exception.CriaturaEvadidaException;
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
import com.mazmorra.model.TipoAtaque;
import com.mazmorra.service.JuegoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JuegoServiceImpl implements JuegoService {

    private final CriaturaDAO criaturaDAO;
    private final AventureroDAO aventureroDAO;
    private final Transaccion transaccion;

    public JuegoServiceImpl() {
        this(new CriaturaDAOMySQL(), new AventureroDAOMySQL(), new TransaccionMySQL());
    }

    public JuegoServiceImpl(CriaturaDAO criaturaDAO, AventureroDAO aventureroDAO, Transaccion transaccion) {
        this.criaturaDAO = Objects.requireNonNull(
                criaturaDAO, "El DAO de criaturas no puede ser nulo.");
        this.aventureroDAO = Objects.requireNonNull(
                aventureroDAO, "El DAO de aventureros no puede ser nulo.");
        this.transaccion = Objects.requireNonNull(
                transaccion, "La transacción no puede ser nula.");
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

        // Arranca en el piso 1 con el oro inicial.
        Aventurero aventurero = new Aventurero(
                0, nombre, 1, vidaMaxima, vidaMaxima, ataqueBase, Reglas.ORO_INICIAL, Reglas.PISO_INICIAL);
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
    public void resolverAtaque(Danable objetivo, int danio, TipoAtaque tipoAtaque)
            throws DanioInvalidoException, CriaturaYaDerrotadaException,
            CriaturaEvadidaException, AventureroDerrotadoException,
            PersistenciaException {
        try {
            transaccion.ejecutar((criaturas, aventureros) -> {
                aplicarAtaque(objetivo, danio, tipoAtaque, criaturas, aventureros);
                return null;
            });
        } catch (JuegoException e) {
            relanzarAtaque(e);
        }
    }

    @Override
    public void resolverTurno(Aventurero aventurero, Criatura criatura, TipoAtaque tipoAtaque)
            throws AventureroDerrotadoException, CriaturaYaDerrotadaException,
            CriaturaEvadidaException, DanioInvalidoException,
            PersistenciaException {
        if (aventurero == null || criatura == null) {
            throw new IllegalArgumentException("El aventurero y la criatura no pueden ser nulos.");
        }
        // Validación y normalización antes de abrir la transacción.
        TipoAtaque tipo = tipoAtaque == null ? TipoAtaque.FISICO : tipoAtaque;

        try {
            transaccion.ejecutar((criaturas, aventureros) -> {
                if (!aplicarAtaque(criatura, aventurero.getAtaqueBase(), tipo, criaturas, aventureros)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para criatura id " + criatura.getId());
                }

                if (criatura.estaViva()) {
                    aplicarTurnoEnemigo(aventurero, criatura, aventureros);
                }

                if (criatura.estaDerrotado()) {
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

        try {
            transaccion.ejecutar((criaturas, aventureros) -> {
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
        int costo = (int) Math.ceil((double) Reglas.COSTO_POCION * curado / Reglas.TAMANO_POCION);
        if (aventurero.getOro() < costo) {
            throw new OroInsuficienteException(
                    "La poción cuesta " + costo + " de oro y " + aventurero.getNombre()
                    + " solo tiene " + aventurero.getOro() + ".");
        }

        try {
            transaccion.ejecutar((criaturas, aventureros) -> {
                // Pago, curación y golpe del enemigo se guardan en una sola transacción.
                aventurero.setOro(aventurero.getOro() - costo);
                aventurero.curar(curado);
                if (!aventureros.actualizar(aventurero)) {
                    throw new PersistenciaException(
                            "Sin filas actualizadas para aventurero id " + aventurero.getId());
                }
                if (enemigo != null && enemigo.estaViva()) {
                    aplicarTurnoEnemigo(aventurero, enemigo, aventureros);
                }
                return null;
            });
        } catch (JuegoException e) {
            relanzarPocion(e);
        }
    }

    @Override
    public Aventurero obtenerEstadoAventurero(int idAventurero) throws PersistenciaException {
        return aventureroDAO.buscarPorId(idAventurero)
                .orElseThrow(() -> new PersistenciaException(
                        "No existe un aventurero con id " + idAventurero));
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

        return transaccion.ejecutar((criaturas, aventureros) -> {
            criaturas.eliminarTodas();
            aventureros.eliminarTodos();
            Aventurero aventurero = new Aventurero(
                    0, nombre, 1, vidaMaxima, vidaMaxima, ataqueBase,
                    Reglas.ORO_INICIAL, Reglas.PISO_INICIAL);
            int id = aventureros.insertar(aventurero);
            for (Criatura criatura : escenario) {
                criaturas.insertar(criatura);
            }
            return id;
        });
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

        if (objetivo instanceof Criatura criatura) {
            aplicarAtaqueCriatura(criatura, danio, tipo);
            return criaturas.actualizar(criatura);
        }

        // Se resuelve por la interfaz, no por la clase concreta.
        objetivo.recibirDanio(danio);
        if (objetivo instanceof Aventurero aventurero) {
            return aventureros.actualizar(aventurero);
        }
        return true;
    }

    private void aplicarAtaqueCriatura(Criatura criatura, int danio, TipoAtaque tipoAtaque)
            throws DanioInvalidoException, CriaturaYaDerrotadaException,
            CriaturaEvadidaException {
        if (danio <= 0) {
            throw new DanioInvalidoException("El daño debe ser mayor que cero: " + danio);
        }
        if (criatura.estaDerrotado()) {
            throw new CriaturaYaDerrotadaException(
                    "La criatura ya estaba derrotada: " + criatura.getNombre());
        }
        if (criatura.isEvadida()) {
            throw new CriaturaEvadidaException(
                    criatura.getNombre() + " quedó atrás al huir y ya no puede pelear.");
        }
        criatura.recibirDanio(danio, tipoAtaque == null ? TipoAtaque.FISICO : tipoAtaque);
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

    private static void relanzarPocion(JuegoException e)
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
