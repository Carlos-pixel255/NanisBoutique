package com.nanisboutique.view;

import com.nanisboutique.dao.BoutiqueDAO;
import com.nanisboutique.models.DetalleVenta;
import com.nanisboutique.models.Empleado;
import com.nanisboutique.models.Producto;
import com.nanisboutique.models.Venta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Punto de venta integrado con Oracle. Los precios y existencias se vuelven a verificar al confirmar. */
public class PosPanel extends JPanel {
    private final BoutiqueDAO dao = new BoutiqueDAO();
    private final Empleado empleadoSesion;
    private final DefaultTableModel modeloProductos = new DefaultTableModel(
            new Object[]{"Código", "Tipo", "Talla", "Color", "Precio COP", "Stock"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel modeloCarrito = new DefaultTableModel(
            new Object[]{"Código", "Tipo", "Talla", "Color", "Precio unitario", "Cantidad", "Subtotal"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tablaProductos = new JTable(modeloProductos);
    private final JTable tablaCarrito = new JTable(modeloCarrito);
    private final JComboBox<String> cmbMedioPago = new JComboBox<>(new String[]{"-- Seleccionar --", "Efectivo", "Transferencia"});
    private final JLabel lblTotalPagar = new JLabel("Total a pagar: $0 COP", SwingConstants.RIGHT);

    public PosPanel(Empleado empleadoSesion) {
        this.empleadoSesion = empleadoSesion;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel catalogo = new JPanel(new BorderLayout(5, 5));
        catalogo.setBorder(BorderFactory.createTitledBorder("Catálogo disponible en Nanis Boutique"));
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setAutoCreateRowSorter(true);
        catalogo.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
        JPanel accionesCatalogo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregar = new JButton("Agregar una unidad al carrito");
        JButton btnActualizarCatalogo = new JButton("Actualizar catálogo");
        btnAgregar.setBackground(new Color(163, 109, 96)); btnAgregar.setForeground(Color.WHITE);
        accionesCatalogo.add(btnAgregar); accionesCatalogo.add(btnActualizarCatalogo);
        catalogo.add(accionesCatalogo, BorderLayout.SOUTH);

        JPanel derecha = new JPanel(new BorderLayout(5, 5));
        derecha.setPreferredSize(new Dimension(430, 0));
        derecha.setBorder(BorderFactory.createTitledBorder("Carrito de venta"));
        tablaCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        derecha.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        JPanel checkout = new JPanel(new GridLayout(0, 1, 5, 5));
        lblTotalPagar.setFont(new Font("Arial", Font.BOLD, 15));
        checkout.add(lblTotalPagar);
        JPanel pago = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pago.add(new JLabel("Medio de pago:")); pago.add(cmbMedioPago);
        checkout.add(pago);
        JPanel botonesCarrito = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnQuitar = new JButton("Quitar seleccionado");
        JButton btnVaciar = new JButton("Vaciar carrito");
        botonesCarrito.add(btnQuitar); botonesCarrito.add(btnVaciar);
        checkout.add(botonesCarrito);
        JButton btnConfirmar = new JButton("Confirmar venta");
        btnConfirmar.setBackground(new Color(46, 139, 87)); btnConfirmar.setForeground(Color.WHITE);
        checkout.add(btnConfirmar);
        derecha.add(checkout, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catalogo, derecha);
        split.setResizeWeight(0.55);
        split.setDividerLocation(500);
        add(split, BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> agregarAlCarrito());
        btnActualizarCatalogo.addActionListener(e -> cargarCatalogo());
        btnQuitar.addActionListener(e -> quitarDelCarrito());
        btnVaciar.addActionListener(e -> vaciarCarrito());
        btnConfirmar.addActionListener(e -> procesarVenta());
        cargarCatalogo();
    }

    private void cargarCatalogo() {
        try {
            List<Producto> productos = dao.obtenerProductos("", "", "");
            modeloProductos.setRowCount(0);
            for (Producto p : productos) {
                modeloProductos.addRow(new Object[]{p.getIdProducto(), p.getTipo(), p.getTalla(), p.getColor(), p.getPrecio(), p.getStock()});
            }
            if (productos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay productos registrados en el inventario.", "Catálogo vacío", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No fue posible cargar el catálogo desde Oracle.\n" + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarAlCarrito() {
        int vista = tablaProductos.getSelectedRow();
        if (vista < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una prenda del catálogo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int fila = tablaProductos.convertRowIndexToModel(vista);
        String codigo = String.valueOf(modeloProductos.getValueAt(fila, 0));
        String tipo = String.valueOf(modeloProductos.getValueAt(fila, 1));
        String talla = String.valueOf(modeloProductos.getValueAt(fila, 2));
        String color = String.valueOf(modeloProductos.getValueAt(fila, 3));
        double precio = ((Number) modeloProductos.getValueAt(fila, 4)).doubleValue();
        int stock = ((Number) modeloProductos.getValueAt(fila, 5)).intValue();
        if (stock <= 0) {
            JOptionPane.showMessageDialog(this, "Este producto está agotado.", "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaCarrito = buscarEnCarrito(codigo);
        if (filaCarrito >= 0) {
            int cantidadActual = ((Number) modeloCarrito.getValueAt(filaCarrito, 5)).intValue();
            if (cantidadActual + 1 > stock) {
                JOptionPane.showMessageDialog(this, "No hay suficiente stock disponible. Existencia actual: " + stock + ".",
                        "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int nuevaCantidad = cantidadActual + 1;
            modeloCarrito.setValueAt(nuevaCantidad, filaCarrito, 5);
            modeloCarrito.setValueAt(precio * nuevaCantidad, filaCarrito, 6);
        } else {
            modeloCarrito.addRow(new Object[]{codigo, tipo, talla, color, precio, 1, precio});
        }
        actualizarTotal();
    }

    private int buscarEnCarrito(String codigo) {
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            if (codigo.equals(String.valueOf(modeloCarrito.getValueAt(i, 0)))) return i;
        }
        return -1;
    }

    private void quitarDelCarrito() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una línea del carrito para quitarla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modeloCarrito.removeRow(tablaCarrito.convertRowIndexToModel(fila));
        actualizarTotal();
    }

    private void vaciarCarrito() {
        modeloCarrito.setRowCount(0);
        actualizarTotal();
    }

    private void actualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            total = total.add(BigDecimal.valueOf(((Number) modeloCarrito.getValueAt(i, 6)).doubleValue()));
        }
        lblTotalPagar.setText("Total a pagar: " + moneda(total.doubleValue()));
    }

    private void procesarVenta() {
        if (modeloCarrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "El carrito de compras está vacío.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String medioPago = String.valueOf(cmbMedioPago.getSelectedItem());
        if (!List.of("Efectivo", "Transferencia").contains(medioPago)) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un medio de pago válido (efectivo o transferencia).",
                    "Validación de pago", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (empleadoSesion == null || empleadoSesion.getIdCedula() == null) {
            JOptionPane.showMessageDialog(this, "No fue posible identificar al empleado que registra la venta. Cierre sesión y vuelva a ingresar.",
                    "Sesión inválida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<DetalleVenta> items = new ArrayList<>();
        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            String codigo = String.valueOf(modeloCarrito.getValueAt(i, 0));
            int cantidad = ((Number) modeloCarrito.getValueAt(i, 5)).intValue();
            double precio = ((Number) modeloCarrito.getValueAt(i, 4)).doubleValue();
            items.add(new DetalleVenta(codigo, cantidad, precio));
        }

        try {
            Venta registrada = dao.registrarVenta(empleadoSesion.getIdCedula(), medioPago, items);
            JOptionPane.showMessageDialog(this,
                    "Venta registrada exitosamente.\nComprobante: " + registrada.getIdVenta()
                            + "\nTotal: " + moneda(registrada.getTotalVenta()),
                    "Venta completada", JOptionPane.INFORMATION_MESSAGE);
            vaciarCarrito();
            cmbMedioPago.setSelectedIndex(0);
            cargarCatalogo();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo completar la venta. No se conservaron cambios parciales.\n" + ex.getMessage(),
                    "Venta no registrada", JOptionPane.ERROR_MESSAGE);
            cargarCatalogo();
        }
    }

    private String moneda(double valor) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        return nf.format(valor);
    }
}
