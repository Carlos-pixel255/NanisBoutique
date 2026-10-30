package com.nanisboutique.view;

import com.nanisboutique.dao.BoutiqueDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private BoutiqueDAO dao;

    public LoginFrame() {
        dao = new BoutiqueDAO();
        setTitle("Nanis Boutique (Popayán) - Inicio de Sesión");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        // Panel de título
        JLabel lblTitulo = new JLabel("Nanis Boutique - Acceso al Sistema", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        add(lblTitulo);

        // Panel de Usuario
        JPanel panelUser = new JPanel(new FlowLayout());
        panelUser.add(new JLabel("Usuario / Cédula:"));
        txtUsuario = new JTextField(15);
        panelUser.add(txtUsuario);
        add(panelUser);

        // Panel de Contraseña
        JPanel panelPass = new JPanel(new FlowLayout());
        panelPass.add(new JLabel("Contraseña:         "));
        txtPassword = new JPasswordField(15);
        panelPass.add(txtPassword);
        add(panelPass);

        // Botón de Ingreso
        JPanel panelBtn = new JPanel(new FlowLayout());
        btnIngresar = new JButton("Ingresar");
        btnIngresar.setBackground(new Color(163, 109, 96)); // Tono Rose Gold del prototipo
        btnIngresar.setForeground(Color.WHITE);
        panelBtn.add(btnIngresar);
        add(panelBtn);

        // Evento del botón login
        btnIngresar.addActionListener((ActionEvent e) -> {
            autenticar();
        });
    }

    private void autenticar() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese su usuario y contraseña para continuar.", "Alerta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación temporal de demostración conectada al sistema (HU9)[cite: 9]
        if ((usuario.equalsIgnoreCase("admin") || usuario.equals("1061234567")) && password.equals("1234")) {
            JOptionPane.showMessageDialog(this, "Sesión iniciada correctamente. Bienvenida Administradora.");
            new MainDashboardFrame("Administrador").setVisible(true);
            this.dispose();
        } else if ((usuario.equalsIgnoreCase("cajero") || usuario.equals("1061987654")) && password.equals("1234")) {
            JOptionPane.showMessageDialog(this, "Sesión iniciada correctamente. Bienvenido Cajero.");
            new MainDashboardFrame("Cajero").setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}