package com.nanisboutique.view;

import com.nanisboutique.dao.BoutiqueDAO;
import com.nanisboutique.models.ParametroTienda;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Administración de parámetros del negocio almacenados en PARAMETROS_TIENDA. */
public class ConfiguracionTiendaPanel extends JPanel {
    private final BoutiqueDAO dao = new BoutiqueDAO();
    private final Map<String, ParametroTienda> parametrosPorId = new LinkedHashMap<>();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Clave", "Parámetro", "Valor actual", "Tipo", "Descripción"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtValor = new JTextField(35);
    private final JLabel lblSeleccion = new JLabel("Seleccione un parámetro de la tabla para modificar su valor.");
    private final JButton btnGuardar = new JButton("Guardar cambios");

    public ConfiguracionTiendaPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel descripcion = new JLabel("Configuración general de Nanis Boutique. Este panel no modifica el esquema físico de Oracle.");
        descripcion.setBorder(BorderFactory.createEmptyBorder(2, 2, 8, 2));
        add(descripcion, BorderLayout.NORTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel edicion = new JPanel(new GridBagLayout());
        edicion.setBorder(BorderFactory.createTitledBorder("Editar parámetro seleccionado"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 5); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; edicion.add(lblSeleccion, c);
        c.gridx = 0; c.gridy = 1; c.gridwidth = 1; edicion.add(new JLabel("Nuevo valor:"), c);
        c.gridx = 1; c.weightx = 1; edicion.add(txtValor, c);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRecargar = new JButton("Recargar");
        btnGuardar.setBackground(new Color(46, 139, 87)); btnGuardar.setForeground(Color.WHITE);
        acciones.add(btnRecargar); acciones.add(btnGuardar);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.weightx = 0; edicion.add(acciones, c);
        add(edicion, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) cargarSeleccion();
        });
        btnRecargar.addActionListener(e -> cargarParametros());
        btnGuardar.addActionListener(e -> guardarParametro());
        btnGuardar.setEnabled(false);
        cargarParametros();
    }

    private void cargarParametros() {
        try {
            List<ParametroTienda> lista = dao.obtenerParametrosTienda();
            modelo.setRowCount(0);
            parametrosPorId.clear();
            for (ParametroTienda p : lista) {
                parametrosPorId.put(p.getId(), p);
                modelo.addRow(new Object[]{p.getId(), p.getNombre(), p.getValor(), p.getTipoDato(), p.getDescripcion()});
            }
            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay parámetros de tienda registrados. Revise el script SQL de instalación.",
                        "Sin parámetros", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No fue posible cargar los parámetros de tienda.\n" + ex.getMessage(),
                    "Error de configuración", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSeleccion() {
        int vista = tabla.getSelectedRow();
        if (vista < 0) return;
        int fila = tabla.convertRowIndexToModel(vista);
        String id = String.valueOf(modelo.getValueAt(fila, 0));
        ParametroTienda p = parametrosPorId.get(id);
        if (p == null) return;
        lblSeleccion.setText(p.getNombre() + " (" + p.getId() + ") — tipo: " + p.getTipoDato());
        txtValor.setText(p.getValor());
        btnGuardar.setEnabled(true);
    }

    private void guardarParametro() {
        int vista = tabla.getSelectedRow();
        if (vista < 0) return;
        int fila = tabla.convertRowIndexToModel(vista);
        String id = String.valueOf(modelo.getValueAt(fila, 0));
        ParametroTienda p = parametrosPorId.get(id);
        if (p == null) return;
        try {
            dao.actualizarParametroTienda(p, txtValor.getText());
            p.setValor(txtValor.getText().trim());
            JOptionPane.showMessageDialog(this, "Parámetros de la tienda actualizados correctamente.", "Configuración guardada", JOptionPane.INFORMATION_MESSAGE);
            cargarParametros();
            seleccionarPorId(id);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el parámetro.\n" + ex.getMessage(),
                    "Error de configuración", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarPorId(String id) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (id.equals(String.valueOf(modelo.getValueAt(i, 0)))) {
                int vista = tabla.convertRowIndexToView(i);
                tabla.setRowSelectionInterval(vista, vista);
                break;
            }
        }
    }
}
