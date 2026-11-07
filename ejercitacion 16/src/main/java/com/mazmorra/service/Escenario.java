package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.model.Arana;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.model.Fantasma;
import com.mazmorra.model.Golem;
import com.mazmorra.model.Orco;

import java.util.ArrayList;
import java.util.List;

public final class Escenario {

    private static final int VIDA_ESQUELETO = 25;
    private static final int AMENAZA_ESQUELETO = 2;
    private static final int VIDA_FANTASMA = 30;
    private static final int AMENAZA_FANTASMA = 3;
    private static final int VIDA_ORCO = 40;
    private static final int AMENAZA_ORCO = 5;
    private static final int VIDA_DRAGON = 60;
    private static final int AMENAZA_DRAGON = 10;

    private Escenario() {
        throw new UnsupportedOperationException("Clase de utilidad: no se instancia.");
    }

    public static List<Criatura> crear(int ronda) {
        if (ronda < 1) {
            throw new IllegalArgumentException("La ronda debe ser al menos 1.");
        }
        double factorVida = Reglas.FACTOR_INICIAL
                + Reglas.ESCALA_VIDA_RONDA * (ronda - 1);
        double factorDanio = Reglas.FACTOR_INICIAL
                + Reglas.ESCALA_DANIO_RONDA * (ronda - 1);

        List<Criatura> criaturas = new ArrayList<>();
        criaturas.add(new Esqueleto(0, "Esqueleto Oxidado", escalar(VIDA_ESQUELETO, factorVida),
                escalar(VIDA_ESQUELETO, factorVida), AMENAZA_ESQUELETO,
                escalar(Reglas.DANIO_ESQUELETO, factorDanio), 1, false,
                Reglas.RESISTENCIA_ESQUELETO));
        criaturas.add(new Fantasma(0, "Fantasma del Pasillo", escalar(VIDA_FANTASMA, factorVida),
                escalar(VIDA_FANTASMA, factorVida), AMENAZA_FANTASMA,
                escalar(Reglas.DANIO_FANTASMA, factorDanio), 1, false, true));
        criaturas.add(new Orco(0, "Orco Guardián", escalar(VIDA_ORCO, factorVida),
                escalar(VIDA_ORCO, factorVida), AMENAZA_ORCO,
                escalar(Reglas.DANIO_ORCO, factorDanio), 2, false,
                Reglas.RESISTENCIA_MAGICA_ORCO));
        criaturas.add(new DragonJefe(0, "Jefe Dragón", escalar(VIDA_DRAGON, factorVida),
                escalar(VIDA_DRAGON, factorVida), AMENAZA_DRAGON,
                escalar(Reglas.DANIO_DRAGON, factorDanio), 3, false, 1, Reglas.FASES_DRAGON, false));

        if (ronda >= 2) {
            criaturas.add(arana(1, "Araña Acechante", factorVida, factorDanio));
            criaturas.add(arana(2, "Araña de las Sombras", factorVida, factorDanio));
        }
        if (ronda >= 3) {
            criaturas.add(golem(2, "Golem de Piedra", factorVida, factorDanio));
            criaturas.add(golem(3, "Golem de Obsidiana", factorVida, factorDanio));
        }

        int extrasPorPiso = Reglas.ENEMIGOS_EXTRA_POR_PISO * (ronda - 1);
        if (extrasPorPiso > 0) {
            agregarExtras(criaturas, 1, extrasPorPiso, new int[]{0, 1, 2},
                    factorVida, factorDanio);
            agregarExtras(criaturas, 2, extrasPorPiso,
                    ronda >= 3 ? new int[]{0, 1, 2} : new int[]{0, 1},
                    factorVida, factorDanio);
            agregarExtras(criaturas, 3, extrasPorPiso,
                    ronda >= 3 ? new int[]{0, 1} : new int[]{1},
                    factorVida, factorDanio);
        }

        criaturas.sort((una, otra) ->
                Integer.compare(una.getPisoMazmorra(), otra.getPisoMazmorra()));
        return criaturas;
    }

    private static void agregarExtras(List<Criatura> criaturas, int piso, int cantidad, int[] slots,
            double factorVida, double factorDanio) {
        int[] copias = new int[3];
        for (int i = 0; i < cantidad; i++) {
            int slot = slots[i % slots.length];
            copias[slot]++;
            criaturas.add(crearCopia(piso, slot, copias[slot] + 1, factorVida, factorDanio));
        }
    }

    private static Criatura crearCopia(int piso, int slot, int copia,
            double factorVida, double factorDanio) {
        if (piso == 1) {
            return switch (slot) {
                case 0 -> new Esqueleto(0, nombreCopia("Esqueleto Oxidado", copia),
                        escalar(VIDA_ESQUELETO, factorVida), escalar(VIDA_ESQUELETO, factorVida),
                        AMENAZA_ESQUELETO, escalar(Reglas.DANIO_ESQUELETO, factorDanio), 1,
                        false, Reglas.RESISTENCIA_ESQUELETO);
                case 1 -> new Fantasma(0, nombreCopia("Fantasma del Pasillo", copia),
                        escalar(VIDA_FANTASMA, factorVida), escalar(VIDA_FANTASMA, factorVida),
                        AMENAZA_FANTASMA, escalar(Reglas.DANIO_FANTASMA, factorDanio), 1,
                        false, true);
                default -> arana(1, nombreCopia("Araña Acechante", copia), factorVida, factorDanio);
            };
        }
        if (piso == 2) {
            return switch (slot) {
                case 0 -> new Orco(0, nombreCopia("Orco Guardián", copia),
                        escalar(VIDA_ORCO, factorVida), escalar(VIDA_ORCO, factorVida),
                        AMENAZA_ORCO, escalar(Reglas.DANIO_ORCO, factorDanio), 2, false,
                        Reglas.RESISTENCIA_MAGICA_ORCO);
                case 1 -> arana(2, nombreCopia("Araña de las Sombras", copia),
                        factorVida, factorDanio);
                default -> golem(2, nombreCopia("Golem de Piedra", copia), factorVida, factorDanio);
            };
        }
        return switch (slot) {
            case 0 -> golem(3, nombreCopia("Golem de Obsidiana", copia),
                    factorVida, factorDanio);
            default -> arana(3, nombreCopiaSima("Araña de la Sima", copia - 1),
                    factorVida, factorDanio);
        };
    }

    private static String nombreCopiaSima(String nombre, int copia) {
        return nombre + " " + copia;
    }

    private static Criatura arana(int piso, String nombre, double factorVida, double factorDanio) {
        return new Arana(0, nombre, escalar(Reglas.VIDA_ARANA, factorVida),
                escalar(Reglas.VIDA_ARANA, factorVida), Reglas.AMENAZA_ARANA,
                escalar(Reglas.DANIO_ARANA, factorDanio), piso, false);
    }

    private static Criatura golem(int piso, String nombre, double factorVida, double factorDanio) {
        return new Golem(0, nombre, escalar(Reglas.VIDA_GOLEM, factorVida),
                escalar(Reglas.VIDA_GOLEM, factorVida), Reglas.AMENAZA_GOLEM,
                escalar(Reglas.DANIO_GOLEM, factorDanio), piso, false, Reglas.RESISTENCIA_GOLEM);
    }

    private static String nombreCopia(String nombre, int copia) {
        return copia <= 1 ? nombre : nombre + " " + copia;
    }

    private static int escalar(int base, double factor) {
        return Math.max(1, (int) Math.round(base * factor));
    }
}
