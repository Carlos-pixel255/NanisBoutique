package com.nanisboutique.view;

import com.nanisboutique.dao.BoutiqueDAO;
import com.nanisboutique.models.Empleado;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** Panel de gestión de personal para el rol Administrador. */
public class EmpleadosPanel extends JPanel {
    private final BoutiqueDAO dao = new BoutiqueDAO();
    private final JTextField txtBusqueda = new JTextField(18);
    private final JComboBox<String> cmbFiltroEstado = new JComboBox<>(new String[]{"Activos", "Inactivos", "Todos"});
    private final JTextField txtCedula = new JTextField(14);
    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtTelefono = new JTextField(12);
    private final JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Administrador", "Cajero", "Vendedor"});
    private final JTextField txtUsuario = new JTextField(14);
    private final JPasswordField txtPassword = new JPasswordField(14);
    private final JCheckBox chkActivo = new JCheckBox("Empleado activo", true);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Cédula", "Nombre", "Teléfono", "Rol", "Usuario", "Estado"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JButton btnRegistrar = new JButton("Registrar empleado");
    private final JButton btnGuardar = new JButton("Guardar cambios");
    private final JButton btnLimpiar = new JButton("Nuevo / Limpiar");
    private boolean modoEdicion;

    public EmpleadosPanel() {
        cmbRol.setSelectedItem("Cajero"); // Evita asignar el rol de Administrador por defecto a un empleado nuevo.
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busqueda.setBorder(BorderFactory.createTitledBorder("Consultar personal"));
        busqueda.add(new JLabel("Nombre, cédula o usuario:"));
        busqueda.add(txtBusqueda);
        busqueda.add(new JLabel("Estado:"));
        busqueda.add(cmbFiltroEstado);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnActualizar = new JButton("Actualizar lista");
        busqueda.add(btnBuscar);
        busqueda.add(btnActualizar);
        add(busqueda, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del empleado"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        agregarCampo(formulario, c, 0, 0, "Cédula:", txtCedula);
        agregarCampo(formulario, c, 2, 0, "Nombre completo:", txtNombre);
        agregarCampo(formulario, c, 0, 1, "Teléfono:", txtTelefono);
        agregarCampo(formulario, c, 2, 1, "Rol:", cmbRol);
        agregarCampo(formulario, c, 0, 2, "Usuario de acceso:", txtUsuario);
        agregarCampo(formulario, c, 2, 2, "Contraseña inicial:", txtPassword);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        formulario.add(chkActivo, c);
        JLabel notaPassword = new JLabel("La contraseña solo se solicita al registrar; se almacena cifrada mediante hash.");
        notaPassword.setForeground(new Color(90, 90, 90));
        c.gridx = 2; c.gridy = 3; c.gridwidth = 2;
        formulario.add(notaPassword, c);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRegistrar.setBackground(new Color(163, 109, 96));
        btnRegistrar.setForeground(Color.WHITE);
        btnGuardar.setBackground(new Color(46, 139, 87));
        btnGuardar.setForeground(Color.WHITE);
        botones.add(btnLimpiar);
        botones.add(btnRegistrar);
        botones.add(btnGuardar);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(formulario, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> cargarEmpleados());
        btnActualizar.addActionListener(e -> cargarEmpleados());
        cmbFiltroEstado.addActionListener(e -> cargarEmpleados());
        btnRegistrar.addActionListener(e -> registrarEmpleado());
        btnGuardar.addActionListener(e -> guardarCambios());
        btnLimpiar.addActionListener(e -> modoNuevo());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) cargarEmpleadoSeleccionado();
        });

        btnGuardar.setEnabled(false);
        cargarEmpleados();
    }

    private void agregarCampo(JPanel panel, GridBagConstraints c, int x, int y, String etiqueta, Component campo) {
        c.gridx = x; c.gridy = y; c.gridwidth = 1; c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = x + 1; c.weightx = 1;
        panel.add(campo, c);
    }

    private void cargarEmpleados() {
        try {
            List<Empleado> empleados = dao.obtenerEmpleados(
                    String.valueOf(cmbFiltroEstado.getSelectedItem()), txtBusqueda.getText());
            modelo.setRowCount(0);
            for (Empleado emp : empleados) {
                modelo.addRow(new Object[]{emp.getIdCedula(), emp.getNombre(), emp.getTelefono(), emp.getRol(),
                        emp.getUsuario(), emp.isActivo() ? "ACTIVO" : "INACTIVO"});
            }
            if (empleados.isEmpty()) {
                // No es error de conexión: la consulta se completó sin coincidencias.
                setToolTipText("No se encontraron empleados con los filtros seleccionados.");
            } else {
                setToolTipText(null);
            }
        } catch (SQLException ex) {
            mostrarError("No fue posible consultar el personal. Verifique la conexión con Oracle.\n" + ex.getMessage());
        }
    }

    private void cargarEmpleadoSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) return;
        int fila = tabla.convertRowIndexToModel(filaVista);
        txtCedula.setText(String.valueOf(modelo.getValueAt(fila, 0)));
        txtNombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        txtTelefono.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        cmbRol.setSelectedItem(String.valueOf(modelo.getValueAt(fila, 3)));
        txtUsuario.setText(String.valueOf(modelo.getValueAt(fila, 4)));
        chkActivo.setSelected("ACTIVO".equals(modelo.getValueAt(fila, 5)));
        txtCedula.setEditable(false);
        txtPassword.setText("");
        txtPassword.setEnabled(false);
        modoEdicion = true;
        btnRegistrar.setEnabled(false);
        btnGuardar.setEnabled(true);
    }

    private void registrarEmpleado() {
        String cedula = txtCedula.getText().trim();
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String usuario = txtUsuario.getText().trim();
        char[] password = txtPassword.getPassword();
        try {
            validarFormulario(cedula, nombre, telefono, usuario);
            if (password.length < 4) throw new IllegalArgumentException("La contraseña inicial debe tener al menos 4 caracteres.");
            Empleado emp = new Empleado(cedula, nombre, telefono, String.valueOf(cmbRol.getSelectedItem()), true, usuario);
            dao.registrarEmpleado(emp, password);
            JOptionPane.showMessageDialog(this, "Empleado registrado con éxito.", "Registro completado", JOptionPane.INFORMATION_MESSAGE);
            modoNuevo();
            cargarEmpleados();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            String msg = ex.getErrorCode() == 1 || ex.getMessage().contains("ORA-00001")
                    ? "La cédula o el nombre de usuario ya se encuentra registrado en la boutique."
                    : "No fue posible registrar el empleado.\n" + ex.getMessage();
            mostrarError(msg);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private void guardarCambios() {
        String cedula = txtCedula.getText().trim();
        try {
            validarFormulario(cedula, txtNombre.getText().trim(), txtTelefono.getText().trim(), txtUsuario.getText().trim());
            Empleado emp = new Empleado(cedula, txtNombre.getText().trim(), txtTelefono.getText().trim(),
                    String.valueOf(cmbRol.getSelectedItem()), chkActivo.isSelected(), txtUsuario.getText().trim());
            dao.actualizarEmpleado(emp);
            JOptionPane.showMessageDialog(this, "Datos del empleado actualizados correctamente.", "Actualización", JOptionPane.INFORMATION_MESSAGE);
            modoNuevo();
            cargarEmpleados();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            String msg = ex.getErrorCode() == 1 || ex.getMessage().contains("ORA-00001")
                    ? "Ese nombre de usuario ya está asignado a otro empleado."
                    : "No fue posible actualizar el empleado.\n" + ex.getMessage();
            mostrarError(msg);
        }
    }

    private void validarFormulario(String cedula, String nombre, String telefono, String usuario) {
        if (cedula.isBlank() || nombre.isBlank() || telefono.isBlank() || usuario.isBlank()) {
            throw new IllegalArgumentException("Complete todos los campos obligatorios.");
        }
        if (!cedula.matches("\\d{5,20}")) throw new IllegalArgumentException("La cédula debe contener entre 5 y 20 dígitos.");
        if (!telefono.matches("\\d{7,15}")) throw new IllegalArgumentException("El teléfono debe contener entre 7 y 15 dígitos.");
        if (nombre.length() > 100) throw new IllegalArgumentException("El nombre no puede superar 100 caracteres.");
        if (usuario.length() > 30 || usuario.contains(" ")) throw new IllegalArgumentException("El usuario no puede superar 30 caracteres ni contener espacios.");
    }

    private void modoNuevo() {
        modoEdicion = false;
        txtCedula.setEditable(true);
        txtCedula.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        cmbRol.setSelectedItem("Cajero");
        txtUsuario.setText("");
        txtPassword.setText("");
        txtPassword.setEnabled(true);
        chkActivo.setSelected(true);
        tabla.clearSelection();
        btnRegistrar.setEnabled(true);
        btnGuardar.setEnabled(false);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
