package com.mazmorra.db;

import com.mazmorra.dao.impl.AventureroDAOMySQL;
import com.mazmorra.db.impl.TransaccionMySQL;
import com.mazmorra.exception.JuegoException;
import com.mazmorra.model.Aventurero;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Solo se ejecuta con DB_URL definida; usuario y contraseña salen de DB_USUARIO/DB_PASSWORD o de db.properties.
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class TransaccionMySQLTest {

    @Test
    void confirmaElAventureroInsertado() throws Exception {
        Aventurero aventurero = new Aventurero(
                0, "Transaccion Commit", 1, 100, 100, 20, 50, 1);
        try {
            int id = new TransaccionMySQL().ejecutar(
                    (criaturas, aventureros) -> aventureros.insertar(aventurero));

            Aventurero persistido = new AventureroDAOMySQL().buscarPorId(id).orElseThrow();
            assertEquals(id, persistido.getId(),
                    "El aventurero insertado debe quedar confirmado tras el commit.");
            assertEquals("Transaccion Commit", persistido.getNombre(),
                    "El aventurero recuperado debe ser el insertado.");
        } finally {
            borrarSiSeInserto(aventurero);
        }
    }

    @Test
    void revierteAnteUnaExcepcionDeJuego() throws Exception {
        Aventurero aventurero = new Aventurero(
                0, "Transaccion Rollback Juego", 1, 100, 100, 20, 50, 1);
        JuegoException esperada = new JuegoException("Falla simulada");
        try {
            JuegoException lanzada = assertThrows(JuegoException.class,
                    () -> new TransaccionMySQL().ejecutar((criaturas, aventureros) -> {
                        aventureros.insertar(aventurero);
                        throw esperada;
                    }));

            assertSame(esperada, lanzada,
                    "La transacción debe propagar la misma excepción de juego.");
            assertTrue(new AventureroDAOMySQL().buscarPorId(aventurero.getId()).isEmpty(),
                    "El rollback debe descartar el aventurero insertado.");
        } finally {
            borrarSiSeInserto(aventurero);
        }
    }

    @Test
    void revierteAnteUnaExcepcionDeEjecucion() throws Exception {
        Aventurero aventurero = new Aventurero(
                0, "Transaccion Rollback Runtime", 1, 100, 100, 20, 50, 1);
        IllegalStateException esperada = new IllegalStateException("Falla simulada");
        try {
            IllegalStateException lanzada = assertThrows(IllegalStateException.class,
                    () -> new TransaccionMySQL().ejecutar((criaturas, aventureros) -> {
                        aventureros.insertar(aventurero);
                        throw esperada;
                    }));

            assertSame(esperada, lanzada,
                    "La transacción debe propagar la misma excepción de ejecución.");
            assertTrue(new AventureroDAOMySQL().buscarPorId(aventurero.getId()).isEmpty(),
                    "El rollback debe descartar el aventurero insertado.");
        } finally {
            borrarSiSeInserto(aventurero);
        }
    }

    private static void borrarSiSeInserto(Aventurero aventurero) throws JuegoException {
        if (aventurero.getId() > 0) {
            new AventureroDAOMySQL().eliminar(aventurero.getId());
        }
    }
}
