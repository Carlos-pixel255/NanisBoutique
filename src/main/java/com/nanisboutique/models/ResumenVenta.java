package com.nanisboutique.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ResumenVenta {
    private final String idVenta;
    private final LocalDateTime fechaHora;
    private final String nombreCajero;
    private final String medioPago;
    private final BigDecimal total;

    public ResumenVenta(String idVenta, LocalDateTime fechaHora, String nombreCajero,
                        String medioPago, BigDecimal total) {
        this.idVenta = idVenta;
        this.fechaHora = fechaHora;
        this.nombreCajero = nombreCajero;
        this.medioPago = medioPago;
        this.total = total == null ? BigDecimal.ZERO : total;
    }

    public String getIdVenta() { return idVenta; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getNombreCajero() { return nombreCajero; }
    public String getMedioPago() { return medioPago; }
    public BigDecimal getTotal() { return total; }
}
