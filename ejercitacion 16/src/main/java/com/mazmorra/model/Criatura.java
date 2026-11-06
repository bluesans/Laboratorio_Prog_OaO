package com.mazmorra.model;

public abstract class Criatura implements Danable {

    private int id;
    private String nombre;
    private int vida;
    private int vidaMaxima;
    private int nivelAmenaza;
    private int danioAtaque;
    private int pisoMazmorra;
    private boolean derrotada;
    private boolean evadida;
    private boolean recompensaPagada;

    protected Criatura(int id, String nombre, int vida, int vidaMaxima,
            int nivelAmenaza, int danioAtaque, int pisoMazmorra, boolean derrotada) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la criatura no puede ser nulo ni vacío.");
        }
        if (vidaMaxima <= 0) {
            throw new IllegalArgumentException("La vida máxima debe ser mayor que cero.");
        }
        if (vida < 0) {
            throw new IllegalArgumentException("La vida no puede ser negativa.");
        }
        if (vida > vidaMaxima) {
            throw new IllegalArgumentException("La vida no puede superar la vida máxima.");
        }
        if (nivelAmenaza < 0) {
            throw new IllegalArgumentException("El nivel de amenaza no puede ser negativo.");
        }
        if (danioAtaque < 0) {
            throw new IllegalArgumentException("El daño de ataque no puede ser negativo.");
        }
        if (pisoMazmorra < 1) {
            throw new IllegalArgumentException("El piso de mazmorra debe ser al menos 1.");
        }
        this.id = id;
        this.nombre = nombre;
        this.vida = vida;
        this.vidaMaxima = vidaMaxima;
        this.nivelAmenaza = nivelAmenaza;
        this.danioAtaque = danioAtaque;
        this.pisoMazmorra = pisoMazmorra;
        this.derrotada = derrotada;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id no puede ser negativo.");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public int getNivelAmenaza() {
        return nivelAmenaza;
    }

    public int getDanioAtaque() {
        return danioAtaque;
    }

    // El jefe recalcula su daño con la fase; la base es la que se guarda en la base.
    public int getDanioAtaqueBase() {
        return danioAtaque;
    }

    public int getPisoMazmorra() {
        return pisoMazmorra;
    }

    public boolean isDerrotada() {
        return derrotada;
    }

    public boolean isEvadida() {
        return evadida;
    }

    public void marcarEvadida() {
        this.evadida = true;
    }

    public boolean isRecompensaPagada() {
        return recompensaPagada;
    }

    // Solo lo usan el DAO al hidratar y el servicio al pagar: no hay setter booleano.
    public void marcarRecompensaPagada() {
        this.recompensaPagada = true;
    }

    @Override
    public void recibirDanio(int cantidad) {
        // Sin tipo de ataque se asume daño físico.
        recibirDanio(cantidad, TipoAtaque.FISICO);
    }

    public void recibirDanio(int cantidad, TipoAtaque tipoAtaque) {
        TipoAtaque tipo = tipoAtaque == null ? TipoAtaque.FISICO : tipoAtaque;
        if (cantidad <= 0 || estaDerrotado()) {
            return;
        }
        try {
            int efectivo = Math.max(0, calcularDanioEfectivo(cantidad, tipo));
            this.vida = Math.max(0, this.vida - efectivo);
            if (this.vida == 0) {
                this.derrotada = true;
            }
        } finally {
            alRecibirDanio();
        }
    }

    protected abstract int calcularDanioEfectivo(int cantidad, TipoAtaque tipoAtaque);

    protected void alRecibirDanio() {
    }

    public abstract TipoCriatura getTipo();

    public boolean permiteHuida() {
        return true;
    }

    public boolean estaViva() {
        // Una criatura evadida quedó atrás: no pelea ni bloquea el piso.
        return !estaDerrotado() && !evadida;
    }

    @Override
    public boolean estaDerrotado() {
        // Vale el flag persistido o la vida en cero.
        return derrotada || vida <= 0;
    }

    @Override
    public String toString() {
        return String.format(
                "%s[id=%d, nombre=%s, tipo=%s, vida=%d/%d, amenaza=%d, piso=%d, derrotada=%b]",
                getClass().getSimpleName(), id, nombre, getTipo(), vida, vidaMaxima,
                nivelAmenaza, pisoMazmorra, estaDerrotado());
    }
}
