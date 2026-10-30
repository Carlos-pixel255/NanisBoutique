package com.nanisboutique.view;

import com.nanisboutique.dao.BoutiqueDAO;
import com.nanisboutique.models.Empleado;
import com.nanisboutique.models.Producto;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SISTEMA NANIS BOUTIQUE - POPAYÁN (TEST)        ");
        System.out.println("==================================================");

        BoutiqueDAO dao = new BoutiqueDAO();

        // Prueba de creación de empleado
        Empleado emp = new Empleado("1061123456", "María Camargo", "3110001122", "Cajero");
        System.out.println("Registrando Empleado...");
        // boolean okEmp = dao.registrarEmpleado(emp);

        // Prueba de creación de producto
        Producto prod = new Producto("PROD-001", "Blusa Seda", "M", "Rojo", 45000, 10);
        System.out.println("Registrando Producto...");
        // boolean okProd = dao.registrarProducto(prod);

        System.out.println("\nEstructura Java compilada exitosamente.");
    }
}