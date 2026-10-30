package com.mazmorra;

import com.mazmorra.exception.JuegoException;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.model.Fantasma;
import com.mazmorra.model.Orco;
import com.mazmorra.model.TipoAtaque;
import com.mazmorra.service.JuegoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public final class JuegoConsola {

    private static final int PISO_FINAL = 3;
    private static final int VIDA_INICIAL = 100;
    private static final int ATAQUE_INICIAL = 20;
    private static final int RESISTENCIA_ESQUELETO = 40;
    private static final int DANIO_ORCO = 12;
    private static final int RESISTENCIA_MAGICA_ORCO = 50;
    private static final int FASES_DRAGON = 3;

    private final JuegoService servicio;
    private final Scanner scanner;

    public JuegoConsola(JuegoService servicio) {
        this.servicio = Objects.requireNonNull(servicio, "El servicio no puede ser nulo.");
        // Nunca se cierra System.in.
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() throws JuegoException {
        while (true) {
            System.out.println("============================================================");
            System.out.println("  MAZMORRA");
            System.out.println("============================================================");
            System.out.println("  1) Nueva partida");
            System.out.println("  2) Reglas del juego");
            System.out.println("  0) Salir");
            System.out.print("Opción: ");

            String linea = leerLinea();
            if (linea == null) {
                return;
            }

            int opcion;
            try {
                opcion = Integer.parseInt(linea.trim());
            } catch (NumberFormatException e) {
                System.out.println("  Opción inválida.");
                System.out.println();
                continue;
            }

            if (opcion == 0) {
                System.out.println();
                System.out.println("Hasta la próxima.");
                return;
            }

            try {
                if (opcion == 1) {
                    nuevaPartida();
                } else if (opcion == 2) {
                    mostrarReglas();
                } else {
                    System.out.println("  Opción inválida.");
                }
            } catch (PersistenciaException e) {
                throw e;
            } catch (JuegoException e) {
                System.out.println("  [!] " + e.getMessage());
            }

            System.out.println();
        }
    }

    private void nuevaPartida() throws JuegoException {
        // Reinicia la base y siembra el escenario inicial.
        System.out.print("Nombre del aventurero: ");
        String linea = leerLinea();
        if (linea == null) {
            return;
        }

        String nombre = linea.trim();
        if (nombre.isEmpty()) {
            nombre = "Aventurero";
        }
        if (nombre.length() > 50) {
            nombre = nombre.substring(0, 50);
        }

        int idAventurero = servicio.iniciarNuevaPartida(
                nombre, VIDA_INICIAL, ATAQUE_INICIAL, construirEscenario());

        System.out.println("Partida nueva: " + nombre + " en el piso 1.");
        System.out.println();
        buclePartida(idAventurero);
    }

    private List<Criatura> construirEscenario() {
        List<Criatura> escenario = new ArrayList<>();
        escenario.add(new Esqueleto(
                0, "Esqueleto Oxidado", 25, 25, 2, 12, 1, false, RESISTENCIA_ESQUELETO));
        escenario.add(new Fantasma(0, "Fantasma del Pasillo", 30, 30, 3, 15, 1, false, true));
        escenario.add(new Orco(0, "Orco Guardian", 40, 40, 5, DANIO_ORCO, 2, false,
                RESISTENCIA_MAGICA_ORCO));
        escenario.add(new DragonJefe(
                0, "Jefe Dragón", 60, 60, 10, 10, 3, false, 1, FASES_DRAGON, false));
        return escenario;
    }

    private void buclePartida(int idAventurero) throws JuegoException {
        while (true) {
            // Vuelve a leer el aventurero para no usar datos viejos.
            Aventurero aventurero = servicio.obtenerEstadoAventurero(idAventurero);

            if (aventurero.estaDerrotado()) {
                System.out.println("Derrota: " + aventurero.getNombre()
                        + " cayó en el piso " + aventurero.getPisoActual() + ".");
                return;
            }

            List<Criatura> vivas = new ArrayList<>();
            for (Criatura criatura : servicio.listarCriaturasPorPiso(aventurero.getPisoActual())) {
                if (criatura.estaViva()) {
                    vivas.add(criatura);
                }
            }

            boolean pisoDespejado = vivas.isEmpty();
            if (pisoDespejado && aventurero.getPisoActual() >= PISO_FINAL) {
                System.out.println("Victoria: no queda nadie en pie.");
                return;
            }

            imprimirPiso(aventurero, vivas);

            if (pisoDespejado) {
                System.out.println("  Piso despejado: avanza con la opción 5.");
            }

            System.out.println("1) Atacar físico   2) Atacar con magia   3) Huir (-hasta "
                    + JuegoService.COSTO_HUIDA + " oro y un golpe)");
            System.out.println("4) Poción (+" + JuegoService.TAMANO_POCION + " vida, hasta "
                    + JuegoService.COSTO_POCION + " oro)   5) Avanzar de piso");
            System.out.println("6) Estado   0) Abandonar");
            System.out.print("Acción: ");

            String linea = leerLinea();
            if (linea == null) {
                return;
            }

            int accion;
            try {
                accion = Integer.parseInt(linea.trim());
            } catch (NumberFormatException e) {
                System.out.println("  Opción inválida.");
                System.out.println();
                continue;
            }

            try {
                switch (accion) {
                    case 1 -> atacar(aventurero, vivas, TipoAtaque.FISICO, "físico");
                    case 2 -> atacar(aventurero, vivas, TipoAtaque.MAGICO, "mágico");
                    case 3 -> {
                        Criatura criatura = elegirCriatura(vivas);
                        if (criatura == null) {
                            continue;
                        }
                        int vidaAntes = aventurero.getVida();
                        int oroAntes = aventurero.getOro();
                        servicio.huirDeCombate(aventurero, criatura);
                        System.out.println("Huiste de " + criatura.getNombre()
                                + ". Queda atrás: no bloquea el piso y no paga recompensa. (-"
                                + (oroAntes - aventurero.getOro()) + " de oro, -"
                                + (vidaAntes - aventurero.getVida()) + " de vida)");
                    }
                    case 4 -> {
                        Criatura enemigo = null;
                        for (Criatura candidata : vivas) {
                            if (enemigo == null
                                    || candidata.getDanioAtaque() > enemigo.getDanioAtaque()) {
                                enemigo = candidata;
                            }
                        }
                        int vidaAntes = aventurero.getVida();
                        int oroAntes = aventurero.getOro();
                        int curado = Math.min(JuegoService.TAMANO_POCION,
                                aventurero.getVidaMaxima() - aventurero.getVida());
                        servicio.usarPocion(aventurero, JuegoService.TAMANO_POCION, enemigo);
                        System.out.println("Poción: vida " + aventurero.getVida()
                                + "/" + aventurero.getVidaMaxima() + ". (curó "
                                + curado + " por " + (oroAntes - aventurero.getOro()) + " de oro)");
                        if (enemigo != null) {
                            System.out.println(enemigo.getNombre() + " aprovecha: recibiste "
                                    + (vidaAntes + curado - aventurero.getVida()) + " de daño.");
                        }
                    }
                    case 5 -> {
                        servicio.avanzarPiso(aventurero);
                        System.out.println("Avanzaste al piso " + aventurero.getPisoActual() + ".");
                    }
                    case 6 -> imprimirEstadoAventurero(aventurero);
                    case 0 -> {
                        System.out.println("Abandonaste la partida.");
                        System.out.println();
                        return;
                    }
                    default -> System.out.println("  Opción inválida.");
                }
            } catch (PersistenciaException e) {
                throw e;
            } catch (JuegoException e) {
                System.out.println("  [!] " + e.getMessage());
            }

            System.out.println();
        }
    }

    private static void mostrarReglas() {
        // Solo texto: no toca la partida ni la base.
        System.out.println();
        System.out.println("============================================================");
        System.out.println("  REGLAS DEL JUEGO");
        System.out.println("============================================================");
        System.out.println();
        System.out.println("Objetivo");
        System.out.println("  Baja los 3 pisos de la mazmorra y derrota al jefe dragón.");
        System.out.println("  Si tu vida llega a 0, pierdes la partida.");
        System.out.println();
        System.out.println("La mazmorra");
        System.out.println("  Piso 1: Esqueleto Oxidado y Fantasma del Pasillo.");
        System.out.println("  Piso 2: Orco Guardian.");
        System.out.println("  Piso 3: Jefe Dragón.");
        System.out.println("  Solo se baja de piso cuando no queda ninguna criatura viva.");
        System.out.println();
        System.out.println("Tu aventurero");
        System.out.println("  Arranca con " + VIDA_INICIAL + " de vida, " + ATAQUE_INICIAL
                + " de ataque y 50 de oro, en el piso 1.");
        System.out.println();
        System.out.println("Acciones");
        System.out.println("  1) Atacar físico: " + ATAQUE_INICIAL
                + " de daño; 12 al Esqueleto y 10 al Fantasma.");
        System.out.println("  2) Atacar con magia: también " + ATAQUE_INICIAL
                + ", pero solo 10 contra el Orco;");
        System.out.println("     ignora la resistencia del esqueleto y el cuerpo del fantasma.");
        System.out.println("  3) Huir: sales del combate y la criatura queda atrás, sin piso");
        System.out.println("     bloqueado y sin recompensa; pierdes hasta " + JuegoService.COSTO_HUIDA
                + " de oro y te golpea al escapar.");
        System.out.println("  4) Poción: cura hasta " + JuegoService.TAMANO_POCION
                + " de vida; cuesta " + JuegoService.COSTO_POCION + " si curas todo");
        System.out.println("     y menos si te falta menos. La criatura golpea mientras te curas.");
        System.out.println("  5) Avanzar de piso: exige el piso despejado.");
        System.out.println("  6) Estado: vida, oro y piso actual.");
        System.out.println();
        System.out.println("Criaturas");
        System.out.println("  Esqueleto: su resistencia recorta el daño físico.");
        System.out.println("  Fantasma: incorpóreo, recibe la mitad del daño físico.");
        System.out.println("  Orco: resiste el " + RESISTENCIA_MAGICA_ORCO
                + "% del daño mágico y, si sobrevive al golpe,");
        System.out.println("  contraataca con " + DANIO_ORCO + " de daño.");
        System.out.println("  Jefe Dragón: no se puede huir. Tiene " + FASES_DRAGON
                + " fases que avanza a medida");
        System.out.println("  que pierde vida, y desde la fase 2 se enciende el aliento de fuego.");
        System.out.println();
        System.out.println("Atacar, curarte o huir le dan a la criatura viva la oportunidad de responder.");
        System.out.println();
        System.out.println("Oro");
        System.out.println("  Cada criatura derrotada paga 5 de oro por nivel de amenaza.");
        System.out.println("  Sin oro no se puede comprar la poción.");
    }

    private void imprimirPiso(Aventurero aventurero, List<Criatura> vivas) {
        System.out.println("--- Piso " + aventurero.getPisoActual() + " ---");
        System.out.println("  " + aventurero.getNombre() + ": vida "
                + aventurero.getVida() + "/" + aventurero.getVidaMaxima()
                + " (" + aventurero.getPorcentajeVida() + "%), oro " + aventurero.getOro());

        for (int i = 0; i < vivas.size(); i++) {
            Criatura criatura = vivas.get(i);
            StringBuilder linea = new StringBuilder();
            linea.append("  ").append(i + 1).append(") ").append(criatura.getNombre())
                    .append(" (").append(criatura.getVida()).append("/")
                    .append(criatura.getVidaMaxima())
                    .append(", amenaza ").append(criatura.getNivelAmenaza());
            if (criatura instanceof DragonJefe dragon) {
                linea.append(", fase ").append(dragon.getFaseActual())
                        .append("/").append(dragon.getCantidadFases());
            }
            linea.append(")");
            if (criatura instanceof DragonJefe dragon && dragon.isAlientoDeFuego()) {
                linea.append(" (aliento de fuego)");
            }
            System.out.println(linea);
        }
    }

    private void imprimirEstadoAventurero(Aventurero aventurero) {
        System.out.println("  " + aventurero.getNombre() + ": vida "
                + aventurero.getVida() + "/" + aventurero.getVidaMaxima()
                + " (" + aventurero.getPorcentajeVida() + "%), oro " + aventurero.getOro()
                + ", piso " + aventurero.getPisoActual());
    }

    private void atacar(Aventurero aventurero, List<Criatura> vivas,
            TipoAtaque tipo, String nombreTipo) throws JuegoException {
        Criatura objetivo = elegirCriatura(vivas);
        if (objetivo == null) {
            return;
        }

        int vidaAntes = aventurero.getVida();
        int oroAntes = aventurero.getOro();
        servicio.resolverTurno(aventurero, objetivo, tipo);
        Aventurero actualizado = servicio.obtenerEstadoAventurero(aventurero.getId());

        System.out.println("Ataque " + nombreTipo + " a " + objetivo.getNombre()
                + ": vida " + objetivo.getVida() + "/" + objetivo.getVidaMaxima() + ".");

        if (objetivo.estaDerrotado()) {
            System.out.println("Derrotaste a " + objetivo.getNombre()
                    + ". Recompensa: +" + (actualizado.getOro() - oroAntes) + " de oro.");
            if (objetivo instanceof DragonJefe) {
                System.out.println("El dragón cae: la mazmorra está limpia.");
            }
            return;
        }

        if (objetivo instanceof Orco orco) {
            System.out.println(orco.getNombre() + " contraataca: recibiste "
                    + (vidaAntes - actualizado.getVida()) + " de daño.");
        } else {
            String golpe = objetivo instanceof DragonJefe dragon && dragon.isAlientoDeFuego()
                    ? " exhala fuego: recibiste "
                    : " ataca: recibiste ";
            System.out.println(objetivo.getNombre() + golpe
                    + (vidaAntes - actualizado.getVida()) + " de daño.");
        }

        System.out.println("Vida: " + actualizado.getVida() + "/"
                + actualizado.getVidaMaxima() + ".");
    }

    private Criatura elegirCriatura(List<Criatura> vivas) {
        if (vivas.isEmpty()) {
            System.out.println("  No hay criaturas vivas.");
            return null;
        }

        System.out.println("Elige un objetivo: ");
        for (int i = 0; i < vivas.size(); i++) {
            Criatura criatura = vivas.get(i);
            System.out.println("  " + (i + 1) + ") " + criatura.getNombre()
                    + " (" + criatura.getVida() + "/" + criatura.getVidaMaxima() + ")");
        }

        String linea = leerLinea();
        if (linea == null) {
            return null;
        }

        int indice;
        try {
            indice = Integer.parseInt(linea.trim());
        } catch (NumberFormatException e) {
            System.out.println("  Número inválido.");
            return null;
        }

        if (indice < 1 || indice > vivas.size()) {
            System.out.println("  Número inválido.");
            return null;
        }
        return vivas.get(indice - 1);
    }

    private String leerLinea() {
        if (!scanner.hasNextLine()) {
            System.out.println();
            return null;
        }
        return scanner.nextLine();
    }
}
