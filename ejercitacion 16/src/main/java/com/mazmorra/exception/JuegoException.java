package com.mazmorra.exception;

public class JuegoException extends Exception {

    public JuegoException(String mensaje) {
        super(mensaje);
    }

    public JuegoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
