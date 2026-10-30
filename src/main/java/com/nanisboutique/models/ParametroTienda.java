package com.nanisboutique.models;

public class ParametroTienda {
    private final String id;
    private final String nombre;
    private String valor;
    private final String descripcion;
    private final String tipoDato;

    public ParametroTienda(String id, String nombre, String valor, String descripcion, String tipoDato) {
        this.id = id;
        this.nombre = nombre;
        this.valor = valor;
        this.descripcion = descripcion;
        this.tipoDato = tipoDato;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getDescripcion() { return descripcion; }
    public String getTipoDato() { return tipoDato; }
}
