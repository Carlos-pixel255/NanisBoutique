package com.nanisboutique.view;

import com.nanisboutique.dao.BoutiqueDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class InventarioPanel extends JPanel {
    private JTable tablaInventario;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> cmbFiltroTipo;
    private JComboBox<String> cmbFiltroTalla;
    private JTextField txtFiltroColor;
    private JButton btnBuscar;
    private BoutiqueDAO dao;

    public InventarioPanel() {
        dao = new BoutiqueDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Panel Superior de Filtros (Épica 2 - HU2)[cite: 5, 7]
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros de Inventario en Tiempo Real"));

        panelFiltros.add(new JLabel("Tipo:"));
        cmbFiltroTipo = new JComboBox<>(new String[]{"Todos", "Blusa", "Jeans", "Vestido", "Chaqueta"});
        panelFiltros.add(cmbFiltroTipo);

        panelFiltros.add(new JLabel("Talla:"));
        cmbFiltroTalla = new JComboBox<>(new String[]{"Todas", "XS", "S", "M", "L", "XL"});
        panelFiltros.add(cmbFiltroTalla);

        panelFiltros.add(new JLabel("Color:"));
        txtFiltroColor = new JTextField(10);
        panelFiltros.add(txtFiltroColor);

        btnBuscar = new JButton("Buscar / Filtrar");
        btnBuscar.setBackground(new Color(163, 109, 96)); // Tono Rose Gold corporativo
        btnBuscar.setForeground(Color.WHITE);
        panelFiltros.add(btnBuscar);

        add(panelFiltros, BorderLayout.NORTH);

        // 2. Tabla Central de Resultados
        String[] columnas = {"Código", "Tipo Prenda", "Talla", "Color", "Precio COP", "Stock Disponible"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaInventario = new JTable(modeloTabla);
        add(new JScrollPane(tablaInventario), BorderLayout.CENTER);

        // 3. Evento del Botón de Búsqueda
        btnBuscar.addActionListener((ActionEvent e) -> {
            cargarDatosInventario();
        });

        // Cargar datos iniciales al abrir el panel
        cargarDatosInventario();
    }

    private void cargarDatosInventario() {
        // Limpiar tabla antes de refrescar
        modeloTabla.setRowCount(0);

        String tipoSeleccionado = cmbFiltroTipo.getSelectedItem().toString();
        String tallaSeleccionada = cmbFiltroTalla.getSelectedItem().toString();
        String colorFiltro = txtFiltroColor.getText().trim().toLowerCase();

        // NOTA: Aquí el DAO consultará directamente la tabla PRODUCTOS en la Base de Datos
        // Ejemplo simulado de filas que coincidirían con los registros de la DB:
        Object[][] datosPrueba = {
            {"PRD-001", "Blusa", "M", "Rosa Pastel", 65000, 12},
            {"PRD-002", "Jeans", "S", "Azul Oscuro", 120000, 8},
            {"PRD-003", "Vestido", "L", "Negro Elegante", 145000, 5},
            {"PRD-004", "Chaqueta", "M", "Verde Olivo", 180000, 4}
        };

        boolean hayResultados = false;
        for (Object[] fila : datosPrueba) {
            String tipo = fila[1].toString();
            String talla = fila[2].toString();
            String color = fila[3].toString().toLowerCase();

            boolean matchTipo = tipoSeleccionado.equals("Todos") || tipo.equalsIgnoreCase(tipoSeleccionado);
            boolean matchTalla = tallaSeleccionada.equals("Todas") || talla.equalsIgnoreCase(tallaSeleccionada);
            boolean matchColor = colorFiltro.isEmpty() || color.contains(colorFiltro);

            if (matchTipo && matchTalla && matchColor) {
                modeloTabla.addRow(fila);
                hayResultados = true;
            }
        }

        if (!hayResultados) {
            JOptionPane.showMessageDialog(this, 
                "No se encontraron prendas en el inventario con los criterios seleccionados.", 
                "Información", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}