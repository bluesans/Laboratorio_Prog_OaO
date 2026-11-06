package com.mazmorra.ui;

import com.mazmorra.exception.JuegoException;
import com.mazmorra.exception.PersistenciaException;
import com.mazmorra.model.Criatura;
import com.mazmorra.model.DragonJefe;
import com.mazmorra.model.Esqueleto;
import com.mazmorra.model.Fantasma;
import com.mazmorra.model.Orco;
import com.mazmorra.model.TipoCriatura;
import com.mazmorra.service.JuegoService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

public class PanelAdministracion extends JPanel {

    private final JuegoService servicio;
    private final ModeloTablaCriaturas modelo;
    private final JTable tabla;

    private final JComboBox<String> tipoCombo = new JComboBox<>(
            new String[]{"Esqueleto", "Fantasma", "Orco", "Jefe Dragón"});
    private final JTextField nombreField = new JTextField(14);
    private final JTextField vidaField = new JTextField("20", 4);
    private final JTextField vidaMaxField = new JTextField("20", 4);
    private final JTextField amenazaField = new JTextField("1", 3);
    private final JTextField pisoField = new JTextField("1", 3);
    private final JTextField danioField = new JTextField("10", 3);
    private final JTextField extraField = new JTextField("40", 4);
    private final JLabel extraLabel = new JLabel("Resistencia física (%):");
    private final JCheckBox incorporeoCheck = new JCheckBox("Incorpóreo", true);

    private final JButton guardarBoton = new JButton("Guardar");
    private final JButton limpiarBoton = new JButton("Limpiar");
    private final JButton eliminarBoton = new JButton("Eliminar");

    private Criatura criaturaEditando;

    public PanelAdministracion(JuegoService servicio) {
        this.servicio = servicio;
        this.modelo = new ModeloTablaCriaturas(servicio);
        this.tabla = new JTable(modelo);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        add(crearFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        configurarTabla();
        conectarEventos();
        actualizarFormulario();
        recargar();
    }

    public void alMostrar() {
        recargar();
    }

    private JComponent crearFormulario() {
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Registrar o modificar criatura"));

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        fila1.add(new JLabel("Tipo:"));
        fila1.add(tipoCombo);
        fila1.add(new JLabel("Nombre:"));
        fila1.add(nombreField);
        fila1.add(new JLabel("Vida:"));
        fila1.add(vidaField);
        fila1.add(new JLabel("Vida máxima:"));
        fila1.add(vidaMaxField);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        fila2.add(new JLabel("Amenaza:"));
        fila2.add(amenazaField);
        fila2.add(new JLabel("Piso:"));
        fila2.add(pisoField);
        fila2.add(new JLabel("Daño:"));
        fila2.add(danioField);
        fila2.add(extraLabel);
        fila2.add(extraField);
        fila2.add(incorporeoCheck);

        panel.add(fila1);
        panel.add(fila2);
        return panel;
    }

    private JComponent crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.add(guardarBoton);
        panel.add(limpiarBoton);
        panel.add(eliminarBoton);
        return panel;
    }

    private void configurarTabla() {
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(80);
    }

    private void conectarEventos() {
        tipoCombo.addActionListener(e -> actualizarFormulario());
        guardarBoton.addActionListener(e -> guardar());
        limpiarBoton.addActionListener(e -> limpiar());
        eliminarBoton.addActionListener(e -> eliminar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
    }

    private void guardar() {
        try {
            Criatura criatura = construirCriatura();
            if (criaturaEditando == null) {
                servicio.registrarCriatura(criatura);
                aviso("Criatura registrada.");
            } else {
                servicio.actualizarCriatura(criatura);
                aviso("Criatura actualizada.");
            }
            limpiar();
            recargar();
        } catch (JuegoException | RuntimeException e) {
            DialogosError.mostrar(this, e);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            aviso("Elegí una criatura de la tabla.");
            return;
        }
        Criatura criatura = modelo.getCriatura(tabla.convertRowIndexToModel(fila));
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + criatura.getNombre() + "?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.eliminarCriatura(criatura.getId());
            limpiar();
            recargar();
        } catch (JuegoException | RuntimeException e) {
            DialogosError.mostrar(this, e);
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        criaturaEditando = modelo.getCriatura(tabla.convertRowIndexToModel(fila));
        tipoCombo.setSelectedIndex(indiceDe(criaturaEditando.getTipo()));
        actualizarFormulario();

        nombreField.setText(criaturaEditando.getNombre());
        vidaField.setText(String.valueOf(criaturaEditando.getVida()));
        vidaMaxField.setText(String.valueOf(criaturaEditando.getVidaMaxima()));
        amenazaField.setText(String.valueOf(criaturaEditando.getNivelAmenaza()));
        pisoField.setText(String.valueOf(criaturaEditando.getPisoMazmorra()));
        danioField.setText(String.valueOf(criaturaEditando.getDanioAtaqueBase()));

        switch (criaturaEditando.getTipo()) {
            case ESQUELETO -> extraField.setText(String.valueOf(
                    ((Esqueleto) criaturaEditando).getResistenciaFisica()));
            case FANTASMA -> incorporeoCheck.setSelected(
                    ((Fantasma) criaturaEditando).isEsIncorporeo());
            case ORCO -> extraField.setText(String.valueOf(
                    ((Orco) criaturaEditando).getResistenciaMagica()));
            case DRAGON_JEFE -> extraField.setText(String.valueOf(
                    ((DragonJefe) criaturaEditando).getCantidadFases()));
            default -> {
            }
        }
        guardarBoton.setText("Guardar cambios");
    }

    private Criatura construirCriatura() {
        String nombre = nombreField.getText().trim();
        int vida = leerEntero(vidaField, "vida");
        int vidaMaxima = leerEntero(vidaMaxField, "vida máxima");
        int amenaza = leerEntero(amenazaField, "amenaza");
        int piso = leerEntero(pisoField, "piso");
        int danio = leerEntero(danioField, "daño");
        int extra = extraField.isVisible() ? leerEntero(extraField, "campo del tipo") : 0;
        return construir(criaturaEditando, nombre, vida, vidaMaxima, amenaza, danio, piso,
                tipoCombo.getSelectedIndex(), extra, incorporeoCheck.isSelected());
    }

    static Criatura construir(Criatura original, String nombre, int vida, int vidaMaxima,
            int amenaza, int danio, int piso, int tipo, int extra, boolean incorporeo) {
        int id = original == null ? 0 : original.getId();
        boolean derrotada = original != null && original.estaDerrotado();

        int fase = 1;
        int fases = Math.max(1, extra);
        boolean aliento = false;
        if (original instanceof DragonJefe dragon) {
            fase = Math.min(dragon.getFaseActual(), fases);
            aliento = dragon.isAlientoDeFuego();
        }

        Criatura criatura = switch (tipo) {
            case 0 -> new Esqueleto(id, nombre, vida, vidaMaxima, amenaza, danio, piso,
                    derrotada, extra);
            case 1 -> new Fantasma(id, nombre, vida, vidaMaxima, amenaza, danio, piso,
                    derrotada, incorporeo);
            case 2 -> new Orco(id, nombre, vida, vidaMaxima, amenaza, danio, piso,
                    derrotada, extra);
            default -> new DragonJefe(id, nombre, vida, vidaMaxima, amenaza, danio, piso,
                    derrotada, fase, fases, aliento);
        };
        if (original != null) {
            if (original.isEvadida()) {
                criatura.marcarEvadida();
            }
            if (original.isRecompensaPagada()) {
                criatura.marcarRecompensaPagada();
            }
        }
        return criatura;
    }

    private int leerEntero(JTextField campo, String nombre) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("El campo \"" + nombre + "\" es obligatorio.");
        }
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El campo \"" + nombre + "\" debe ser numérico.");
        }
    }

    private void actualizarFormulario() {
        switch (tipoCombo.getSelectedIndex()) {
            case 0 -> {
                extraLabel.setText("Resistencia física (%):");
                extraField.setText("40");
                mostrarExtra(true);
                incorporeoCheck.setVisible(false);
            }
            case 1 -> {
                mostrarExtra(false);
                incorporeoCheck.setVisible(true);
            }
            case 2 -> {
                extraLabel.setText("Resistencia mágica (%):");
                extraField.setText("50");
                mostrarExtra(true);
                incorporeoCheck.setVisible(false);
            }
            default -> {
                extraLabel.setText("Fases:");
                extraField.setText("3");
                mostrarExtra(true);
                incorporeoCheck.setVisible(false);
            }
        }
        revalidate();
        repaint();
    }

    private void mostrarExtra(boolean visible) {
        extraLabel.setVisible(visible);
        extraField.setVisible(visible);
    }

    private void limpiar() {
        criaturaEditando = null;
        nombreField.setText("");
        vidaField.setText("20");
        vidaMaxField.setText("20");
        amenazaField.setText("1");
        pisoField.setText("1");
        danioField.setText("10");
        tipoCombo.setSelectedIndex(0);
        actualizarFormulario();
        tabla.clearSelection();
        guardarBoton.setText("Guardar");
    }

    private void recargar() {
        try {
            modelo.recargar();
        } catch (PersistenciaException e) {
            modelo.vaciar();
            DialogosError.mostrar(this, e);
        }
    }

    private void aviso(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Administración",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static int indiceDe(TipoCriatura tipo) {
        return switch (tipo) {
            case ESQUELETO -> 0;
            case FANTASMA -> 1;
            case ORCO -> 2;
            case DRAGON_JEFE -> 3;
        };
    }
}
