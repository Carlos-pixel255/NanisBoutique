package com.nanisboutique.dao;

import com.nanisboutique.config.ConexionBD;
import com.nanisboutique.models.DetalleVenta;
import com.nanisboutique.models.Empleado;
import com.nanisboutique.models.ParametroTienda;
import com.nanisboutique.models.Producto;
import com.nanisboutique.models.ResumenVenta;
import com.nanisboutique.models.Venta;
import com.nanisboutique.security.PasswordUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Acceso a datos para el esquema definido en database/nanis_boutique_script.sql. */
public class BoutiqueDAO {

    // ÉPICA 4: autenticación contra Oracle; nunca se usan claves de demostración en el código.
    public Empleado autenticarUsuario(String usuarioOCedula, char[] password) throws SQLException {
        String sql = "SELECT id_cedula, nombre, telefono, rol, activo, usuario, password_hash "
                + "FROM EMPLEADOS WHERE (LOWER(usuario) = LOWER(?) OR id_cedula = ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioOCedula.trim());
            ps.setString(2, usuarioOCedula.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt("activo") != 1) return null;
                if (!PasswordUtil.verificar(password, rs.getString("password_hash"))) return null;
                return new Empleado(rs.getString("id_cedula"), rs.getString("nombre"),
                        rs.getString("telefono"), rs.getString("rol"), true, rs.getString("usuario"));
            }
        }
    }

    // ÉPICA 1: registrar empleado con nombre de usuario y contraseña inicial.
    public void registrarEmpleado(Empleado empleado, char[] password) throws SQLException {
        validarEmpleado(empleado);
        if (password == null || password.length < 4) {
            throw new SQLException("La contraseña inicial debe tener al menos 4 caracteres.");
        }
        String sql = "INSERT INTO EMPLEADOS (id_cedula, nombre, telefono, rol, activo, usuario, password_hash) "
                + "VALUES (?, ?, ?, ?, 1, ?, ?)";
        String hash = PasswordUtil.hash(password);
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empleado.getIdCedula().trim());
            ps.setString(2, empleado.getNombre().trim());
            ps.setString(3, empleado.getTelefono().trim());
            ps.setString(4, empleado.getRol());
            ps.setString(5, empleado.getUsuario().trim());
            ps.setString(6, hash);
            ps.executeUpdate();
        }
    }

    public List<Empleado> obtenerEmpleados(String filtroEstado, String textoBusqueda) throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id_cedula, nombre, telefono, rol, activo, usuario FROM EMPLEADOS WHERE 1=1 ");
        if ("Activos".equalsIgnoreCase(filtroEstado)) sql.append("AND activo = 1 ");
        if ("Inactivos".equalsIgnoreCase(filtroEstado)) sql.append("AND activo = 0 ");
        if (textoBusqueda != null && !textoBusqueda.isBlank()) {
            sql.append("AND (UPPER(id_cedula) LIKE ? OR UPPER(nombre) LIKE ? OR UPPER(usuario) LIKE ?) ");
        }
        sql.append("ORDER BY nombre");
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (textoBusqueda != null && !textoBusqueda.isBlank()) {
                String filtro = "%" + textoBusqueda.trim().toUpperCase() + "%";
                ps.setString(1, filtro);
                ps.setString(2, filtro);
                ps.setString(3, filtro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Empleado(rs.getString("id_cedula"), rs.getString("nombre"),
                            rs.getString("telefono"), rs.getString("rol"), rs.getInt("activo") == 1,
                            rs.getString("usuario")));
                }
            }
        }
        return lista;
    }

    public void actualizarEmpleado(Empleado empleado) throws SQLException {
        validarEmpleado(empleado);
        String sql = "UPDATE EMPLEADOS SET nombre = ?, telefono = ?, rol = ?, activo = ?, usuario = ? WHERE id_cedula = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empleado.getNombre().trim());
            ps.setString(2, empleado.getTelefono().trim());
            ps.setString(3, empleado.getRol());
            ps.setInt(4, empleado.isActivo() ? 1 : 0);
            ps.setString(5, empleado.getUsuario().trim());
            ps.setString(6, empleado.getIdCedula().trim());
            if (ps.executeUpdate() == 0) throw new SQLException("No se encontró el empleado que desea actualizar.");
        }
    }

    private void validarEmpleado(Empleado e) throws SQLException {
        if (e == null || e.getIdCedula() == null || e.getIdCedula().isBlank()
                || e.getNombre() == null || e.getNombre().isBlank()
                || e.getTelefono() == null || e.getTelefono().isBlank()
                || e.getRol() == null || e.getUsuario() == null || e.getUsuario().isBlank()) {
            throw new SQLException("Complete la identificación, nombre, teléfono, rol y usuario.");
        }
        if (!List.of("Administrador", "Cajero", "Vendedor").contains(e.getRol())) {
            throw new SQLException("El rol seleccionado no es válido.");
        }
    }

    // ÉPICA 2: registro, búsqueda y actualización del inventario.
    public String registrarProducto(Producto producto) throws SQLException {
        validarProducto(producto, true);
        if (producto.getIdProducto() == null || producto.getIdProducto().isBlank()) {
            producto.setIdProducto("P" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        }
        String sql = "INSERT INTO PRODUCTOS (id_producto, tipo, talla, color, precio, stock) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getIdProducto());
            ps.setString(2, producto.getTipo().trim());
            ps.setString(3, producto.getTalla().trim());
            ps.setString(4, producto.getColor().trim());
            ps.setBigDecimal(5, BigDecimal.valueOf(producto.getPrecio()));
            ps.setInt(6, producto.getStock());
            ps.executeUpdate();
        }
        return producto.getIdProducto();
    }

    public List<Producto> obtenerProductos(String tipo, String talla, String color) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id_producto, tipo, talla, color, precio, stock FROM PRODUCTOS WHERE 1=1 ");
        List<String> parametros = new ArrayList<>();
        if (tipo != null && !tipo.isBlank()) {
            sql.append("AND UPPER(tipo) LIKE ? ");
            parametros.add("%" + tipo.trim().toUpperCase() + "%");
        }
        if (talla != null && !talla.isBlank()) {
            sql.append("AND UPPER(talla) = ? ");
            parametros.add(talla.trim().toUpperCase());
        }
        if (color != null && !color.isBlank()) {
            sql.append("AND UPPER(color) LIKE ? ");
            parametros.add("%" + color.trim().toUpperCase() + "%");
        }
        sql.append("ORDER BY tipo, talla, color");
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) ps.setString(i + 1, parametros.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Producto(rs.getString("id_producto"), rs.getString("tipo"),
                            rs.getString("talla"), rs.getString("color"),
                            rs.getBigDecimal("precio").doubleValue(), rs.getInt("stock")));
                }
            }
        }
        return lista;
    }

    public void actualizarProducto(Producto producto) throws SQLException {
        validarProducto(producto, false);
        String sql = "UPDATE PRODUCTOS SET tipo = ?, talla = ?, color = ?, precio = ?, stock = ? WHERE id_producto = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getTipo().trim());
            ps.setString(2, producto.getTalla().trim());
            ps.setString(3, producto.getColor().trim());
            ps.setBigDecimal(4, BigDecimal.valueOf(producto.getPrecio()));
            ps.setInt(5, producto.getStock());
            ps.setString(6, producto.getIdProducto());
            if (ps.executeUpdate() == 0) throw new SQLException("No se encontró el producto que desea modificar.");
        }
    }

    private void validarProducto(Producto p, boolean registro) throws SQLException {
        if (p == null || p.getTipo() == null || p.getTipo().isBlank()
                || p.getTalla() == null || p.getTalla().isBlank()
                || p.getColor() == null || p.getColor().isBlank()) {
            throw new SQLException("Complete el tipo de prenda, talla y color.");
        }
        if (!Double.isFinite(p.getPrecio()) || p.getPrecio() <= 0) {
            throw new SQLException("El precio debe ser un valor mayor a cero.");
        }
        if (p.getStock() < 0 || (registro && p.getStock() == 0)) {
            throw new SQLException(registro ? "La cantidad inicial debe ser mayor a cero." : "El stock no puede ser negativo.");
        }
    }

    // ÉPICA 3: venta atómica. El precio y el stock se vuelven a validar directamente en Oracle.
    public Venta registrarVenta(String idCajero, String medioPago, List<DetalleVenta> items) throws SQLException {
        if (idCajero == null || idCajero.isBlank()) throw new SQLException("No se identificó al empleado que registra la venta.");
        if (!List.of("Efectivo", "Transferencia").contains(medioPago)) throw new SQLException("Seleccione Efectivo o Transferencia.");
        if (items == null || items.isEmpty()) throw new SQLException("Agregue al menos un producto a la venta.");

        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (DetalleVenta item : items) {
            if (item.getCantidad() <= 0) throw new SQLException("La cantidad de cada producto debe ser mayor a cero.");
            cantidades.merge(item.getIdProducto(), item.getCantidad(), Integer::sum);
        }

        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                Map<String, BigDecimal> precios = new LinkedHashMap<>();
                for (Map.Entry<String, Integer> entry : cantidades.entrySet()) {
                    String sqlProducto = "SELECT precio, stock FROM PRODUCTOS WHERE id_producto = ? FOR UPDATE";
                    try (PreparedStatement ps = conn.prepareStatement(sqlProducto)) {
                        ps.setString(1, entry.getKey());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new SQLException("El producto " + entry.getKey() + " ya no existe en el inventario.");
                            int stock = rs.getInt("stock");
                            if (stock < entry.getValue()) {
                                throw new SQLException("Stock insuficiente para el producto " + entry.getKey()
                                        + ". Disponible: " + stock + ", solicitado: " + entry.getValue() + ".");
                            }
                            precios.put(entry.getKey(), rs.getBigDecimal("precio"));
                        }
                    }
                }

                BigDecimal total = BigDecimal.ZERO;
                List<DetalleVenta> detalleValidado = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : cantidades.entrySet()) {
                    BigDecimal precio = precios.get(entry.getKey());
                    BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(entry.getValue()));
                    total = total.add(subtotal);
                    detalleValidado.add(new DetalleVenta(entry.getKey(), entry.getValue(), precio.doubleValue()));
                }

                String idVenta = generarIdVenta();
                String sqlCabecera = "INSERT INTO VENTAS (id_venta, id_cajero, medio_pago, total) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlCabecera)) {
                    ps.setString(1, idVenta);
                    ps.setString(2, idCajero);
                    ps.setString(3, medioPago);
                    ps.setBigDecimal(4, total);
                    ps.executeUpdate();
                }

                String sqlDetalle = "INSERT INTO DETALLE_VENTA (id_venta, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
                String sqlStock = "UPDATE PRODUCTOS SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
                try (PreparedStatement psDetalle = conn.prepareStatement(sqlDetalle);
                     PreparedStatement psStock = conn.prepareStatement(sqlStock)) {
                    for (DetalleVenta item : detalleValidado) {
                        psDetalle.setString(1, idVenta);
                        psDetalle.setString(2, item.getIdProducto());
                        psDetalle.setInt(3, item.getCantidad());
                        psDetalle.setBigDecimal(4, BigDecimal.valueOf(item.getPrecioUnitario()));
                        psDetalle.executeUpdate();

                        psStock.setInt(1, item.getCantidad());
                        psStock.setString(2, item.getIdProducto());
                        psStock.setInt(3, item.getCantidad());
                        if (psStock.executeUpdate() != 1) {
                            throw new SQLException("El stock cambió durante la operación. La venta fue cancelada; actualice el catálogo.");
                        }
                    }
                }
                conn.commit();
                return new Venta(idVenta, idCajero, medioPago, detalleValidado);
            } catch (SQLException | RuntimeException ex) {
                try { conn.rollback(); } catch (SQLException rollbackError) { ex.addSuppressed(rollbackError); }
                if (ex instanceof SQLException sqlEx) throw sqlEx;
                throw ex;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) { }
            }
        }
    }

    private String generarIdVenta() {
        return "V" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
    }

    // ÉPICA 3 - HU2: historial detallado y resumen para el cierre diario de caja.
    public List<ResumenVenta> obtenerResumenVentas(LocalDate desde, LocalDate hasta) throws SQLException {
        if (desde == null || hasta == null) throw new SQLException("Seleccione las dos fechas del informe.");
        if (desde.isAfter(hasta)) throw new SQLException("La fecha inicial no puede ser posterior a la fecha final.");
        if (hasta.isAfter(LocalDate.now())) throw new SQLException("No se pueden consultar fechas futuras.");

        String sql = "SELECT v.id_venta, v.fecha_hora, NVL(e.nombre, 'Empleado no disponible') AS nombre_cajero, "
                + "v.medio_pago, v.total FROM VENTAS v LEFT JOIN EMPLEADOS e ON e.id_cedula = v.id_cajero "
                + "WHERE v.fecha_hora >= ? AND v.fecha_hora < ? ORDER BY v.fecha_hora DESC";
        List<ResumenVenta> lista = new ArrayList<>();
        Timestamp inicio = Timestamp.valueOf(LocalDateTime.of(desde, LocalTime.MIDNIGHT));
        Timestamp finExclusivo = Timestamp.valueOf(LocalDateTime.of(hasta.plusDays(1), LocalTime.MIDNIGHT));
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, inicio);
            ps.setTimestamp(2, finExclusivo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp fecha = rs.getTimestamp("fecha_hora");
                    lista.add(new ResumenVenta(rs.getString("id_venta"),
                            fecha == null ? null : fecha.toLocalDateTime(), rs.getString("nombre_cajero"),
                            rs.getString("medio_pago"), rs.getBigDecimal("total")));
                }
            }
        }
        return lista;
    }

    // ÉPICA 4 - HU1: editar parámetros de negocio; no altera el esquema físico de Oracle.
    public List<ParametroTienda> obtenerParametrosTienda() throws SQLException {
        String sql = "SELECT parametro_id, nombre_parametro, valor_parametro, descripcion, tipo_dato "
                + "FROM PARAMETROS_TIENDA ORDER BY nombre_parametro";
        List<ParametroTienda> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new ParametroTienda(rs.getString("parametro_id"), rs.getString("nombre_parametro"),
                        rs.getString("valor_parametro"), rs.getString("descripcion"), rs.getString("tipo_dato")));
            }
        }
        return lista;
    }

    public void actualizarParametroTienda(ParametroTienda parametro, String nuevoValor) throws SQLException {
        if (parametro == null || nuevoValor == null || nuevoValor.isBlank()) {
            throw new SQLException("El valor del parámetro es obligatorio.");
        }
        String tipo = parametro.getTipoDato() == null ? "TEXTO" : parametro.getTipoDato().toUpperCase();
        try {
            switch (tipo) {
                case "NUMERO" -> new BigDecimal(nuevoValor.trim());
                case "BOOLEANO" -> {
                    if (!nuevoValor.equalsIgnoreCase("true") && !nuevoValor.equalsIgnoreCase("false")
                            && !nuevoValor.equalsIgnoreCase("si") && !nuevoValor.equalsIgnoreCase("no")) {
                        throw new NumberFormatException("Use true/false o si/no.");
                    }
                }
                default -> { /* Texto: se valida que no esté vacío. */ }
            }
        } catch (NumberFormatException ex) {
            throw new SQLException("El formato ingresado no es válido para este parámetro.");
        }
        if (nuevoValor.length() > 255) throw new SQLException("El valor no puede superar 255 caracteres.");
        String sql = "UPDATE PARAMETROS_TIENDA SET valor_parametro = ?, fecha_actualizacion = SYSTIMESTAMP WHERE parametro_id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoValor.trim());
            ps.setString(2, parametro.getId());
            if (ps.executeUpdate() == 0) throw new SQLException("El parámetro ya no existe. Actualice la tabla e intente de nuevo.");
        }
    }
}
