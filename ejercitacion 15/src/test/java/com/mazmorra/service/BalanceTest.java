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
    void lineaOptimaGanaCon16DeVida() throws Exception {
        int idAventurero = sembrarEscenario();

        jugar(idAventurero, TipoAtaque.MAGICO, TipoAtaque.FISICO);

        Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);
        assertTrue(aventurero.estaVivo(),
                "La línea óptima debe terminar con el aventurero vivo.");
        assertEquals(16, aventurero.getVida(),
                "La línea óptima debe terminar con 16 de vida.");
        assertEquals(150, aventurero.getOro(),
                "La línea óptima debe terminar con 150 de oro.");
    }

    @Test
    void huidaPermiteGanarSinRecompensaDelEsqueleto() throws Exception {
        int idAventurero = sembrarEscenario();
        Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);
        Criatura esqueleto = buscarViva(idAventurero, "Esqueleto Oxidado");

        servicio.huirDeCombate(aventurero, esqueleto);

        pelear(idAventurero, "Fantasma del Pasillo", TipoAtaque.MAGICO);
        avanzar(idAventurero);
        pelear(idAventurero, "Orco Guardian", TipoAtaque.FISICO);
        avanzar(idAventurero);
        pelear(idAventurero, "Jefe Dragón", TipoAtaque.FISICO);

        Aventurero finalizado = servicio.obtenerEstadoAventurero(idAventurero);
        assertTrue(finalizado.estaVivo(),
                "Huir del esqueleto y pelear el resto debe terminar en victoria.");
        assertEquals(16, finalizado.getVida(),
                "La victoria tras huir debe terminar con 16 de vida.");
        assertEquals(130, finalizado.getOro(),
                "Sin la recompensa del esqueleto deben quedar 130 de oro.");
    }

    @Test
    void pocionAntesDelDragonPermiteGanar() throws Exception {
        int idAventurero = sembrarEscenario();

        pelear(idAventurero, "Esqueleto Oxidado", TipoAtaque.FISICO);
        pelear(idAventurero, "Fantasma del Pasillo", TipoAtaque.FISICO);
        avanzar(idAventurero);
        pelear(idAventurero, "Orco Guardian", TipoAtaque.FISICO);

        Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);
        assertEquals(34, aventurero.getVida(),
                "La línea física debe llegar al dragón con 34 de vida.");
        assertEquals(100, aventurero.getOro(),
                "Antes de la poción deben quedar 100 de oro.");

        servicio.usarPocion(aventurero, Reglas.TAMANO_POCION);
        assertEquals(64, aventurero.getVida(),
                "La poción debe curar 30 y dejar 64 de vida.");
        assertEquals(80, aventurero.getOro(),
                "La poción debe costar 20 de oro.");

        avanzar(idAventurero);
        pelear(idAventurero, "Jefe Dragón", TipoAtaque.FISICO);

        Aventurero finalizado = servicio.obtenerEstadoAventurero(idAventurero);
        assertTrue(finalizado.estaVivo(),
                "La línea física con poción debe terminar en victoria.");
        assertEquals(19, finalizado.getVida(),
                "La victoria con una poción debe terminar con 19 de vida.");
        assertEquals(130, finalizado.getOro(),
                "La victoria con una poción debe terminar con 130 de oro.");
    }

    @Test
    void soloMagiaTerminaEnDerrota() throws Exception {
        int idAventurero = sembrarEscenario();

        jugar(idAventurero, TipoAtaque.MAGICO, TipoAtaque.MAGICO);

        Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);
        assertTrue(aventurero.estaDerrotado(),
                "Atacar siempre con magia debe terminar en derrota.");
    }

    @Test
    void soloFisicoTerminaEnDerrota() throws Exception {
        int idAventurero = sembrarEscenario();

        jugar(idAventurero, TipoAtaque.FISICO, TipoAtaque.FISICO);

        Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);
        assertTrue(aventurero.estaDerrotado(),
                "Atacar siempre con el puño debe terminar en derrota.");
    }

    private int sembrarEscenario() throws JuegoException {
        List<Criatura> escenario = new ArrayList<>();
        escenario.add(new Esqueleto(
                0, "Esqueleto Oxidado", 25, 25, 2, Reglas.DANIO_ESQUELETO, 1, false,
                Reglas.RESISTENCIA_ESQUELETO));
        escenario.add(new Fantasma(
                0, "Fantasma del Pasillo", 30, 30, 3, Reglas.DANIO_FANTASMA, 1, false, true));
        escenario.add(new Orco(
                0, "Orco Guardian", 40, 40, 5, Reglas.DANIO_ORCO, 2, false,
                Reglas.RESISTENCIA_MAGICA_ORCO));
        escenario.add(new DragonJefe(
                0, "Jefe Dragón", 60, 60, 10, Reglas.DANIO_DRAGON, 3, false, 1,
                Reglas.FASES_DRAGON, false));
        return servicio.iniciarNuevaPartida(
                "Aventurero", Reglas.VIDA_INICIAL, Reglas.ATAQUE_INICIAL, escenario);
    }

    private void jugar(int idAventurero, TipoAtaque tipoPrimerPiso, TipoAtaque tipoPisosSiguientes)
            throws JuegoException {
        while (true) {
            Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);
            if (aventurero.estaDerrotado()) {
                return;
            }

            List<Criatura> vivas = new ArrayList<>();
            for (Criatura criatura : servicio.listarCriaturasPorPiso(aventurero.getPisoActual())) {
                if (criatura.estaViva()) {
                    vivas.add(criatura);
                }
            }

            if (vivas.isEmpty()) {
                if (aventurero.getPisoActual() >= Reglas.PISO_FINAL) {
                    return;
                }
                servicio.avanzarPiso(aventurero);
                continue;
            }

            TipoAtaque tipo = aventurero.getPisoActual() == Reglas.PISO_INICIAL
                    ? tipoPrimerPiso
                    : tipoPisosSiguientes;
            servicio.resolverTurno(aventurero, vivas.get(0), tipo);
        }
    }

    private void pelear(int idAventurero, String nombreCriatura, TipoAtaque tipo)
            throws JuegoException {
        while (true) {
            Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);
            if (aventurero.estaDerrotado()) {
                return;
            }
            Criatura criatura = buscarViva(idAventurero, nombreCriatura);
            if (criatura == null) {
                return;
            }
            servicio.resolverTurno(aventurero, criatura, tipo);
        }
    }

    private Criatura buscarViva(int idAventurero, String nombreCriatura) throws JuegoException {
        Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);
        for (Criatura criatura : servicio.listarCriaturasPorPiso(aventurero.getPisoActual())) {
            if (criatura.estaViva() && criatura.getNombre().equals(nombreCriatura)) {
                return criatura;
            }
        }
        return null;
    }

    private void avanzar(int idAventurero) throws JuegoException {
        Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);
        servicio.avanzarPiso(aventurero);
    }
}
