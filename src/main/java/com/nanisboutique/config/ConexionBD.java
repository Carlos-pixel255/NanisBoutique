package com.nanisboutique.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Configuración de conexión. Las variables de entorno permiten cambiar las credenciales sin modificar el código. */
public final class ConexionBD {
    private static final String URL = System.getenv().getOrDefault(
            "NANIS_DB_URL", "jdbc:oracle:thin:@//localhost:1521/XEPDB1");
    private static final String USUARIO = System.getenv().getOrDefault("NANIS_DB_USER", "nanis_admin");
    private static final String PASSWORD = System.getenv().getOrDefault("NANIS_DB_PASSWORD", "admin_password");

    private ConexionBD() { }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
