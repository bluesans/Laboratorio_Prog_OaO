package com.mazmorra;

import com.mazmorra.exception.JuegoException;
import com.mazmorra.service.JuegoService;
import com.mazmorra.service.impl.JuegoServiceImpl;

public final class Main {

    // Cambiar a true para correr la demo guionada.
    private static boolean modoDemo = false;

    private Main() {
        throw new UnsupportedOperationException("Clase de entrada: no se instancia.");
    }

    public static void main(String[] args) {
        JuegoService servicio = new JuegoServiceImpl();
        try {
            if (modoDemo) {
                DemoGuionada.ejecutar(servicio);
            } else {
                new JuegoConsola(servicio).iniciar();
            }
        } catch (JuegoException | IllegalStateException e) {
            String mensaje = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            System.err.println("No se pudo jugar: " + mensaje);
            System.exit(1);
        }
    }
}
