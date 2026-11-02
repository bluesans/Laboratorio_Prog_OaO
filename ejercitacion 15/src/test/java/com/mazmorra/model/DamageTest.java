package com.mazmorra.model;

import com.mazmorra.Reglas;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las reglas de daño, curación y estado de las criaturas.
 */
class DamageTest {

    private static final int BREATH_BONUS = 5;
    private static final int PHASE_BONUS = 5;

    @Test
    void skeletonReducesPhysicalDamage() {
        Esqueleto skeleton = new Esqueleto(
                1, "Esqueleto", 100, 100, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);

        skeleton.recibirDanio(20, TipoAtaque.FISICO);

        assertEquals(88, skeleton.getVida(),
                "El daño físico de 20 debe quedar reducido a 12 por la resistencia del 40 %.");
    }

    @Test
    void skeletonTakesFullMagicDamage() {
        Esqueleto skeleton = new Esqueleto(
                1, "Esqueleto", 100, 100, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);

        skeleton.recibirDanio(20, TipoAtaque.MAGICO);

        assertEquals(80, skeleton.getVida(),
                "La resistencia física no debe reducir el daño mágico.");
    }

    @Test
    void ghostReducesPhysicalDamageWhenIncorporeal() {
        Fantasma ghost = new Fantasma(
                1, "Fantasma", 100, 100, 1, Reglas.DANIO_FANTASMA, 1, false, true);

        ghost.recibirDanio(20, TipoAtaque.FISICO);

        assertEquals(90, ghost.getVida(),
                "Un fantasma incorpóreo debe recibir la mitad del daño físico.");
    }

    @Test
    void ghostTakesFullMagicDamage() {
        Fantasma ghost = new Fantasma(
                1, "Fantasma", 100, 100, 1, Reglas.DANIO_FANTASMA, 1, false, true);

        ghost.recibirDanio(20, TipoAtaque.MAGICO);

        assertEquals(80, ghost.getVida(),
                "La incorporeidad no debe reducir el daño mágico.");
    }

    @Test
    void ghostTruncatesOddPhysicalDamage() {
        Fantasma ghost = new Fantasma(
                1, "Fantasma", 100, 100, 1, Reglas.DANIO_FANTASMA, 1, false, true);

        ghost.recibirDanio(1, TipoAtaque.FISICO);

        assertEquals(100, ghost.getVida(),
                "Con daño físico 1 la mitad entera es 0 y la vida no debe cambiar.");
    }

    @Test
    void orcTakesFullPhysicalDamage() {
        Orco orc = new Orco(
                1, "Orco", 100, 100, 1, Reglas.DANIO_ORCO, 1, false, 50);

        orc.recibirDanio(20, TipoAtaque.FISICO);

        assertEquals(80, orc.getVida(),
                "La resistencia mágica no debe reducir el daño físico.");
    }

    @Test
    void orcReducesMagicDamage() {
        Orco orc = new Orco(
                1, "Orco", 100, 100, 1, Reglas.DANIO_ORCO, 1, false, 50);

        orc.recibirDanio(20, TipoAtaque.MAGICO);

        assertEquals(90, orc.getVida(),
                "El daño mágico de 20 debe quedar reducido a 10 por la resistencia del 50 %.");
    }

    @Test
    void dragonCalculatesAttackDamagePerPhase() {
        DragonJefe dragon = new DragonJefe(
                1, "Dragón", 100, 100, 3, Reglas.DANIO_DRAGON, 3, false, 1, 3, false);

        assertEquals(Reglas.DANIO_DRAGON, dragon.getDanioAtaque(),
                "En la fase 1 y sin aliento, el daño debe ser el ataque base.");
    }

    @Test
    void dragonCannotBeEscaped() {
        DragonJefe dragon = new DragonJefe(
                1, "Dragón", 100, 100, 3, Reglas.DANIO_DRAGON, 3, false, 1, 3, false);

        assertFalse(dragon.permiteHuida(),
                "Del jefe final no se debe poder huir.");
    }

    @Test
    void dragonRecalculatesPhaseAndAttackDamageAfterTakingDamage() {
        DragonJefe dragon = new DragonJefe(
                1, "Dragón", 100, 100, 3, Reglas.DANIO_DRAGON, 3, false, 1, 3, false);

        dragon.recibirDanio(40);

        assertEquals(60, dragon.getVida(), "La vida debe bajar de 100 a 60.");
        assertEquals(2, dragon.getFaseActual(), "Con 60 de vida el dragón debe entrar en la fase 2.");
        assertTrue(dragon.isAlientoDeFuego(), "El aliento de fuego debe activarse desde la fase 2.");
        assertEquals(Reglas.DANIO_DRAGON + BREATH_BONUS + PHASE_BONUS,
                dragon.getDanioAtaque(),
                "En la fase 2 el daño debe sumar el bono de aliento y un bono de fase.");

        dragon.recibirDanio(40);

        assertEquals(20, dragon.getVida(), "La vida debe bajar de 60 a 20.");
        assertEquals(3, dragon.getFaseActual(), "Con 20 de vida el dragón debe entrar en la fase 3.");
        assertTrue(dragon.isAlientoDeFuego(), "El aliento de fuego debe seguir activo en la fase 3.");
        assertEquals(Reglas.DANIO_DRAGON + BREATH_BONUS + PHASE_BONUS * 2,
                dragon.getDanioAtaque(),
                "En la fase 3 el daño debe sumar el bono de aliento y dos bonos de fase.");
    }

    @Test
    void healingNeverExceedsMaximumLife() {
        Aventurero adventurer = new Aventurero(1, "Aventurero", 1, 90, 100, 20, 50, 1);

        adventurer.curar(50);

        assertEquals(100, adventurer.getVida(),
                "La curación debe quedar limitada por la vida máxima.");

        adventurer.curar(10);

        assertEquals(100, adventurer.getVida(),
                "Curar con la vida al máximo no debe modificarla.");
    }

    @Test
    void adventurerIsDefeatedAtZeroLife() {
        Aventurero adventurer = new Aventurero(1, "Aventurero", 1, 20, 100, 20, 50, 1);

        adventurer.recibirDanio(20);

        assertEquals(0, adventurer.getVida(), "La vida no debe quedar negativa.");
        assertTrue(adventurer.estaDerrotado(),
                "Un aventurero con 0 de vida debe estar derrotado.");
    }

    @Test
    void evictedCreatureIsNotAlive() {
        Esqueleto skeleton = new Esqueleto(
                1, "Esqueleto", 100, 100, 1, Reglas.DANIO_ESQUELETO, 1, false, 40);

        assertTrue(skeleton.estaViva(), "La criatura recién creada debe estar viva.");

        skeleton.marcarEvadida();

        assertFalse(skeleton.estaViva(),
                "Una criatura evadida no debe considerarse viva.");
        assertFalse(skeleton.estaDerrotado(),
                "Una criatura evadida no está derrotada, solo quedó atrás.");
    }
}
