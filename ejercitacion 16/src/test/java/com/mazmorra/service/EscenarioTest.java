package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.TipoCriatura;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del escenario inicial de la mazmorra.
 */
class EscenarioTest {

    @Test
    void elEscenarioTieneExactamenteCuatroCriaturas() {
        List<Criatura> criaturas = Escenario.crear();

        assertEquals(4, criaturas.size(),
                "El escenario inicial debe tener exactamente cuatro criaturas.");
    }

    @Test
    void lasCriaturasAparecenEnElOrdenEsperado() {
        List<TipoCriatura> tipos = new ArrayList<>();
        for (Criatura criatura : Escenario.crear()) {
            tipos.add(criatura.getTipo());
        }

        assertEquals(List.of(
                TipoCriatura.ESQUELETO,
                TipoCriatura.FANTASMA,
                TipoCriatura.ORCO,
                TipoCriatura.DRAGON_JEFE), tipos,
                "Las criaturas deben aparecer en el orden esqueleto, fantasma, orco y dragón.");
    }

    @Test
    void losPisosSiguenLaProgresionDeLaMazmorra() {
        List<Integer> pisos = new ArrayList<>();
        for (Criatura criatura : Escenario.crear()) {
            pisos.add(criatura.getPisoMazmorra());
        }

        assertEquals(List.of(1, 1, 2, 3), pisos,
                "Los pisos deben ser 1, 1, 2 y 3.");
    }

    @Test
    void laVidaCoincideConLaVidaMaximaYConLosValoresEsperados() {
        List<Integer> vidas = new ArrayList<>();
        List<Integer> vidasMaximas = new ArrayList<>();
        for (Criatura criatura : Escenario.crear()) {
            vidas.add(criatura.getVida());
            vidasMaximas.add(criatura.getVidaMaxima());
        }

        assertEquals(List.of(25, 30, 40, 60), vidas,
                "La vida inicial debe ser 25, 30, 40 y 60.");
        assertEquals(List.of(25, 30, 40, 60), vidasMaximas,
                "La vida máxima debe ser 25, 30, 40 y 60.");
    }

    @Test
    void losDaniosCoincidenConLasReglasYElDragonTieneTresFases() {
        List<Criatura> criaturas = Escenario.crear();

        assertEquals(Reglas.DANIO_ESQUELETO, criaturas.get(0).getDanioAtaque(),
                "El esqueleto debe pegar 15.");
        assertEquals(Reglas.DANIO_FANTASMA, criaturas.get(1).getDanioAtaque(),
                "El fantasma debe pegar 18.");
        assertEquals(Reglas.DANIO_ORCO, criaturas.get(2).getDanioAtaque(),
                "El orco debe pegar 15.");

        DragonJefe dragon = (DragonJefe) criaturas.get(3);
        assertEquals(Reglas.DANIO_DRAGON, dragon.getDanioAtaqueBase(),
                "El daño base del dragón debe ser 13.");
        assertEquals(Reglas.DANIO_DRAGON, dragon.getDanioAtaque(),
                "En la primera fase, sin aliento de fuego, el dragón debe pegar su daño base de 13.");
        assertEquals(Reglas.FASES_DRAGON, dragon.getCantidadFases(),
                "El dragón debe tener tres fases.");
    }

    @Test
    void todasEmpiezanVivasYSinRecompensaPagada() {
        for (Criatura criatura : Escenario.crear()) {
            assertTrue(criatura.estaViva(),
                    "La criatura " + criatura.getNombre() + " debe empezar viva.");
            assertFalse(criatura.isRecompensaPagada(),
                    "La criatura " + criatura.getNombre() + " no debe tener la recompensa pagada.");
        }
    }

    @Test
    void soloElDragonBloqueaLaHuida() {
        List<Criatura> criaturas = Escenario.crear();

        assertTrue(criaturas.get(0).permiteHuida(),
                "Se debe poder huir del esqueleto.");
        assertTrue(criaturas.get(1).permiteHuida(),
                "Se debe poder huir del fantasma.");
        assertTrue(criaturas.get(2).permiteHuida(),
                "Se debe poder huir del orco.");
        assertFalse(criaturas.get(3).permiteHuida(),
                "No se debe poder huir del jefe final.");
    }
}
