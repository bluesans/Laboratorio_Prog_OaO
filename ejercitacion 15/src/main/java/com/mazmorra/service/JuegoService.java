package com.mazmorra.service;

import com.mazmorra.exception.AventureroDerrotadoException;
import com.mazmorra.exception.CriaturaEvadidaException;
import com.mazmorra.exception.CriaturaYaDerrotadaException;
import com.mazmorra.exception.CriaturasPendientesException;
import com.mazmorra.exception.DanioInvalidoException;
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

    Criatura registrarCriatura(Criatura criatura) throws PersistenciaException;

    List<Criatura> listarCriaturasPorPiso(int piso) throws PersistenciaException;

    List<Criatura> listarCriaturas() throws PersistenciaException;

    // Recibe la interfaz: el mismo método ataca criaturas y aventureros.
    void resolverAtaque(Danable objetivo, int danio, TipoAtaque tipoAtaque)
            throws DanioInvalidoException, CriaturaYaDerrotadaException,
            CriaturaEvadidaException, AventureroDerrotadoException,
            PersistenciaException;

    // Ataque, respuesta y recompensa en una sola transacción.
    void resolverTurno(Aventurero aventurero, Criatura criatura, TipoAtaque tipo)
            throws AventureroDerrotadoException, CriaturaYaDerrotadaException,
            CriaturaEvadidaException, DanioInvalidoException,
            PersistenciaException;

    void avanzarPiso(Aventurero aventurero)
            throws CriaturasPendientesException, AventureroDerrotadoException,
            PersistenciaException;

    void huirDeCombate(Aventurero aventurero, Criatura criatura)
            throws HuidaProhibidaException, AventureroDerrotadoException,
            PersistenciaException;

    void usarPocion(Aventurero aventurero, int cantidad, Criatura enemigo)
            throws VidaCompletaException, AventureroDerrotadoException,
            OroInsuficienteException, PersistenciaException;

    Aventurero obtenerEstadoAventurero(int idAventurero) throws PersistenciaException;

    // Reinicia, siembra el escenario e inserta al aventurero en una sola transacción.
    int iniciarNuevaPartida(String nombre, int vidaMaxima, int ataqueBase,
            List<Criatura> escenario) throws JuegoException;
}
