package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.exception.JuegoException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.model.Fantasma;
import com.mazmorra.model.Orco;
import com.mazmorra.model.TipoAtaque;
import com.mazmorra.service.impl.JuegoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de balance que simulan partidas completas con el escenario de la consola.
 */
class BalanceTest {

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
    void optimalLineWinsWithExactLifeAndGold() throws Exception {
        int adventurerId = seedScenario();

        playGame(adventurerId, TipoAtaque.MAGICO, TipoAtaque.FISICO);

        Aventurero adventurer = service.obtenerEstadoAventurero(adventurerId);
        assertTrue(adventurer.estaVivo(),
                "La línea óptima debe terminar con el aventurero vivo.");
        assertEquals(16, adventurer.getVida(),
                "La línea óptima debe terminar con 16 de vida.");
        assertEquals(150, adventurer.getOro(),
                "La línea óptima debe terminar con 150 de oro.");
    }

    @Test
    void magicOnlyLineEndsDefeated() throws Exception {
        int adventurerId = seedScenario();

        playGame(adventurerId, TipoAtaque.MAGICO, TipoAtaque.MAGICO);

        Aventurero adventurer = service.obtenerEstadoAventurero(adventurerId);
        assertTrue(adventurer.estaDerrotado(),
                "Atacar siempre con magia debe terminar en derrota.");
    }

    @Test
    void physicalOnlyLineEndsDefeated() throws Exception {
        int adventurerId = seedScenario();

        playGame(adventurerId, TipoAtaque.FISICO, TipoAtaque.FISICO);

        Aventurero adventurer = service.obtenerEstadoAventurero(adventurerId);
        assertTrue(adventurer.estaDerrotado(),
                "Atacar siempre con el puño debe terminar en derrota.");
    }

    private int seedScenario() throws JuegoException {
        List<Criatura> scenario = new ArrayList<>();
        scenario.add(new Esqueleto(
                0, "Esqueleto Oxidado", 25, 25, 2, Reglas.DANIO_ESQUELETO, 1, false,
                Reglas.RESISTENCIA_ESQUELETO));
        scenario.add(new Fantasma(
                0, "Fantasma del Pasillo", 30, 30, 3, Reglas.DANIO_FANTASMA, 1, false, true));
        scenario.add(new Orco(
                0, "Orco Guardian", 40, 40, 5, Reglas.DANIO_ORCO, 2, false,
                Reglas.RESISTENCIA_MAGICA_ORCO));
        scenario.add(new DragonJefe(
                0, "Jefe Dragón", 60, 60, 10, Reglas.DANIO_DRAGON, 3, false, 1,
                Reglas.FASES_DRAGON, false));
        return service.iniciarNuevaPartida(
                "Aventurero", Reglas.VIDA_INICIAL, Reglas.ATAQUE_INICIAL, scenario);
    }

    private void playGame(int adventurerId, TipoAtaque firstFloorType, TipoAtaque laterFloorType)
            throws JuegoException {
        while (true) {
            Aventurero adventurer = service.obtenerEstadoAventurero(adventurerId);
            if (adventurer.estaDerrotado()) {
                return;
            }

            List<Criatura> living = new ArrayList<>();
            for (Criatura creature : service.listarCriaturasPorPiso(adventurer.getPisoActual())) {
                if (creature.estaViva()) {
                    living.add(creature);
                }
            }

            if (living.isEmpty()) {
                if (adventurer.getPisoActual() >= Reglas.PISO_FINAL) {
                    return;
                }
                service.avanzarPiso(adventurer);
                continue;
            }

            TipoAtaque type = adventurer.getPisoActual() == Reglas.PISO_INICIAL
                    ? firstFloorType
                    : laterFloorType;
            service.resolverTurno(adventurer, living.get(0), type);
        }
    }
}
