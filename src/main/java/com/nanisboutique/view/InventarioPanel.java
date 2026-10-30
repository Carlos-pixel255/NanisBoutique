package com.nanisboutique.view;

import com.nanisboutique.dao.BoutiqueDAO;
import com.nanisboutique.models.Empleado;
import com.nanisboutique.models.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** Consulta de inventario para todos los roles; registro y edición para Administrador. */
public class InventarioPanel extends JPanel {
    private final BoutiqueDAO dao = new BoutiqueDAO();
    private final Empleado sesion;
    private final JTextField txtFiltroTipo = new JTextField(10);
    private final JTextField txtFiltroTalla = new JTextField(5);
    private final JTextField txtFiltroColor = new JTextField(9);
    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtTipo = new JTextField(12);
    private final JTextField txtTalla = new JTextField(5);
    private final JTextField txtColor = new JTextField(10);
    private final JTextField txtPrecio = new JTextField(8);
    private final JTextField txtStock = new JTextField(5);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Código", "Tipo de prenda", "Talla", "Color", "Precio COP", "Stock"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JButton btnRegistrar = new JButton("Registrar producto");
    private final JButton btnGuardar = new JButton("Guardar cambios");
    private boolean modoEdicion;
    private final boolean esAdministrador;

    public InventarioPanel(Empleado sesion) {
        this.sesion = sesion;
        this.esAdministrador = sesion != null && "Administrador".equalsIgnoreCase(sesion.getRol());
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 7));
        filtros.setBorder(BorderFactory.createTitledBorder("Consultar existencias"));
        filtros.add(new JLabel("Tipo:")); filtros.add(txtFiltroTipo);
        filtros.add(new JLabel("Talla:")); filtros.add(txtFiltroTalla);
        filtros.add(new JLabel("Color:")); filtros.add(txtFiltroColor);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar filtros");
        filtros.add(btnBuscar); filtros.add(btnLimpiar);
        add(filtros, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0 && esAdministrador) cargarSeleccion();
        });
        btnBuscar.addActionListener(e -> cargarDatosInventario());
        btnLimpiar.addActionListener(e -> { txtFiltroTipo.setText(""); txtFiltroTalla.setText(""); txtFiltroColor.setText(""); cargarDatosInventario(); });

        if (esAdministrador) add(crearFormularioAdmin(), BorderLayout.SOUTH);
        cargarDatosInventario();
    }

    private JPanel crearFormularioAdmin() {
        JPanel exterior = new JPanel(new BorderLayout(5, 5));
        exterior.setBorder(BorderFactory.createTitledBorder("Administrar productos (solo Administrador)"));
        txtCodigo.setEditable(false);
        JPanel campos = new JPanel(new GridLayout(2, 6, 5, 5));
        campos.add(new JLabel("Código (auto.)")); campos.add(new JLabel("Tipo de prenda*"));
        campos.add(new JLabel("Talla*")); campos.add(new JLabel("Color*"));
        campos.add(new JLabel("Precio COP*")); campos.add(new JLabel("Stock*"));
        campos.add(txtCodigo); campos.add(txtTipo); campos.add(txtTalla); campos.add(txtColor); campos.add(txtPrecio); campos.add(txtStock);
        exterior.add(campos, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevo = new JButton("Nuevo / Limpiar");
        btnRegistrar.setBackground(new Color(163, 109, 96)); btnRegistrar.setForeground(Color.WHITE);
        btnGuardar.setBackground(new Color(46, 139, 87)); btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setEnabled(false);
        botones.add(btnNuevo); botones.add(btnRegistrar); botones.add(btnGuardar);
        exterior.add(botones, BorderLayout.SOUTH);
        btnNuevo.addActionListener(e -> modoNuevo());
        btnRegistrar.addActionListener(e -> registrarProducto());
        btnGuardar.addActionListener(e -> actualizarProducto());
        return exterior;
    }

    private void cargarDatosInventario() {
        try {
            List<Producto> productos = dao.obtenerProductos(txtFiltroTipo.getText(), txtFiltroTalla.getText(), txtFiltroColor.getText());
            modelo.setRowCount(0);
            for (Producto p : productos) {
                modelo.addRow(new Object[]{p.getIdProducto(), p.getTipo(), p.getTalla(), p.getColor(), p.getPrecio(), p.getStock()});
            }
            if (productos.isEmpty()) JOptionPane.showMessageDialog(this,
                    "No se encontraron prendas en el inventario con los criterios seleccionados.", "Consulta", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error de conexión: no fue posible consultar el inventario en tiempo real.\n" + ex.getMessage(),
                    "Error de inventario", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSeleccion() {
        int vista = tabla.getSelectedRow();
        if (vista < 0) return;
        int fila = tabla.convertRowIndexToModel(vista);
        txtCodigo.setText(String.valueOf(modelo.getValueAt(fila, 0)));
        txtTipo.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        txtTalla.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        txtColor.setText(String.valueOf(modelo.getValueAt(fila, 3)));
        txtPrecio.setText(String.valueOf(modelo.getValueAt(fila, 4)));
        txtStock.setText(String.valueOf(modelo.getValueAt(fila, 5)));
        modoEdicion = true;
        btnRegistrar.setEnabled(false);
        btnGuardar.setEnabled(true);
    }

    private void registrarProducto() {
        try {
            Producto p = leerFormulario(false);
            String codigo = dao.registrarProducto(p);
            JOptionPane.showMessageDialog(this, "Producto registrado exitosamente en el inventario. Código: " + codigo,
                    "Registro completado", JOptionPane.INFORMATION_MESSAGE);
            modoNuevo();
            cargarDatosInventario();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No fue posible registrar el producto.\n" + ex.getMessage(), "Error de inventario", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarProducto() {
        try {
            Producto p = leerFormulario(true);
            dao.actualizarProducto(p);
            JOptionPane.showMessageDialog(this, "Producto y stock actualizados correctamente.", "Actualización", JOptionPane.INFORMATION_MESSAGE);
            modoNuevo();
            cargarDatosInventario();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No fue posible actualizar el producto.\n" + ex.getMessage(), "Error de inventario", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Producto leerFormulario(boolean edicion) {
        String tipo = txtTipo.getText().trim();
        String talla = txtTalla.getText().trim();
        String color = txtColor.getText().trim();
        if (tipo.isBlank() || talla.isBlank() || color.isBlank() || txtPrecio.getText().isBlank() || txtStock.getText().isBlank()) {
            throw new IllegalArgumentException("Complete todos los campos obligatorios del producto.");
        }
        double precio;
        int stock;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));
            stock = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El precio debe ser numérico y el stock debe ser un número entero.");
        }
        if (!Double.isFinite(precio) || precio <= 0) throw new IllegalArgumentException("El precio del producto debe ser mayor a cero.");
        if (stock < 0 || (!edicion && stock == 0)) throw new IllegalArgumentException(edicion
                ? "El stock del producto no puede ser negativo."
                : "La cantidad inicial debe ser mayor a cero.");
        String codigo = edicion ? txtCodigo.getText().trim() : "";
        return new Producto(codigo, tipo, talla, color, precio, stock);
    }

    private void modoNuevo() {
        modoEdicion = false;
        txtCodigo.setText(""); txtTipo.setText(""); txtTalla.setText(""); txtColor.setText(""); txtPrecio.setText(""); txtStock.setText("");
        tabla.clearSelection();
        btnRegistrar.setEnabled(true);
        btnGuardar.setEnabled(false);
    }
}
