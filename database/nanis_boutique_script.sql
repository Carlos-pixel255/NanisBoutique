-- ============================================================
-- BASE DE DATOS NANIS BOUTIQUE (POPAYÁN)
-- Tablas y Procedimientos para 4 Épicas
-- ============================================================

-- ÉPICA 1: USUARIOS / EMPLEADOS
CREATE TABLE EMPLEADOS (
    id_cedula VARCHAR2(20) PRIMARY KEY,
    nombre VARCHAR2(100) NOT NULL,
    telefono VARCHAR2(15),
    rol VARCHAR2(20) CHECK (rol IN ('Administrador', 'Cajero', 'Vendedor')),
    activo NUMBER(1) DEFAULT 1
);

-- ÉPICA 2: INVENTARIO / PRODUCTOS
CREATE TABLE PRODUCTOS (
    id_producto VARCHAR2(20) PRIMARY KEY,
    tipo VARCHAR2(50) NOT NULL,
    talla VARCHAR2(10) NOT NULL,
    color VARCHAR2(30) NOT NULL,
    precio NUMBER(10, 2) NOT NULL,
    stock NUMBER(5) DEFAULT 0 CHECK (stock >= 0)
);

-- ÉPICA 3: CABECERA Y DETALLE DE VENTAS
CREATE TABLE VENTAS (
    id_venta VARCHAR2(20) PRIMARY KEY,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_cajero VARCHAR2(20) REFERENCES EMPLEADOS(id_cedula),
    medio_pago VARCHAR2(20) CHECK (medio_pago IN ('Efectivo', 'Transferencia')),
    total NUMBER(10, 2) NOT NULL
);

CREATE TABLE DETALLE_VENTA (
    id_detalle NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_venta VARCHAR2(20) REFERENCES VENTAS(id_venta),
    id_producto VARCHAR2(20) REFERENCES PRODUCTOS(id_producto),
    cantidad NUMBER(4) NOT NULL,
    precio_unitario NUMBER(10, 2) NOT NULL
);

-- DATOS INICIALES DE PRUEBA
INSERT INTO EMPLEADOS VALUES ('1061999888', 'Admin Nanis', '3151234567', 'Administrador', 1);
INSERT INTO PRODUCTOS VALUES ('P001', 'Blusa', 'S', 'Rosa', 55000, 15);
INSERT INTO PRODUCTOS VALUES ('P002', 'Jeans', 'M', 'Azul', 95000, 8);

COMMIT;