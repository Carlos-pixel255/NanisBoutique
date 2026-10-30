-- ============================================================================
-- NANIS BOUTIQUE - ESQUEMA CANÓNICO PARA LA APLICACIÓN JAVA SWING
-- Compatible con el DAO de esta versión y Oracle Database XE 18c o superior.
-- EJECUTAR SOLO EN UN ESQUEMA LIMPIO. No ejecutar junto con script_legacy.
-- Las contraseñas iniciales de los usuarios semilla son 1234, almacenadas con
-- PBKDF2-HMAC-SHA256. Cambiarlas una vez verificado el sistema.
-- ============================================================================

-- 1. PERSONAL Y AUTENTICACIÓN
CREATE TABLE EMPLEADOS (
    id_cedula   VARCHAR2(20) PRIMARY KEY,
    nombre      VARCHAR2(100) NOT NULL,
    telefono    VARCHAR2(15) NOT NULL,
    rol         VARCHAR2(20) NOT NULL,
    activo      NUMBER(1) DEFAULT 1 NOT NULL,
    usuario     VARCHAR2(30) NOT NULL UNIQUE,
    password_hash VARCHAR2(300) NOT NULL,
    CONSTRAINT chk_empleados_rol CHECK (rol IN ('Administrador', 'Cajero', 'Vendedor')),
    CONSTRAINT chk_empleados_activo CHECK (activo IN (0, 1))
);

-- 2. CATÁLOGO E INVENTARIO
CREATE TABLE PRODUCTOS (
    id_producto VARCHAR2(20) PRIMARY KEY,
    tipo        VARCHAR2(50) NOT NULL,
    talla       VARCHAR2(10) NOT NULL,
    color       VARCHAR2(30) NOT NULL,
    precio      NUMBER(10,2) NOT NULL,
    stock       NUMBER(6) DEFAULT 0 NOT NULL,
    CONSTRAINT chk_productos_precio CHECK (precio > 0),
    CONSTRAINT chk_productos_stock CHECK (stock >= 0)
);

CREATE INDEX ix_productos_busqueda ON PRODUCTOS (tipo, talla, color);

-- 3. VENTAS Y DETALLE (la cabecera conserva el empleado responsable)
CREATE TABLE VENTAS (
    id_venta    VARCHAR2(20) PRIMARY KEY,
    fecha_hora  TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    id_cajero   VARCHAR2(20) NOT NULL,
    medio_pago  VARCHAR2(20) NOT NULL,
    total       NUMBER(12,2) NOT NULL,
    CONSTRAINT fk_ventas_empleado FOREIGN KEY (id_cajero) REFERENCES EMPLEADOS(id_cedula),
    CONSTRAINT chk_ventas_medio_pago CHECK (medio_pago IN ('Efectivo', 'Transferencia')),
    CONSTRAINT chk_ventas_total CHECK (total > 0)
);

CREATE INDEX ix_ventas_fecha ON VENTAS (fecha_hora);

CREATE TABLE DETALLE_VENTA (
    id_detalle      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_venta        VARCHAR2(20) NOT NULL,
    id_producto     VARCHAR2(20) NOT NULL,
    cantidad        NUMBER(6) NOT NULL,
    precio_unitario NUMBER(10,2) NOT NULL,
    CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta) REFERENCES VENTAS(id_venta),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES PRODUCTOS(id_producto),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario > 0)
);

-- 4. PARÁMETROS GENERALES DE LA TIENDA (la aplicación no modifica DDL de Oracle)
CREATE TABLE PARAMETROS_TIENDA (
    parametro_id       VARCHAR2(30) PRIMARY KEY,
    nombre_parametro   VARCHAR2(100) NOT NULL,
    valor_parametro    VARCHAR2(255) NOT NULL,
    descripcion        VARCHAR2(255),
    tipo_dato          VARCHAR2(15) DEFAULT 'TEXTO' NOT NULL,
    fecha_actualizacion TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT chk_parametro_tipo CHECK (tipo_dato IN ('TEXTO', 'NUMERO', 'BOOLEANO'))
);

-- 5. USUARIOS DE DEMOSTRACIÓN. Contraseña inicial para los tres: 1234.
INSERT INTO EMPLEADOS (id_cedula, nombre, telefono, rol, activo, usuario, password_hash)
VALUES ('1061234567', 'Adriana Gómez', '3151234567', 'Administrador', 1, 'admin',
        'PBKDF2$120000$QrbrGNkc2DYSuRVekDS3Uw$MKUaBcEKyU/2QbMzflgNaNZpIlRwhPWm7s7aMfEpcLM');

INSERT INTO EMPLEADOS (id_cedula, nombre, telefono, rol, activo, usuario, password_hash)
VALUES ('1061987654', 'Carolina Claros', '3187654321', 'Cajero', 1, 'cajero',
        'PBKDF2$120000$LMP0NeOS5PGw+ZG1s9UVnQ$TcoW4RyhLuT+xKMeWJk70BA4CHslxyELD80NM1Rh+Es');

INSERT INTO EMPLEADOS (id_cedula, nombre, telefono, rol, activo, usuario, password_hash)
VALUES ('1061456789', 'Luisa Fernanda Paz', '3119876543', 'Vendedor', 1, 'vendedor',
        'PBKDF2$120000$mDiQEYpguQ1NDpuYo5hHbw$oTYNy/rguRPukeUJLF/3HGRCVNxKrVlgcJuA8X0aVAA');

-- 6. INVENTARIO INICIAL DE EJEMPLO
INSERT INTO PRODUCTOS (id_producto, tipo, talla, color, precio, stock) VALUES ('P001', 'Blusa', 'S', 'Rosa', 55000, 15);
INSERT INTO PRODUCTOS (id_producto, tipo, talla, color, precio, stock) VALUES ('P002', 'Jeans', 'M', 'Azul', 95000, 8);
INSERT INTO PRODUCTOS (id_producto, tipo, talla, color, precio, stock) VALUES ('P003', 'Vestido', 'L', 'Rojo', 145000, 5);
INSERT INTO PRODUCTOS (id_producto, tipo, talla, color, precio, stock) VALUES ('P004', 'Chaqueta', 'M', 'Negro', 180000, 4);
INSERT INTO PRODUCTOS (id_producto, tipo, talla, color, precio, stock) VALUES ('P005', 'Blusa', 'M', 'Blanco', 85000, 12);
INSERT INTO PRODUCTOS (id_producto, tipo, talla, color, precio, stock) VALUES ('P006', 'Pantalón', 'S', 'Negro', 110000, 7);
INSERT INTO PRODUCTOS (id_producto, tipo, talla, color, precio, stock) VALUES ('P007', 'Falda', 'M', 'Beige', 72000, 9);
INSERT INTO PRODUCTOS (id_producto, tipo, talla, color, precio, stock) VALUES ('P008', 'Camisa', 'L', 'Azul claro', 78000, 6);

-- 7. PARÁMETROS INICIALES
INSERT INTO PARAMETROS_TIENDA (parametro_id, nombre_parametro, valor_parametro, descripcion, tipo_dato)
VALUES ('NOMBRE_TIENDA', 'Nombre de la tienda', 'Nanis Boutique', 'Nombre comercial del establecimiento.', 'TEXTO');
INSERT INTO PARAMETROS_TIENDA (parametro_id, nombre_parametro, valor_parametro, descripcion, tipo_dato)
VALUES ('CIUDAD_SEDE', 'Ciudad de la sede', 'Popayán, Cauca', 'Ubicación de la única sede física.', 'TEXTO');
INSERT INTO PARAMETROS_TIENDA (parametro_id, nombre_parametro, valor_parametro, descripcion, tipo_dato)
VALUES ('MONEDA', 'Moneda', 'COP', 'Moneda de los precios y ventas.', 'TEXTO');

COMMIT;

-- Comprobaciones opcionales después de ejecutar el script:
-- SELECT id_cedula, nombre, rol, usuario, activo FROM EMPLEADOS;
-- SELECT * FROM PRODUCTOS ORDER BY tipo, talla;
-- SELECT * FROM PARAMETROS_TIENDA ORDER BY parametro_id;
