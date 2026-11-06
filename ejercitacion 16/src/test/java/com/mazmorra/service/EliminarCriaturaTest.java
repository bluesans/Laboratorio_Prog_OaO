package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.service.impl.JuegoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del borrado de criaturas usando DAO en memoria y una transacción directa.
 */
class EliminarCriaturaTest {

    private CriaturaDAOEnMemoria criaturaDAO;
    private AventureroDAOEnMemoria aventureroDAO;
    private JuegoServiceImpl servicio;

    @BeforeEach
    void preparar() {
        criaturaDAO = new CriaturaDAOEnMemoria();
        aventureroDAO = new AventureroDAOEnMemoria();
        servicio = new JuegoServiceImpl(
                criaturaDAO, aventureroDAO,
                new TransaccionDirecta(criaturaDAO, aventureroDAO));
    }

    @Test
    void eliminarCriaturaRegistradaLaQuitaDelListado() throws Exception {
        Criatura criatura = servicio.registrarCriatura(new Esqueleto(
                0, "Esqueleto a borrar", 25, 25, 2, Reglas.DANIO_ESQUELETO, 1,
                false, Reglas.RESISTENCIA_ESQUELETO));
        int id = criatura.getId();

        assertEquals(1, servicio.listarCriaturas().size(),
                "Tras registrar la criatura debe haber exactamente una en el listado.");

        servicio.eliminarCriatura(id);

        List<Criatura> criaturas = servicio.listarCriaturas();
        assertFalse(criaturas.stream().anyMatch(c -> c.getId() == id),
                "El listado no debe contener la criatura eliminada.");
        assertTrue(criaturas.isEmpty(),
                "Tras eliminar la única criatura el listado debe quedar vacío.");
    }

    @Test
    void eliminarCriaturaInexistenteFallaConElIdEnElMensaje() throws Exception {
        int idInexistente = 99;

        PersistenciaException error = assertThrows(PersistenciaException.class,
                () -> servicio.eliminarCriatura(idInexistente),
                "Eliminar una criatura inexistente debe lanzar PersistenciaException.");

        assertTrue(error.getMessage().contains(String.valueOf(idInexistente)),
                "El mensaje del error debe contener el id " + idInexistente + ".");
    }
}
