package com.nanisboutique.dao;

import com.nanisboutique.config.ConexionBD;
import com.nanisboutique.models.Empleado;
import com.nanisboutique.models.Producto;
import com.nanisboutique.models.Venta;
import com.nanisboutique.models.DetalleVenta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoutiqueDAO {

    // Épica 1: Registrar Empleado (Validando Cédula Única)
    public boolean registrarEmpleado(Empleado emp) {
        String sql = "INSERT INTO EMPLEADOS (id_cedula, nombre, telefono, rol, activo) VALUES (?, ?, ?, ?, 1)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, emp.getIdCedula());
            pstmt.setString(2, emp.getNombre());
            pstmt.setString(3, emp.getTelefono());
            pstmt.setString(4, emp.getRol());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar empleado: " + e.getMessage());
            return false;
        }
    }

    // Épica 2: Registrar Producto
    public boolean registrarProducto(Producto prod) {
        String sql = "INSERT INTO PRODUCTOS (id_producto, tipo, talla, color, precio, stock) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, prod.getIdProducto());
            pstmt.setString(2, prod.getTipo());
            pstmt.setString(3, prod.getTalla());
            pstmt.setString(4, prod.getColor());
            pstmt.setDouble(5, prod.getPrecio());
            pstmt.setInt(6, prod.getStock());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar producto: " + e.getMessage());
            return false;
        }
    }

    // Épica 2: Consultar Todos los Productos
    public List<Producto> obtenerProductos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTOS";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                lista.add(new Producto(
                    rs.getString("id_producto"),
                    rs.getString("tipo"),
                    rs.getString("talla"),
                    rs.getString("color"),
                    rs.getDouble("precio"),
                    rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }
        return lista;
    }

    // Épica 3: Registrar Venta con transacción (PL/SQL o Transacción Manual JDBC)
    public boolean procesarVenta(Venta venta) {
        String sqlVenta = "INSERT INTO VENTAS (id_venta, id_cajero, medio_pago, total) VALUES (?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO DETALLE_VENTA (id_venta, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        String sqlStock = "UPDATE PRODUCTOS SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";

        Connection conn = null;
        try {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false); // Iniciar Transacción

            // 1. Insertar Cabecera
            try (PreparedStatement pstmtVenta = conn.prepareStatement(sqlVenta)) {
                pstmtVenta.setString(1, venta.getIdVenta());
                pstmtVenta.setString(2, venta.getIdCajero());
                pstmtVenta.setString(3, venta.getMedioPago());
                pstmtVenta.setDouble(4, venta.getTotalVenta());
                pstmtVenta.executeUpdate();
            }

            // 2. Insertar Detalle y Descontar Stock
            try (PreparedStatement pstmtDet = conn.prepareStatement(sqlDetalle);
                 PreparedStatement pstmtStock = conn.prepareStatement(sqlStock)) {
                
                for (DetalleVenta item : venta.getItems()) {
                    // Detalle
                    pstmtDet.setString(1, venta.getIdVenta());
                    pstmtDet.setString(2, item.getIdProducto());
                    pstmtDet.setInt(3, item.getCantidad());
                    pstmtDet.setDouble(4, item.getPrecioUnitario());
                    pstmtDet.executeUpdate();

                    // Actualizar Stock
                    pstmtStock.setInt(1, item.getCantidad());
                    pstmtStock.setString(2, item.getIdProducto());
                    pstmtStock.setInt(3, item.getCantidad());
                    int filasAfectadas = pstmtStock.executeUpdate();

                    if (filasAfectadas == 0) {
                        throw new SQLException("Stock insuficiente para el producto: " + item.getIdProducto());
                    }
                }
            }

            conn.commit(); // Confirmar Transacción
            return true;

        } catch (SQLException e) {
            System.err.println("Error procesando venta. Transacción abortada: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}
