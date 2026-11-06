package com.mazmorra.service;

import com.mazmorra.exception.AventureroDerrotadoException;
import com.mazmorra.exception.CriaturaDuplicadaException;
import com.mazmorra.exception.CriaturaEvadidaException;
import com.mazmorra.exception.CriaturaYaDerrotadaException;
import com.mazmorra.exception.CriaturasPendientesException;
import com.mazmorra.exception.DanioInvalidoException;
import com.mazmorra.exception.DatosCriaturaInvalidosException;
import com.mazmorra.exception.HuidaProhibidaException;
import com.mazmorra.exception.JuegoException;
import com.mazmorra.exception.OroInsuficienteException;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.exception.VidaCompletaException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.Danable;
import com.mazmorra.model.TipoAtaque;

import java.util.List;

public interface JuegoService {

    Aventurero registrarAventurero(String nombre, int vidaMaxima, int ataqueBase)
            throws PersistenciaException;

    List<Aventurero> listarAventureros() throws PersistenciaException;

    Criatura registrarCriatura(Criatura criatura)
            throws DatosCriaturaInvalidosException, CriaturaDuplicadaException,
            PersistenciaException;

    void actualizarCriatura(Criatura criatura)
            throws DatosCriaturaInvalidosException, CriaturaDuplicadaException,
            PersistenciaException;

    List<Criatura> listarCriaturasPorPiso(int piso) throws PersistenciaException;

    List<Criatura> listarCriaturas() throws PersistenciaException;

    void eliminarCriatura(int id) throws PersistenciaException;

    // Recibe la interfaz: el mismo metodo ataca criaturas y aventureros.
    void resolverAtaque(Danable objetivo, int danio, TipoAtaque tipoAtaque)
            throws DanioInvalidoException, CriaturaYaDerrotadaException,
            CriaturaEvadidaException, AventureroDerrotadoException,
            PersistenciaException;

    // Ataque, respuesta y recompensa en una sola transaccion.
    void resolverTurno(Aventurero aventurero, Criatura criatura, TipoAtaque tipo)
            throws AventureroDerrotadoException, CriaturaYaDerrotadaException,
            CriaturaEvadidaException, DanioInvalidoException,
            PersistenciaException;

    // Devuelve el danio aplicado por la criatura; 0 si ya estaba fuera de combate.
    int resolverAtaqueEnemigo(Aventurero aventurero, Criatura criatura)
            throws AventureroDerrotadoException, PersistenciaException;

    // Verdadero solo si el Jefe Dragon del ultimo piso ya fue derrotado.
    boolean esVictoria(int idAventurero) throws PersistenciaException;

    void avanzarPiso(Aventurero aventurero)
            throws CriaturasPendientesException, AventureroDerrotadoException,
            PersistenciaException;

    void huirDeCombate(Aventurero aventurero, Criatura criatura)
            throws HuidaProhibidaException, AventureroDerrotadoException,
            PersistenciaException;

    // Devuelve el resultado de la poción y la criatura que respondió, si había alguna.
    ResultadoPocion usarPocion(Aventurero aventurero, int cantidad)
            throws VidaCompletaException, AventureroDerrotadoException,
            OroInsuficienteException, PersistenciaException;

    // Solo responde la criatura indicada; si es null, la poción no recibe golpe.
    ResultadoPocion usarPocion(Aventurero aventurero, int cantidad, Criatura enemigo)
            throws VidaCompletaException, AventureroDerrotadoException,
            OroInsuficienteException, PersistenciaException;

    Aventurero obtenerEstadoAventurero(int idAventurero) throws PersistenciaException;

    // Reinicia, siembra el escenario e inserta al aventurero en una sola transacción.
    int iniciarNuevaPartida(String nombre, int vidaMaxima, int ataqueBase,
            List<Criatura> escenario) throws JuegoException;
}
