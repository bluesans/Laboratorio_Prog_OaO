package com.mazmorra;

import com.formdev.flatlaf.FlatLightLaf;
import com.mazmorra.dao.impl.AventureroDAOMySQL;
import com.mazmorra.dao.impl.CriaturaDAOMySQL;
import com.mazmorra.dao.impl.PartidaDAOMySQL;
import com.mazmorra.dao.impl.PersonajeDAOMySQL;
import com.mazmorra.db.impl.TransaccionMySQL;
import com.mazmorra.service.JuegoService;
import com.mazmorra.service.impl.JuegoServiceImpl;
import com.mazmorra.ui.DialogosError;
import com.mazmorra.ui.VentanaJuego;

import javax.swing.SwingUtilities;

public final class Main {

    private Main() {
        throw new UnsupportedOperationException("Clase de entrada: no se instancia.");
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) ->
                SwingUtilities.invokeLater(() -> DialogosError.mostrar(null, error)));
        FlatLightLaf.setup();
        JuegoService servicio = new JuegoServiceImpl(
                new CriaturaDAOMySQL(), new AventureroDAOMySQL(),
                new PersonajeDAOMySQL(), new PartidaDAOMySQL(), new TransaccionMySQL());
        SwingUtilities.invokeLater(() -> {
            try {
                new VentanaJuego(servicio).setVisible(true);
            } catch (RuntimeException e) {
                DialogosError.mostrar(null, e);
                System.exit(1);
            }
        });
    }
}
