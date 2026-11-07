package com.mazmorra.ui;

import com.mazmorra.Reglas;
import com.mazmorra.exception.JuegoException;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Partida;
import com.mazmorra.model.Personaje;
import com.mazmorra.model.TipoAtaque;
import com.mazmorra.model.TipoCriatura;
import com.mazmorra.service.Escenario;
import com.mazmorra.service.JuegoService;
import com.mazmorra.service.InicioPartida;
import com.mazmorra.service.ResultadoPocion;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class PanelJuego extends JPanel {

    private static final int COLUMNAS = 18;
    private static final int FILAS = 12;
    private static final int TILE = 48;
    private static final int ANCHO_TABLERO = COLUMNAS * TILE;
    private static final int ALTO_TABLERO = FILAS * TILE;
    private static final int ALTO_HUD = 100;

    private static final int JUGADOR_COL = 9;
    private static final int JUGADOR_FILA = 10;
    private static final int ESCALERA_COL = 9;
    private static final int ESCALERA_FILA = 1;

    private static final long DURACION_MOVIMIENTO = 120;
    private static final long DURACION_GOLPE = 180;
    private static final long DURACION_NUMERO = 900;
    private static final long DURACION_PROYECTIL = 240;
    private static final long BLOQUEO_TURNO = 170;

    private static final Color FONDO = new Color(0x0E0C16);
    private static final Color PANEL = new Color(0x14121F);
    private static final Color BORDE = new Color(0x2C2640);
    private static final Color TEXTO = new Color(0xE8E4F0);
    private static final Color TEXTO_SUAVE = new Color(0x9A93B0);
    private static final Color SANGRE = new Color(0xC0392B);
    private static final Color CURA = new Color(0x58C46A);
    private static final Color MAGIA = new Color(0x7FB2FF);
    private static final Color[] COLORES_RONDA = {
        new Color(0xE0B341), new Color(0x58C46A), new Color(0x7FB2FF),
        new Color(0xC77DFF), new Color(0xE67E22), new Color(0xE85D75)
    };

    private static final int[] TORCHAS_COL = {3, 14, 0, 17, 0, 17, 3, 14};
    private static final int[] TORCHAS_FILA = {0, 0, 4, 4, 8, 8, 11, 11};

    private final JuegoService servicio;
    private final Timer temporizador;
    private final List<CriaturaTablero> criaturas = new ArrayList<>();
    private final List<NumeroFlotante> numeros = new ArrayList<>();
    private final List<MuerteVisual> muertes = new ArrayList<>();
    private final List<Proyectil> proyectiles = new ArrayList<>();
    private final Map<Integer, Color> tonosSuelo = new HashMap<>();
    private final Random azar = new Random();

    private Aventurero aventurero;
    private Personaje personajeActual;
    private final List<Personaje> ranking = new ArrayList<>();
    private int puntaje;
    private boolean resultadoRegistrado;
    private int piso = Reglas.PISO_INICIAL;
    private int jugadorCol = JUGADOR_COL;
    private int jugadorFila = JUGADOR_FILA;

    private double jugadorDibujoX = JUGADOR_COL;
    private double jugadorDibujoY = JUGADOR_FILA;
    private double jugadorDesdeX;
    private double jugadorDesdeY;
    private double jugadorHastaX;
    private double jugadorHastaY;
    private long jugadorMovInicio;
    private boolean jugadorMoviendose;
    private long jugadorGolpeHasta;
    private long jugadorLungeHasta;
    private int jugadorLungeDX;
    private int jugadorLungeDY;
    private int miradaX = 0;
    private int miradaY = 1;
    private long bloqueoHasta;
    private boolean mostrandoInicio = true;
    private boolean victoria;
    private boolean derrota;
    private boolean hayPartidaGuardada;
    private boolean partidaVerificada;
    private int turnos;
    private int idGuardia = -1;
    private final List<Particula> particulas = new ArrayList<>();
    private final List<LenguaFuego> fuegos = new ArrayList<>();
    private long sacudidaHasta;
    private int sacudidaFuerza;

    private int tableroX;
    private int tableroY;

    public PanelJuego(JuegoService servicio) {
        this.servicio = servicio;
        setPreferredSize(new java.awt.Dimension(1000, 670));
        setBackground(FONDO);
        setFocusable(true);
        setDoubleBuffered(true);
        prepararTonos();
        conectarTeclas();
        temporizador = new Timer(16, e -> {
            actualizarAnimaciones();
            repaint();
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        verificarPartidaGuardada();
        if (!temporizador.isRunning()) {
            temporizador.start();
        }
        requestFocusInWindow();
    }

    private void verificarPartidaGuardada() {
        if (partidaVerificada) {
            return;
        }
        partidaVerificada = true;
        try {
            hayPartidaGuardada = false;
            for (Aventurero guardado : servicio.listarAventureros()) {
                if (guardado.getPersonajeId() > 0 && !guardado.estaDerrotado()) {
                    hayPartidaGuardada = true;
                    break;
                }
            }
        } catch (PersistenciaException e) {
            hayPartidaGuardada = false;
        }
    }

    private void prepararTonos() {
        Color[] base = {
            new Color(0x2A2438), new Color(0x262133), new Color(0x2E2840),
            new Color(0x241F30), new Color(0x2C2639)
        };
        for (int f = 0; f < FILAS; f++) {
            for (int c = 0; c < COLUMNAS; c++) {
                tonosSuelo.put(f * COLUMNAS + c, base[Math.abs(azar.nextInt()) % base.length]);
            }
        }
    }

    private void conectarTeclas() {
        atajo(KeyEvent.VK_A, "izquierda", () -> mover(-1, 0));
        atajo(KeyEvent.VK_D, "derecha", () -> mover(1, 0));
        atajo(KeyEvent.VK_W, "arriba", () -> mover(0, -1));
        atajo(KeyEvent.VK_S, "abajo", () -> mover(0, 1));
        atajo(KeyEvent.VK_1, "fisico", this::atacarFisico);
        atajo(KeyEvent.VK_2, "magia", this::atacarMagico);
        atajo(KeyEvent.VK_P, "pocion", this::usarPocion);
        atajo(KeyEvent.VK_H, "huir", this::huir);
        atajo(KeyEvent.VK_R, "reiniciar", this::reiniciar);
        atajo(KeyEvent.VK_C, "continuar", this::continuarPartida);
        atajo(KeyEvent.VK_N, "nuevo", this::nuevoDesdeFinal);
        atajo(KeyEvent.VK_ENTER, "empezar", this::empezarDesdeInicio);
    }

    private void atajo(int tecla, String nombre, Runnable accion) {
        InputMap entrada = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap acciones = getActionMap();
        entrada.put(KeyStroke.getKeyStroke(tecla, 0), nombre);
        acciones.put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isShowing()) {
                    return;
                }
                accion.run();
            }
        });
    }

    private void empezarDesdeInicio() {
        if (mostrandoInicio) {
            pedirNombreEIniciar();
        }
    }

    private void reiniciar() {
        if (!bloqueoVencido()) {
            return;
        }
        if (victoria) {
            siguienteRonda();
            return;
        }
        if (derrota) {
            return;
        }
        pedirNombreEIniciar();
    }

    private void nuevoDesdeFinal() {
        if ((victoria || derrota) && bloqueoVencido()) {
            pedirNombreEIniciar();
        }
    }

    private void pedirNombreEIniciar() {
        if (!bloqueoVencido()) {
            return;
        }
        if ((victoria || derrota) && !resultadoRegistrado) {
            finalizarPartida(derrota ? "Derrota" : "Victoria");
            if (!resultadoRegistrado) {
                return;
            }
        }
        if (hayPartidaGuardada) {
            int opcion = JOptionPane.showConfirmDialog(this,
                    "Se va a reemplazar la partida guardada. ¿Empezar con un personaje nuevo?",
                    "Personaje nuevo", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }
        }
        String nombre = (String) JOptionPane.showInputDialog(this,
                "Nombre del personaje:", "Personaje nuevo",
                JOptionPane.QUESTION_MESSAGE, null, null, "");
        if (nombre == null) {
            return;
        }
        nombre = nombre.trim();
        if (nombre.isEmpty()) {
            DialogosError.mostrar(this,
                    new IllegalArgumentException("El nombre no puede estar vacío."));
            return;
        }
        if (nombre.length() > 50) {
            DialogosError.mostrar(this,
                    new IllegalArgumentException("El nombre no puede superar los 50 caracteres."));
            return;
        }

        try {
            InicioPartida inicio = servicio.iniciarPartidaNueva(nombre, 1, Escenario.crear(1));
            personajeActual = inicio.personaje();
            aventurero = inicio.aventurero();
            aplicarEstadoInicialDePartida();
            cargarPiso();
            hayPartidaGuardada = true;
            bloqueoHasta = System.currentTimeMillis() + BLOQUEO_TURNO;
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
        }
    }

    private void continuarPartida() {
        if (!mostrandoInicio || !hayPartidaGuardada || !bloqueoVencido()) {
            return;
        }
        try {
            List<Aventurero> lista = servicio.listarAventureros();
            if (lista.isEmpty()) {
                hayPartidaGuardada = false;
                return;
            }
            Aventurero ultimo = lista.get(0);
            for (Aventurero candidato : lista) {
                if (candidato.getId() > ultimo.getId()) {
                    ultimo = candidato;
                }
            }
            if (ultimo.getPersonajeId() <= 0) {
                DialogosError.mostrar(this, new IllegalStateException(
                        "La partida guardada es anterior al registro de personajes. "
                        + "Empezá una nueva."));
                return;
            }
            personajeActual = servicio.obtenerPersonaje(ultimo.getPersonajeId());
            aventurero = servicio.obtenerEstadoAventurero(ultimo.getId());
            piso = aventurero.getPisoActual();
            mostrandoInicio = false;
            victoria = false;
            derrota = false;
            turnos = 0;
            puntaje = 0;
            resultadoRegistrado = false;
            ranking.clear();
            reiniciarEfectos();
            if (aventurero.estaDerrotado()) {
                derrota = true;
                finalizarPartida("Derrota");
            } else if (servicio.esVictoria(aventurero.getId())) {
                victoria = true;
                finalizarPartida("Victoria");
            } else {
                cargarPiso();
            }
            bloqueoHasta = System.currentTimeMillis() + BLOQUEO_TURNO;
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
        }
    }

    private void aplicarEstadoInicialDePartida() {
        piso = aventurero.getPisoActual();
        victoria = false;
        derrota = false;
        mostrandoInicio = false;
        turnos = 0;
        puntaje = 0;
        resultadoRegistrado = false;
        ranking.clear();
        reiniciarEfectos();
    }

    private void siguienteRonda() {
        if (!victoria) {
            return;
        }
        if (personajeActual == null || aventurero == null) {
            return;
        }
        if (!resultadoRegistrado) {
            finalizarPartida(derrota ? "Derrota" : "Victoria");
            if (!resultadoRegistrado) {
                return;
            }
        }
        int siguiente = aventurero.getRonda() + 1;
        try {
            Aventurero nuevo = servicio.iniciarPartida(personajeActual, siguiente,
                    Escenario.crear(siguiente));
            aventurero = nuevo;
            aplicarEstadoInicialDePartida();
            cargarPiso();
            hayPartidaGuardada = true;
            bloqueoHasta = System.currentTimeMillis() + BLOQUEO_TURNO;
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
        }
    }

    private void finalizarPartida(String resultado) {
        if (resultadoRegistrado || personajeActual == null || aventurero == null) {
            return;
        }
        try {
            List<Partida> partidas = servicio.listarPartidas(personajeActual.getId());
            Partida anterior = null;
            for (Partida partida : partidas) {
                if (partida.ronda() == aventurero.getRonda()
                        && partida.resultado().equalsIgnoreCase(resultado)) {
                    anterior = partida;
                    break;
                }
            }
            if (anterior != null) {
                puntaje = anterior.puntos();
                resultadoRegistrado = true;
            } else {
                puntaje = servicio.registrarResultado(personajeActual, aventurero, resultado);
                resultadoRegistrado = true;
            }
            if ("Derrota".equalsIgnoreCase(resultado)) {
                hayPartidaGuardada = false;
            }
            personajeActual = servicio.obtenerPersonaje(personajeActual.getId());
            ranking.clear();
            ranking.addAll(servicio.listarRanking(Reglas.RANKING_LIMITE));
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
            if (personajeActual != null) {
                try {
                    personajeActual = servicio.obtenerPersonaje(personajeActual.getId());
                } catch (JuegoException ex) {
                    DialogosError.mostrar(this, ex);
                }
            }
            try {
                ranking.clear();
                ranking.addAll(servicio.listarRanking(Reglas.RANKING_LIMITE));
            } catch (JuegoException ex) {
                DialogosError.mostrar(this, ex);
            }
        }
    }

    private int puntosProyectados() {
        if (aventurero == null) {
            return 0;
        }
        return aventurero.getRonda() * Reglas.PUNTOS_RONDA
                + piso * Reglas.PUNTOS_PISO
                + aventurero.getOro()
                + aventurero.getEnemigosDerrotados() * Reglas.PUNTOS_ENEMIGO;
    }

    private Color colorRonda() {
        int ronda = aventurero == null ? 1 : aventurero.getRonda();
        return COLORES_RONDA[Math.floorMod(ronda - 1, COLORES_RONDA.length)];
    }

    public void recargarTablero() {
        if (mostrandoInicio || victoria || derrota || aventurero == null) {
            return;
        }
        try {
            aventurero = servicio.obtenerEstadoAventurero(aventurero.getId());
            piso = aventurero.getPisoActual();
            cargarPiso();
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
        }
    }

    private void recargarAventurero() {
        if (aventurero == null) {
            return;
        }
        try {
            aventurero = servicio.obtenerEstadoAventurero(aventurero.getId());
            piso = aventurero.getPisoActual();
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
        }
    }

    private void mover(int dx, int dy) {
        if (!partidaActiva()) {
            return;
        }

        int destinoCol = jugadorCol + dx;
        int destinoFila = jugadorFila + dy;
        if (esPared(destinoCol, destinoFila)) {
            return;
        }

        CriaturaTablero enemigo = criaturaEn(destinoCol, destinoFila);
        if (enemigo != null) {
            return;
        }

        miradaX = dx;
        miradaY = dy;

        boolean haciaEscalera = destinoCol == ESCALERA_COL && destinoFila == ESCALERA_FILA;
        if (haciaEscalera && pisoDespejado() && piso < Reglas.PISO_FINAL) {
            bajarPiso();
            return;
        }

        jugadorDesdeX = jugadorDibujoX;
        jugadorDesdeY = jugadorDibujoY;
        jugadorHastaX = destinoCol;
        jugadorHastaY = destinoFila;
        jugadorMovInicio = System.currentTimeMillis();
        jugadorMoviendose = true;
        jugadorCol = destinoCol;
        jugadorFila = destinoFila;
        bloqueoHasta = System.currentTimeMillis() + BLOQUEO_TURNO;
        turnos++;

        if (haciaEscalera && pisoDespejado()) {
            bajarPiso();
            return;
        }

        turnoEnemigos(-1);
        if (aventurero.estaDerrotado()) {
            derrota = true;
            finalizarPartida("Derrota");
        }
    }

    private void atacarFisico() {
        CriaturaTablero enemigo = criaturaAdyacente();
        if (enemigo == null) {
            return;
        }
        atacar(enemigo, TipoAtaque.FISICO);
    }

    private void atacarMagico() {
        CriaturaTablero enemigo = criaturaAdyacente();
        if (enemigo == null) {
            return;
        }
        atacar(enemigo, TipoAtaque.MAGICO);
    }

    private void atacar(CriaturaTablero enemigo, TipoAtaque tipo) {
        if (!partidaActiva()) {
            return;
        }

        long ahora = System.currentTimeMillis();
        int vidaJugadorAntes = aventurero.getVida();
        int vidaEnemigoAntes = enemigo.criatura.getVida();

        try {
            servicio.resolverTurno(aventurero, enemigo.criatura, tipo);
            aventurero = servicio.obtenerEstadoAventurero(aventurero.getId());
            ahora = System.currentTimeMillis();

            int danioHecho = Math.max(0, vidaEnemigoAntes - enemigo.criatura.getVida());
            enemigo.golpeHasta = ahora + DURACION_GOLPE;
            enemigo.lungeHasta = ahora + DURACION_GOLPE;
            enemigo.lungeDX = Integer.signum(enemigo.col - jugadorCol);
            enemigo.lungeDY = Integer.signum(enemigo.fila - jugadorFila);

            lanzarGolpeJugador(enemigo);
            if (tipo == TipoAtaque.MAGICO) {
                proyectiles.add(new Proyectil(jugadorCol, jugadorFila, enemigo.col, enemigo.fila,
                        ahora, ahora + DURACION_PROYECTIL));
                agregarNumero(enemigo.col, enemigo.fila, "-" + danioHecho, MAGIA);
            } else {
                agregarNumero(enemigo.col, enemigo.fila, "-" + danioHecho, TEXTO);
            }
            explotar(enemigo.col, enemigo.fila,
                    tipo == TipoAtaque.MAGICO ? MAGIA : new Color(0xE9DFC3), 9);
            sacudir(3);

            int danioRecibido = Math.max(0, vidaJugadorAntes - aventurero.getVida());
            if (danioRecibido > 0) {
                jugadorGolpeHasta = ahora + DURACION_GOLPE;
                agregarNumero(jugadorCol, jugadorFila, "-" + danioRecibido, SANGRE);
                if (enemigo.criatura instanceof DragonJefe dragonHumo
                        && dragonHumo.isAlientoDeFuego()) {
                    lanzarFuego(enemigo.col, enemigo.fila, jugadorCol, jugadorFila);
                } else {
                    explotar(jugadorCol, jugadorFila, SANGRE, 8);
                }
                sacudir(4);
            }

            if (enemigo.criatura.estaDerrotado()) {
                long duracion = duracionMuerte(enemigo.criatura);
                muertes.add(new MuerteVisual(enemigo.criatura, enemigo.col, enemigo.fila,
                        ahora, ahora + duracion));
                criaturas.remove(enemigo);
                explotarMuerte(enemigo.criatura, enemigo.col, enemigo.fila);
                sacudir(5);
                victoria = servicio.esVictoria(aventurero.getId());
            }

            turnos++;
            if (victoria) {
                finalizarPartida("Victoria");
            } else if (aventurero.estaDerrotado()) {
                derrota = true;
                finalizarPartida("Derrota");
            } else {
                turnoEnemigos(enemigo.criatura.getId());
                if (aventurero.estaDerrotado()) {
                    derrota = true;
                    finalizarPartida("Derrota");
                }
            }
            bloqueoHasta = System.currentTimeMillis() + BLOQUEO_TURNO;
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
            if (e instanceof PersistenciaException) {
                recargarTablero();
            }
        }
    }

    private void usarPocion() {
        if (!partidaActiva()) {
            return;
        }

        long ahora = System.currentTimeMillis();
        try {
            CriaturaTablero cercano = criaturaAdyacente();
            ResultadoPocion resultado = servicio.usarPocion(aventurero, Reglas.TAMANO_POCION,
                    cercano == null ? null : cercano.criatura);
            aventurero = servicio.obtenerEstadoAventurero(aventurero.getId());
            ahora = System.currentTimeMillis();

            if (resultado.vidaCurada() > 0) {
                agregarNumero(jugadorCol, jugadorFila, "+" + resultado.vidaCurada(), CURA);
                explotar(jugadorCol, jugadorFila, CURA, 8);
            }
            if (resultado.danioRecibido() > 0) {
                jugadorGolpeHasta = ahora + DURACION_GOLPE;
                agregarNumero(jugadorCol, jugadorFila, "-" + resultado.danioRecibido(), SANGRE);
                explotar(jugadorCol, jugadorFila, SANGRE, 6);
                sacudir(3);
            }

            turnos++;
            if (aventurero.estaDerrotado()) {
                derrota = true;
                finalizarPartida("Derrota");
            }
            bloqueoHasta = System.currentTimeMillis() + BLOQUEO_TURNO;
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
            if (e instanceof PersistenciaException) {
                recargarTablero();
            }
        }
    }

    private void huir() {
        CriaturaTablero enemigo = criaturaAdyacente();
        if (enemigo == null) {
            return;
        }
        if (!partidaActiva()) {
            return;
        }

        long ahora = System.currentTimeMillis();
        int vidaAntes = aventurero.getVida();
        try {
            servicio.huirDeCombate(aventurero, enemigo.criatura);
            aventurero = servicio.obtenerEstadoAventurero(aventurero.getId());
            ahora = System.currentTimeMillis();

            int vidaPerdida = Math.max(0, vidaAntes - aventurero.getVida());
            criaturas.remove(enemigo);
            explotar(enemigo.col, enemigo.fila, new Color(0x5A4A6E), 10);
            if (vidaPerdida > 0) {
                jugadorGolpeHasta = ahora + DURACION_GOLPE;
                agregarNumero(jugadorCol, jugadorFila, "-" + vidaPerdida, SANGRE);
                explotar(jugadorCol, jugadorFila, SANGRE, 6);
                sacudir(3);
            }
            turnos++;
            if (aventurero.estaDerrotado()) {
                derrota = true;
                finalizarPartida("Derrota");
            } else {
                turnoEnemigos(enemigo.criatura.getId());
                if (aventurero.estaDerrotado()) {
                    derrota = true;
                    finalizarPartida("Derrota");
                }
            }
            bloqueoHasta = System.currentTimeMillis() + BLOQUEO_TURNO;
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
            if (e instanceof PersistenciaException) {
                recargarTablero();
            }
        }
    }

    private void bajarPiso() {
        if (piso >= Reglas.PISO_FINAL) {
            try {
                victoria = servicio.esVictoria(aventurero.getId());
            } catch (PersistenciaException e) {
                DialogosError.mostrar(this, e);
            }
            return;
        }

        try {
            servicio.avanzarPiso(aventurero);
            aventurero = servicio.obtenerEstadoAventurero(aventurero.getId());
            piso = aventurero.getPisoActual();
            jugadorCol = JUGADOR_COL;
            jugadorFila = JUGADOR_FILA;
            jugadorDibujoX = JUGADOR_COL;
            jugadorDibujoY = JUGADOR_FILA;
            jugadorMoviendose = false;
            reiniciarEfectos();
            cargarPiso();
            bloqueoHasta = System.currentTimeMillis() + BLOQUEO_TURNO;
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
            if (e instanceof PersistenciaException) {
                recargarTablero();
            }
        }
    }

    private void turnoEnemigos(int exceptoId) {
        List<CriaturaTablero> orden = new ArrayList<>(criaturas);
        java.util.Collections.shuffle(orden, azar);

        CriaturaTablero guardia = buscarCriatura(idGuardia);
        boolean guardiaNuevo = false;
        if (guardia == null) {
            idGuardia = -1;
            for (CriaturaTablero enemigo : orden) {
                if (adyacente(enemigo)) {
                    guardia = enemigo;
                    break;
                }
            }
            if (guardia != null) {
                idGuardia = guardia.criatura.getId();
                guardiaNuevo = true;
            }
        }

        for (CriaturaTablero enemigo : orden) {
            if (aventurero.estaDerrotado()) {
                break;
            }

            boolean esGuardia = guardia != null
                    && enemigo.criatura.getId() == guardia.criatura.getId();
            if (enemigo.criatura.getId() == exceptoId) {
                continue;
            }

            long ahora = System.currentTimeMillis();
            if (esGuardia) {
                if (adyacente(enemigo)) {
                    if (!guardiaNuevo) {
                        atacarConEnemigo(enemigo);
                    }
                } else {
                    moverEnemigoHacia(enemigo, ahora, false);
                }
            } else if (adyacente(enemigo)) {
                retroceder(enemigo, ahora);
            } else if (guardia != null) {
                if (distancia(enemigo.col, enemigo.fila, jugadorCol, jugadorFila) <= 3) {
                    retroceder(enemigo, ahora);
                } else {
                    moverEnemigoHacia(enemigo, ahora, true);
                }
            } else {
                moverEnemigoHacia(enemigo, ahora, false);
                if (adyacente(enemigo)) {
                    guardia = enemigo;
                    idGuardia = enemigo.criatura.getId();
                    guardiaNuevo = true;
                }
            }
        }

        if (aventurero.estaDerrotado()) {
            derrota = true;
        }
    }

    private CriaturaTablero buscarCriatura(int id) {
        for (CriaturaTablero criatura : criaturas) {
            if (criatura.criatura.getId() == id) {
                return criatura;
            }
        }
        return null;
    }

    private void atacarConEnemigo(CriaturaTablero enemigo) {
        try {
            int danio = servicio.resolverAtaqueEnemigo(aventurero, enemigo.criatura);
            aventurero = servicio.obtenerEstadoAventurero(aventurero.getId());
            long ahora = System.currentTimeMillis();
            if (danio > 0) {
                jugadorGolpeHasta = ahora + DURACION_GOLPE;
                agregarNumero(jugadorCol, jugadorFila, "-" + danio, SANGRE);
                if (enemigo.criatura instanceof DragonJefe dragonAliento
                        && dragonAliento.isAlientoDeFuego()) {
                    lanzarFuego(enemigo.col, enemigo.fila, jugadorCol, jugadorFila);
                } else {
                    explotar(jugadorCol, jugadorFila, SANGRE, 7);
                }
                sacudir(3);
                enemigo.lungeHasta = ahora + DURACION_GOLPE;
                enemigo.lungeDX = Integer.signum(jugadorCol - enemigo.col);
                enemigo.lungeDY = Integer.signum(jugadorFila - enemigo.fila);
            }
        } catch (JuegoException e) {
            DialogosError.mostrar(this, e);
            if (e instanceof PersistenciaException) {
                recargarAventurero();
            }
        }
    }

    private boolean adyacente(CriaturaTablero criatura) {
        return distancia(criatura.col, criatura.fila, jugadorCol, jugadorFila) == 1;
    }

    private void retroceder(CriaturaTablero enemigo, long ahora) {
        int distanciaActual = distancia(enemigo.col, enemigo.fila, jugadorCol, jugadorFila);
        int mejorCol = enemigo.col;
        int mejorFila = enemigo.fila;
        int mejorDistancia = distanciaActual;

        int[][] direcciones = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] paso : direcciones) {
            int nuevaCol = enemigo.col + paso[0];
            int nuevaFila = enemigo.fila + paso[1];
            if (esPared(nuevaCol, nuevaFila) || (nuevaCol == jugadorCol
                    && nuevaFila == jugadorFila)) {
                continue;
            }
            if (criaturaEn(nuevaCol, nuevaFila) != null) {
                continue;
            }
            int distancia = distancia(nuevaCol, nuevaFila, jugadorCol, jugadorFila);
            if (distancia > mejorDistancia) {
                mejorDistancia = distancia;
                mejorCol = nuevaCol;
                mejorFila = nuevaFila;
            }
        }

        if (mejorDistancia >= 4 || mejorDistancia > distanciaActual) {
            enemigo.desdeX = enemigo.dibujoX;
            enemigo.desdeY = enemigo.dibujoY;
            enemigo.hastaX = mejorCol;
            enemigo.hastaY = mejorFila;
            enemigo.movInicio = ahora;
            enemigo.moviendose = true;
            enemigo.col = mejorCol;
            enemigo.fila = mejorFila;
        }
    }

    private void moverEnemigoHacia(CriaturaTablero enemigo, long ahora, boolean mantenerDistancia) {
        int[] paso = buscarPaso(enemigo.col, enemigo.fila);
        if (paso == null) {
            return;
        }
        if (mantenerDistancia
                && distancia(paso[0], paso[1], jugadorCol, jugadorFila) < 4) {
            return;
        }

        enemigo.desdeX = enemigo.dibujoX;
        enemigo.desdeY = enemigo.dibujoY;
        enemigo.hastaX = paso[0];
        enemigo.hastaY = paso[1];
        enemigo.movInicio = ahora;
        enemigo.moviendose = true;
        enemigo.col = paso[0];
        enemigo.fila = paso[1];
    }

    private int[] buscarPaso(int origenCol, int origenFila) {
        if (distancia(origenCol, origenFila, jugadorCol, jugadorFila) == 1) {
            return null;
        }

        int total = COLUMNAS * FILAS;
        int origen = origenFila * COLUMNAS + origenCol;
        int[] anterior = new int[total];
        java.util.Arrays.fill(anterior, -2);
        anterior[origen] = -1;

        java.util.ArrayDeque<Integer> cola = new java.util.ArrayDeque<>();
        cola.add(origen);

        int[] dc = {1, -1, 0, 0};
        int[] df = {0, 0, 1, -1};

        while (!cola.isEmpty()) {
            int actual = cola.poll();
            int ac = actual % COLUMNAS;
            int af = actual / COLUMNAS;

            if (actual != origen && distancia(ac, af, jugadorCol, jugadorFila) == 1) {
                int nodo = actual;
                while (anterior[nodo] != origen && anterior[nodo] >= 0) {
                    nodo = anterior[nodo];
                }
                return new int[]{nodo % COLUMNAS, nodo / COLUMNAS};
            }

            for (int i = 0; i < 4; i++) {
                int nc = ac + dc[i];
                int nf = af + df[i];
                if (esPared(nc, nf)) {
                    continue;
                }
                int vecino = nf * COLUMNAS + nc;
                if (anterior[vecino] != -2) {
                    continue;
                }
                if (nc == jugadorCol && nf == jugadorFila) {
                    continue;
                }
                if (criaturaEn(nc, nf) != null) {
                    continue;
                }
                anterior[vecino] = actual;
                cola.add(vecino);
            }
        }
        return null;
    }

    private void cargarPiso() throws PersistenciaException {
        criaturas.clear();
        idGuardia = -1;
        List<Criatura> lista = servicio.listarCriaturasPorPiso(piso);
        Set<Integer> usados = new HashSet<>();
        usados.add(JUGADOR_FILA * COLUMNAS + JUGADOR_COL);
        usados.add(ESCALERA_FILA * COLUMNAS + ESCALERA_COL);
        usados.add(JUGADOR_FILA * COLUMNAS + JUGADOR_COL);

        jugadorCol = JUGADOR_COL;
        jugadorFila = JUGADOR_FILA;
        jugadorDibujoX = JUGADOR_COL;
        jugadorDibujoY = JUGADOR_FILA;
        jugadorMoviendose = false;

        for (Criatura criatura : lista) {
            if (!criatura.estaViva()) {
                continue;
            }
            int[] posicion = posicionInicial(criatura.getTipo(), usados);
            usados.add(posicion[1] * COLUMNAS + posicion[0]);
            criaturas.add(new CriaturaTablero(criatura, posicion[0], posicion[1]));
        }
    }

    private int[] posicionInicial(TipoCriatura tipo, Set<Integer> usados) {
        int[][] candidatas;
        if (piso == 1 && tipo == TipoCriatura.ESQUELETO) {
            candidatas = new int[][]{{6, 4}};
        } else if (piso == 1 && tipo == TipoCriatura.FANTASMA) {
            candidatas = new int[][]{{12, 4}};
        } else if (piso == 2 && tipo == TipoCriatura.ORCO) {
            candidatas = new int[][]{{10, 6}};
        } else if (piso == 3 && tipo == TipoCriatura.DRAGON_JEFE) {
            candidatas = new int[][]{{9, 3}};
        } else {
            candidatas = new int[][]{{4, 3}, {13, 3}, {4, 7}, {13, 7}, {8, 5}, {10, 5}};
        }

        for (int[] candidata : candidatas) {
            int clave = candidata[1] * COLUMNAS + candidata[0];
            if (!usados.contains(clave) && !esPared(candidata[0], candidata[1])) {
                return candidata;
            }
        }

        for (int fila = 1; fila < FILAS - 1; fila++) {
            for (int col = 1; col < COLUMNAS - 1; col++) {
                int clave = fila * COLUMNAS + col;
                if (!usados.contains(clave) && !esPared(col, fila)) {
                    return new int[]{col, fila};
                }
            }
        }
        return new int[]{1, 1};
    }

    private boolean partidaActiva() {
        if (mostrandoInicio || victoria || derrota) {
            return false;
        }
        if (aventurero == null || aventurero.estaDerrotado()) {
            return false;
        }
        return bloqueoVencido();
    }

    private boolean bloqueoVencido() {
        return System.currentTimeMillis() >= bloqueoHasta;
    }

    private boolean pisoDespejado() {
        return criaturas.isEmpty();
    }

    private boolean esPared(int col, int fila) {
        if (col <= 0 || col >= COLUMNAS - 1 || fila <= 0 || fila >= FILAS - 1) {
            return true;
        }
        return (col == 5 && fila == 3) || (col == 12 && fila == 3)
                || (col == 5 && fila == 8) || (col == 12 && fila == 8);
    }

    private CriaturaTablero criaturaEn(int col, int fila) {
        for (CriaturaTablero criatura : criaturas) {
            if (criatura.col == col && criatura.fila == fila) {
                return criatura;
            }
        }
        return null;
    }

    private CriaturaTablero criaturaAdyacente() {
        CriaturaTablero alFrente = criaturaEn(jugadorCol + miradaX, jugadorFila + miradaY);
        if (alFrente != null) {
            return alFrente;
        }
        for (CriaturaTablero criatura : criaturas) {
            if (distancia(criatura.col, criatura.fila, jugadorCol, jugadorFila) == 1) {
                return criatura;
            }
        }
        return null;
    }

    private static int distancia(int c1, int f1, int c2, int f2) {
        return Math.abs(c1 - c2) + Math.abs(f1 - f2);
    }

    private void lanzarGolpeJugador(CriaturaTablero enemigo) {
        jugadorLungeHasta = System.currentTimeMillis() + DURACION_GOLPE;
        jugadorLungeDX = Integer.signum(enemigo.col - jugadorCol);
        jugadorLungeDY = Integer.signum(enemigo.fila - jugadorFila);
    }

    private void agregarNumero(int col, int fila, String texto, Color color) {
        long ahora = System.currentTimeMillis();
        numeros.add(new NumeroFlotante(col, fila, texto, color, ahora, ahora + DURACION_NUMERO));
    }

    private void explotar(int col, int fila, Color color, int cantidad) {
        long ahora = System.currentTimeMillis();
        for (int i = 0; i < cantidad; i++) {
            double angulo = azar.nextDouble() * Math.PI * 2;
            double velocidad = 0.02 + azar.nextDouble() * 0.055;
            particulas.add(new Particula(col, fila,
                    Math.cos(angulo) * velocidad,
                    Math.sin(angulo) * velocidad - 0.018,
                    ahora, ahora + 380 + azar.nextInt(240),
                    color, 2 + azar.nextInt(3)));
        }
    }

    private void sacudir(int fuerza) {
        sacudidaHasta = System.currentTimeMillis() + 150;
        sacudidaFuerza = Math.max(sacudidaFuerza, fuerza);
    }

    private void lanzarFuego(int colOrigen, int filaOrigen, int colDestino, int filaDestino) {
        long ahora = System.currentTimeMillis();
        fuegos.add(new LenguaFuego(colOrigen, filaOrigen, colDestino, filaDestino,
                ahora, ahora + 540));
        explotar(colDestino, filaDestino, new Color(0xE67E22), 12);
        explotar(colDestino, filaDestino, new Color(0xF2C230), 6);
        sacudir(4);
    }

    private void explotarMuerte(Criatura criatura, int col, int fila) {
        Color[] paleta = paletaMuerte(criatura.getTipo());
        long ahora = System.currentTimeMillis();
        int cantidad = criatura instanceof DragonJefe ? 36 : 24;
        for (int i = 0; i < cantidad; i++) {
            double angulo = azar.nextDouble() * Math.PI * 2;
            double velocidad = 0.03 + azar.nextDouble() * 0.1;
            particulas.add(new Particula(col, fila,
                    Math.cos(angulo) * velocidad,
                    Math.sin(angulo) * velocidad - 0.03,
                    ahora, ahora + 480 + azar.nextInt(420),
                    paleta[azar.nextInt(paleta.length)], 2 + azar.nextInt(4)));
        }
    }

    private static Color[] paletaMuerte(TipoCriatura tipo) {
        return switch (tipo) {
            case ESQUELETO -> new Color[]{new Color(0xE8E2CF), new Color(0xCFC7B0),
                new Color(0x9A93A8)};
            case FANTASMA -> new Color[]{new Color(0xDCF5FA), new Color(0xBFE8F2),
                new Color(0x6FB7D6)};
            case ORCO -> new Color[]{new Color(0x5B8C3A), new Color(0x3F6328),
                new Color(0x8D5A3B)};
            case DRAGON_JEFE -> new Color[]{new Color(0xE67E22), new Color(0xF2C230),
                new Color(0xA83232), new Color(0x6E1F1F)};
            case ARANA -> new Color[]{new Color(0x8E5AA8), new Color(0x6B3B84),
                new Color(0x3B2148)};
            case GOLEM -> new Color[]{new Color(0x9AA0A8), new Color(0x6E747C),
                new Color(0x3B3F45)};
        };
    }

    private static long duracionMuerte(Criatura criatura) {
        return criatura instanceof DragonJefe ? 900 : 650;
    }

    private void reiniciarEfectos() {
        numeros.clear();
        muertes.clear();
        proyectiles.clear();
        particulas.clear();
        fuegos.clear();
        jugadorGolpeHasta = 0;
        jugadorLungeHasta = 0;
        sacudidaHasta = 0;
        sacudidaFuerza = 0;
    }

    private void actualizarAnimaciones() {
        long ahora = System.currentTimeMillis();

        if (jugadorMoviendose) {
            double t = progreso(ahora, jugadorMovInicio, DURACION_MOVIMIENTO);
            double suave = t * t * (3 - 2 * t);
            jugadorDibujoX = jugadorDesdeX + (jugadorHastaX - jugadorDesdeX) * suave;
            jugadorDibujoY = jugadorDesdeY + (jugadorHastaY - jugadorDesdeY) * suave;
            if (t >= 1) {
                jugadorMoviendose = false;
                jugadorDibujoX = jugadorHastaX;
                jugadorDibujoY = jugadorHastaY;
            }
        }

        for (CriaturaTablero criatura : criaturas) {
            if (criatura.moviendose) {
                double t = progreso(ahora, criatura.movInicio, DURACION_MOVIMIENTO);
                double suave = t * t * (3 - 2 * t);
                criatura.dibujoX = criatura.desdeX + (criatura.hastaX - criatura.desdeX) * suave;
                criatura.dibujoY = criatura.desdeY + (criatura.hastaY - criatura.desdeY) * suave;
                if (t >= 1) {
                    criatura.moviendose = false;
                    criatura.dibujoX = criatura.hastaX;
                    criatura.dibujoY = criatura.hastaY;
                }
            }
        }

        numeros.removeIf(numero -> ahora > numero.fin);
        muertes.removeIf(muerte -> ahora > muerte.fin);
        proyectiles.removeIf(proyectil -> ahora > proyectil.fin);
        particulas.removeIf(particula -> ahora > particula.fin);
        fuegos.removeIf(fuego -> ahora > fuego.fin);
    }

    private static double progreso(long ahora, long inicio, long duracion) {
        if (ahora <= inicio) {
            return 0;
        }
        if (ahora >= inicio + duracion) {
            return 1;
        }
        return (double) (ahora - inicio) / duracion;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();
        tableroX = (ancho - ANCHO_TABLERO) / 2;
        tableroY = ALTO_HUD + (alto - ALTO_HUD - ALTO_TABLERO) / 2;

        g2.setPaint(new GradientPaint(0, 0, new Color(0x17131F), 0, alto, new Color(0x0A0910)));
        g2.fillRect(0, 0, ancho, alto);

        long ahora = System.currentTimeMillis();
        int sacudidaX = 0;
        int sacudidaY = 0;
        if (ahora < sacudidaHasta) {
            double fuerza = (sacudidaHasta - ahora) / 150.0;
            sacudidaX = (int) Math.round(Math.sin(ahora * 0.13) * sacudidaFuerza * fuerza);
            sacudidaY = (int) Math.round(Math.cos(ahora * 0.17) * sacudidaFuerza * fuerza);
        } else {
            sacudidaFuerza = 0;
        }

        Graphics2D tablero = (Graphics2D) g2.create();
        tablero.translate(sacudidaX, sacudidaY);
        dibujarTablero(tablero);
        dibujarObjetos(tablero);
        dibujarVineta(tablero);
        dibujarEfectos(tablero);
        tablero.dispose();

        dibujarHud(g2, ancho);

        if (mostrandoInicio) {
            dibujarInicio(g2, ancho, alto);
        } else if (victoria) {
            dibujarFinal(g2, ancho, alto, "VICTORIA", "El Jefe Dragón cayó.",
                    "La mazmorra está limpia.");
        } else if (derrota) {
            dibujarFinal(g2, ancho, alto, "DERROTA",
                    (aventurero != null ? aventurero.getNombre() : "El aventurero")
                    + " cayó en el piso " + piso + ".",
                    "La mazmorra se queda con otro héroe.");
        }

        g2.dispose();
    }

    private void dibujarTablero(Graphics2D g2) {
        for (int fila = 0; fila < FILAS; fila++) {
            for (int col = 0; col < COLUMNAS; col++) {
                int x = tableroX + col * TILE;
                int y = tableroY + fila * TILE;
                if (esPared(col, fila)) {
                    dibujarPared(g2, x, y, col, fila);
                } else {
                    dibujarSuelo(g2, x, y, col, fila);
                }
            }
        }

        int escaleraX = tableroX + ESCALERA_COL * TILE;
        int escaleraY = tableroY + ESCALERA_FILA * TILE;
        if (pisoDespejado() && !mostrandoInicio && !victoria) {
            long ahora = System.currentTimeMillis();
            int pulso = (int) (28 + 22 * Math.sin(ahora / 260.0));
            g2.setColor(new Color(242, 194, 48, pulso));
            g2.fillRect(escaleraX, escaleraY, TILE, TILE);
        }
        Sprites.escaleras(g2, escaleraX, escaleraY, 3, pisoDespejado() && !mostrandoInicio);

        if (pisoDespejado() && !mostrandoInicio && !victoria) {
            long ahora = System.currentTimeMillis();
            int vaiven = (int) (4 * Math.sin(ahora / 240.0));
            int cx = escaleraX + TILE / 2;
            int cy = escaleraY - 10 + vaiven;
            g2.setColor(new Color(242, 194, 48, 200));
            g2.fillPolygon(new int[]{cx - 8, cx + 8, cx},
                    new int[]{cy - 7, cy - 7, cy + 3}, 3);
        }

        long ahora = System.currentTimeMillis();
        for (int i = 0; i < TORCHAS_COL.length; i++) {
            int x = tableroX + TORCHAS_COL[i] * TILE;
            int y = tableroY + TORCHAS_FILA[i] * TILE;
            double parpadeo = 0.82 + 0.18 * Math.sin(ahora / 120.0 + i * 1.7);
            int radio = (int) (120 * parpadeo);
            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Double(x + TILE / 2.0, y + TILE / 2.0), radio,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 160, 60, 55), new Color(255, 160, 60, 0)}));
            g2.fill(new Ellipse2D.Double(x + TILE / 2.0 - radio, y + TILE / 2.0 - radio,
                    radio * 2.0, radio * 2.0));
            Sprites.antorcha(g2, x, y, 3);
        }

        g2.setColor(aventurero == null ? new Color(0x000000) : colorRonda().darker());
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(tableroX, tableroY, ANCHO_TABLERO, ALTO_TABLERO);
    }

    private void dibujarSuelo(Graphics2D g2, int x, int y, int col, int fila) {
        Color base = tonosSuelo.getOrDefault(fila * COLUMNAS + col, new Color(0x2A2438));
        g2.setColor(base);
        g2.fillRect(x, y, TILE, TILE);

        g2.setColor(new Color(0x17131F));
        g2.fillRect(x, y, TILE, 2);
        g2.fillRect(x, y, 2, TILE);

        g2.setColor(new Color(255, 255, 255, 12));
        g2.drawLine(x + 3, y + 3, x + TILE - 3, y + 3);
        g2.drawLine(x + 3, y + 3, x + 3, y + TILE - 3);

        g2.setColor(new Color(0, 0, 0, 70));
        g2.drawLine(x + 3, y + TILE - 1, x + TILE - 1, y + TILE - 1);
        g2.drawLine(x + TILE - 1, y + 3, x + TILE - 1, y + TILE - 1);

        int detalle = Math.floorMod(col * 31 + fila * 17, 9);
        if (detalle == 0) {
            g2.setColor(new Color(0, 0, 0, 45));
            g2.fillRect(x + 14, y + 28, 12, 2);
            g2.fillRect(x + 24, y + 30, 2, 6);
        } else if (detalle == 4) {
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillRect(x + 30, y + 12, 8, 2);
        }
    }

    private void dibujarPared(Graphics2D g2, int x, int y, int col, int fila) {
        boolean pilar = (col == 5 || col == 12) && (fila == 3 || fila == 8);
        if (pilar) {
            dibujarPilar(g2, x, y);
            return;
        }

        g2.setColor(new Color(0x16121F));
        g2.fillRect(x, y, TILE, TILE);

        Shape recorte = g2.getClip();
        g2.clipRect(x, y, TILE, TILE);

        Color claro = new Color(0x35304A);
        Color medio = new Color(0x2A2539);
        Color oscuro = new Color(0x221D30);
        int altoLadrillo = 12;

        for (int i = 0; i < 4; i++) {
            int filaGlobal = fila * 4 + i;
            int offset = filaGlobal % 2 == 0 ? 0 : 12;
            for (int bx = -24 + offset; bx < TILE; bx += 24) {
                int indice = Math.floorMod((bx + 48) / 24 + filaGlobal, 3);
                g2.setColor(indice == 0 ? claro : indice == 1 ? medio : oscuro);
                g2.fillRect(x + bx + 1, y + i * altoLadrillo + 1, 22, altoLadrillo - 2);
            }
        }

        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRect(x, y, TILE, 3);
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRect(x, y + TILE - 3, TILE, 3);
        g2.setClip(recorte);
    }

    private void dibujarPilar(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(0x1A1626));
        g2.fillRect(x, y, TILE, TILE);
        g2.setColor(new Color(0x3B3452));
        g2.fillRect(x + 10, y + 6, 28, 36);
        g2.setColor(new Color(0x4C4468));
        g2.fillRect(x + 10, y + 6, 6, 36);
        g2.setColor(new Color(0x2B2540));
        g2.fillRect(x + 32, y + 6, 6, 36);
        g2.setColor(new Color(0x514969));
        g2.fillRect(x + 6, y + 2, 36, 6);
        g2.fillRect(x + 6, y + 40, 36, 6);
        g2.setColor(new Color(0x2A2438));
        g2.fillRect(x + 14, y + 12, 20, 2);
        g2.fillRect(x + 14, y + 34, 20, 2);
    }

    private void dibujarObjetos(Graphics2D g2) {
        List<CriaturaTablero> orden = new ArrayList<>(criaturas);
        orden.sort((a, b) -> Double.compare(a.dibujoY, b.dibujoY));
        for (CriaturaTablero criatura : orden) {
            dibujarCriatura(g2, criatura);
        }
        dibujarJugador(g2);
    }

    private void dibujarJugador(Graphics2D g2) {
        long ahora = System.currentTimeMillis();
        double bob = Math.sin(ahora / 420.0) * 1.6;
        double offsetX = 0;
        double offsetY = 0;
        if (ahora < jugadorLungeHasta) {
            double t = 1 - (jugadorLungeHasta - ahora) / (double) DURACION_GOLPE;
            double empuje = Math.sin(t * Math.PI) * 9;
            offsetX = jugadorLungeDX * empuje;
            offsetY = jugadorLungeDY * empuje;
        }

        int x = (int) (tableroX + jugadorDibujoX * TILE + offsetX);
        int y = (int) (tableroY + jugadorDibujoY * TILE + offsetY + bob);

        Sprites.caballero(g2, x, y, 3);

        g2.setColor(new Color(224, 179, 65, 130));
        if (miradaX != 0) {
            int bx = miradaX > 0 ? x + TILE - 3 : x + 1;
            g2.fillRect(bx, y + TILE / 2 - 5, 2, 10);
        } else {
            int by = miradaY > 0 ? y + TILE - 3 : y + 1;
            g2.fillRect(x + TILE / 2 - 5, by, 10, 2);
        }

        if (ahora < jugadorGolpeHasta) {
            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Double(x + TILE / 2.0, y + TILE / 2.0), TILE * 0.55f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 70, 70, 80), new Color(255, 70, 70, 0)}));
            g2.fill(new Ellipse2D.Double(x - 6, y - 6, TILE + 12, TILE + 12));
        }
    }

    private void dibujarCriatura(Graphics2D g2, CriaturaTablero enemigo) {
        long ahora = System.currentTimeMillis();
        double bob = Math.sin(ahora / 460.0 + enemigo.criatura.getId()) * 1.6;
        double offsetX = 0;
        double offsetY = 0;
        if (ahora < enemigo.lungeHasta) {
            double t = 1 - (enemigo.lungeHasta - ahora) / (double) DURACION_GOLPE;
            double empuje = Math.sin(t * Math.PI) * 9;
            offsetX = enemigo.lungeDX * empuje;
            offsetY = enemigo.lungeDY * empuje;
        }

        boolean dragon = enemigo.criatura instanceof DragonJefe;
        int escala = 3;
        int tamano = dragon ? 72 : 48;
        int x = (int) (tableroX + enemigo.dibujoX * TILE + (TILE - tamano) / 2.0 + offsetX);
        int y = (int) (tableroY + enemigo.dibujoY * TILE + TILE - tamano + offsetY + bob);

        if (enemigo.criatura instanceof DragonJefe dragonAura) {
            if (dragonAura.getFaseActual() >= 2) {
                int intensidad = dragonAura.getFaseActual() == 3 ? 60 : 38;
                g2.setPaint(new RadialGradientPaint(
                        new Point2D.Double(x + tamano / 2.0, y + tamano / 2.0), tamano * 0.8f,
                        new float[]{0f, 1f},
                        new Color[]{new Color(255, 90, 40, intensidad),
                            new Color(255, 90, 40, 0)}));
                g2.fill(new Ellipse2D.Double(x - 12, y - 12, tamano + 24, tamano + 24));
            }
        }

        if (aventurero != null && aventurero.getRonda() > 1) {
            Color acento = colorRonda();
            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Double(x + tamano / 2.0, y + tamano / 2.0), tamano * 0.78f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 48),
                        new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 0)}));
            g2.fill(new Ellipse2D.Double(x - 10, y - 10, tamano + 20, tamano + 20));
        }

        switch (enemigo.criatura.getTipo()) {
            case ESQUELETO -> Sprites.esqueleto(g2, x, y, escala);
            case FANTASMA -> Sprites.fantasma(g2, x, y, escala);
            case ORCO -> Sprites.orco(g2, x, y, escala);
            case DRAGON_JEFE -> Sprites.dragon(g2, x, y, escala);
            case ARANA -> Sprites.arana(g2, x, y, escala);
            case GOLEM -> Sprites.golem(g2, x, y, escala);
        }

        if (ahora < enemigo.golpeHasta) {
            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Double(x + tamano / 2.0, y + tamano / 2.0), tamano * 0.55f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(255, 70, 70, 75), new Color(255, 70, 70, 0)}));
            g2.fill(new Ellipse2D.Double(x - 6, y - 6, tamano + 12, tamano + 12));
        }

        int anchoBarra = dragon ? 62 : 38;
        int barraX = x + (tamano - anchoBarra) / 2;
        int barraY = Math.max(tableroY + 2, y - 7);
        g2.setColor(new Color(0x0E0C16));
        g2.fillRoundRect(barraX, barraY, anchoBarra, 5, 3, 3);
        double vida = enemigo.criatura.getVidaMaxima() > 0
                ? (double) enemigo.criatura.getVida() / enemigo.criatura.getVidaMaxima()
                : 0;
        g2.setColor(vida > 0.5 ? new Color(0x6FBF5A) : new Color(0xD64B3F));
        g2.fillRoundRect(barraX, barraY, (int) (anchoBarra * vida), 5, 3, 3);

        if (enemigo.criatura instanceof DragonJefe dragonFase) {
            g2.setFont(new Font("Consolas", Font.BOLD, 10));
            g2.setColor(new Color(0xF2C230));
            String fase = "FASE " + dragonFase.getFaseActual() + "/" + dragonFase.getCantidadFases();
            g2.drawString(fase, x + (tamano - anchoTexto(g2, fase)) / 2, barraY - 4);
        }
    }

    private void dibujarEfectos(Graphics2D g2) {
        long ahora = System.currentTimeMillis();

        for (MuerteVisual muerte : muertes) {
            long duracion = Math.max(1, muerte.fin - muerte.inicio);
            double t = progreso(ahora, muerte.inicio, duracion);
            boolean dragonMuerto = muerte.criatura instanceof DragonJefe;
            int tamanoMuerte = dragonMuerto ? 72 : 48;
            int x = tableroX + muerte.col * TILE + (TILE - tamanoMuerte) / 2;
            int y = tableroY + muerte.fila * TILE + TILE - tamanoMuerte;

            if (t < 0.55) {
                double radio = 10 + t * (dragonMuerto ? 150 : 95);
                float onda = (float) ((0.55 - t) * 1.4);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(255, 220, 160, (int) (onda * 160)));
                double cx = x + tamanoMuerte / 2.0;
                double cy = y + tamanoMuerte / 2.0;
                g2.draw(new Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));
            }

            if (t < 0.22) {
                float destello = (float) (1 - t / 0.22);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                        destello * 0.55f));
                g2.setColor(new Color(255, 250, 220));
                g2.fill(new Ellipse2D.Double(x + 6, y + 6, tamanoMuerte - 12,
                        tamanoMuerte - 12));
            }

            int subida = (int) (t * (dragonMuerto ? 22 : 12));
            float alfa = (float) Math.max(0, 1 - t);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));
            switch (muerte.criatura.getTipo()) {
                case ESQUELETO -> Sprites.esqueleto(g2, x, y - subida, 3);
                case FANTASMA -> Sprites.fantasma(g2, x, y - subida, 3);
                case ORCO -> Sprites.orco(g2, x, y - subida, 3);
                case DRAGON_JEFE -> Sprites.dragon(g2, x, y - subida, 3);
                case ARANA -> Sprites.arana(g2, x, y - subida, 3);
                case GOLEM -> Sprites.golem(g2, x, y - subida, 3);
            }
            g2.setComposite(AlphaComposite.SrcOver);
        }

        for (LenguaFuego fuego : fuegos) {
            double t = progreso(ahora, fuego.inicio, fuego.fin - fuego.inicio);
            double x1 = tableroX + (fuego.colOrigen + 0.5) * TILE;
            double y1 = tableroY + (fuego.filaOrigen + 0.5) * TILE;
            double x2 = tableroX + (fuego.colDestino + 0.5) * TILE;
            double y2 = tableroY + (fuego.filaDestino + 0.5) * TILE;

            for (int i = 0; i < 8; i++) {
                double avance = Math.min(1, t * 1.6 - i * 0.075);
                if (avance <= 0) {
                    continue;
                }
                double x = x1 + (x2 - x1) * avance
                        + Math.sin(ahora * 0.02 + i * 1.3) * 5 * avance;
                double y = y1 + (y2 - y1) * avance
                        + Math.cos(ahora * 0.024 + i * 1.7) * 5 * avance;
                double radio = 8 + 22 * avance + i * 1.0;
                float alfa = (float) ((1 - t) * (0.5 + 0.5 * (1 - avance)));
                g2.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), (float) radio,
                        new float[]{0f, 0.5f, 1f},
                        new Color[]{
                            new Color(255, 245, 180, (int) (190 * alfa)),
                            new Color(255, 140, 40, (int) (170 * alfa)),
                            new Color(190, 40, 10, 0)}));
                g2.fill(new Ellipse2D.Double(x - radio, y - radio, radio * 2, radio * 2));
            }
        }

        for (Proyectil proyectil : proyectiles) {
            double t = progreso(ahora, proyectil.inicio, DURACION_PROYECTIL);
            double x1 = tableroX + (proyectil.colOrigen + 0.5) * TILE;
            double y1 = tableroY + (proyectil.filaOrigen + 0.5) * TILE;
            double x2 = tableroX + (proyectil.colDestino + 0.5) * TILE;
            double y2 = tableroY + (proyectil.filaDestino + 0.5) * TILE;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;

            g2.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), 22,
                    new float[]{0f, 1f},
                    new Color[]{new Color(180, 215, 255, 220), new Color(120, 160, 255, 0)}));
            g2.fill(new Ellipse2D.Double(x - 22, y - 22, 44, 44));
            g2.setColor(new Color(220, 235, 255));
            g2.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
        }

        for (NumeroFlotante numero : numeros) {
            double t = progreso(ahora, numero.inicio, DURACION_NUMERO);
            int alfa = (int) (255 * (1 - t));
            int x = tableroX + numero.col * TILE + TILE / 2;
            int y = (int) (tableroY + numero.fila * TILE + 12 - t * 34);
            g2.setFont(new Font("Consolas", Font.BOLD, 18));
            g2.setColor(new Color(0, 0, 0, Math.min(180, alfa)));
            g2.drawString(numero.texto, x - anchoTexto(g2, numero.texto) / 2 + 1, y + 1);
            g2.setColor(new Color(numero.color.getRed(), numero.color.getGreen(),
                    numero.color.getBlue(), alfa));
            g2.drawString(numero.texto, x - anchoTexto(g2, numero.texto) / 2, y);
        }

        for (Particula particula : particulas) {
            double t = progreso(ahora, particula.inicio, particula.fin - particula.inicio);
            double transcurrido = ahora - particula.inicio;
            double x = tableroX + (particula.col + 0.5) * TILE + particula.vx * transcurrido;
            double y = tableroY + (particula.fila + 0.5) * TILE + particula.vy * transcurrido
                    + 0.00018 * transcurrido * transcurrido;
            int alfa = Math.max(0, (int) (255 * (1 - t)));
            g2.setColor(new Color(particula.color.getRed(), particula.color.getGreen(),
                    particula.color.getBlue(), alfa));
            int tamano = Math.max(1, (int) (particula.tamano * (1 - t * 0.4)));
            g2.fillRect((int) x, (int) y, tamano, tamano);
        }
    }

    private static int anchoTexto(Graphics2D g2, String texto) {
        return g2.getFontMetrics().stringWidth(texto);
    }

    private void dibujarVineta(Graphics2D g2) {
        float radio = ANCHO_TABLERO * 0.62f;
        g2.setPaint(new RadialGradientPaint(
                new Point2D.Double(tableroX + ANCHO_TABLERO / 2.0,
                        tableroY + ALTO_TABLERO / 2.0), radio,
                new float[]{0f, 0.7f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 30),
                    new Color(0, 0, 0, 150)}));
        g2.fill(new Rectangle2D.Double(tableroX, tableroY, ANCHO_TABLERO, ALTO_TABLERO));
    }

    private void dibujarHud(Graphics2D g2, int ancho) {
        g2.setColor(PANEL);
        g2.fillRect(0, 0, ancho, ALTO_HUD);
        g2.setColor(BORDE);
        g2.fillRect(0, ALTO_HUD - 2, ancho, 2);

        g2.setFont(new Font("Consolas", Font.BOLD, 22));
        g2.setColor(TEXTO);
        g2.drawString("MAZMORRA", 24, 38);
        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2.setColor(TEXTO_SUAVE);
        g2.drawString("PISO " + piso, 24, 64);
        for (int i = 0; i < 3; i++) {
            g2.setColor(i < piso ? new Color(0xE0B341) : new Color(0x2A2438));
            g2.fillRoundRect(88 + i * 14, 52, 10, 8, 3, 3);
        }
        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        g2.setColor(colorRonda());
        g2.drawString("RONDA " + (aventurero != null ? aventurero.getRonda() : 1),
                150, 64);

        int barraX = 220;
        int barraY = 22;
        int barraAncho = 260;
        int barraAlto = 20;
        g2.setColor(new Color(0x2A2438));
        g2.fillRoundRect(barraX, barraY, barraAncho, barraAlto, 8, 8);

        int vida = aventurero != null ? aventurero.getVida() : 0;
        int vidaMaxima = aventurero != null ? aventurero.getVidaMaxima() : 0;
        double proporcion = vidaMaxima > 0 ? (double) vida / vidaMaxima : 0;
        if (proporcion > 0) {
            g2.setColor(SANGRE);
            g2.fillRoundRect(barraX, barraY, (int) (barraAncho * proporcion), barraAlto, 8, 8);
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillRoundRect(barraX, barraY, (int) (barraAncho * proporcion), barraAlto / 2, 8, 8);
        }
        g2.setColor(BORDE);
        g2.drawRoundRect(barraX, barraY, barraAncho, barraAlto, 8, 8);

        g2.setFont(new Font("Consolas", Font.BOLD, 13));
        g2.setColor(TEXTO);
        g2.drawString("VIDA", barraX, 60);
        g2.drawString(vida + "/" + vidaMaxima, barraX + barraAncho + 10, barraY + 16);

        int oro = aventurero != null ? aventurero.getOro() : 0;
        g2.setColor(new Color(0xE0B341));
        g2.fill(new Ellipse2D.Double(560, 22, 18, 18));
        g2.setColor(new Color(0x8A6A1E));
        g2.draw(new Ellipse2D.Double(560, 22, 18, 18));
        g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.setColor(TEXTO);
        g2.drawString(String.valueOf(oro), 586, 36);
        g2.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2.setColor(TEXTO_SUAVE);
        g2.drawString("ORO", 560, 60);

        g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.setColor(TEXTO);
        g2.drawString(String.valueOf(puntosProyectados()), 680, 36);
        g2.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2.setColor(TEXTO_SUAVE);
        g2.drawString("PUNTOS", 680, 60);

        if (aventurero != null) {
            g2.setFont(new Font("Consolas", Font.BOLD, 14));
            g2.setColor(TEXTO);
            String nombre = aventurero.getNombre();
            int maxNombre = Math.max(30, ancho - 118 - 800 - 12);
            while (!nombre.isEmpty()
                    && g2.getFontMetrics().stringWidth(nombre) > maxNombre) {
                nombre = nombre.substring(0, nombre.length() - 1);
            }
            if (!nombre.equals(aventurero.getNombre())) {
                nombre = nombre + "...";
            }
            g2.drawString(nombre, 800, 36);
            g2.setFont(new Font("Consolas", Font.PLAIN, 11));
            g2.setColor(TEXTO_SUAVE);
            g2.drawString("AVENTURERO", 800, 60);
        }

        g2.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2.setColor(TEXTO_SUAVE);
        g2.drawString("TURNO", ancho - 118, 36);
        g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.setColor(TEXTO);
        g2.drawString(String.valueOf(turnos), ancho - 62, 36);

        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2.setColor(TEXTO_SUAVE);
        String ayuda = "WASD mover   1 físico   2 magia   " + textoPocion()
                + "   H huir   R nuevo   C continuar   N nuevo";
        g2.drawString(ayuda, (ancho - anchoTexto(g2, ayuda)) / 2, 88);
    }

    private String textoPocion() {
        if (aventurero == null) {
            return "P: poción (cura hasta " + Reglas.TAMANO_POCION + " por "
                    + Reglas.COSTO_POCION + " de oro)";
        }
        int faltante = aventurero.getVidaMaxima() - aventurero.getVida();
        if (faltante <= 0) {
            return "P: poción (vida completa)";
        }
        int curado = Math.min(Reglas.TAMANO_POCION, faltante);
        int costo = (int) Math.ceil((double) Reglas.COSTO_POCION * curado
                / Reglas.TAMANO_POCION);
        return "P: poción (+" + curado + " vida por " + costo + " de oro)";
    }

    private void dibujarInicio(Graphics2D g2, int ancho, int alto) {
        g2.setColor(new Color(6, 5, 10, 205));
        g2.fillRect(0, 0, ancho, alto);

        g2.setFont(new Font("Consolas", Font.BOLD, 46));
        g2.setColor(TEXTO);
        dibujarCentrado(g2, "MAZMORRA", ancho, 190);

        g2.setFont(new Font("Consolas", Font.PLAIN, 15));
        g2.setColor(TEXTO_SUAVE);
        dibujarCentrado(g2, "WASD: moverte por la sala", ancho, 320);
        dibujarCentrado(g2, "1: ataque físico   ·   2: magia", ancho, 350);
        dibujarCentrado(g2, "P: poción (cura hasta " + Reglas.TAMANO_POCION + " por "
                + Reglas.COSTO_POCION + " de oro)   ·   H: huir   ·   R: personaje nuevo", ancho, 380);
        dibujarCentrado(g2, "Atacá con 1 o 2 al enemigo de al lado", ancho, 410);
        dibujarCentrado(g2, "Caminá hacia la escalera cuando el piso esté limpio", ancho, 440);

        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.setColor(new Color(0xC9C3DD));
        dibujarCentrado(g2, "Solo una victoria sube la ronda.", ancho, 490);
        dibujarCentrado(g2, "Al ganar, vida y oro se reinician; el personaje sigue.", ancho, 515);
        dibujarCentrado(g2, "Si mueres, el personaje queda eliminado.", ancho, 540);
        dibujarCentrado(g2, "El ranking guarda el top 5 por mejor puntaje.", ancho, 565);

        int pulso = (int) (150 + 105 * Math.sin(System.currentTimeMillis() / 400.0));
        g2.setFont(new Font("Consolas", Font.BOLD, 20));
        g2.setColor(new Color(242, 194, 48, pulso));
        dibujarCentrado(g2, hayPartidaGuardada
                ? "Enter: personaje nuevo   ·   C: continuar"
                : "Enter para empezar", ancho, 595);
    }

    private void dibujarFinal(Graphics2D g2, int ancho, int alto, String titulo,
            String linea1, String linea2) {
        g2.setColor(new Color(6, 5, 10, 200));
        g2.fillRect(0, 0, ancho, alto);

        g2.setFont(new Font("Consolas", Font.BOLD, 44));
        g2.setColor(victoria ? new Color(0xE0B341) : SANGRE);
        dibujarCentrado(g2, titulo, ancho, 165);

        g2.setFont(new Font("Consolas", Font.PLAIN, 18));
        g2.setColor(TEXTO);
        dibujarCentrado(g2, linea1, ancho, 215);
        dibujarCentrado(g2, linea2, ancho, 245);

        int puntosMostrados = resultadoRegistrado ? puntaje : puntosProyectados();
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.setColor(TEXTO_SUAVE);
        String resumen = "Ronda " + (aventurero != null ? aventurero.getRonda() : 1)
                + "   ·   Piso " + piso + "   ·   Puntos " + puntosMostrados;
        dibujarCentrado(g2, resumen, ancho, 285);

        g2.setFont(new Font("Consolas", Font.BOLD, 16));
        g2.setColor(colorRonda());
        dibujarCentrado(g2, "TOP 5", ancho, 325);

        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.setColor(TEXTO);
        if (!resultadoRegistrado) {
            g2.setColor(SANGRE);
            dibujarCentrado(g2, "No se pudo guardar el resultado; N reintenta.", ancho, 355);
        } else if (ranking.isEmpty()) {
            dibujarCentrado(g2, "Todavía no hay puntajes registrados.", ancho, 355);
        } else {
            for (int i = 0; i < ranking.size(); i++) {
                Personaje personaje = ranking.get(i);
                String fila = (i + 1) + ". " + personaje.getNombre()
                        + "   ·   " + personaje.getMejorPuntaje() + " pts"
                        + "   ·   rondas " + personaje.getRondas();
                dibujarCentrado(g2, fila, ancho, 355 + i * 24);
            }
        }

        if (aventurero != null) {
            g2.setFont(new Font("Consolas", Font.PLAIN, 13));
            g2.setColor(TEXTO_SUAVE);
            String detalle = "Enemigos " + aventurero.getEnemigosDerrotados()
                    + "   ·   Oro " + aventurero.getOro()
                    + "   ·   Turnos " + turnos;
            dibujarCentrado(g2, detalle, ancho, 500);
        }

        int pulso = (int) (150 + 105 * Math.sin(System.currentTimeMillis() / 400.0));
        g2.setFont(new Font("Consolas", Font.BOLD, 18));
        g2.setColor(new Color(242, 194, 48, pulso));
        String accion = victoria
                ? "R: siguiente ronda   ·   N: personaje nuevo"
                : "N: personaje nuevo";
        dibujarCentrado(g2, accion, ancho, 545);

        g2.setFont(new Font("Consolas", Font.PLAIN, 13));
        g2.setColor(TEXTO_SUAVE);
        String pista = victoria
                ? "La vida y el oro se reinician; el personaje y su puntaje quedan."
                : "El personaje quedó eliminado; su puntaje queda en el ranking.";
        dibujarCentrado(g2, pista, ancho, 578);
    }

    private static void dibujarCentrado(Graphics2D g2, String texto, int ancho, int y) {
        g2.drawString(texto, (ancho - anchoTexto(g2, texto)) / 2, y);
    }

    private static final class CriaturaTablero {

        private Criatura criatura;
        private int col;
        private int fila;
        private double dibujoX;
        private double dibujoY;
        private double desdeX;
        private double desdeY;
        private double hastaX;
        private double hastaY;
        private long movInicio;
        private boolean moviendose;
        private long golpeHasta;
        private long lungeHasta;
        private int lungeDX;
        private int lungeDY;

        private CriaturaTablero(Criatura criatura, int col, int fila) {
            this.criatura = criatura;
            this.col = col;
            this.fila = fila;
            this.dibujoX = col;
            this.dibujoY = fila;
        }
    }

    private static final class LenguaFuego {

        private final int colOrigen;
        private final int filaOrigen;
        private final int colDestino;
        private final int filaDestino;
        private final long inicio;
        private final long fin;

        private LenguaFuego(int colOrigen, int filaOrigen, int colDestino, int filaDestino,
                long inicio, long fin) {
            this.colOrigen = colOrigen;
            this.filaOrigen = filaOrigen;
            this.colDestino = colDestino;
            this.filaDestino = filaDestino;
            this.inicio = inicio;
            this.fin = fin;
        }
    }

    private static final class Particula {

        private final int col;
        private final int fila;
        private final double vx;
        private final double vy;
        private final long inicio;
        private final long fin;
        private final Color color;
        private final int tamano;

        private Particula(int col, int fila, double vx, double vy, long inicio, long fin,
                Color color, int tamano) {
            this.col = col;
            this.fila = fila;
            this.vx = vx;
            this.vy = vy;
            this.inicio = inicio;
            this.fin = fin;
            this.color = color;
            this.tamano = tamano;
        }
    }

    private static final class NumeroFlotante {

        private final int col;
        private final int fila;
        private final String texto;
        private final Color color;
        private final long inicio;
        private final long fin;

        private NumeroFlotante(int col, int fila, String texto, Color color, long inicio,
                long fin) {
            this.col = col;
            this.fila = fila;
            this.texto = texto;
            this.color = color;
            this.inicio = inicio;
            this.fin = fin;
        }
    }

    private static final class MuerteVisual {

        private final Criatura criatura;
        private final int col;
        private final int fila;
        private final long inicio;
        private final long fin;

        private MuerteVisual(Criatura criatura, int col, int fila, long inicio, long fin) {
            this.criatura = criatura;
            this.col = col;
            this.fila = fila;
            this.inicio = inicio;
            this.fin = fin;
        }
    }

    private static final class Proyectil {

        private final int colOrigen;
        private final int filaOrigen;
        private final int colDestino;
        private final int filaDestino;
        private final long inicio;
        private final long fin;

        private Proyectil(int colOrigen, int filaOrigen, int colDestino, int filaDestino,
                long inicio, long fin) {
            this.colOrigen = colOrigen;
            this.filaOrigen = filaOrigen;
            this.colDestino = colDestino;
            this.filaDestino = filaDestino;
            this.inicio = inicio;
            this.fin = fin;
        }
    }
}
