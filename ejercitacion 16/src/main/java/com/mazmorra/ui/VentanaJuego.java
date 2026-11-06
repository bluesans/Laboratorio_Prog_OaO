package com.mazmorra.ui;

import com.mazmorra.service.JuegoService;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

public final class VentanaJuego extends JFrame {

    public VentanaJuego(JuegoService servicio) {
        super("Mazmorra");

        PanelJuego panelJuego = new PanelJuego(servicio);
        PanelAdministracion panelAdministracion = new PanelAdministracion(servicio);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Juego", panelJuego);
        pestanas.addTab("Administración", panelAdministracion);
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedComponent() == panelAdministracion) {
                panelAdministracion.alMostrar();
            } else {
                panelJuego.recargarTablero();
                panelJuego.requestFocusInWindow();
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(pestanas, BorderLayout.CENTER);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1000, 720);
        setLocationRelativeTo(null);
    }
}
