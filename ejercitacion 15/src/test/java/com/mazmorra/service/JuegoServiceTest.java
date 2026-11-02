package com.mazmorra.service;

import com.mazmorra.Reglas;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del servicio de juego usando DAO en memoria y una transacción directa.
 */
class JuegoServiceTest {

    private InMemoryCriaturaDAO creatureDAO;
    private InMemoryAventureroDAO adventurerDAO;
    private JuegoServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        creatureDAO = new InMemoryCriaturaDAO();
        adventurerDAO = new InMemoryAventureroDAO();
        service = new JuegoServiceImpl(
                creatureDAO, adventurerDAO,
                new TransaccionDirecta(creatureDAO, adventurerDAO));
    }

    @Test
    void usePotionFailsWhenLifeIsFull() throws Exception {
        Aventurero adventurer = createAdventurer(100, 50);

        assertThrows(VidaCompletaException.class,
                () -> service.usarPocion(adventurer, Reglas.TAMANO_POCION),
                "Con la vida al máximo la poción debe rechazarse.");
    }

    @Test
    void usePotionFailsWhenGoldIsBelowProportionalCost() throws Exception {
        Aventurero adventurer = createAdventurer(88, 7);

        assertThrows(OroInsuficienteException.class,
                () -> service.usarPocion(adventurer, Reglas.TAMANO_POCION),
                "Con 7 de oro no se puede pagar el costo proporcional de 8.");
    }

    @Test
    void usePotionHealsExactMissingLifeAndChargesProportionalCost() throws Exception {
        Aventurero adventurer = createAdventurer(88, 50);
        Esqueleto skeleton = new Esqueleto(
                0, "Esqueleto enemigo", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        creatureDAO.insertar(skeleton);

        service.usarPocion(adventurer, Reglas.TAMANO_POCION);

        assertEquals(42, adventurer.getOro(),
                "Curar 12 debe costar 8 de oro: de 50 debe quedar 42.");
        assertEquals(88, adventurer.getVida(),
                "La curación lleva la vida a 100 y el enemigo del piso devuelve 12 de daño.");
        assertEquals(42, adventurerDAO.buscarPorId(adventurer.getId()).orElseThrow().getOro(),
                "El oro actualizado debe quedar persistido en el DAO.");
    }

    @Test
    void fleeFromCombatFailsAgainstTheDragon() throws Exception {
        Aventurero adventurer = createAdventurer();
        DragonJefe dragon = new DragonJefe(
                0, "Dragón", 100, 100, 3, Reglas.DANIO_DRAGON, 3, false, 1, 3, false);
        creatureDAO.insertar(dragon);

        assertThrows(HuidaProhibidaException.class,
                () -> service.huirDeCombate(adventurer, dragon),
                "No se debe poder huir del jefe final.");
    }

    @Test
    void fleeFromCombatChargesGoldTakesDamageAndEvictsCreature() throws Exception {
        Aventurero adventurer = createAdventurer(100, 50);
        Esqueleto skeleton = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        creatureDAO.insertar(skeleton);

        service.huirDeCombate(adventurer, skeleton);

        assertEquals(40, adventurer.getOro(),
                "Huir debe descontar 10 de oro.");
        assertEquals(88, adventurer.getVida(),
                "El aventurero debe perder el daño de ataque del esqueleto al huir.");
        assertTrue(skeleton.isEvadida(),
                "La criatura de la que se huye debe quedar marcada como evadida.");

        Criatura persisted = creatureDAO.buscarPorId(skeleton.getId()).orElseThrow();
        assertTrue(persisted.isEvadida(),
                "La criatura evadida debe quedar persistida con ese estado.");
    }

    @Test
    void advanceFloorFailsWhenLivingCreaturesRemain() throws Exception {
        Aventurero adventurer = createAdventurer();
        Esqueleto skeleton = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        creatureDAO.insertar(skeleton);

        assertThrows(CriaturasPendientesException.class,
                () -> service.avanzarPiso(adventurer),
                "No se debe avanzar de piso mientras queden criaturas vivas.");
        assertEquals(1, adventurer.getPisoActual(),
                "El piso del aventurero no debe cambiar tras el rechazo.");
    }

    @Test
    void resolveTurnFailsAgainstAlreadyDefeatedCreature() throws Exception {
        Aventurero adventurer = createAdventurer();
        Esqueleto skeleton = new Esqueleto(
                0, "Esqueleto derrotado", 0, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        creatureDAO.insertar(skeleton);

        assertThrows(CriaturaYaDerrotadaException.class,
                () -> service.resolverTurno(adventurer, skeleton, TipoAtaque.FISICO),
                "No se puede resolver un turno contra una criatura ya derrotada.");
    }

    @Test
    void resolveTurnKillsSkeletonWithTwoHitsAndPaysRewardOnce() throws Exception {
        Aventurero adventurer = createAdventurer(100, 50);
        // Dos golpes físicos de 20 dejan 12 de daño efectivo cada uno: se usa una vida de 24
        // para que el esqueleto muera exactamente en el segundo turno.
        Esqueleto skeleton = new Esqueleto(
                0, "Esqueleto", 24, 24, 2, Reglas.DANIO_ESQUELETO, 1, false, 40);
        creatureDAO.insertar(skeleton);

        service.resolverTurno(adventurer, skeleton, TipoAtaque.FISICO);

        assertEquals(12, skeleton.getVida(),
                "El primer golpe de 20 debe dejar 12 de daño efectivo.");
        assertEquals(88, adventurer.getVida(),
                "El esqueleto vivo debe devolver su ataque en el primer turno.");
        assertEquals(50, adventurer.getOro(),
                "Mientras la criatura siga viva no se debe pagar recompensa.");

        service.resolverTurno(adventurer, skeleton, TipoAtaque.FISICO);

        assertEquals(0, skeleton.getVida(),
                "El segundo golpe de 20 debe dejar 12 de daño efectivo y matar al esqueleto.");
        assertTrue(skeleton.isDerrotada(),
                "El esqueleto debe quedar marcado como derrotado.");
        assertEquals(50 + 2 * Reglas.ORO_POR_NIVEL_AMENAZA, adventurer.getOro(),
                "La recompensa debe ser nivel de amenaza por oro por nivel.");
        assertTrue(skeleton.isRecompensaPagada(),
                "La recompensa debe quedar marcada como pagada.");

        assertThrows(CriaturaYaDerrotadaException.class,
                () -> service.resolverTurno(adventurer, skeleton, TipoAtaque.FISICO),
                "Un turno extra contra la criatura derrotada debe rechazarse.");
        assertEquals(50 + 2 * Reglas.ORO_POR_NIVEL_AMENAZA, adventurer.getOro(),
                "La recompensa no debe pagarse dos veces.");
    }

    @Test
    void resolveAttackRejectsZeroDamage() throws Exception {
        Esqueleto skeleton = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        creatureDAO.insertar(skeleton);

        assertThrows(DanioInvalidoException.class,
                () -> service.resolverAtaque(skeleton, 0, TipoAtaque.FISICO),
                "Un ataque con daño 0 debe rechazarse.");
    }

    @Test
    void resolveAttackRejectsEvictedCreature() throws Exception {
        Esqueleto skeleton = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        skeleton.marcarEvadida();
        creatureDAO.insertar(skeleton);

        assertThrows(CriaturaEvadidaException.class,
                () -> service.resolverAtaque(skeleton, 20, TipoAtaque.FISICO),
                "No se puede atacar a una criatura que quedó atrás al huir.");
    }

    @Test
    void resolveAttackReducesLifeThroughDanableInterface() throws Exception {
        Aventurero adventurer = createAdventurer(100, 50);
        Esqueleto skeleton = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 0);
        creatureDAO.insertar(skeleton);

        Danable creatureTarget = skeleton;
        Danable adventurerTarget = adventurer;

        service.resolverAtaque(creatureTarget, 20, TipoAtaque.FISICO);
        service.resolverAtaque(adventurerTarget, 20, TipoAtaque.MAGICO);

        assertEquals(30, skeleton.getVida(),
                "La criatura debe recibir el daño a través de la interfaz Danable.");
        assertEquals(80, adventurer.getVida(),
                "El aventurero debe recibir el daño a través de la interfaz Danable.");
    }

    @Test
    void startNewGameClearsStateAndInsertsAdventurerAndScenario() throws Exception {
        // Se dejan datos previos para comprobar que la partida nueva los reemplaza.
        createAdventurer();
        creatureDAO.insertar(new Esqueleto(
                0, "Criatura vieja", 10, 10, 1, 1, 1, false, 10));

        Esqueleto skeleton = new Esqueleto(
                0, "Esqueleto", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);
        Orco orc = new Orco(
                0, "Orco", 60, 60, 2, Reglas.DANIO_ORCO, 2, false, 50);

        int adventurerId = service.iniciarNuevaPartida(
                "Aventurero nuevo", 100, 20, List.of(skeleton, orc));

        List<Aventurero> adventurers = adventurerDAO.listarTodos();
        List<Criatura> creatures = creatureDAO.listarTodas();

        assertEquals(1, adventurers.size(),
                "Debe quedar exactamente un aventurero.");
        assertEquals(2, creatures.size(),
                "Debe quedar exactamente el escenario indicado.");
        assertEquals(adventurerId, adventurers.get(0).getId(),
                "El id devuelto debe corresponder al aventurero insertado.");
        assertEquals(Reglas.VIDA_INICIAL, adventurers.get(0).getVida(),
                "El aventurero nuevo debe empezar con la vida inicial.");
        assertEquals(Reglas.ORO_INICIAL, adventurers.get(0).getOro(),
                "El aventurero nuevo debe empezar con el oro inicial.");
        assertEquals(Reglas.PISO_INICIAL, adventurers.get(0).getPisoActual(),
                "El aventurero nuevo debe empezar en el piso inicial.");
        assertTrue(skeleton.getId() > 0, "La criatura del escenario debe recibir un id.");
        assertTrue(orc.getId() > 0, "La criatura del escenario debe recibir un id.");
    }

    @Test
    void transactionRethrowsWorkExceptionUnchanged() throws Exception {
        PersistenciaException expected = new PersistenciaException("Falla simulada");
        TransaccionDirecta transaction = new TransaccionDirecta(creatureDAO, adventurerDAO);

        PersistenciaException thrown = assertThrows(PersistenciaException.class,
                () -> transaction.ejecutar((creatures, adventurers) -> {
                    throw expected;
                }),
                "La transacción debe propagar la excepción lanzada dentro del trabajo.");

        assertEquals(expected, thrown,
                "Debe propagarse la misma instancia de la excepción.");
    }

    private Aventurero createAdventurer() throws Exception {
        return createAdventurer(Reglas.VIDA_INICIAL, Reglas.ORO_INICIAL);
    }

    private Aventurero createAdventurer(int health, int gold) throws Exception {
        Aventurero adventurer = new Aventurero(
                0, "Aventurero", 1, health, Reglas.VIDA_INICIAL,
                Reglas.ATAQUE_INICIAL, gold, Reglas.PISO_INICIAL);
        adventurerDAO.insertar(adventurer);
        return adventurer;
    }
}
