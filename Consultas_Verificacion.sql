-- Consultas_Verificacion
-- QuickDelivery S.A. - Grupo 6 MN
-- Estas consultas sirven para comprobar que lo que se registra desde la APP

USE quick_delivery;

-- 1. PAQUETES CARGADOS
SELECT
    p.id_paquete                AS paquete,
    p.destinatario              AS destinatario,
    p.peso                      AS peso_kg,
    ep.nombre_estado            AS estado,
    d.detalle                   AS direccion_destino,
    v.placa                     AS vehiculo,
    tv.nombre_tipo              AS tipo_vehiculo,
    uc.usuario                  AS conductor,
    ud.usuario                  AS despachador
FROM paquetes p
INNER JOIN estados_paquete ep ON ep.id_estado_paquete = p.id_estado_paquete
INNER JOIN direcciones     d  ON d.id_direccion       = p.id_direccion_destino
LEFT  JOIN asignaciones    a  ON a.id_paquete         = p.id_paquete
LEFT  JOIN vehiculos       v  ON v.id_vehiculo        = a.id_vehiculo
LEFT  JOIN tipos_vehiculo  tv ON tv.id_tipo_vehiculo  = v.id_tipo_vehiculo
LEFT  JOIN usuarios        uc ON uc.id_usuario        = v.id_conductor
LEFT  JOIN usuarios        ud ON ud.id_usuario        = a.id_despachador
ORDER BY p.id_paquete;


-- 2. INCIDENCIAS
SELECT
    i.id_incidencia   AS incidencia,
    i.id_paquete      AS paquete,
    p.destinatario    AS destinatario,
    v.placa           AS vehiculo,
    u.usuario         AS conductor,
    ti.nombre_tipo    AS tipo,
    i.descripcion     AS descripcion,
    i.fecha           AS fecha
FROM incidencias i
INNER JOIN paquetes         p  ON p.id_paquete         = i.id_paquete
INNER JOIN vehiculos        v  ON v.id_vehiculo        = i.id_vehiculo
INNER JOIN usuarios         u  ON u.id_usuario         = i.id_conductor
INNER JOIN tipos_incidencia ti ON ti.id_tipo_incidencia = i.id_tipo_incidencia
ORDER BY i.id_incidencia;

-- 3. UBICACIONES REPORTADAS POR LOS CONDUCTORES

SELECT
    ub.id_ubicacion   AS reporte,
    v.placa           AS vehiculo,
    tv.nombre_tipo    AS tipo,
    u.usuario         AS conductor,
    ub.latitud        AS latitud,
    ub.longitud       AS longitud,
    ub.estado_reportado AS estado,
    ub.fecha_hora     AS fecha
FROM ubicaciones_vehiculo ub
INNER JOIN vehiculos      v  ON v.id_vehiculo       = ub.id_vehiculo
INNER JOIN tipos_vehiculo tv ON tv.id_tipo_vehiculo = v.id_tipo_vehiculo
LEFT  JOIN usuarios       u  ON u.id_usuario        = v.id_conductor
ORDER BY ub.id_ubicacion DESC;

-- 5. RESUMEN
SELECT 'usuarios'             AS tabla, COUNT(*) AS registros FROM usuarios
UNION ALL SELECT 'vehiculos',            COUNT(*) FROM vehiculos
UNION ALL SELECT 'direcciones',          COUNT(*) FROM direcciones
UNION ALL SELECT 'paquetes',             COUNT(*) FROM paquetes
UNION ALL SELECT 'asignaciones',         COUNT(*) FROM asignaciones
UNION ALL SELECT 'ubicaciones_vehiculo', COUNT(*) FROM ubicaciones_vehiculo
UNION ALL SELECT 'incidencias',          COUNT(*) FROM incidencias
UNION ALL SELECT 'logs_sistema',         COUNT(*) FROM logs_sistema;
