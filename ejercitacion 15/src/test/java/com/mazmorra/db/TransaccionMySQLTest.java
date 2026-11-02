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
    void commitsInsertedAdventurer() throws Exception {
        Aventurero adventurer = new Aventurero(
                0, "Transaccion Commit", 1, 100, 100, 20, 50, 1);
        try {
            int id = new TransaccionMySQL().ejecutar(
                    (creatures, adventurers) -> adventurers.insertar(adventurer));

            Aventurero persisted = new AventureroDAOMySQL().buscarPorId(id).orElseThrow();
            assertEquals(id, persisted.getId(),
                    "El aventurero insertado debe quedar confirmado tras el commit.");
            assertEquals("Transaccion Commit", persisted.getNombre(),
                    "El aventurero recuperado debe ser el insertado.");
        } finally {
            deleteIfInserted(adventurer);
        }
    }

    @Test
    void rollsBackOnJuegoException() throws Exception {
        Aventurero adventurer = new Aventurero(
                0, "Transaccion Rollback Juego", 1, 100, 100, 20, 50, 1);
        JuegoException expected = new JuegoException("Falla simulada");
        try {
            JuegoException thrown = assertThrows(JuegoException.class,
                    () -> new TransaccionMySQL().ejecutar((creatures, adventurers) -> {
                        adventurers.insertar(adventurer);
                        throw expected;
                    }));

            assertSame(expected, thrown,
                    "La transacción debe propagar la misma excepción de juego.");
            assertTrue(new AventureroDAOMySQL().buscarPorId(adventurer.getId()).isEmpty(),
                    "El rollback debe descartar el aventurero insertado.");
        } finally {
            deleteIfInserted(adventurer);
        }
    }

    @Test
    void rollsBackOnRuntimeException() throws Exception {
        Aventurero adventurer = new Aventurero(
                0, "Transaccion Rollback Runtime", 1, 100, 100, 20, 50, 1);
        IllegalStateException expected = new IllegalStateException("Falla simulada");
        try {
            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> new TransaccionMySQL().ejecutar((creatures, adventurers) -> {
                        adventurers.insertar(adventurer);
                        throw expected;
                    }));

            assertSame(expected, thrown,
                    "La transacción debe propagar la misma excepción de ejecución.");
            assertTrue(new AventureroDAOMySQL().buscarPorId(adventurer.getId()).isEmpty(),
                    "El rollback debe descartar el aventurero insertado.");
        } finally {
            deleteIfInserted(adventurer);
        }
    }

    private static void deleteIfInserted(Aventurero adventurer) throws JuegoException {
        if (adventurer.getId() > 0) {
            new AventureroDAOMySQL().eliminar(adventurer.getId());
        }
    }
}
