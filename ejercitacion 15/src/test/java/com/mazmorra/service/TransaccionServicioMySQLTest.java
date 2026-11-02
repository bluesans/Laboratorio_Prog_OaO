package com.mazmorra.service;

import com.mazmorra.Reglas;
import com.mazmorra.dao.AventureroDAO;
import com.mazmorra.dao.CriaturaDAO;
import com.mazmorra.dao.impl.AventureroDAOMySQL;
import com.mazmorra.dao.impl.CriaturaDAOMySQL;
import com.mazmorra.db.Transaccion;
import com.mazmorra.db.impl.TransaccionMySQL;
import com.mazmorra.exception.JuegoException;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.service.impl.JuegoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Solo se ejecuta con DB_URL definida; usuario y contraseña salen de DB_USUARIO/DB_PASSWORD o de db.properties.
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class TransaccionServicioMySQLTest {

    @Test
    void laHuidaNoCambiaNadaSiFallaLaSegundaEscritura() throws Exception {
        AventureroDAO aventureroDAO = new AventureroDAOMySQL();
        CriaturaDAO criaturaDAO = new CriaturaDAOMySQL();
        Aventurero aventurero = new Aventurero(
                0, "Huida sin cambios", 1, 100, 100, 20, 50, 1);
        Esqueleto esqueleto = new Esqueleto(
                0, "Esqueleto sin cambios", 50, 50, 1, Reglas.DANIO_ESQUELETO, 1, false,
                Reglas.RESISTENCIA_ESQUELETO);
        try {
            aventureroDAO.insertar(aventurero);
            criaturaDAO.insertar(esqueleto);

            Transaccion transaccion = new Transaccion() {
                @Override
                public <T> T ejecutar(Trabajo<T> trabajo) throws JuegoException {
                    // La criatura delega todo salvo la segunda escritura, que falla a propósito.
                    return new TransaccionMySQL().ejecutar((criaturas, aventureros) ->
                            trabajo.ejecutar(new CriaturaDAOFallida(criaturas), aventureros));
                }
            };
            JuegoServiceImpl servicio = new JuegoServiceImpl(
                    criaturaDAO, aventureroDAO, transaccion);

            assertThrows(PersistenciaException.class,
                    () -> servicio.huirDeCombate(aventurero, esqueleto),
                    "La segunda escritura fallida debe abortar la huida.");

            Aventurero persistido = aventureroDAO.buscarPorId(aventurero.getId()).orElseThrow();
            assertEquals(50, persistido.getOro(),
                    "El rollback debe devolver el oro a 50.");
            assertEquals(100, persistido.getVida(),
                    "El rollback debe devolver la vida a 100.");
            assertEquals(1, persistido.getPisoActual(),
                    "El rollback no debe mover al aventurero de piso.");

            Criatura criaturaPersistida = criaturaDAO
                    .buscarPorId(esqueleto.getId()).orElseThrow();
            assertFalse(criaturaPersistida.isEvadida(),
                    "El rollback no debe dejar al esqueleto como evadido.");
        } finally {
            if (aventurero.getId() > 0) {
                aventureroDAO.eliminar(aventurero.getId());
            }
            if (esqueleto.getId() > 0) {
                criaturaDAO.eliminar(esqueleto.getId());
            }
        }
    }

    private static final class CriaturaDAOFallida implements CriaturaDAO {

        private final CriaturaDAO delegada;

        private CriaturaDAOFallida(CriaturaDAO delegada) {
            this.delegada = delegada;
        }

        @Override
        public int insertar(Criatura criatura) throws PersistenciaException {
            return delegada.insertar(criatura);
        }

        @Override
        public boolean actualizar(Criatura criatura) throws PersistenciaException {
            throw new PersistenciaException("Fallo simulado en la segunda escritura.");
        }

        @Override
        public Optional<Criatura> buscarPorId(int id) throws PersistenciaException {
            return delegada.buscarPorId(id);
        }

        @Override
        public List<Criatura> listarPorPiso(int piso) throws PersistenciaException {
            return delegada.listarPorPiso(piso);
        }

        @Override
        public List<Criatura> listarTodas() throws PersistenciaException {
            return delegada.listarTodas();
        }

        @Override
        public boolean eliminar(int id) throws PersistenciaException {
            return delegada.eliminar(id);
        }

        @Override
        public int eliminarTodas() throws PersistenciaException {
            return delegada.eliminarTodas();
        }
    }
}
