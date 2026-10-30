package com.nanisboutique.view;

import javax.swing.SwingUtilities;

/** Punto de entrada alternativo para abrir la interfaz de escritorio. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
