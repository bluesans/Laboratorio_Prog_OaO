package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.exception.AventureroDerrotadoException;
import com.mazmorra.exception.CriaturaEvadidaException;
import com.mazmorra.exception.CriaturaYaDerrotadaException;
import com.mazmorra.exception.CriaturasPendientesException;
import com.mazmorra.exception.DanioInvalidoException;
import com.mazmorra.exception.HuidaProhibidaException;
import com.mazmorra.exception.OroInsuficienteException;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.exception.VidaCompletaException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.Danable;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.model.Orco;
import com.mazmorra.model.TipoAtaque;
import com.mazmorra.service.impl.JuegoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del servicio de juego usando DAO en memoria y una transacción directa.
 */
class JuegoServicioTest {

    private CriaturaDAOEnMemoria criaturaDAO;
    private AventureroDAOEnMemoria aventureroDAO;
    private JuegoServiceImpl servicio;

    @BeforeEach
    void preparar() throws Exception {
        criaturaDAO = new CriaturaDAOEnMemoria();
        aventureroDAO = new AventureroDAOEnMemoria();
        servicio = new JuegoServiceImpl(
                criaturaDAO, aventureroDAO,
                new TransaccionDirecta(criaturaDAO, aventureroDAO));
    }

    @Test
    void pocionFallaConLaVidaCompleta() throws Exception {
        Aventurero aventurero = crearAventurero(100, 50);

        assertThrows(VidaCompletaException.class,
                () -> servicio.usarPocion(aventurero, Reglas.TAMANO_POCION),
                "Con la vida al máximo la poción debe rechazarse.");
    }

    @Test
    void pocionFallaConOroInsuficiente() throws Exception {
        Aventurero aventurero = crearAventurero(88, 7);

        assertThrows(OroInsuficienteException.class,
                () -> servicio.usarPocion(aventurero, Reglas.TAMANO_POCION),
                "Con 7 de oro no se puede pagar el costo proporcional de 8.");
    }

    @Test
    void pocionCuraLoJustoYCobraProporcional() throws Exception {
        Aventurero aventurero = crearAventurero(88, 50);
        Esqueleto esqueleto = new Esqueleto(
                0, "Esqueleto enemigo", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        criaturaDAO.insertar(esqueleto);

        ResultadoPocion resultado = servicio.usarPocion(aventurero, Reglas.TAMANO_POCION);

        assertEquals(12, resultado.vidaCurada(),
                "Con 12 de vida faltante la poción solo debe curar eso.");
        assertEquals(8, resultado.oroPagado(),
                "Curar 12 debe costar 8 de oro.");
        assertEquals(esqueleto, resultado.enemigo(),
                "El esqueleto vivo del piso debe responder a la poción.");
        assertEquals(15, resultado.danioRecibido(),
                "La respuesta del esqueleto debe ser su daño de ataque.");
        assertEquals(42, aventurero.getOro(),
                "Curar 12 debe costar 8 de oro: de 50 debe quedar 42.");
        assertEquals(85, aventurero.getVida(),
                "La curación lleva la vida a 100 y el enemigo del piso devuelve 12 de daño.");
        assertEquals(42, aventureroDAO.buscarPorId(aventurero.getId()).orElseThrow().getOro(),
                "El oro actualizado debe quedar persistido en el DAO.");
    }

    @Test
    void huidaDelDragonFalla() throws Exception {
        Aventurero aventurero = crearAventurero();
        DragonJefe dragon = new DragonJefe(
                0, "Dragón", 100, 100, 3, Reglas.DANIO_DRAGON, 3, false, 1, 3, false);
        criaturaDAO.insertar(dragon);

        assertThrows(HuidaProhibidaException.class,
                () -> servicio.huirDeCombate(aventurero, dragon),
                "No se debe poder huir del jefe final.");
    }

    @Test
    void huirCobraOroRecibeGolpeYDejaAtras() throws Exception {
        Aventurero aventurero = crearAventurero(100, 50);
        Esqueleto esqueleto = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        criaturaDAO.insertar(esqueleto);

        servicio.huirDeCombate(aventurero, esqueleto);

        assertEquals(40, aventurero.getOro(),
                "Huir debe descontar 10 de oro.");
        assertEquals(85, aventurero.getVida(),
                "El aventurero debe perder el daño de ataque del esqueleto al huir.");
        assertTrue(esqueleto.isEvadida(),
                "La criatura de la que se huye debe quedar marcada como evadida.");

        Criatura persistida = criaturaDAO.buscarPorId(esqueleto.getId()).orElseThrow();
        assertTrue(persistida.isEvadida(),
                "La criatura evadida debe quedar persistida con ese estado.");
    }

    @Test
    void avanzarPisoFallaConCriaturasVivas() throws Exception {
        Aventurero aventurero = crearAventurero();
        Esqueleto esqueleto = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        criaturaDAO.insertar(esqueleto);

        assertThrows(CriaturasPendientesException.class,
                () -> servicio.avanzarPiso(aventurero),
                "No se debe avanzar de piso mientras queden criaturas vivas.");
        assertEquals(1, aventurero.getPisoActual(),
                "El piso del aventurero no debe cambiar tras el rechazo.");
    }

    @Test
    void resolverTurnoFallaContraCriaturaDerrotada() throws Exception {
        Aventurero aventurero = crearAventurero();
        Esqueleto esqueleto = new Esqueleto(
                0, "Esqueleto derrotado", 0, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        criaturaDAO.insertar(esqueleto);

        assertThrows(CriaturaYaDerrotadaException.class,
                () -> servicio.resolverTurno(aventurero, esqueleto, TipoAtaque.FISICO),
                "No se puede resolver un turno contra una criatura ya derrotada.");
    }

    @Test
    void esqueletoCaeADosGolpesYPagaUnaVez() throws Exception {
        Aventurero aventurero = crearAventurero(100, 50);
        // Dos golpes físicos de 20 dejan 12 de daño efectivo cada uno: se usa una vida de 24
        // para que el esqueleto muera exactamente en el segundo turno.
        Esqueleto esqueleto = new Esqueleto(
                0, "Esqueleto", 24, 24, 2, Reglas.DANIO_ESQUELETO, 1, false, 40);
        criaturaDAO.insertar(esqueleto);

        servicio.resolverTurno(aventurero, esqueleto, TipoAtaque.FISICO);

        assertEquals(12, esqueleto.getVida(),
                "El primer golpe de 20 debe dejar 12 de daño efectivo.");
        assertEquals(85, aventurero.getVida(),
                "El esqueleto vivo debe devolver su ataque en el primer turno.");
        assertEquals(50, aventurero.getOro(),
                "Mientras la criatura siga viva no se debe pagar recompensa.");

        servicio.resolverTurno(aventurero, esqueleto, TipoAtaque.FISICO);

        assertEquals(0, esqueleto.getVida(),
                "El segundo golpe de 20 debe dejar 12 de daño efectivo y matar al esqueleto.");
        assertTrue(esqueleto.isDerrotada(),
                "El esqueleto debe quedar marcado como derrotado.");
        assertEquals(50 + 2 * Reglas.ORO_POR_NIVEL_AMENAZA, aventurero.getOro(),
                "La recompensa debe ser nivel de amenaza por oro por nivel.");
        assertTrue(esqueleto.isRecompensaPagada(),
                "La recompensa debe quedar marcada como pagada.");

        assertThrows(CriaturaYaDerrotadaException.class,
                () -> servicio.resolverTurno(aventurero, esqueleto, TipoAtaque.FISICO),
                "Un turno extra contra la criatura derrotada debe rechazarse.");
        assertEquals(50 + 2 * Reglas.ORO_POR_NIVEL_AMENAZA, aventurero.getOro(),
                "La recompensa no debe pagarse dos veces.");
    }

    @Test
    void resolverAtaqueRechazaDanioCero() throws Exception {
        Esqueleto esqueleto = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        criaturaDAO.insertar(esqueleto);

        assertThrows(DanioInvalidoException.class,
                () -> servicio.resolverAtaque(esqueleto, 0, TipoAtaque.FISICO),
                "Un ataque con daño 0 debe rechazarse.");
    }

    @Test
    void resolverAtaqueRechazaCriaturaEvadida() throws Exception {
        Esqueleto esqueleto = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        esqueleto.marcarEvadida();
        criaturaDAO.insertar(esqueleto);

        assertThrows(CriaturaEvadidaException.class,
                () -> servicio.resolverAtaque(esqueleto, 20, TipoAtaque.FISICO),
                "No se puede atacar a una criatura que quedó atrás al huir.");
    }

    @Test
    void resolverAtaqueUsaLaInterfazDanable() throws Exception {
        Aventurero aventurero = crearAventurero(100, 50);
        Esqueleto esqueleto = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 0);
        criaturaDAO.insertar(esqueleto);

        Danable objetivoCriatura = esqueleto;
        Danable objetivoAventurero = aventurero;

        servicio.resolverAtaque(objetivoCriatura, 20, TipoAtaque.FISICO);
        servicio.resolverAtaque(objetivoAventurero, 20, TipoAtaque.MAGICO);

        assertEquals(30, esqueleto.getVida(),
                "La criatura debe recibir el daño a través de la interfaz Danable.");
        assertEquals(80, aventurero.getVida(),
                "El aventurero debe recibir el daño a través de la interfaz Danable.");
    }

    @Test
    void iniciarNuevaPartidaDejaElEscenarioCompleto() throws Exception {
        // Se dejan datos previos para comprobar que la partida nueva los reemplaza.
        crearAventurero();
        criaturaDAO.insertar(new Esqueleto(
                0, "Criatura vieja", 10, 10, 1, 1, 1, false, 10));

        Esqueleto esqueleto = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        Orco orco = new Orco(
                0, "Orco", 60, 60, 2, Reglas.DANIO_ORCO, 2, false, 50);

        int idAventurero = servicio.iniciarNuevaPartida(
                "Aventurero nuevo", 100, 20, List.of(esqueleto, orco));

        List<Aventurero> aventureros = aventureroDAO.listarTodos();
        List<Criatura> criaturas = criaturaDAO.listarTodas();

        assertEquals(1, aventureros.size(),
                "Debe quedar exactamente un aventurero.");
        assertEquals(2, criaturas.size(),
                "Debe quedar exactamente el escenario indicado.");
        assertEquals(idAventurero, aventureros.get(0).getId(),
                "El id devuelto debe corresponder al aventurero insertado.");
        assertEquals(Reglas.VIDA_INICIAL, aventureros.get(0).getVida(),
                "El aventurero nuevo debe empezar con la vida inicial.");
        assertEquals(Reglas.ORO_INICIAL, aventureros.get(0).getOro(),
                "El aventurero nuevo debe empezar con el oro inicial.");
        assertEquals(Reglas.PISO_INICIAL, aventureros.get(0).getPisoActual(),
                "El aventurero nuevo debe empezar en el piso inicial.");
        assertTrue(esqueleto.getId() > 0, "La criatura del escenario debe recibir un id.");
        assertTrue(orco.getId() > 0, "La criatura del escenario debe recibir un id.");
    }

    @Test
    void laTransaccionPropagaLaExcepcion() throws Exception {
        PersistenciaException esperada = new PersistenciaException("Falla simulada");
        TransaccionDirecta transaccion = new TransaccionDirecta(criaturaDAO, aventureroDAO);

        PersistenciaException lanzada = assertThrows(PersistenciaException.class,
                () -> transaccion.ejecutar((criaturas, aventureros) -> {
                    throw esperada;
                }),
                "La transacción debe propagar la excepción lanzada dentro del trabajo.");

        assertEquals(esperada, lanzada,
                "Debe propagarse la misma instancia de la excepción.");
    }

    @Test
    void laPocionSoloRecibeElGolpeDelEnemigoIndicado() throws Exception {
        Aventurero aventurero = crearAventurero(50, 50);
        Esqueleto esqueleto = new Esqueleto(1, "Esqueleto", 25, 25, 2,
                Reglas.DANIO_ESQUELETO, 1, false, Reglas.RESISTENCIA_ESQUELETO);

        ResultadoPocion resultado = servicio.usarPocion(aventurero, Reglas.TAMANO_POCION, esqueleto);

        assertEquals(65, aventurero.getVida(),
                "La poción cura 30 y el esqueleto devuelve 15.");
        assertEquals(Reglas.DANIO_ESQUELETO, resultado.danioRecibido(),
                "Solo el enemigo indicado debe golpear.");
        assertEquals(esqueleto, resultado.enemigo(),
                "El resultado debe informar ese enemigo.");
    }

    @Test
    void laPocionSinEnemigoAlLadoNoRecibeGolpe() throws Exception {
        Aventurero aventurero = crearAventurero(50, 50);

        ResultadoPocion resultado = servicio.usarPocion(aventurero, Reglas.TAMANO_POCION, null);

        assertEquals(80, aventurero.getVida(), "La poción debe curar 30.");
        assertEquals(0, resultado.danioRecibido(),
                "Sin enemigo al lado no debe recibir daño.");
        assertNull(resultado.enemigo(), "No debe haber enemigo en el resultado.");
    }

    @Test
    void unAventureroDerrotadoNoPuedeAtacar() throws Exception {
        Aventurero aventurero = crearAventurero(0, 50);
        Esqueleto esqueleto = new Esqueleto(1, "Esqueleto", 25, 25, 2,
                Reglas.DANIO_ESQUELETO, 1, false, 40);

        assertThrows(AventureroDerrotadoException.class,
                () -> servicio.resolverTurno(aventurero, esqueleto, TipoAtaque.FISICO));
    }

    @Test
    void unAventureroDerrotadoNoPuedeUsarPocion() throws Exception {
        Aventurero aventurero = crearAventurero(0, 50);

        assertThrows(AventureroDerrotadoException.class,
                () -> servicio.usarPocion(aventurero, Reglas.TAMANO_POCION));
    }

    @Test
    void unAventureroDerrotadoNoPuedeHuir() throws Exception {
        Aventurero aventurero = crearAventurero(0, 50);
        Esqueleto esqueleto = new Esqueleto(1, "Esqueleto", 25, 25, 2,
                Reglas.DANIO_ESQUELETO, 1, false, 40);

        assertThrows(AventureroDerrotadoException.class,
                () -> servicio.huirDeCombate(aventurero, esqueleto));
    }

    @Test
    void unAventureroDerrotadoNoPuedeAvanzar() throws Exception {
        Aventurero aventurero = crearAventurero(0, 50);

        assertThrows(AventureroDerrotadoException.class,
                () -> servicio.avanzarPiso(aventurero));
    }

    @Test
    void unAventureroDerrotadoNoPuedeRecibirAtaques() throws Exception {
        Aventurero aventurero = crearAventurero(0, 50);
        Esqueleto esqueleto = new Esqueleto(1, "Esqueleto", 25, 25, 2,
                Reglas.DANIO_ESQUELETO, 1, false, 40);

        assertThrows(AventureroDerrotadoException.class,
                () -> servicio.resolverAtaqueEnemigo(aventurero, esqueleto));
    }

    @Test
    void elAtaqueEnemigoAplicaElDanioYLoPersiste() throws Exception {
        Aventurero aventurero = crearAventurero(100, 50);
        Esqueleto esqueleto = new Esqueleto(1, "Esqueleto", 25, 25, 2,
                Reglas.DANIO_ESQUELETO, 1, false, 40);

        int danio = servicio.resolverAtaqueEnemigo(aventurero, esqueleto);

        assertEquals(Reglas.DANIO_ESQUELETO, danio);
        assertEquals(100 - Reglas.DANIO_ESQUELETO, aventurero.getVida());
        assertEquals(aventurero.getVida(),
                servicio.obtenerEstadoAventurero(aventurero.getId()).getVida());
    }

    @Test
    void elAtaqueDeUnaCriaturaDerrotadaNoHaceDanio() throws Exception {
        Aventurero aventurero = crearAventurero(100, 50);
        Esqueleto esqueleto = new Esqueleto(1, "Esqueleto", 0, 25, 2,
                Reglas.DANIO_ESQUELETO, 1, true, 40);

        assertEquals(0, servicio.resolverAtaqueEnemigo(aventurero, esqueleto));
        assertEquals(100, aventurero.getVida());
    }

    @Test
    void noHayVictoriaAntesDelUltimoPiso() throws Exception {
        Aventurero aventurero = crearAventurero(100, 50);

        assertFalse(servicio.esVictoria(aventurero.getId()));
    }

    @Test
    void noHayVictoriaSiElDragonSigueVivo() throws Exception {
        Aventurero aventurero = crearAventurero(100, 50);
        aventurero.setPisoActual(Reglas.PISO_FINAL);
        aventureroDAO.actualizar(aventurero);
        criaturaDAO.insertar(new DragonJefe(0, "Jefe Dragón", 60, 60, 10,
                Reglas.DANIO_DRAGON, Reglas.PISO_FINAL, false, 1, Reglas.FASES_DRAGON, false));

        assertFalse(servicio.esVictoria(aventurero.getId()));
    }

    @Test
    void hayVictoriaCuandoElDragonDelUltimoPisoEstaDerrotado() throws Exception {
        Aventurero aventurero = crearAventurero(100, 50);
        aventurero.setPisoActual(Reglas.PISO_FINAL);
        aventureroDAO.actualizar(aventurero);
        DragonJefe dragon = new DragonJefe(0, "Jefe Dragón", 60, 60, 10,
                Reglas.DANIO_DRAGON, Reglas.PISO_FINAL, false, 1, Reglas.FASES_DRAGON, false);
        criaturaDAO.insertar(dragon);
        dragon.recibirDanio(999);

        assertTrue(servicio.esVictoria(aventurero.getId()));
    }

    @Test
    void noHayVictoriaSiQuedaUnDragonVivo() throws Exception {
        Aventurero aventurero = crearAventurero(100, 50);
        aventurero.setPisoActual(Reglas.PISO_FINAL);
        aventureroDAO.actualizar(aventurero);
        criaturaDAO.insertar(new DragonJefe(0, "Jefe Muerto", 0, 60, 10,
                Reglas.DANIO_DRAGON, Reglas.PISO_FINAL, true, 3, 3, true));
        criaturaDAO.insertar(new DragonJefe(0, "Jefe Vivo", 60, 60, 10,
                Reglas.DANIO_DRAGON, Reglas.PISO_FINAL, false, 1, 3, false));

        assertFalse(servicio.esVictoria(aventurero.getId()));
    }

    private Aventurero crearAventurero() throws Exception {
        return crearAventurero(Reglas.VIDA_INICIAL, Reglas.ORO_INICIAL);
    }

    private Aventurero crearAventurero(int vida, int oro) throws Exception {
        Aventurero aventurero = new Aventurero(
                0, "Aventurero", 1, vida, Reglas.VIDA_INICIAL,
                Reglas.ATAQUE_INICIAL, oro, Reglas.PISO_INICIAL);
        aventureroDAO.insertar(aventurero);
        return aventurero;
    }
}
