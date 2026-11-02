package com.mazmorra.service;

import com.mazmorra.model.Criatura;

/**
 * Lo que dejó una poción: curación, costo y la respuesta del enemigo.
 */
public record ResultadoPocion(int vidaCurada, int oroPagado, Criatura enemigo, int danioRecibido) {
}
