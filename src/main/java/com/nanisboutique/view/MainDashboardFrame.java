package com.nanisboutique.view;

import javax.swing.*;
import java.awt.*;

public class MainDashboardFrame extends JFrame {
    private String rolUsuario;

    public MainDashboardFrame(String rol) {
        this.rolUsuario = rol;
        setTitle("Nanis Boutique (Popayán) - Panel Principal [" + rolUsuario + "]");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Barra superior de bienvenida
        JLabel lblWelcome = new JLabel(" Bienvenido a Nanis Boutique - Rol activo: " + rolUsuario, JLabel.LEFT);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 12));
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(lblWelcome, BorderLayout.NORTH);

        // Panel con pestañas (JTabbedPane) para navegar entre las Épicas
        JTabbedPane tabbedPane = new JTabbedPane();

        // Pestaña 1: Inventario (Épica 2)[cite: 5]
        tabbedPane.addTab("Inventario & Productos", crearPanelInventario());

        // Pestaña 2: Punto de Venta / POS (Épica 3)[cite: 5, 8]
        tabbedPane.addTab("Punto de Venta (POS)", crearPanelPOS());

        // Pestaña 3: Gestión de Empleados y Configuración (Solo Administrador - Épica 1 y 4)[cite: 5, 6, 9]
        if (rolUsuario.equals("Administrador")) {
            tabbedPane.addTab("Gestión de Personal", crearPanelEmpleados());
            tabbedPane.addTab("Configuración DB", crearPanelConfig());
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel crearPanelInventario() {
        // Retorna el panel funcional de inventario con filtros y tabla en tiempo real (Épica 2)[cite: 5, 7]
        return new InventarioPanel();
    }

    private JPanel crearPanelPOS() {
        // Retorna el panel funcional del Punto de Venta y Carrito (Épica 3)[cite: 5, 8]
        return new PosPanel();
    }

    private JPanel crearPanelEmpleados() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Módulo de Registro y Modificación de Empleados (Épica 1)", JLabel.CENTER));
        return panel;
    }

    private JPanel crearPanelConfig() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Módulo de Parámetros de la Base de Datos (Épica 4)", JLabel.CENTER));
        return panel;
    }
}