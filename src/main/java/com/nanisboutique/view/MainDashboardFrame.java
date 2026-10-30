package com.nanisboutique.view;

import com.nanisboutique.models.Empleado;

import javax.swing.*;
import java.awt.*;

/** Ventana principal; muestra los módulos según el rol almacenado del usuario autenticado. */
public class MainDashboardFrame extends JFrame {
    private final Empleado usuarioSesion;

    public MainDashboardFrame(Empleado usuarioSesion) {
        this.usuarioSesion = usuarioSesion;
        String rol = usuarioSesion.getRol();
        setTitle("Nanis Boutique - Panel principal [" + rol + "]");
        setSize(1120, 760);
        setMinimumSize(new Dimension(900, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        JLabel bienvenida = new JLabel("  Nanis Boutique  |  " + usuarioSesion.getNombre()
                + "  |  Rol: " + rol + "  |  Cédula: " + usuarioSesion.getIdCedula());
        bienvenida.setFont(new Font("SansSerif", Font.BOLD, 13));
        bienvenida.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(bienvenida, BorderLayout.CENTER);
        encabezado.add(btnCerrarSesion, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.addTab("Inventario", new InventarioPanel(usuarioSesion));

        boolean esAdmin = "Administrador".equalsIgnoreCase(rol);
        boolean esCajero = "Cajero".equalsIgnoreCase(rol);
        if (esAdmin || esCajero) {
            pestañas.addTab("Punto de venta", new PosPanel(usuarioSesion));
            pestañas.addTab("Historial diario / Caja", new HistorialVentasPanel());
        }
        if (esAdmin) {
            pestañas.addTab("Gestión de empleados", new EmpleadosPanel());
            pestañas.addTab("Configuración de tienda", new ConfiguracionTiendaPanel());
        }
        add(pestañas, BorderLayout.CENTER);

        btnCerrarSesion.addActionListener(e -> {
            int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la sesión actual?",
                    "Cerrar sesión", JOptionPane.YES_NO_OPTION);
            if (respuesta == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
    }
}
