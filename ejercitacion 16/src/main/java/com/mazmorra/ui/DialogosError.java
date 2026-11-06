package com.mazmorra.ui;

import com.mazmorra.exception.JuegoException;
import com.mazmorra.exception.PersistenciaException;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class DialogosError {

    private DialogosError() {
        throw new UnsupportedOperationException("Clase de utilidad: no se instancia.");
    }

    public static void mostrar(Component padre, Throwable error) {
        String mensaje;
        int tipo = JOptionPane.ERROR_MESSAGE;
        if (error instanceof PersistenciaException) {
            mensaje = error.getMessage();
        } else if (error instanceof JuegoException) {
            mensaje = error.getMessage();
            tipo = JOptionPane.WARNING_MESSAGE;
        } else if (error instanceof NumberFormatException) {
            mensaje = "Revisá los campos numéricos.";
            tipo = JOptionPane.WARNING_MESSAGE;
        } else if (error instanceof IllegalStateException) {
            mensaje = error.getMessage();
        } else if (error instanceof IllegalArgumentException) {
            mensaje = error.getMessage();
            tipo = JOptionPane.WARNING_MESSAGE;
        } else {
            mensaje = "Ocurrió un error inesperado.";
        }
        if (mensaje == null || mensaje.isBlank()) {
            mensaje = "Ocurrió un error inesperado.";
        }
        JOptionPane.showMessageDialog(padre, mensaje,
                "No se pudo completar la acción", tipo);
    }
}
