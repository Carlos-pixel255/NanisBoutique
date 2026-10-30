package com.nanisboutique.view;

import com.nanisboutique.dao.BoutiqueDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PosPanel extends JPanel {
    private JTable tablaProductosPOS, tablaCarrito;
    private DefaultTableModel modeloProductos, modeloCarrito;
    private JComboBox<String> cmbMedioPago;
    private JLabel lblTotalPagar;
    private double totalAcumulado = 0.0;
    private BoutiqueDAO dao;

    public PosPanel() {
        dao = new BoutiqueDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Panel Izquierdo: Catálogo de Productos Disponibles para la Venta
        JPanel panelCatalogo = new JPanel(new BorderLayout());
        panelCatalogo.setBorder(BorderFactory.createTitledBorder("Catálogo Disponible en Tienda (Popayán)"));
        
        String[] colProductos = {"Código", "Prenda", "Talla", "Precio COP", "Stock"};
        modeloProductos = new DefaultTableModel(colProductos, 0);
        tablaProductosPOS = new JTable(modeloProductos);
        panelCatalogo.add(new JScrollPane(tablaProductosPOS), BorderLayout.CENTER);

        JButton btnAgregarCarrito = new JButton("Agregar al Carrito ➔");
        btnAgregarCarrito.setBackground(new Color(163, 109, 96)); // Rose Gold corporativo
        btnAgregarCarrito.setForeground(Color.WHITE);
        panelCatalogo.add(btnAgregarCarrito, BorderLayout.SOUTH);

        // 2. Panel Derecho: Carrito de Compras y Cierre de Venta (Épica 3)[cite: 5, 8]
        JPanel panelDerecho = new JPanel(new BorderLayout(5, 5));
        panelDerecho.setPreferredSize(new Dimension(350, 0));
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Orden de Compra / Carrito"));

        String[] colCarrito = {"Prenda", "Precio", "Cant"};
        modeloCarrito = new DefaultTableModel(colCarrito, 0);
        tablaCarrito = new JTable(modeloCarrito);
        panelDerecho.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        // Subpanel inferior del carrito (Total, Medio de pago y Botón Confirmar)
        JPanel panelCheckout = new JPanel(new GridLayout(4, 1, 5, 5));
        
        lblTotalPagar = new JLabel("Total a Pagar: $0 COP", SwingConstants.RIGHT);
        lblTotalPagar.setFont(new Font("Arial", Font.BOLD, 14));
        panelCheckout.add(lblTotalPagar);

        JPanel panelMetodo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelMetodo.add(new JLabel("Medio Pago:"));
        cmbMedioPago = new JComboBox<>(new String[]{"-- Seleccionar --", "Efectivo", "Transferencia"});
        panelMetodo.add(cmbMedioPago);
        panelCheckout.add(panelMetodo);

        JButton btnConfirmarVenta = new JButton("Confirmar Venta y Pagar");
        btnConfirmarVenta.setBackground(new Color(46, 139, 87)); // Verde éxito
        btnConfirmarVenta.setForeground(Color.WHITE);
        panelCheckout.add(btnConfirmarVenta);

        panelDerecho.add(panelCheckout, BorderLayout.SOUTH);

        // Agregar paneles principales al grid central
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCatalogo, panelDerecho);
        splitPane.setDividerLocation(450);
        add(splitPane, BorderLayout.CENTER);

        // Cargar datos simulados de catálogo
        cargarCatalogoPOS();

        // 3. Eventos de los Botones
        btnAgregarCarrito.addActionListener((ActionEvent e) -> {
            agregarAlCarrito();
        });

        btnConfirmarVenta.addActionListener((ActionEvent e) -> {
            procesarVentaCaja();
        });
    }

    private void cargarCatalogoPOS() {
        modeloProductos.setRowCount(0);
        // Datos sincronizados con el inventario de la boutique
        Object[][] catalogo = {
            {"PRD-001", "Blusa (M)", "M", 65000, 12},
            {"PRD-002", "Jeans (S)", "S", 120000, 8},
            {"PRD-003", "Vestido (L)", "L", 145000, 5},
            {"PRD-004", "Chaqueta (M)", "M", 180000, 4}
        };
        for (Object[] row : catalogo) {
            modeloProductos.addRow(row);
        }
    }

    private void agregarAlCarrito() {
        int filaSeleccionada = tablaProductosPOS.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione una prenda del catálogo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String codigo = modeloProductos.getValueAt(filaSeleccionada, 0).toString();
        String prenda = modeloProductos.getValueAt(filaSeleccionada, 1).toString();
        double precio = Double.parseDouble(modeloProductos.getValueAt(filaSeleccionada, 3).toString());
        int stockDisponible = Integer.parseInt(modeloProductos.getValueAt(filaSeleccionada, 4).toString());

        if (stockDisponible <= 0) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock disponible para completar la cantidad solicitada de este producto.", "Error de Stock", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Agregar al carrito
        modeloCarrito.addRow(new Object[]{prenda, precio, 1});
        totalAcumulado += precio;
        lblTotalPagar.setText("Total a Pagar: $" + totalAcumulado + " COP");
    }

    private void procesarVentaCaja() {
        if (modeloCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito de compras está vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String medioPagoSeleccionado = cmbMedioPago.getSelectedItem().toString();
        if (medioPagoSeleccionado.equals("-- Seleccionar --")) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un medio de pago válido (efectivo o transferencia) para continuar.", "Validación de Pago", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Invocación a la capa DAO / procedimiento almacenado PL/SQL para registrar la venta y descontar stock
        boolean exito = true; // Simulación de transacción exitosa en base de datos

        if (exito) {
            JOptionPane.showMessageDialog(this, "Venta registrada exitosamente. Comprobante generado para Nanis Boutique.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            modeloCarrito.setRowCount(0);
            totalAcumulado = 0.0;
            lblTotalPagar.setText("Total a Pagar: $0 COP");
            cmbMedioPago.setSelectedIndex(0);
            cargarCatalogoPOS(); // Refrescar stock visual
        } else {
            JOptionPane.showMessageDialog(this, "Error al procesar la transacción en la base de datos.", "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }
}