package com.nanisboutique.models;

import java.util.List;

public class Venta {
    private String idVenta;
    private String fechaHora;
    private String idCajero;
    private String medioPago;
    private double totalVenta;
    private List<DetalleVenta> items;

    public Venta(String idVenta, String idCajero, String medioPago, List<DetalleVenta> items) {
        this.idVenta = idVenta;
        this.idCajero = idCajero;
        this.medioPago = medioPago;
        setItems(items);
    }

    private double calcularTotal() {
        double suma = 0.0;
        if (items != null) {
            for (DetalleVenta item : items) suma += item.calcularSubtotal();
        }
        return suma;
    }

    public String getIdVenta() { return idVenta; }
    public void setIdVenta(String idVenta) { this.idVenta = idVenta; }
    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
    public String getIdCajero() { return idCajero; }
    public void setIdCajero(String idCajero) { this.idCajero = idCajero; }
    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
    public double getTotalVenta() { return totalVenta; }
    public void setTotalVenta(double totalVenta) { this.totalVenta = totalVenta; }
    public List<DetalleVenta> getItems() { return items; }
    public void setItems(List<DetalleVenta> items) {
        this.items = items;
        this.totalVenta = calcularTotal();
    }
}
