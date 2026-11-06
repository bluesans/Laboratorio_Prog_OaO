package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.service.impl.JuegoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VictoriaDragonTest {

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
    void noHayVictoriaSiQuedaUnDragonVivo() throws Exception {
        Aventurero aventurero = crearAventurero();
        criaturaDAO.insertar(new DragonJefe(0, "Jefe Muerto", 0, 60, 10,
                Reglas.DANIO_DRAGON, Reglas.PISO_FINAL, true, 3, 3, true));
        criaturaDAO.insertar(new DragonJefe(0, "Jefe Vivo", 60, 60, 10,
                Reglas.DANIO_DRAGON, Reglas.PISO_FINAL, false, 1, 3, false));

        assertFalse(servicio.esVictoria(aventurero.getId()));
    }

    @Test
    void hayVictoriaSiTodosLosDragonesEstanDerrotados() throws Exception {
        Aventurero aventurero = crearAventurero();
        criaturaDAO.insertar(new DragonJefe(0, "Jefe Uno", 0, 60, 10,
                Reglas.DANIO_DRAGON, Reglas.PISO_FINAL, true, 3, 3, true));
        criaturaDAO.insertar(new DragonJefe(0, "Jefe Dos", 0, 60, 10,
                Reglas.DANIO_DRAGON, Reglas.PISO_FINAL, true, 3, 3, true));

        assertTrue(servicio.esVictoria(aventurero.getId()));
    }

    private Aventurero crearAventurero() throws Exception {
        Aventurero aventurero = new Aventurero(0, "Test", 1, 100, Reglas.VIDA_INICIAL,
                Reglas.ATAQUE_INICIAL, Reglas.ORO_INICIAL, Reglas.PISO_FINAL);
        aventureroDAO.insertar(aventurero);
        return aventurero;
    }
}
