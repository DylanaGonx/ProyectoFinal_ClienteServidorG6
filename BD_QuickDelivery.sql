-- BD_QuickDelivery
-- Base de datos basada en el Diagrama Entidad-Relacion normalizado - QuickDelivery S.A.
--
-- Este script crea la base completa y la deja lista para trabajar con el
-- proyecto Java. Se ejecuta de una sola vez desde MySQL Workbench.
--
-- La primera linea borra la base si ya existe, para que el script se pueda
-- volver a correr cuantas veces sea necesario y siempre quede igual.

DROP DATABASE IF EXISTS quick_delivery;

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
-- Aqui se maneja si el vehiculo es moto, auto, camion o furgon. No hay una
-- tabla por cada tipo: el tipo es un dato, no una entidad aparte.
-- El nombre del tipo es el mismo que devuelve el metodo getTipo() de cada
-- subclase de model.Vehiculo, por eso dice Automovil y no Auto.
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

-- tabla estados_paquete
-- Los nombres son los mismos del enum model.EstadoPaquete
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
-- destinatario y peso corresponden a los atributos de la clase model.Paquete.
-- El peso se necesita para validar que el vehiculo tenga capacidad suficiente.
CREATE TABLE IF NOT EXISTS paquetes (
	id_paquete INT AUTO_INCREMENT PRIMARY KEY,
    id_estado_paquete INT NOT NULL,
    id_direccion_origen INT NOT NULL,
    id_direccion_destino INT NOT NULL,
    destinatario VARCHAR(120) NOT NULL,
    peso DECIMAL(10,2) NOT NULL,
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
-- El despachador acepta nulos porque el administrador tambien puede asignar
-- un vehiculo y en ese caso no hay despachador responsable.
CREATE TABLE IF NOT EXISTS asignaciones (
	id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_paquete INT NOT NULL,
    id_vehiculo INT NOT NULL,
    id_despachador INT NULL,
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
    latitud DECIMAL(10,7) NULL,
    longitud DECIMAL(10,7) NULL,
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
-- Estos son los usuarios de prueba del sistema, coinciden con el archivo
-- UsuariosContrasena.txt

INSERT INTO usuarios (id_rol, usuario, contrasena_hash, intentos, activo)
VALUES (1, 'admin', '1234', 0, TRUE);

INSERT INTO usuarios (id_rol, usuario, contrasena_hash, intentos, activo)
VALUES (2, 'cond', '1234', 0, TRUE);

INSERT INTO usuarios (id_rol, usuario, contrasena_hash, intentos, activo)
VALUES (3, 'desp', '1234', 0, TRUE);

-- Segundo conductor, para poder probar dos vehiculos conectados a la vez

INSERT INTO usuarios (id_rol, usuario, contrasena_hash, intentos, activo)
VALUES (2, 'cond2', '1234', 0, TRUE);

-- especializaciones de usuarios
-- El administrador no lleva tabla aparte: ser administrador es un rol, y el rol
-- ya esta en la tabla roles. Conductores y despachadores si llevan tabla porque
-- guardan datos propios (licencia, telefono, zona).

INSERT INTO conductores (id_usuario, licencia, telefono)
VALUES (2, 'B1-123456', '8888-8888');

INSERT INTO despachadores (id_usuario, zona)
VALUES (3, 'San Jose Centro');

INSERT INTO conductores (id_usuario, licencia, telefono)
VALUES (4, 'B1-987654', '7777-7777');

-- tipos y estados de vehiculo

INSERT INTO tipos_vehiculo (nombre_tipo, descripcion)
VALUES ('Automovil', 'Vehiculo liviano');

INSERT INTO tipos_vehiculo (nombre_tipo, descripcion)
VALUES ('Camion', 'Vehiculo de carga pesada');

INSERT INTO tipos_vehiculo (nombre_tipo, descripcion)
VALUES ('Motocicleta', 'Vehiculo de dos ruedas');

INSERT INTO tipos_vehiculo (nombre_tipo, descripcion)
VALUES ('Furgon', 'Vehiculo de carga mediana');

INSERT INTO estados_vehiculo (nombre_estado, descripcion)
VALUES ('Disponible', 'Vehiculo listo para asignar');

INSERT INTO estados_vehiculo (nombre_estado, descripcion)
VALUES ('En ruta', 'Vehiculo realizando entrega');

-- vehiculos

INSERT INTO vehiculos (id_tipo_vehiculo, id_estado_vehiculo, id_conductor, placa, capacidad_kg, activo)
VALUES (1, 1, 2, 'CRC-001', 500.00, TRUE);

INSERT INTO vehiculos (id_tipo_vehiculo, id_estado_vehiculo, id_conductor, placa, capacidad_kg, activo)
VALUES (3, 1, 4, 'MOT-045', 30.00, TRUE);

-- estados de paquete

INSERT INTO estados_paquete (nombre_estado, descripcion)
VALUES ('PENDIENTE', 'Paquete registrado sin salir de bodega');

INSERT INTO estados_paquete (nombre_estado, descripcion)
VALUES ('ENTREGADO', 'Paquete entregado al destinatario');

INSERT INTO estados_paquete (nombre_estado, descripcion)
VALUES ('EN_TRANSITO', 'Paquete en ruta hacia el destino');

-- direcciones
-- La direccion 1 es la bodega central y se usa como origen de todos los
-- paquetes que se registran desde el sistema.

INSERT INTO direcciones (provincia, canton, distrito, detalle)
VALUES ('San Jose', 'Central', 'Carmen', 'Avenida 1, Bodega #10');

INSERT INTO direcciones (provincia, canton, distrito, detalle)
VALUES ('Cartago', 'Central', 'Oriental', 'Casa color azul, 200m sur de la iglesia');

-- paquetes

INSERT INTO paquetes (id_estado_paquete, id_direccion_origen, id_direccion_destino, destinatario, peso, descripcion, fecha_registro)
VALUES (1, 1, 2, 'Cliente de prueba', 12.50, 'Caja de repuestos electronicos', '2026-07-23 08:30:00');

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

INSERT INTO tipos_incidencia (nombre_tipo, descripcion)
VALUES ('Otro', 'Incidencia no clasificada');

-- incidencias

INSERT INTO incidencias (id_paquete, id_vehiculo, id_conductor, id_tipo_incidencia, descripcion, fecha)
VALUES (1, 1, 2, 1, 'Retraso por congestion vial', '2026-07-23 10:00:00');

-- logs del sistema

INSERT INTO logs_sistema (id_usuario, accion, modulo, fecha_hora, detalle)
VALUES (1, 'LOGIN', 'Seguridad', '2026-07-23 08:00:00', 'Inicio de sesion del administrador');

COMMIT;

-- SELECT DE VERIFICACION
--
-- Estos SELECT confirman que la base quedo bien creada con los datos iniciales.
--
-- Para comprobar despues que lo que se registra desde la aplicacion si llega a
-- la base, se usa el archivo Consultas_Verificacion.sql, que cruza varias
-- tablas y no modifica nada.
--
-- OJO: no se debe volver a correr este archivo para revisar datos, porque la
-- primera linea borra la base completa.

SELECT u.id_usuario, u.usuario, r.nombre_rol
FROM usuarios u
INNER JOIN roles r ON r.id_rol = u.id_rol;

SELECT v.id_vehiculo, v.placa, t.nombre_tipo, v.capacidad_kg, u.usuario AS conductor
FROM vehiculos v
INNER JOIN tipos_vehiculo t ON t.id_tipo_vehiculo = v.id_tipo_vehiculo
LEFT JOIN usuarios u ON u.id_usuario = v.id_conductor;

SELECT p.id_paquete, p.destinatario, p.peso, e.nombre_estado, d.detalle
FROM paquetes p
INNER JOIN estados_paquete e ON e.id_estado_paquete = p.id_estado_paquete
INNER JOIN direcciones d ON d.id_direccion = p.id_direccion_destino;

SELECT * FROM incidencias;
