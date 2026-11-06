package com.mazmorra.ui;

import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Criatura;
import com.mazmorra.service.JuegoService;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ModeloTablaCriaturas extends AbstractTableModel {

    private static final String[] COLUMNAS = {
        "ID", "Nombre", "Tipo", "Piso", "Vida", "Amenaza", "Daño", "Estado"
    };

    private final JuegoService servicio;
    private List<Criatura> criaturas = List.of();

    public ModeloTablaCriaturas(JuegoService servicio) {
        this.servicio = servicio;
    }

    public void recargar() throws PersistenciaException {
        criaturas = servicio.listarCriaturas();
        fireTableDataChanged();
    }

    public void vaciar() {
        criaturas = List.of();
        fireTableDataChanged();
    }

    public Criatura getCriatura(int fila) {
        return criaturas.get(fila);
    }

    @Override
    public int getRowCount() {
        return criaturas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNAS.length;
    }

    @Override
    public String getColumnName(int columna) {
        return COLUMNAS[columna];
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Criatura criatura = criaturas.get(fila);
        return switch (columna) {
            case 0 -> criatura.getId();
            case 1 -> criatura.getNombre();
            case 2 -> criatura.getTipo().name();
            case 3 -> criatura.getPisoMazmorra();
            case 4 -> criatura.getVida() + "/" + criatura.getVidaMaxima();
            case 5 -> criatura.getNivelAmenaza();
            case 6 -> criatura.getDanioAtaqueBase();
            default -> estado(criatura);
        };
    }

    private static String estado(Criatura criatura) {
        if (criatura.estaDerrotado()) {
            return "Derrotada";
        }
        if (criatura.isEvadida()) {
            return "Evadida";
        }
        return "Viva";
    }
}
