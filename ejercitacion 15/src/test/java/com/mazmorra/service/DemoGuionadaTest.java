package com.mazmorra.service;

import com.mazmorra.DemoGuionada;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.TipoCriatura;
import com.mazmorra.service.impl.JuegoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la demo guionada sobre los DAO en memoria.
 */
class DemoGuionadaTest {

    private CriaturaDAOEnMemoria criaturaDAO;
    private AventureroDAOEnMemoria aventureroDAO;
    private JuegoServiceImpl servicio;

    @BeforeEach
    void preparar() throws Exception {
        criaturaDAO = new CriaturaDAOEnMemoria();
        aventureroDAO = new AventureroDAOEnMemoria();
        servicio = new JuegoServiceImpl(
                criaturaDAO, aventureroDAO,
                new TransaccionDirecta(criaturaDAO, aventureroDAO));
    }

    @Test
    void laDemoTerminaConElDragonDerrotadoSinFallos() throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));
            DemoGuionada.ejecutar(servicio);
        } finally {
            System.setOut(original);
        }

        String texto = salida.toString(StandardCharsets.UTF_8);
        assertFalse(texto.contains("FALLO"),
                "La demo no debe imprimir ningún FALLO.");
        assertEquals(7, contarOcurrencias(texto, "EXCEPCIÓN ESPERADA"),
                "La demo debe mostrar las siete excepciones esperadas.");
        assertTrue(texto.contains("Victoria: el Jefe Dragón cayó."),
                "La demo debe cantar la victoria cuando el dragón cae.");

        Criatura dragon = null;
        for (Criatura criatura : criaturaDAO.listarTodas()) {
            if (criatura.getTipo() == TipoCriatura.DRAGON_JEFE) {
                dragon = criatura;
                break;
            }
        }
        assertNotNull(dragon, "El escenario de la demo debe incluir al Jefe Dragón.");
        assertTrue(dragon.estaDerrotado(),
                "El Jefe Dragón guardado en el DAO debe quedar derrotado.");
    }

    private int contarOcurrencias(String texto, String buscado) {
        int ocurrencias = 0;
        int indice = texto.indexOf(buscado);
        while (indice >= 0) {
            ocurrencias++;
            indice = texto.indexOf(buscado, indice + buscado.length());
        }
        return ocurrencias;
    }
}
