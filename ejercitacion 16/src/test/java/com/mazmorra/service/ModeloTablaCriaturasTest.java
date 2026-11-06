package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.service.impl.JuegoServiceImpl;
import com.mazmorra.ui.ModeloTablaCriaturas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModeloTablaCriaturasTest {

    private CriaturaDAOEnMemoria criaturaDAO;
    private AventureroDAOEnMemoria aventureroDAO;
    private JuegoServiceImpl servicio;

    @BeforeEach
    void preparar() {
        criaturaDAO = new CriaturaDAOEnMemoria();
        aventureroDAO = new AventureroDAOEnMemoria();
        servicio = new JuegoServiceImpl(criaturaDAO, aventureroDAO,
                new TransaccionDirecta(criaturaDAO, aventureroDAO));
    }

    @Test
    void cargaLasCriaturasYSeRecargaTrasUnaBaja() throws Exception {
        servicio.registrarCriatura(new Esqueleto(0, "Esqueleto", 25, 25, 2,
                Reglas.DANIO_ESQUELETO, 1, false, Reglas.RESISTENCIA_ESQUELETO));
        Criatura fantasma = servicio.registrarCriatura(new Esqueleto(0, "Otro", 20, 20, 3,
                Reglas.DANIO_ESQUELETO, 2, false, Reglas.RESISTENCIA_ESQUELETO));
        ModeloTablaCriaturas modelo = new ModeloTablaCriaturas(servicio);

        modelo.recargar();

        assertEquals(2, modelo.getRowCount());
        assertEquals(8, modelo.getColumnCount());
        assertEquals("Esqueleto", modelo.getValueAt(0, 1));
        assertEquals("ESQUELETO", modelo.getValueAt(0, 2));
        assertEquals("25/25", modelo.getValueAt(0, 4));
        assertEquals("Viva", modelo.getValueAt(0, 7));

        servicio.eliminarCriatura(fantasma.getId());
        modelo.recargar();

        assertEquals(1, modelo.getRowCount());
    }
}
