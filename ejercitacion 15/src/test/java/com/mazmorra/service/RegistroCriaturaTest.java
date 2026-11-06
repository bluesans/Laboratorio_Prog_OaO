package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.exception.CriaturaDuplicadaException;
import com.mazmorra.exception.DatosCriaturaInvalidosException;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.service.impl.JuegoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroCriaturaTest {

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
    void registraUnaCriaturaValida() throws Exception {
        Criatura criatura = servicio.registrarCriatura(esqueleto("Esqueleto", 1));

        assertTrue(criatura.getId() > 0, "El alta debe devolver un id.");
        assertEquals(1, servicio.listarCriaturas().size());
    }

    @Test
    void rechazaNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> esqueleto("   ", 1));
    }

    @Test
    void rechazaNombreDemasiadoLargo() {
        String largo = "x".repeat(51);

        assertThrows(DatosCriaturaInvalidosException.class,
                () -> servicio.registrarCriatura(esqueleto(largo, 1)));
    }

    @Test
    void rechazaVidaMaximaNoPositiva() {
        assertThrows(IllegalArgumentException.class,
                () -> new Esqueleto(0, "Esqueleto", 0, 0, 2,
                        Reglas.DANIO_ESQUELETO, 1, false, 40));
    }

    @Test
    void rechazaVidaMayorQueLaMaxima() {
        assertThrows(IllegalArgumentException.class,
                () -> new Esqueleto(0, "Esqueleto", 30, 25, 2,
                        Reglas.DANIO_ESQUELETO, 1, false, 40));
    }

    @Test
    void rechazaPisoFueraDeRango() {
        assertThrows(IllegalArgumentException.class,
                () -> esqueleto("Esqueleto", 0));
        assertThrows(DatosCriaturaInvalidosException.class,
                () -> servicio.registrarCriatura(esqueleto("Esqueleto", Reglas.PISO_FINAL + 1)));
    }

    @Test
    void elDragonSoloPuedeEstarEnElUltimoPiso() {
        assertThrows(DatosCriaturaInvalidosException.class,
                () -> servicio.registrarCriatura(dragon("Jefe Dragón", 1)));
    }

    @Test
    void soloPuedeHaberUnDragon() throws Exception {
        servicio.registrarCriatura(dragon("Jefe Dragón", Reglas.PISO_FINAL));

        assertThrows(CriaturaDuplicadaException.class,
                () -> servicio.registrarCriatura(dragon("Otro Dragón", Reglas.PISO_FINAL)));
    }

    @Test
    void noPermiteUnSegundoDragonAunqueElPrimeroEsteDerrotado() throws Exception {
        Criatura primero = servicio.registrarCriatura(
                dragon("Jefe Dragón", Reglas.PISO_FINAL));
        primero.recibirDanio(999);
        criaturaDAO.actualizar(primero);

        assertThrows(CriaturaDuplicadaException.class,
                () -> servicio.registrarCriatura(dragon("Otro Dragón", Reglas.PISO_FINAL)));
    }

    @Test
    void noRepiteNombreEnElMismoPiso() throws Exception {
        servicio.registrarCriatura(esqueleto("Rata", 1));

        assertThrows(CriaturaDuplicadaException.class,
                () -> servicio.registrarCriatura(esqueleto("rata", 1)));
    }

    @Test
    void permiteElMismoNombreEnOtroPiso() throws Exception {
        servicio.registrarCriatura(esqueleto("Rata", 1));

        Criatura otra = servicio.registrarCriatura(esqueleto("Rata", 2));

        assertTrue(otra.getId() > 0, "El mismo nombre en otro piso debe permitirse.");
    }

    @Test
    void actualizaLosDatosDeUnaCriatura() throws Exception {
        Criatura criatura = servicio.registrarCriatura(esqueleto("Viejo", 1));
        Esqueleto cambiada = new Esqueleto(criatura.getId(), "Nuevo", 20, 30, 3,
                Reglas.DANIO_ESQUELETO, 2, false, 20);

        servicio.actualizarCriatura(cambiada);

        Criatura guardada = servicio.listarCriaturas().get(0);
        assertEquals("Nuevo", guardada.getNombre());
        assertEquals(2, guardada.getPisoMazmorra());
        assertEquals(30, guardada.getVidaMaxima());
    }

    @Test
    void actualizarUnaCriaturaInexistenteFalla() {
        Esqueleto cambiada = new Esqueleto(999, "Fantasma", 20, 20, 1,
                Reglas.DANIO_ESQUELETO, 1, false, 40);

        assertThrows(PersistenciaException.class,
                () -> servicio.actualizarCriatura(cambiada));
    }

    @Test
    void actualizarNoPermiteRepetirNombreEnElPiso() throws Exception {
        servicio.registrarCriatura(esqueleto("Uno", 1));
        Criatura segunda = servicio.registrarCriatura(esqueleto("Dos", 1));
        Esqueleto cambiada = new Esqueleto(segunda.getId(), "Uno", 25, 25, 2,
                Reglas.DANIO_ESQUELETO, 1, false, 40);

        assertThrows(CriaturaDuplicadaException.class,
                () -> servicio.actualizarCriatura(cambiada));
    }

    private Esqueleto esqueleto(String nombre, int piso) {
        return new Esqueleto(0, nombre, 25, 25, 2, Reglas.DANIO_ESQUELETO, piso, false,
                Reglas.RESISTENCIA_ESQUELETO);
    }

    private DragonJefe dragon(String nombre, int piso) {
        return new DragonJefe(0, nombre, 60, 60, 10, Reglas.DANIO_DRAGON, piso, false, 1,
                Reglas.FASES_DRAGON, false);
    }
}
