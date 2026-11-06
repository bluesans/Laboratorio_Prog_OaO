package com.mazmorra.consola;

import com.mazmorra.Reglas;

import com.mazmorra.exception.AventureroDerrotadoException;
import com.mazmorra.exception.CriaturaYaDerrotadaException;
import com.mazmorra.exception.CriaturasPendientesException;
import com.mazmorra.exception.DanioInvalidoException;
import com.mazmorra.exception.HuidaProhibidaException;
import com.mazmorra.exception.JuegoException;
import com.mazmorra.exception.OroInsuficienteException;
import com.mazmorra.exception.VidaCompletaException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.model.Fantasma;
import com.mazmorra.model.Orco;
import com.mazmorra.model.TipoAtaque;
import com.mazmorra.service.JuegoService;
import com.mazmorra.service.ResultadoPocion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DemoGuionada {

    private DemoGuionada() {
        throw new UnsupportedOperationException("Clase de utilidad: no se instancia.");
    }

    public static void ejecutar(JuegoService servicio) throws JuegoException {
        Objects.requireNonNull(servicio, "El servicio no puede ser nulo.");

        System.out.println("============================================================");
        System.out.println("  DEMO GUIONADA DE MAZMORRA");
        System.out.println("============================================================");
        System.out.println();

        System.out.println("==== 1) PREPARACIÓN ====");
        List<Criatura> escenario = construirEscenario();
        int idAventurero = servicio.iniciarNuevaPartida(
                "Aventurero Demo", Reglas.VIDA_INICIAL, Reglas.ATAQUE_INICIAL, escenario);
        Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);
        Criatura esqueleto = escenario.get(0);
        Criatura fantasma = escenario.get(1);
        Criatura orco = escenario.get(2);
        DragonJefe dragon = (DragonJefe) escenario.get(3);

        System.out.println("  Partida nueva sembrada en el piso " + aventurero.getPisoActual() + ".");
        imprimirEstado(aventurero);
        for (Criatura criatura : escenario) {
            imprimirCriatura(criatura);
        }
        System.out.println();

        System.out.println("==== 2) ERRORES BÁSICOS DE REGLA ====");

        System.out.println("--- CASO DE ERROR: poción con la vida al máximo");
        try {
            servicio.usarPocion(aventurero, Reglas.TAMANO_POCION);
            System.out.println("FALLO: se esperaba una excepción y no ocurrió");
        } catch (VidaCompletaException e) {
            System.out.println("EXCEPCIÓN ESPERADA -> "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();

        System.out.println("--- CASO DE ERROR: avanzar de piso con criaturas vivas");
        try {
            servicio.avanzarPiso(aventurero);
            System.out.println("FALLO: se esperaba una excepción y no ocurrió");
        } catch (CriaturasPendientesException e) {
            System.out.println("EXCEPCIÓN ESPERADA -> "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();

        System.out.println("--- CASO DE ERROR: ataque con daño cero");
        try {
            servicio.resolverAtaque(esqueleto, 0, TipoAtaque.FISICO);
            System.out.println("FALLO: se esperaba una excepción y no ocurrió");
        } catch (DanioInvalidoException e) {
            System.out.println("EXCEPCIÓN ESPERADA -> "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();

        System.out.println("==== 3) USO POLIMÓRFICO DE DANABLE ====");
        System.out.println("  El mismo método resuelve el ataque sin conocer la clase concreta.");

        int vidaFantasmaAntes = fantasma.getVida();
        servicio.resolverAtaque(fantasma, Reglas.ATAQUE_INICIAL, TipoAtaque.MAGICO);
        System.out.println("  Criatura: " + fantasma.getNombre() + " pasó de "
                + vidaFantasmaAntes + " a " + fantasma.getVida() + "/"
                + fantasma.getVidaMaxima() + " de vida.");

        Aventurero decoy = servicio.registrarAventurero(
                "Aventurero de Prueba", Reglas.VIDA_INICIAL, Reglas.ATAQUE_INICIAL);
        int vidaDecoyAntes = decoy.getVida();
        servicio.resolverAtaque(decoy, Reglas.VIDA_INICIAL / 2, TipoAtaque.FISICO);
        System.out.println("  Aventurero: " + decoy.getNombre() + " pasó de "
                + vidaDecoyAntes + " a " + decoy.getVida() + "/"
                + decoy.getVidaMaxima() + " de vida.");
        System.out.println();

        System.out.println("==== 4) COMBATE EN EL PISO 1 ====");
        atacarConTurno(servicio, aventurero, fantasma, TipoAtaque.FISICO, "físico");
        atacarConTurno(servicio, aventurero, esqueleto, TipoAtaque.FISICO, "físico");
        atacarConTurno(servicio, aventurero, esqueleto, TipoAtaque.FISICO, "físico");
        atacarConTurno(servicio, aventurero, esqueleto, TipoAtaque.FISICO, "físico");
        System.out.println("  La resistencia del esqueleto recortó el daño físico.");
        System.out.println();

        System.out.println("--- CASO DE ERROR: atacar a una criatura ya derrotada");
        try {
            servicio.resolverAtaque(esqueleto, Reglas.ATAQUE_INICIAL, TipoAtaque.FISICO);
            System.out.println("FALLO: se esperaba una excepción y no ocurrió");
        } catch (CriaturaYaDerrotadaException e) {
            System.out.println("EXCEPCIÓN ESPERADA -> "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();

        System.out.println("==== 5) COMBATE EN EL PISO 2 ====");
        servicio.avanzarPiso(aventurero);
        System.out.println("  Avanzaste al piso " + aventurero.getPisoActual() + ".");
        int vidaOrcoAntesMagia = orco.getVida();
        atacarConTurno(servicio, aventurero, orco, TipoAtaque.MAGICO, "mágico");
        int danioMagico = vidaOrcoAntesMagia - orco.getVida();
        int danioNominal = aventurero.getAtaqueBase();
        boolean orcoContraataco = orco.estaViva();
        atacarConTurno(servicio, aventurero, orco, TipoAtaque.FISICO, "físico");
        atacarConTurno(servicio, aventurero, orco, TipoAtaque.FISICO, "físico");
        System.out.println("  El orco recibió " + danioMagico + " de daño mágico de "
                + danioNominal + " posibles (absorbió " + (danioNominal - danioMagico) + ")"
                + (orcoContraataco ? " y contraatacó." : "."));
        System.out.println();

        System.out.println("==== 6) POCIÓN Y AVANCE ====");
        ResultadoPocion pocion = servicio.usarPocion(aventurero, Reglas.TAMANO_POCION);
        System.out.println("  Poción: vida " + aventurero.getVida() + "/"
                + aventurero.getVidaMaxima() + " (curó " + pocion.vidaCurada()
                + " por " + pocion.oroPagado() + " de oro).");
        servicio.avanzarPiso(aventurero);
        System.out.println("  Avanzaste al piso " + aventurero.getPisoActual() + ".");
        System.out.println();

        System.out.println("==== 7) COMBATE FINAL ====");
        System.out.println("--- CASO DE ERROR: huir del jefe final");
        try {
            servicio.huirDeCombate(aventurero, dragon);
            System.out.println("FALLO: se esperaba una excepción y no ocurrió");
        } catch (HuidaProhibidaException e) {
            System.out.println("EXCEPCIÓN ESPERADA -> "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();

        atacarConTurno(servicio, aventurero, dragon, TipoAtaque.FISICO, "físico");
        atacarConTurno(servicio, aventurero, dragon, TipoAtaque.FISICO, "físico");
        atacarConTurno(servicio, aventurero, dragon, TipoAtaque.FISICO, "físico");
        if (dragon.estaDerrotado()) {
            System.out.println("  Victoria: el Jefe Dragón cayó.");
        } else {
            System.out.println("  La demo terminó con el Jefe Dragón en pie.");
        }
        System.out.println();

        System.out.println("==== 8) ERRORES DEL AVENTURERO ====");

        int oroAnterior = decoy.getOro();
        decoy.setOro(0);
        System.out.println("--- CASO DE ERROR: poción sin oro suficiente");
        try {
            servicio.usarPocion(decoy, Reglas.TAMANO_POCION);
            System.out.println("FALLO: se esperaba una excepción y no ocurrió");
        } catch (OroInsuficienteException e) {
            System.out.println("EXCEPCIÓN ESPERADA -> "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            decoy.setOro(oroAnterior);
        }
        System.out.println();

        servicio.resolverAtaque(decoy, decoy.getVida(), TipoAtaque.FISICO);
        System.out.println("  El aventurero de prueba quedó en " + decoy.getVida()
                + "/" + decoy.getVidaMaxima() + " de vida.");
        System.out.println("--- CASO DE ERROR: actuar con un aventurero derrotado");
        try {
            servicio.usarPocion(decoy, Reglas.TAMANO_POCION);
            System.out.println("FALLO: se esperaba una excepción y no ocurrió");
        } catch (AventureroDerrotadoException e) {
            System.out.println("EXCEPCIÓN ESPERADA -> "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();

        System.out.println("==== 9) ESTADO FINAL EN LA BASE ====");
        Aventurero persistido = servicio.obtenerEstadoAventurero(idAventurero);
        imprimirEstado(persistido);
        for (Criatura criatura : servicio.listarCriaturas()) {
            imprimirCriatura(criatura);
        }
        System.out.println();
        System.out.println("FIN DE LA DEMO");
    }

    private static List<Criatura> construirEscenario() {
        List<Criatura> escenario = new ArrayList<>();
        escenario.add(new Esqueleto(
                0, "Esqueleto Oxidado", 25, 25, 2, Reglas.DANIO_ESQUELETO, 1, false,
                Reglas.RESISTENCIA_ESQUELETO));
        escenario.add(new Fantasma(
                0, "Fantasma del Pasillo", 30, 30, 3, Reglas.DANIO_FANTASMA, 1, false, true));
        escenario.add(new Orco(0, "Orco Guardian", 40, 40, 5, Reglas.DANIO_ORCO, 2, false,
                Reglas.RESISTENCIA_MAGICA_ORCO));
        escenario.add(new DragonJefe(
                0, "Jefe Dragón", 60, 60, 10, Reglas.DANIO_DRAGON, 3, false, 1,
                Reglas.FASES_DRAGON, false));
        return escenario;
    }

    private static void atacarConTurno(JuegoService servicio, Aventurero aventurero,
            Criatura criatura, TipoAtaque tipo, String nombreTipo) throws JuegoException {
        int vidaCriaturaAntes = criatura.getVida();
        int vidaAventureroAntes = aventurero.getVida();
        int oroAntes = aventurero.getOro();
        servicio.resolverTurno(aventurero, criatura, tipo);
        System.out.println("  Ataque " + nombreTipo + " a " + criatura.getNombre()
                + ": criatura " + vidaCriaturaAntes + " -> " + criatura.getVida()
                + "/" + criatura.getVidaMaxima() + ", aventurero " + vidaAventureroAntes
                + " -> " + aventurero.getVida() + ", oro " + oroAntes + " -> "
                + aventurero.getOro() + ".");
        if (criatura instanceof DragonJefe dragon) {
            System.out.println("  Fase del dragón: " + dragon.getFaseActual() + "/"
                    + dragon.getCantidadFases() + ", aliento de fuego: "
                    + dragon.isAlientoDeFuego() + ".");
        }
    }

    private static void imprimirEstado(Aventurero aventurero) {
        System.out.println("  " + aventurero.getNombre() + " (id " + aventurero.getId()
                + "): vida " + aventurero.getVida() + "/" + aventurero.getVidaMaxima()
                + ", ataque " + aventurero.getAtaqueBase() + ", oro " + aventurero.getOro()
                + ", piso " + aventurero.getPisoActual() + ".");
    }

    private static void imprimirCriatura(Criatura criatura) {
        StringBuilder linea = new StringBuilder();
        linea.append("  ").append(criatura.getNombre()).append(" (id ")
                .append(criatura.getId()).append("): vida ").append(criatura.getVida())
                .append("/").append(criatura.getVidaMaxima())
                .append(", amenaza ").append(criatura.getNivelAmenaza())
                .append(", piso ").append(criatura.getPisoMazmorra())
                .append(", derrotada ").append(criatura.estaDerrotado())
                .append(", evadida ").append(criatura.isEvadida())
                .append(", recompensa pagada ").append(criatura.isRecompensaPagada());
        if (criatura instanceof DragonJefe dragon) {
            linea.append(", fase ").append(dragon.getFaseActual())
                    .append("/").append(dragon.getCantidadFases())
                    .append(", aliento ").append(dragon.isAlientoDeFuego());
        }
        System.out.println(linea);
    }
}
