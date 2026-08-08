-- BD_QuickDelivery
-- Base de datos basada en el Diagrama Entidad-Relacion normalizado - QuickDelivery S.A.

CREATE DATABASE IF NOT EXISTS quick_delivery
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE quick_delivery;

-- tabla roles
CREATE TABLE IF NOT EXISTS roles (
	id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(150)
    );

-- tabla usuarios
CREATE TABLE IF NOT EXISTS usuarios (
	id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_rol INT NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(255) NOT NULL,
    intentos INT DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_usuarios_rol
		FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla administradores (especializacion de usuarios)
CREATE TABLE IF NOT EXISTS administradores (
	id_usuario INT PRIMARY KEY,
    nivel INT NOT NULL,
    CONSTRAINT fk_administradores_usuario
		FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla conductores (especializacion de usuarios)
CREATE TABLE IF NOT EXISTS conductores (
	id_usuario INT PRIMARY KEY,
    licencia VARCHAR(30) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    CONSTRAINT fk_conductores_usuario
		FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla despachadores (especializacion de usuarios)
CREATE TABLE IF NOT EXISTS despachadores (
	id_usuario INT PRIMARY KEY,
    zona VARCHAR(80),
    CONSTRAINT fk_despachadores_usuario
		FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla tipos_vehiculo
CREATE TABLE IF NOT EXISTS tipos_vehiculo (
	id_tipo_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
    nombre_tipo VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(150)
    );

-- tabla estados_vehiculo
CREATE TABLE IF NOT EXISTS estados_vehiculo (
	id_estado_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(150)
    );

-- tabla vehiculos
CREATE TABLE IF NOT EXISTS vehiculos (
	id_vehiculo INT AUTO_INCREMENT PRIMARY KEY,
    id_tipo_vehiculo INT NOT NULL,
    id_estado_vehiculo INT NOT NULL,
    id_conductor INT NULL,
    placa VARCHAR(20) NOT NULL UNIQUE,
    capacidad_kg DECIMAL(10,2),
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_vehiculos_tipo
		FOREIGN KEY (id_tipo_vehiculo) REFERENCES tipos_vehiculo(id_tipo_vehiculo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT fk_vehiculos_estado
		FOREIGN KEY (id_estado_vehiculo) REFERENCES estados_vehiculo(id_estado_vehiculo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT fk_vehiculos_conductor
		FOREIGN KEY (id_conductor) REFERENCES conductores(id_usuario)
        ON DELETE SET NULL
        ON UPDATE CASCADE
    );

-- tabla autos (especializacion de vehiculos)
CREATE TABLE IF NOT EXISTS autos (
	id_vehiculo INT PRIMARY KEY,
    num_puertas INT NOT NULL,
    CONSTRAINT fk_autos_vehiculo
		FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla camiones (especializacion de vehiculos)
CREATE TABLE IF NOT EXISTS camiones (
	id_vehiculo INT PRIMARY KEY,
    toneladas DECIMAL(8,2) NOT NULL,
    CONSTRAINT fk_camiones_vehiculo
		FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla motos (especializacion de vehiculos)
CREATE TABLE IF NOT EXISTS motos (
	id_vehiculo INT PRIMARY KEY,
    caja_lateral BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_motos_vehiculo
		FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla estados_paquete
CREATE TABLE IF NOT EXISTS estados_paquete (
	id_estado_paquete INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(150)
    );

-- tabla direcciones
CREATE TABLE IF NOT EXISTS direcciones (
	id_direccion INT AUTO_INCREMENT PRIMARY KEY,
    provincia VARCHAR(60),
    canton VARCHAR(60),
    distrito VARCHAR(60),
    detalle VARCHAR(255)
    );

-- tabla paquetes
CREATE TABLE IF NOT EXISTS paquetes (
	id_paquete INT AUTO_INCREMENT PRIMARY KEY,
    id_estado_paquete INT NOT NULL,
    id_direccion_origen INT NOT NULL,
    id_direccion_destino INT NOT NULL,
    descripcion VARCHAR(255),
    fecha_registro DATETIME,
    CONSTRAINT fk_paquetes_estado
		FOREIGN KEY (id_estado_paquete) REFERENCES estados_paquete(id_estado_paquete)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT fk_paquetes_direccion_origen
		FOREIGN KEY (id_direccion_origen) REFERENCES direcciones(id_direccion)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT fk_paquetes_direccion_destino
		FOREIGN KEY (id_direccion_destino) REFERENCES direcciones(id_direccion)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla estados_asignacion
CREATE TABLE IF NOT EXISTS estados_asignacion (
	id_estado_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(150)
    );

-- tabla asignaciones
CREATE TABLE IF NOT EXISTS asignaciones (
	id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_paquete INT NOT NULL,
    id_vehiculo INT NOT NULL,
    id_despachador INT NOT NULL,
    id_estado_asignacion INT NOT NULL,
    fecha_asignada DATETIME,
    fecha_entrega DATETIME NULL,
    CONSTRAINT fk_asignaciones_paquete
		FOREIGN KEY (id_paquete) REFERENCES paquetes(id_paquete)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT fk_asignaciones_vehiculo
		FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT fk_asignaciones_despachador
		FOREIGN KEY (id_despachador) REFERENCES despachadores(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT fk_asignaciones_estado
		FOREIGN KEY (id_estado_asignacion) REFERENCES estados_asignacion(id_estado_asignacion)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla ubicaciones_vehiculo
CREATE TABLE IF NOT EXISTS ubicaciones_vehiculo (
	id_ubicacion INT AUTO_INCREMENT PRIMARY KEY,
    id_vehiculo INT NOT NULL,
    latitud DECIMAL(10,7),
    longitud DECIMAL(10,7),
    fecha_hora DATETIME,
    estado_reportado VARCHAR(80),
    CONSTRAINT fk_ubicaciones_vehiculo
		FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla tipos_incidencia
CREATE TABLE IF NOT EXISTS tipos_incidencia (
	id_tipo_incidencia INT AUTO_INCREMENT PRIMARY KEY,
    nombre_tipo VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(150)
    );

-- tabla incidencias
CREATE TABLE IF NOT EXISTS incidencias (
	id_incidencia INT AUTO_INCREMENT PRIMARY KEY,
    id_paquete INT NOT NULL,
    id_vehiculo INT NOT NULL,
    id_conductor INT NOT NULL,
    id_tipo_incidencia INT NOT NULL,
    descripcion VARCHAR(255),
    fecha DATETIME,
    CONSTRAINT fk_incidencias_paquete
		FOREIGN KEY (id_paquete) REFERENCES paquetes(id_paquete)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT fk_incidencias_vehiculo
		FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT fk_incidencias_conductor
		FOREIGN KEY (id_conductor) REFERENCES conductores(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT fk_incidencias_tipo
		FOREIGN KEY (id_tipo_incidencia) REFERENCES tipos_incidencia(id_tipo_incidencia)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- tabla logs_sistema
CREATE TABLE IF NOT EXISTS logs_sistema (
	id_log INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    accion VARCHAR(80),
    modulo VARCHAR(80),
    fecha_hora DATETIME,
    detalle TEXT,
    CONSTRAINT fk_logs_usuario
		FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    );

-- INSERTS

-- roles
INSERT INTO roles (nombre_rol, descripcion)
VALUES ('Administrador', 'Administra el sistema QuickDelivery');

INSERT INTO roles (nombre_rol, descripcion)
VALUES ('Conductor', 'Realiza las entregas de paquetes');

INSERT INTO roles (nombre_rol, descripcion)
VALUES ('Despachador', 'Asigna paquetes a los vehiculos');

-- usuarios
INSERT INTO usuarios (id_rol, usuario, contrasena_hash, intentos, activo)
VALUES (1, 'admin', 'hash_admin', 0, TRUE);

INSERT INTO usuarios (id_rol, usuario, contrasena_hash, intentos, activo)
VALUES (2, 'jperez', 'hash_jperez', 0, TRUE);

INSERT INTO usuarios (id_rol, usuario, contrasena_hash, intentos, activo)
VALUES (3, 'mgomez', 'hash_mgomez', 0, TRUE);

-- especializaciones de usuarios
INSERT INTO administradores (id_usuario, nivel)
VALUES (1, 1);

INSERT INTO conductores (id_usuario, licencia, telefono)
VALUES (2, 'B1-123456', '8888-8888');

INSERT INTO despachadores (id_usuario, zona)
VALUES (3, 'San Jose Centro');

-- tipos y estados de vehiculo
INSERT INTO tipos_vehiculo (nombre_tipo, descripcion)
VALUES ('Auto', 'Vehiculo liviano');

INSERT INTO tipos_vehiculo (nombre_tipo, descripcion)
VALUES ('Camion', 'Vehiculo de carga pesada');

INSERT INTO tipos_vehiculo (nombre_tipo, descripcion)
VALUES ('Moto', 'Vehiculo de dos ruedas');

INSERT INTO estados_vehiculo (nombre_estado, descripcion)
VALUES ('Disponible', 'Vehiculo listo para asignar');

INSERT INTO estados_vehiculo (nombre_estado, descripcion)
VALUES ('En ruta', 'Vehiculo realizando entrega');

-- vehiculos
INSERT INTO vehiculos (id_tipo_vehiculo, id_estado_vehiculo, id_conductor, placa, capacidad_kg, activo)
VALUES (1, 1, 2, 'CRC-001', 500.00, TRUE);

INSERT INTO vehiculos (id_tipo_vehiculo, id_estado_vehiculo, id_conductor, placa, capacidad_kg, activo)
VALUES (3, 1, NULL, 'MOT-045', 30.00, TRUE);

-- especializaciones de vehiculos
INSERT INTO autos (id_vehiculo, num_puertas)
VALUES (1, 4);

INSERT INTO motos (id_vehiculo, caja_lateral)
VALUES (2, TRUE);

-- estados de paquete
INSERT INTO estados_paquete (nombre_estado, descripcion)
VALUES ('Registrado', 'Paquete ingresado al sistema');

INSERT INTO estados_paquete (nombre_estado, descripcion)
VALUES ('Entregado', 'Paquete entregado al destinatario');

-- direcciones
INSERT INTO direcciones (provincia, canton, distrito, detalle)
VALUES ('San Jose', 'Central', 'Carmen', 'Avenida 1, Bodega #10');

INSERT INTO direcciones (provincia, canton, distrito, detalle)
VALUES ('Cartago', 'Central', 'Oriental', 'Casa color azul, 200m sur de la iglesia');

-- paquetes
INSERT INTO paquetes (id_estado_paquete, id_direccion_origen, id_direccion_destino, descripcion, fecha_registro)
VALUES (1, 1, 2, 'Caja de repuestos electronicos', '2026-07-23 08:30:00');

-- estados de asignacion
INSERT INTO estados_asignacion (nombre_estado, descripcion)
VALUES ('Pendiente', 'Asignacion creada sin iniciar');

INSERT INTO estados_asignacion (nombre_estado, descripcion)
VALUES ('Completada', 'Asignacion finalizada');

-- asignaciones 
INSERT INTO asignaciones (id_paquete, id_vehiculo, id_despachador, id_estado_asignacion, fecha_asignada, fecha_entrega)
VALUES (1, 1, 3, 1, '2026-07-23 09:00:00', NULL);

-- ubicaciones de vehiculo
INSERT INTO ubicaciones_vehiculo (id_vehiculo, latitud, longitud, fecha_hora, estado_reportado)
VALUES (1, 9.9333300, -84.0833300, '2026-07-23 09:15:00', 'En ruta');

-- tipos de incidencia
INSERT INTO tipos_incidencia (nombre_tipo, descripcion)
VALUES ('Retraso', 'Demora en la entrega');

INSERT INTO tipos_incidencia (nombre_tipo, descripcion)
VALUES ('Averia', 'Fallo mecanico del vehiculo');

-- inciidencias
INSERT INTO incidencias (id_paquete, id_vehiculo, id_conductor, id_tipo_incidencia, descripcion, fecha)
VALUES (1, 1, 2, 1, 'Retraso por congestion vial', '2026-07-23 10:00:00');

-- logs del sistema
INSERT INTO logs_sistema (id_usuario, accion, modulo, fecha_hora, detalle)
VALUES (1, 'LOGIN', 'Seguridad', '2026-07-23 08:00:00', 'Inicio de sesion del administrador');

COMMIT;

-- SELECT

SELECT * FROM usuarios;


SELECT * FROM vehiculos
WHERE id_estado_vehiculo = 1;

SELECT * FROM incidencias;
