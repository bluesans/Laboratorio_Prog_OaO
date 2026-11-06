package com.mazmorra.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

final class Sprites {

    static final Color CONTORNO = new Color(0x14121F);
    static final Color ACERO = new Color(0xAEBACD);
    static final Color ACERO_CLARO = new Color(0xDCE6F2);
    static final Color ACERO_OSCURO = new Color(0x6E7C93);
    static final Color CUERO = new Color(0x5B3A29);
    static final Color ORO = new Color(0xE0B341);
    static final Color PIEL = new Color(0xE7B58A);
    static final Color ROJO = new Color(0xC0392B);
    static final Color HUESO = new Color(0xE8E2CF);
    static final Color HUESO_OSCURO = new Color(0xCFC7B0);
    static final Color VERDE = new Color(0x5B8C3A);
    static final Color VERDE_OSCURO = new Color(0x3F6328);
    static final Color DRAGON = new Color(0xA83232);
    static final Color DRAGON_OSCURO = new Color(0x6E1F1F);
    static final Color DRAGON_VIENTRE = new Color(0xE0A060);
    static final Color LLAMA = new Color(0xE67E22);
    static final Color LLAMA_CLARA = new Color(0xF5C542);

    private Sprites() {
        throw new UnsupportedOperationException("Clase de utilidad: no se instancia.");
    }

    static void caballero(Graphics2D g, int ox, int oy, int escala) {
        sombra(g, ox, oy, escala);
        bloque(g, CONTORNO, 4, 12, 3, 4, escala, ox, oy);
        bloque(g, CONTORNO, 9, 12, 3, 4, escala, ox, oy);
        bloque(g, ACERO_OSCURO, 4, 10, 3, 3, escala, ox, oy);
        bloque(g, ACERO_OSCURO, 9, 10, 3, 3, escala, ox, oy);
        bloque(g, ACERO, 4, 6, 8, 4, escala, ox, oy);
        bloque(g, ACERO_CLARO, 5, 6, 3, 2, escala, ox, oy);
        bloque(g, CUERO, 4, 9, 8, 1, escala, ox, oy);
        bloque(g, ACERO_OSCURO, 2, 6, 2, 3, escala, ox, oy);
        bloque(g, ACERO_OSCURO, 12, 6, 2, 3, escala, ox, oy);
        bloque(g, ACERO_OSCURO, 2, 9, 2, 3, escala, ox, oy);
        bloque(g, ACERO_OSCURO, 12, 9, 2, 3, escala, ox, oy);
        bloque(g, CONTORNO, 2, 12, 2, 2, escala, ox, oy);
        bloque(g, CONTORNO, 12, 12, 2, 2, escala, ox, oy);
        bloque(g, PIEL, 6, 3, 4, 2, escala, ox, oy);
        bloque(g, ACERO, 5, 1, 6, 3, escala, ox, oy);
        bloque(g, ACERO_CLARO, 6, 1, 4, 1, escala, ox, oy);
        bloque(g, CONTORNO, 6, 3, 4, 1, escala, ox, oy);
        bloque(g, ROJO, 7, 0, 2, 1, escala, ox, oy);
        bloque(g, CUERO, 1, 7, 3, 5, escala, ox, oy);
        bloque(g, ORO, 2, 9, 1, 1, escala, ox, oy);
        bloque(g, ACERO_CLARO, 13, 3, 1, 7, escala, ox, oy);
        bloque(g, ORO, 12, 9, 3, 1, escala, ox, oy);
        bloque(g, CUERO, 13, 10, 1, 2, escala, ox, oy);
    }

    static void esqueleto(Graphics2D g, int ox, int oy, int escala) {
        sombra(g, ox, oy, escala);
        bloque(g, HUESO_OSCURO, 4, 13, 3, 2, escala, ox, oy);
        bloque(g, HUESO_OSCURO, 9, 13, 3, 2, escala, ox, oy);
        bloque(g, HUESO, 4, 10, 2, 4, escala, ox, oy);
        bloque(g, HUESO, 10, 10, 2, 4, escala, ox, oy);
        bloque(g, HUESO, 4, 9, 8, 2, escala, ox, oy);
        bloque(g, HUESO_OSCURO, 7, 6, 2, 4, escala, ox, oy);
        bloque(g, HUESO, 5, 6, 6, 1, escala, ox, oy);
        bloque(g, HUESO, 5, 8, 6, 1, escala, ox, oy);
        bloque(g, HUESO, 3, 6, 2, 5, escala, ox, oy);
        bloque(g, HUESO, 11, 6, 2, 5, escala, ox, oy);
        bloque(g, HUESO, 4, 4, 8, 1, escala, ox, oy);
        bloque(g, HUESO, 5, 1, 6, 4, escala, ox, oy);
        bloque(g, HUESO_OSCURO, 5, 4, 6, 1, escala, ox, oy);
        bloque(g, CONTORNO, 6, 2, 2, 2, escala, ox, oy);
        bloque(g, CONTORNO, 9, 2, 1, 2, escala, ox, oy);
        bloque(g, HUESO_OSCURO, 6, 4, 4, 1, escala, ox, oy);
        bloque(g, new Color(0x8D5A3B), 13, 5, 1, 6, escala, ox, oy);
    }

    static void fantasma(Graphics2D g, int ox, int oy, int escala) {
        sombra(g, ox, oy, escala);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
        bloque(g, new Color(0xBFE8F2), 4, 2, 8, 10, escala, ox, oy);
        bloque(g, new Color(0xDCF5FA), 5, 3, 6, 7, escala, ox, oy);
        bloque(g, new Color(0xBFE8F2), 3, 5, 10, 5, escala, ox, oy);
        bloque(g, new Color(0x9FD3E0), 3, 11, 2, 2, escala, ox, oy);
        bloque(g, new Color(0x9FD3E0), 6, 11, 2, 3, escala, ox, oy);
        bloque(g, new Color(0x9FD3E0), 9, 11, 2, 2, escala, ox, oy);
        bloque(g, new Color(0x9FD3E0), 12, 11, 1, 3, escala, ox, oy);
        bloque(g, CONTORNO, 6, 5, 1, 2, escala, ox, oy);
        bloque(g, CONTORNO, 9, 5, 1, 2, escala, ox, oy);
        g.setComposite(AlphaComposite.SrcOver);
    }

    static void orco(Graphics2D g, int ox, int oy, int escala) {
        sombra(g, ox, oy, escala);
        bloque(g, CONTORNO, 4, 12, 3, 4, escala, ox, oy);
        bloque(g, CONTORNO, 9, 12, 3, 4, escala, ox, oy);
        bloque(g, CUERO, 4, 10, 3, 3, escala, ox, oy);
        bloque(g, CUERO, 9, 10, 3, 3, escala, ox, oy);
        bloque(g, VERDE, 4, 6, 8, 5, escala, ox, oy);
        bloque(g, VERDE_OSCURO, 4, 9, 8, 1, escala, ox, oy);
        bloque(g, VERDE_OSCURO, 2, 6, 2, 4, escala, ox, oy);
        bloque(g, VERDE_OSCURO, 12, 6, 2, 4, escala, ox, oy);
        bloque(g, VERDE, 2, 10, 2, 3, escala, ox, oy);
        bloque(g, VERDE, 12, 10, 2, 3, escala, ox, oy);
        bloque(g, VERDE, 5, 2, 6, 4, escala, ox, oy);
        bloque(g, VERDE_OSCURO, 5, 2, 6, 1, escala, ox, oy);
        bloque(g, CONTORNO, 6, 3, 1, 2, escala, ox, oy);
        bloque(g, CONTORNO, 9, 3, 1, 2, escala, ox, oy);
        bloque(g, new Color(0xC0392B), 6, 3, 1, 1, escala, ox, oy);
        bloque(g, new Color(0xC0392B), 9, 3, 1, 1, escala, ox, oy);
        bloque(g, HUESO, 6, 5, 1, 2, escala, ox, oy);
        bloque(g, HUESO, 9, 5, 1, 2, escala, ox, oy);
        bloque(g, CUERO, 13, 7, 2, 2, escala, ox, oy);
        bloque(g, CUERO, 13, 9, 2, 5, escala, ox, oy);
        bloque(g, new Color(0x8E9AA0), 13, 6, 2, 1, escala, ox, oy);
    }

    static void dragon(Graphics2D g, int ox, int oy, int escala) {
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new Ellipse2D.Double(ox + 3 * escala, oy + 21 * escala,
                18 * escala, 3 * escala));
        bloque(g, DRAGON_OSCURO, 7, 16, 3, 6, escala, ox, oy);
        bloque(g, DRAGON_OSCURO, 14, 16, 3, 6, escala, ox, oy);
        bloque(g, DRAGON_OSCURO, 0, 6, 8, 8, escala, ox, oy);
        bloque(g, DRAGON_OSCURO, 16, 6, 8, 8, escala, ox, oy);
        bloque(g, new Color(0x8F2E2E), 1, 7, 6, 5, escala, ox, oy);
        bloque(g, new Color(0x8F2E2E), 17, 7, 6, 5, escala, ox, oy);
        bloque(g, DRAGON, 5, 9, 14, 10, escala, ox, oy);
        bloque(g, DRAGON_VIENTRE, 9, 13, 6, 5, escala, ox, oy);
        bloque(g, DRAGON_OSCURO, 3, 12, 4, 3, escala, ox, oy);
        bloque(g, DRAGON_OSCURO, 18, 12, 4, 3, escala, ox, oy);
        bloque(g, DRAGON, 8, 4, 8, 7, escala, ox, oy);
        bloque(g, DRAGON_OSCURO, 8, 8, 8, 2, escala, ox, oy);
        bloque(g, DRAGON, 9, 2, 7, 4, escala, ox, oy);
        bloque(g, HUESO, 9, 0, 1, 3, escala, ox, oy);
        bloque(g, HUESO, 15, 0, 1, 3, escala, ox, oy);
        bloque(g, new Color(0xF2C230), 10, 4, 1, 1, escala, ox, oy);
        bloque(g, new Color(0xF2C230), 14, 4, 1, 1, escala, ox, oy);
        bloque(g, CONTORNO, 10, 6, 5, 1, escala, ox, oy);
        bloque(g, DRAGON_OSCURO, 16, 12, 6, 3, escala, ox, oy);
        bloque(g, DRAGON_OSCURO, 20, 14, 4, 2, escala, ox, oy);
    }

    static void antorcha(Graphics2D g, int ox, int oy, int escala) {
        bloque(g, new Color(0x4A2E1C), 7, 8, 2, 7, escala, ox, oy);
        bloque(g, new Color(0x8E9AA0), 6, 7, 4, 1, escala, ox, oy);
        bloque(g, LLAMA, 5, 2, 6, 6, escala, ox, oy);
        bloque(g, LLAMA_CLARA, 7, 3, 2, 4, escala, ox, oy);
        bloque(g, new Color(0xFFF3B0), 7, 4, 2, 2, escala, ox, oy);
    }

    static void escaleras(Graphics2D g, int ox, int oy, int escala, boolean activas) {
        Color piedra = activas ? new Color(0x8E9AA0) : new Color(0x4E5666);
        Color sombra = new Color(0x1A1A26);
        bloque(g, sombra, 2, 2, 12, 12, escala, ox, oy);
        for (int i = 0; i < 4; i++) {
            bloque(g, piedra, 3, 3 + i * 3, 10, 2, escala, ox, oy);
        }
        if (activas) {
            bloque(g, new Color(0xF2C230), 7, 1, 2, 1, escala, ox, oy);
        }
    }

    static void sombra(Graphics2D g, int ox, int oy, int escala) {
        g.setColor(new Color(0, 0, 0, 80));
        g.fill(new Ellipse2D.Double(ox + 3 * escala, oy + 14 * escala,
                10 * escala, 2.4 * escala));
    }

    private static void bloque(Graphics2D g, Color color, int x, int y, int ancho,
            int alto, int escala, int ox, int oy) {
        g.setColor(color);
        g.fillRect(ox + x * escala, oy + y * escala, ancho * escala, alto * escala);
    }
}
