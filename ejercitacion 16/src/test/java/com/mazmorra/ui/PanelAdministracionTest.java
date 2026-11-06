package com.mazmorra.ui;

import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Esqueleto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PanelAdministracionTest {

    @Test
    void conservaLaFaseYElAlientoAlEditarUnDragon() {
        DragonJefe original = new DragonJefe(7, "Jefe", 20, 60, 10, 13, 3, false, 3, 3, true);
        original.marcarRecompensaPagada();

        Criatura reconstruida = PanelAdministracion.construir(original, "Jefe", 20, 60, 10, 13,
                3, 3, 3, false);

        assertTrue(reconstruida instanceof DragonJefe);
        DragonJefe dragon = (DragonJefe) reconstruida;
        assertEquals(3, dragon.getFaseActual());
        assertTrue(dragon.isAlientoDeFuego());
        assertTrue(dragon.isRecompensaPagada());
    }

    @Test
    void conservaEvadidaYRecompensaAlEditarOtraCriatura() {
        Esqueleto original = new Esqueleto(5, "Esqueleto", 13, 25, 2, 15, 1, false, 40);
        original.marcarEvadida();
        original.marcarRecompensaPagada();

        Criatura reconstruida = PanelAdministracion.construir(original, "Esqueleto", 13, 25,
                2, 15, 1, 0, 40, false);

        assertTrue(reconstruida.isEvadida());
        assertTrue(reconstruida.isRecompensaPagada());
    }
}
