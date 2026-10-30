package com.nanisboutique.view;

import com.nanisboutique.dao.BoutiqueDAO;
import com.nanisboutique.models.ResumenVenta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** Historial de ventas y resumen de caja para hoy o para un rango histórico válido. */
public class HistorialVentasPanel extends JPanel {
    private final BoutiqueDAO dao = new BoutiqueDAO();
    private final JTextField txtDesde = new JTextField(10);
    private final JTextField txtHasta = new JTextField(10);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Venta", "Fecha y hora", "Cajero responsable", "Medio de pago", "Total COP"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblEfectivo = new JLabel("Efectivo: $0");
    private final JLabel lblTransferencia = new JLabel("Transferencias: $0");
    private final JLabel lblTotal = new JLabel("Total recaudado: $0");

    public HistorialVentasPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String hoy = LocalDate.now().toString();
        txtDesde.setText(hoy);
        txtHasta.setText(hoy);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filtros.setBorder(BorderFactory.createTitledBorder("Consultar historial de ventas"));
        filtros.add(new JLabel("Desde (AAAA-MM-DD):")); filtros.add(txtDesde);
        filtros.add(new JLabel("Hasta (AAAA-MM-DD):")); filtros.add(txtHasta);
        JButton btnConsultar = new JButton("Generar resumen");
        JButton btnHoy = new JButton("Hoy");
        filtros.add(btnConsultar); filtros.add(btnHoy);
        add(filtros, BorderLayout.NORTH);

        tabla.setAutoCreateRowSorter(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel resumen = new JPanel(new GridLayout(1, 3, 10, 5));
        resumen.setBorder(BorderFactory.createTitledBorder("Resumen de caja"));
        lblEfectivo.setFont(new Font("Arial", Font.BOLD, 13));
        lblTransferencia.setFont(new Font("Arial", Font.BOLD, 13));
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        resumen.add(lblEfectivo); resumen.add(lblTransferencia); resumen.add(lblTotal);
        add(resumen, BorderLayout.SOUTH);

        btnConsultar.addActionListener(e -> consultar());
        btnHoy.addActionListener(e -> { String fecha = LocalDate.now().toString(); txtDesde.setText(fecha); txtHasta.setText(fecha); consultar(); });
        consultar();
    }

    private void consultar() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(txtDesde.getText().trim());
            hasta = LocalDate.parse(txtHasta.getText().trim());
            if (desde.isAfter(hasta)) throw new IllegalArgumentException("La fecha inicial debe ser anterior o igual a la fecha final.");
            if (hasta.isAfter(LocalDate.now())) throw new IllegalArgumentException("No se pueden consultar fechas futuras.");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese las fechas con formato AAAA-MM-DD, por ejemplo " + LocalDate.now() + ".",
                    "Formato de fecha inválido", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Rango de fechas inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<ResumenVenta> ventas = dao.obtenerResumenVentas(desde, hasta);
            modelo.setRowCount(0);
            BigDecimal efectivo = BigDecimal.ZERO;
            BigDecimal transferencia = BigDecimal.ZERO;
            BigDecimal total = BigDecimal.ZERO;
            DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            for (ResumenVenta venta : ventas) {
                LocalDateTime fecha = venta.getFechaHora();
                modelo.addRow(new Object[]{venta.getIdVenta(), fecha == null ? "" : fecha.format(formatoFecha),
                        venta.getNombreCajero(), venta.getMedioPago(), venta.getTotal().doubleValue()});
                total = total.add(venta.getTotal());
                if ("Efectivo".equalsIgnoreCase(venta.getMedioPago())) efectivo = efectivo.add(venta.getTotal());
                if ("Transferencia".equalsIgnoreCase(venta.getMedioPago())) transferencia = transferencia.add(venta.getTotal());
            }
            lblEfectivo.setText("Efectivo: " + moneda(efectivo));
            lblTransferencia.setText("Transferencias: " + moneda(transferencia));
            lblTotal.setText("Total recaudado: " + moneda(total));
            if (ventas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se registraron ventas en el período seleccionado. Los totales están en cero.",
                        "Sin transacciones", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No fue posible consultar el historial de ventas.\n" + ex.getMessage(),
                    "Error de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String moneda(BigDecimal valor) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        nf.setMaximumFractionDigits(0); nf.setMinimumFractionDigits(0);
        return nf.format(valor);
    }
}
