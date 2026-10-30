package com.nanisboutique.view;

import com.nanisboutique.dao.BoutiqueDAO;
import com.nanisboutique.models.Empleado;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

/** Inicio de sesión: verifica las credenciales contra EMPLEADOS en Oracle. */
public class LoginFrame extends JFrame {
    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JButton btnIngresar = new JButton("Iniciar sesión");
    private final BoutiqueDAO dao = new BoutiqueDAO();

    public LoginFrame() {
        setTitle("Nanis Boutique - Inicio de sesión");
        setSize(430, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("NANIS BOUTIQUE", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(new Color(128, 78, 69));
        JLabel subtitulo = new JLabel("Sistema local de inventario, ventas y personal", SwingConstants.CENTER);
        JPanel encabezado = new JPanel(new GridLayout(2, 1, 2, 2));
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 15, 4, 15));
        encabezado.add(titulo); encabezado.add(subtitulo);
        add(encabezado, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; formulario.add(new JLabel("Usuario o cédula:"), c);
        c.gridx = 1; c.weightx = 1; formulario.add(txtUsuario, c);
        c.gridx = 0; c.gridy = 1; c.weightx = 0; formulario.add(new JLabel("Contraseña:"), c);
        c.gridx = 1; c.weightx = 1; formulario.add(txtPassword, c);
        add(formulario, BorderLayout.CENTER);

        btnIngresar.setBackground(new Color(163, 109, 96));
        btnIngresar.setForeground(Color.WHITE);
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pie.add(btnIngresar);
        pie.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));
        add(pie, BorderLayout.SOUTH);

        btnIngresar.addActionListener(e -> autenticar());
        txtPassword.addActionListener(e -> autenticar());
    }

    private void autenticar() {
        String login = txtUsuario.getText().trim();
        char[] password = txtPassword.getPassword();
        if (login.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese su usuario y contraseña para continuar.",
                    "Datos obligatorios", JOptionPane.WARNING_MESSAGE);
            Arrays.fill(password, '\0');
            return;
        }
        btnIngresar.setEnabled(false);
        try {
            Empleado empleado = dao.autenticarUsuario(login, password);
            if (empleado == null) {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos, o la cuenta está inactiva.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocusInWindow();
                return;
            }
            JOptionPane.showMessageDialog(this, "Sesión iniciada correctamente. Bienvenido/a " + empleado.getNombre() + ".",
                    "Bienvenido/a", JOptionPane.INFORMATION_MESSAGE);
            new MainDashboardFrame(empleado).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No fue posible validar las credenciales en Oracle. Compruebe que la base de datos esté iniciada y que la conexión esté configurada.\n\nDetalle: " + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
            btnIngresar.setEnabled(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
