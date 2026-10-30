package com.nanisboutique.models;

public class Producto {
    private String idProducto;
    private String tipo;    // Blusa, Jeans, Vestido, etc.
    private String talla;   // XS, S, M, L, XL
    private String color;
    private double precio;
    private int stock;

    public Producto(String idProducto, String tipo, String talla, String color, double precio, int stock) {
        this.idProducto = idProducto;
        this.tipo = tipo;
        this.talla = talla;
        this.color = color;
        this.precio = precio;
        this.stock = stock;
    }

    // Getters y Setters
    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public void disminuirStock(int cantidad) {
        if (cantidad <= this.stock) {
            this.stock -= cantidad;
        }
    }
}