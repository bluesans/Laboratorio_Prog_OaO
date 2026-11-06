package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.exception.AventureroDerrotadoException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.model.TipoAtaque;
import com.mazmorra.service.impl.JuegoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AventureroDerrotadoTest {

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
    void noPuedeAtacar() throws Exception {
        Aventurero aventurero = crearAventureroDerrotado();
        Esqueleto esqueleto = crearEsqueleto();

        assertThrows(AventureroDerrotadoException.class,
                () -> servicio.resolverTurno(aventurero, esqueleto, TipoAtaque.FISICO));
    }

    @Test
    void noPuedeUsarPocion() throws Exception {
        Aventurero aventurero = crearAventureroDerrotado();

        assertThrows(AventureroDerrotadoException.class,
                () -> servicio.usarPocion(aventurero, Reglas.TAMANO_POCION));
    }

    @Test
    void noPuedeHuir() throws Exception {
        Aventurero aventurero = crearAventureroDerrotado();
        Esqueleto esqueleto = crearEsqueleto();

        assertThrows(AventureroDerrotadoException.class,
                () -> servicio.huirDeCombate(aventurero, esqueleto));
    }

    @Test
    void noPuedeAvanzarDePiso() throws Exception {
        Aventurero aventurero = crearAventureroDerrotado();

        assertThrows(AventureroDerrotadoException.class,
                () -> servicio.avanzarPiso(aventurero));
    }

    @Test
    void noPuedeRecibirAtaques() throws Exception {
        Aventurero aventurero = crearAventureroDerrotado();
        Esqueleto esqueleto = crearEsqueleto();

        assertThrows(AventureroDerrotadoException.class,
                () -> servicio.resolverAtaqueEnemigo(aventurero, esqueleto));
    }

    private Aventurero crearAventureroDerrotado() throws Exception {
        Aventurero aventurero = new Aventurero(0, "Aventurero", 1, 0, Reglas.VIDA_INICIAL,
                Reglas.ATAQUE_INICIAL, Reglas.ORO_INICIAL, Reglas.PISO_INICIAL);
        aventureroDAO.insertar(aventurero);
        return aventurero;
    }

    private Esqueleto crearEsqueleto() {
        return new Esqueleto(1, "Esqueleto", 25, 25, 2, Reglas.DANIO_ESQUELETO, 1, false,
                Reglas.RESISTENCIA_ESQUELETO);
    }
}
