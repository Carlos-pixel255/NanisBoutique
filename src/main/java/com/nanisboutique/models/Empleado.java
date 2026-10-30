package com.nanisboutique.models;

public class Empleado {
    private String idCedula;
    private String nombre;
    private String telefono;
    private String rol; // Administrador, Cajero, Vendedor
    private boolean activo;

    public Empleado(String idCedula, String nombre, String telefono, String rol) {
        this.idCedula = idCedula;
        this.nombre = nombre;
        this.telefono = telefono;
        this.rol = rol;
        this.activo = true;
    }

    public Empleado(String idCedula, String nombre, String telefono, String rol, boolean activo) {
        this.idCedula = idCedula;
        this.nombre = nombre;
        this.telefono = telefono;
        this.rol = rol;
        this.activo = activo;
    }

    // Getters y Setters
    public String getIdCedula() { return idCedula; }
    public void setIdCedula(String idCedula) { this.idCedula = idCedula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
