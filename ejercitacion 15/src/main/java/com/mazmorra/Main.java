package com.mazmorra;

import com.mazmorra.dao.impl.AventureroDAOMySQL;
import com.mazmorra.dao.impl.CriaturaDAOMySQL;
import com.mazmorra.db.impl.TransaccionMySQL;
import com.mazmorra.exception.JuegoException;
import com.mazmorra.service.JuegoService;
import com.mazmorra.service.impl.JuegoServiceImpl;

public final class Main {

    private Main() {
        throw new UnsupportedOperationException("Clase de entrada: no se instancia.");
    }

    public static void main(String[] args) {
        JuegoService servicio = new JuegoServiceImpl(
                new CriaturaDAOMySQL(), new AventureroDAOMySQL(), new TransaccionMySQL());
        boolean consola = args.length > 0 && "--consola".equalsIgnoreCase(args[0]);
        try {
            if (consola) {
                new JuegoConsola(servicio).iniciar();
            } else {
                DemoGuionada.ejecutar(servicio);
            }
        } catch (JuegoException | IllegalStateException e) {
            String mensaje = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            System.err.println("No se pudo jugar: " + mensaje);
            System.exit(1);
        }
    }
}
