package com.mazmorra.service;

import com.mazmorra.exception.AventureroDerrotadoException;
import com.mazmorra.exception.CriaturaEvadidaException;
import com.mazmorra.exception.CriaturaYaDerrotadaException;
import com.mazmorra.exception.CriaturasPendientesException;
import com.mazmorra.exception.DanioInvalidoException;
import com.mazmorra.exception.HuidaProhibidaException;
import com.mazmorra.exception.OroInsuficienteException;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.exception.PersonajeDuplicadoException;
import com.mazmorra.exception.PisoFinalException;
import com.mazmorra.exception.VidaCompletaException;
import com.mazmorra.model.Aventurero;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.Partida;
import com.mazmorra.model.Personaje;
import com.mazmorra.model.TipoAtaque;

import java.util.List;

public interface JuegoService {

    List<Aventurero> listarAventureros() throws PersistenciaException;

    List<Criatura> listarCriaturasPorPiso(int piso) throws PersistenciaException;

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
            PisoFinalException, PersistenciaException;

    void huirDeCombate(Aventurero aventurero, Criatura criatura)
            throws HuidaProhibidaException, AventureroDerrotadoException,
            PersistenciaException;

    // Devuelve el resultado de la poción y la criatura que respondió, si había alguna.
    ResultadoPocion usarPocion(Aventurero aventurero, int cantidad, Criatura enemigo)
            throws VidaCompletaException, AventureroDerrotadoException,
            OroInsuficienteException, PersistenciaException;

    Aventurero obtenerEstadoAventurero(int idAventurero) throws PersistenciaException;

    Personaje obtenerPersonaje(int id) throws PersistenciaException;

    List<Personaje> listarRanking(int limite) throws PersistenciaException;

    List<Partida> listarPartidas(int personajeId) throws PersistenciaException;

    InicioPartida iniciarPartidaNueva(String nombre, int ronda, List<Criatura> escenario)
            throws PersistenciaException, PersonajeDuplicadoException;

    Aventurero iniciarPartida(Personaje personaje, int ronda, List<Criatura> escenario)
            throws PersistenciaException;

    int registrarResultado(Personaje personaje, Aventurero aventurero, String resultado)
            throws PersistenciaException;
}
