package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.model.Fantasma;
import com.mazmorra.model.Orco;

import java.util.List;

public final class Escenario {

    private Escenario() {
        throw new UnsupportedOperationException("Clase de utilidad: no se instancia.");
    }

    public static List<Criatura> crear() {
        return List.of(
                new Esqueleto(0, "Esqueleto Oxidado", 25, 25, 2,
                        Reglas.DANIO_ESQUELETO, 1, false, Reglas.RESISTENCIA_ESQUELETO),
                new Fantasma(0, "Fantasma del Pasillo", 30, 30, 3,
                        Reglas.DANIO_FANTASMA, 1, false, true),
                new Orco(0, "Orco Guardian", 40, 40, 5,
                        Reglas.DANIO_ORCO, 2, false, Reglas.RESISTENCIA_MAGICA_ORCO),
                new DragonJefe(0, "Jefe Dragón", 60, 60, 10,
                        Reglas.DANIO_DRAGON, 3, false, 1, Reglas.FASES_DRAGON, false));
    }
}
