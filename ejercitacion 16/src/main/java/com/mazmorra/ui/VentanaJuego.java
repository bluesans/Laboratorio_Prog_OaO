package com.mazmorra.ui;

import com.mazmorra.service.JuegoService;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;

public final class VentanaJuego extends JFrame {

    public VentanaJuego(JuegoService servicio) {
        super("Mazmorra");

        PanelJuego panelJuego = new PanelJuego(servicio);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(panelJuego, BorderLayout.CENTER);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1000, 740);
        setLocationRelativeTo(null);
    }
}
