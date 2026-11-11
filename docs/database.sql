-- ============================================================================
-- PROJECT:        Little Horizons — School ERP
-- FILE:           docs/database.sql
-- PURPOSE:        Complete database definition for the Little Horizons school
--                 management system. It contains the full schema (DDL) plus a
--                 coherent set of realistic Colombian test data (DML) for
--                 development and testing environments.
-- ENGINE:         MySQL 8.0+ (relies on CHECK constraints, ENUM and utf8mb4)
-- CHARACTER SET:  utf8mb4 / utf8mb4_unicode_ci (full Spanish accent support)
-- HOW TO RUN:     mysql -u root -p < docs/database.sql
--                 (or, inside a MySQL session:  SOURCE docs/database.sql;)
-- WARNING:        The script drops and recreates the erp_little_horizons
--                 database. Do NOT run it against a production instance.
-- ============================================================================


-- ============================================================================
-- SECTION 1: DDL — Official corrected schema (reproduced verbatim, do not alter)
-- ============================================================================

DROP DATABASE IF EXISTS erp_little_horizons;
CREATE DATABASE erp_little_horizons CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_little_horizons;

CREATE TABLE grados (
    id_grado INT AUTO_INCREMENT PRIMARY KEY,
    nombre_grado VARCHAR(50) NOT NULL UNIQUE COMMENT 'Ej: Preescolar, Primero, Segundo, ... Once',
    nivel_numerico INT NOT NULL UNIQUE COMMENT 'Nivel numérico para ordenamiento (0 para preescolar, 1-11 para grados)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Catálogo de grados académicos del colegio';

CREATE TABLE estudiantes (
    id_estudiante INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('TI', 'CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'TI',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    genero ENUM('M', 'F', 'Otro') NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    correo_electronico VARCHAR(100),
    tipo_sangre VARCHAR(5),
    alergias TEXT,
    condiciones_medicas TEXT,
    fecha_ingreso DATE NOT NULL,
    estado ENUM('Activo', 'Graduado', 'Retirado', 'Suspendido') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido, primer_nombre)
) ENGINE=InnoDB COMMENT='Información completa de estudiantes';

CREATE TABLE acudientes (
    id_acudiente INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    parentesco VARCHAR(50) NOT NULL COMMENT 'Padre, Madre, Abuelo, Tío, Tutor Legal, etc.',
    telefono_principal VARCHAR(20) NOT NULL,
    telefono_secundario VARCHAR(20),
    correo_electronico VARCHAR(100),
    direccion VARCHAR(255),
    ocupacion VARCHAR(100),
    es_responsable_financiero BOOLEAN DEFAULT TRUE COMMENT 'Indica si es responsable de pagos',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Acudientes y tutores legales';

CREATE TABLE estudiante_acudiente (
    id_estudiante INT NOT NULL,
    id_acudiente INT NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE COMMENT 'Acudiente principal para comunicaciones',
    es_contacto_emergencia BOOLEAN DEFAULT TRUE COMMENT 'Disponible para emergencias',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_estudiante, id_acudiente),
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_acudiente (id_acudiente)
) ENGINE=InnoDB COMMENT='Relación entre estudiantes y sus acudientes';

CREATE TABLE matriculas (
    id_matricula INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_grado INT NOT NULL,
    año_academico INT NOT NULL COMMENT 'Año académico (ej: 2024)',
    estado_matricula ENUM('Activa', 'Completada', 'Retirada') DEFAULT 'Activa',
    fecha_matricula DATE NOT NULL,
    fecha_retiro DATE,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    UNIQUE KEY matricula_unica (id_estudiante, año_academico),
    INDEX idx_año_grado (año_academico, id_grado),
    INDEX idx_estado (estado_matricula)
) ENGINE=InnoDB COMMENT='Historial de matrículas de estudiantes por año y grado';

CREATE TABLE materias (
    id_materia INT AUTO_INCREMENT PRIMARY KEY,
    codigo_materia VARCHAR(20) NOT NULL UNIQUE COMMENT 'Código único de la materia (ej: MAT-5, ESP-3)',
    nombre_materia VARCHAR(100) NOT NULL,
    descripcion TEXT,
    id_grado INT NOT NULL COMMENT 'Grado al que pertenece la materia',
    horas_semanales INT DEFAULT 2 COMMENT 'Horas semanales asignadas',
    esta_activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    INDEX idx_grado (id_grado),
    INDEX idx_activa (esta_activa),
    INDEX idx_codigo (codigo_materia)
) ENGINE=InnoDB COMMENT='Catálogo de materias por grado';

CREATE TABLE profesores (
    id_profesor INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    correo_electronico VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    especializacion VARCHAR(100) COMMENT 'Área de especialización (Matemáticas, Español, etc.)',
    fecha_contratacion DATE NOT NULL,
    estado ENUM('Activo', 'Inactivo', 'Licencia') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Información de profesores';

CREATE TABLE asignaciones_profesor (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_profesor INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    seccion VARCHAR(10) COMMENT 'Sección o grupo (A, B, C)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY asignacion_unica (id_profesor, id_materia, año_academico, seccion),
    INDEX idx_profesor_año (id_profesor, año_academico),
    INDEX idx_materia_año (id_materia, año_academico)
) ENGINE=InnoDB COMMENT='Asignación de profesores a materias por año académico';

CREATE TABLE inscripciones_materia (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV') NOT NULL COMMENT 'Período académico',
    seccion VARCHAR(10) COMMENT 'Sección o grupo asignado',
    fecha_inscripcion DATE NOT NULL,
    estado ENUM('Activa', 'Retirada', 'Completada') DEFAULT 'Activa',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY inscripcion_unica (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico)
) ENGINE=InnoDB COMMENT='Inscripción de estudiantes en materias específicas';

CREATE TABLE registros_academicos (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    id_profesor INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Final') NOT NULL,
    valor_nota DECIMAL(2,1) NOT NULL COMMENT 'Nota de 0.0 a 5.0 según estándar colombiano',
    observaciones TEXT,
    fecha_registro DATE NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor),
    CHECK (valor_nota >= 0.0 AND valor_nota <= 5.0),
    UNIQUE KEY registro_unico (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico),
    INDEX idx_valor_nota (valor_nota)
) ENGINE=InnoDB COMMENT='Registro de calificaciones de estudiantes';

CREATE TABLE asistencias (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    fecha_asistencia DATE NOT NULL,
    estado_asistencia ENUM('Presente', 'Ausente', 'Tardanza', 'Excusa Médica') NOT NULL,
    observaciones TEXT,
    registrado_por INT COMMENT 'ID del profesor que registró la asistencia',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (registrado_por) REFERENCES profesores(id_profesor),
    UNIQUE KEY asistencia_unica (id_estudiante, id_materia, fecha_asistencia),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_asistencia),
    INDEX idx_materia_fecha (id_materia, fecha_asistencia),
    INDEX idx_estado (estado_asistencia)
) ENGINE=InnoDB COMMENT='Registro de asistencia diaria de estudiantes';

CREATE TABLE clubes (
    id_club INT AUTO_INCREMENT PRIMARY KEY,
    nombre_club VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    id_profesor_supervisor INT COMMENT 'Profesor encargado del club',
    capacidad_maxima INT DEFAULT 30 COMMENT 'Cupo máximo de estudiantes',
    horario_reunion VARCHAR(100) COMMENT 'Días y horario de reunión (ej: Martes 3-5 PM)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor_supervisor) REFERENCES profesores(id_profesor),
    INDEX idx_activo (esta_activo),
    INDEX idx_nombre (nombre_club)
) ENGINE=InnoDB COMMENT='Catálogo de clubes extracurriculares';

CREATE TABLE membresias_club (
    id_membresia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_club INT NOT NULL,
    fecha_ingreso DATE NOT NULL,
    estado_membresia ENUM('Activa', 'Retirada') DEFAULT 'Activa',
    fecha_retiro DATE,
    motivo_retiro TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_club) REFERENCES clubes(id_club) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_club_estado (id_club, estado_membresia)
) ENGINE=InnoDB COMMENT='Membresías de estudiantes en clubes';

CREATE TABLE eventos (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_evento VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_evento ENUM('Académico', 'Cultural', 'Deportivo', 'Social', 'Científico') NOT NULL,
    fecha_evento DATE NOT NULL,
    lugar VARCHAR(150),
    tiene_competencia BOOLEAN DEFAULT FALSE COMMENT 'Indica si el evento tiene competencia con posiciones',
    id_organizador INT COMMENT 'Profesor organizador del evento',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_organizador) REFERENCES profesores(id_profesor),
    INDEX idx_fecha (fecha_evento),
    INDEX idx_tipo (tipo_evento),
    INDEX idx_activo (esta_activo)
) ENGINE=InnoDB COMMENT='Catálogo de eventos del colegio';

CREATE TABLE participantes_evento (
    id_participacion INT AUTO_INCREMENT PRIMARY KEY,
    id_evento INT NOT NULL,
    id_estudiante INT NOT NULL,
    rol_participacion ENUM('Participante', 'Competidor', 'Organizador', 'Asistente') DEFAULT 'Participante',
    posicion_obtenida VARCHAR(20) COMMENT 'Posición obtenida si hay competencia (1er Lugar, 2do Lugar, 3er Lugar, Participante)',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    UNIQUE KEY participacion_unica (id_evento, id_estudiante),
    INDEX idx_evento (id_evento),
    INDEX idx_estudiante (id_estudiante)
) ENGINE=InnoDB COMMENT='Registro de participación de estudiantes en eventos';

CREATE TABLE conceptos_pago (
    id_concepto INT AUTO_INCREMENT PRIMARY KEY,
    codigo_concepto VARCHAR(20) NOT NULL UNIQUE,
    nombre_concepto VARCHAR(100) NOT NULL,
    descripcion TEXT,
    monto_base DECIMAL(10,2) NOT NULL COMMENT 'Valor base del concepto en pesos colombianos',
    es_recurrente BOOLEAN DEFAULT FALSE COMMENT 'Indica si es un pago recurrente (ej: pensión mensual)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activo (esta_activo),
    INDEX idx_codigo (codigo_concepto)
) ENGINE=InnoDB COMMENT='Catálogo de conceptos de pago del colegio';

CREATE TABLE pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_acudiente INT COMMENT 'Acudiente que realizó el pago',
    id_concepto INT NOT NULL,
    monto_pagado DECIMAL(10,2) NOT NULL,
    fecha_pago DATE NOT NULL,
    metodo_pago ENUM('Efectivo', 'Transferencia', 'Tarjeta Débito', 'Tarjeta Crédito', 'Cheque') NOT NULL,
    numero_recibo VARCHAR(50) UNIQUE,
    año_academico INT COMMENT 'Año al que aplica el pago',
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Anual') COMMENT 'Período al que aplica el pago',
    notas TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente),
    FOREIGN KEY (id_concepto) REFERENCES conceptos_pago(id_concepto),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_pago),
    INDEX idx_concepto (id_concepto),
    INDEX idx_fecha (fecha_pago),
    INDEX idx_recibo (numero_recibo)
) ENGINE=InnoDB COMMENT='Registro de todos los pagos realizados';

CREATE TABLE facturas (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(50) NOT NULL UNIQUE COMMENT 'Número consecutivo de factura',
    id_pago INT NOT NULL COMMENT 'Pago asociado a esta factura',
    fecha_emision DATE NOT NULL,
    cufe VARCHAR(100) COMMENT 'Código Único de Factura Electrónica (simulado)',
    nombre_archivo_pdf VARCHAR(255) COMMENT 'Nombre del archivo de factura generado (formato .txt)',
    ruta_archivo VARCHAR(500) COMMENT 'Ruta completa donde se guardó el archivo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_pago) REFERENCES pagos(id_pago) ON DELETE CASCADE,
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_fecha_emision (fecha_emision),
    INDEX idx_pago (id_pago)
) ENGINE=InnoDB COMMENT='Registro de facturas electrónicas generadas';

CREATE TABLE categorias_recurso (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Categorías de recursos (Libro, Equipo de Laboratorio, etc.)';

CREATE TABLE recursos (
    id_recurso INT AUTO_INCREMENT PRIMARY KEY,
    codigo_recurso VARCHAR(50) NOT NULL UNIQUE COMMENT 'Código de inventario único',
    nombre_recurso VARCHAR(150) NOT NULL,
    id_categoria INT NOT NULL,
    descripcion TEXT,
    fecha_adquisicion DATE,
    ubicacion_fisica VARCHAR(100) COMMENT 'Ubicación física (Biblioteca, Laboratorio 1, etc.)',
    estado_recurso ENUM('Disponible', 'Prestado', 'En Mantenimiento', 'Dado de Baja') DEFAULT 'Disponible',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES categorias_recurso(id_categoria),
    INDEX idx_codigo (codigo_recurso),
    INDEX idx_estado (estado_recurso),
    INDEX idx_categoria (id_categoria),
    INDEX idx_nombre (nombre_recurso)
) ENGINE=InnoDB COMMENT='Inventario de recursos prestables del colegio';

CREATE TABLE prestamos_estudiante (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_estudiante INT NOT NULL COMMENT 'Estudiante que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a estudiantes';

CREATE TABLE prestamos_profesor (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_profesor INT NOT NULL COMMENT 'Profesor que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_profesor (id_profesor),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a profesores';


-- ============================================================================
-- SECTION 2: DML — Realistic Colombian test data (academic year 2026)
-- All inserts use explicit ids so cross-table references stay consistent.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Academic grades: the 12 official grades, Preescolar (level 0) to Once (level 11)
-- ----------------------------------------------------------------------------
INSERT INTO grados (id_grado, nombre_grado, nivel_numerico) VALUES
(1,  'Preescolar', 0),
(2,  'Primero',    1),
(3,  'Segundo',    2),
(4,  'Tercero',    3),
(5,  'Cuarto',     4),
(6,  'Quinto',     5),
(7,  'Sexto',      6),
(8,  'Séptimo',    7),
(9,  'Octavo',     8),
(10, 'Noveno',     9),
(11, 'Décimo',    10),
(12, 'Once',      11);

-- ----------------------------------------------------------------------------
-- Resource categories: the 5 official inventory categories
-- ----------------------------------------------------------------------------
INSERT INTO categorias_recurso (id_categoria, nombre_categoria, descripcion) VALUES
(1, 'Libro',                   'Textos escolares y de consulta de la biblioteca'),
(2, 'Equipo de Laboratorio',   'Instrumentos y materiales para prácticas de ciencias'),
(3, 'Equipo Deportivo',        'Balones, redes, uniformes e implementos deportivos'),
(4, 'Dispositivo Tecnológico', 'Tabletas, computadores, proyectores y cámaras'),
(5, 'Material Didáctico',      'Juegos, maletas didácticas y material de apoyo docente');

-- ----------------------------------------------------------------------------
-- Payment concepts: the 7 official concepts with base amounts in COP
-- ----------------------------------------------------------------------------
INSERT INTO conceptos_pago (id_concepto, codigo_concepto, nombre_concepto, descripcion, monto_base, es_recurrente) VALUES
(1, 'MAT',          'Matrícula',                  'Pago anual de matrícula del estudiante',            500000.00, FALSE),
(2, 'PENSION',      'Pensión Mensual',            'Pensión académica mensual',                          350000.00, TRUE),
(3, 'UNIFORME',     'Uniforme',                   'Juego de uniforme escolar (diario y deportivo)',     250000.00, FALSE),
(4, 'TRANSPORTE',   'Transporte Escolar',         'Servicio de ruta escolar mensual',                   150000.00, TRUE),
(5, 'ALIMENTACION', 'Servicio de Alimentación',   'Almuerzos escolares del mes',                        120000.00, TRUE),
(6, 'ACTIVIDAD',    'Actividad Especial',         'Salidas pedagógicas y actividades extracurriculares', 50000.00, FALSE),
(7, 'CERTIFICADO',  'Certificado',                'Emisión de certificados y constancias',               15000.00, FALSE);

-- ----------------------------------------------------------------------------
-- Teachers: 30 teachers (ids 1-30). Hire dates in the past. Subject and club
-- assignments below reference these ids.
-- ----------------------------------------------------------------------------
INSERT INTO profesores (id_profesor, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) VALUES
(1,  'CC', '1015234567', 'Ana Lucía',        'María',      'Peña',        'Salazar',   'ana.pena@littlehorizons.edu.co',       '+57 301 245 7896', 'Matemáticas',           '2012-01-16', 'Activo'),
(2,  'CC', '1025345678', 'Ricardo',          'Enrique',    'Bermúdez',    'Ortiz',     'ricardo.bermudez@littlehorizons.edu.co','+57 302 356 7891', 'Matemáticas',           '2015-02-02', 'Activo'),
(3,  'CC', '1035456789', 'Paula',            'Andrea',     'Salgar',      'Vargas',    'paula.salgar@littlehorizons.edu.co',   '+57 313 467 8902', 'Matemáticas',           '2018-01-22', 'Activo'),
(4,  'CC', '1040567890', 'Óscar',            'Iván',       'Mesa',        'Duarte',    'oscar.mesa@littlehorizons.edu.co',     '+57 320 578 9013', 'Matemáticas',           '2010-01-18', 'Activo'),
(5,  'CC', '1050678901', 'Carlos',           'Arturo',     'Sabogal',     'Vargas',    'carlos.sabogal@littlehorizons.edu.co', '+57 315 689 0124', 'Matemáticas',           '2011-01-24', 'Activo'),
(6,  'CC', '1060789012', 'Julián',           'Esteban',    'Rivera',      'Álvarez',   'julian.rivera@littlehorizons.edu.co',  '+57 316 790 1235', 'Español y Literatura',  '2013-01-21', 'Activo'),
(7,  'CC', '1070890123', 'Diana',            'Carolina',   'Ayala',       'Pinto',     'diana.ayala@littlehorizons.edu.co',    '+57 317 801 2346', 'Español y Literatura',  '2016-01-25', 'Activo'),
(8,  'CC', '1080901234', 'Sergio',           'Andrés',     'Gutiérrez',   'Rojas',     'sergio.gutierrez@littlehorizons.edu.co','+57 318 912 3457', 'Español y Literatura',  '2014-01-20', 'Activo'),
(9,  'CC', '1090012345', 'Luz',              'Adriana',    'Carrillo',    'Nieto',     'luz.carrillo@littlehorizons.edu.co',   '+57 319 023 4568', 'Inglés',                '2017-01-23', 'Activo'),
(10, 'CC', '1101123456', 'John',             'Freddy',     'Vélez',       'Ramírez',   'john.velez@littlehorizons.edu.co',     '+57 310 134 5679', 'Inglés',                '2019-01-21', 'Activo'),
(11, 'CC', '1112234567', 'Margarita',        'Rosa',       'Cabanzo',     'Herrera',   'margarita.cabanzo@littlehorizons.edu.co','+57 311 245 6780', 'Educación Física',      '2012-02-06', 'Activo'),
(12, 'CC', '1123345678', 'Iván',             'Darío',      'Lozano',      'Prada',     'ivan.lozano@littlehorizons.edu.co',    '+57 312 356 7891', 'Educación Física',      '2015-01-26', 'Activo'),
(13, 'CC', '1134456789', 'Néstor',           'Julián',     'Quintero',    'Rincón',    'nestor.quintero@littlehorizons.edu.co','+57 314 467 8902', 'Ciencias Naturales',    '2013-07-15', 'Activo'),
(14, 'CC', '1145567890', 'Adriana',          'Lucía',      'Ocampo',      'Salamanca', 'adriana.ocampo@littlehorizons.edu.co', '+57 320 578 9013', 'Ciencias Naturales',    '2018-01-29', 'Activo'),
(15, 'CC', '1156678901', 'Álvaro',           'José',       'Sanabria',    'Torres',    'alvaro.sanabria@littlehorizons.edu.co','+57 321 689 0124', 'Ciencias Sociales',     '2011-08-01', 'Activo'),
(16, 'CC', '1167789012', 'Diana',            'Marcela',    'Contreras',   'Pachón',    'diana.contreras@littlehorizons.edu.co','+57 322 790 1235', 'Ciencias Sociales',     '2016-02-01', 'Activo'),
(17, 'CC', '1178890123', 'Laura',            'Ximena',     'Valcárcel',   'Gómez',     'laura.valcarcel@littlehorizons.edu.co','+57 323 801 2346', 'Matemáticas',           '2020-01-20', 'Activo'),
(18, 'CC', '1189901234', 'Andrés',           'Felipe',     'Duarte',      'Mora',      'andres.duarte@littlehorizons.edu.co',  '+57 324 912 3457', 'Matemáticas',           '2019-01-28', 'Activo'),
(19, 'CC', '1191012345', 'Mónica',           'Liliana',    'Fajardo',     'Céspedes',  'monica.fajardo@littlehorizons.edu.co', '+57 325 023 4568', 'Matemáticas',           '2021-01-25', 'Activo'),
(20, 'CC', '1202123456', 'Camilo',           'Andrés',     'Niño',        'Rojas',     'camilo.nino@littlehorizons.edu.co',    '+57 326 134 5679', 'Matemáticas',           '2022-01-24', 'Activo'),
(21, 'CC', '1213234567', 'Blanca',           'Nubia',      'Torrado',     'Pardo',     'blanca.torrado@littlehorizons.edu.co', '+57 327 245 6780', 'Matemáticas',           '2017-01-30', 'Activo'),
(22, 'CC', '1224345678', 'María',            'Alexandra',  'Prieto',      'Cuello',    'maria.prieto@littlehorizons.edu.co',   '+57 328 356 7891', 'Español y Literatura',  '2020-02-03', 'Activo'),
(23, 'CC', '1235456789', 'Édgar',            'Mauricio',   'Villamizar',  'Gómez',     'edgar.villamizar@littlehorizons.edu.co','+57 329 467 8902', 'Biología',              '2015-08-10', 'Activo'),
(24, 'CC', '1246567890', 'Yolanda',          'Rosa',       'Patarroyo',   'Díaz',      'yolanda.patarroyo@littlehorizons.edu.co','+57 330 578 9013', 'Química',               '2018-07-23', 'Activo'),
(25, 'CC', '1257678901', 'Gustavo',          'Adolfo',     'Rueda',       'Zabala',    'gustavo.rueda@littlehorizons.edu.co',  '+57 331 689 0124', 'Matemáticas',           '2014-02-10', 'Activo'),
(26, 'CC', '1268789012', 'Ángela',           'Patricia',   'Rozo',        'Martín',    'angela.rozo@littlehorizons.edu.co',    '+57 332 790 1235', 'Español y Literatura',  '2021-08-16', 'Activo'),
(27, 'CC', '1279890123', 'Julián',           'Camilo',     'Angarita',    'Suárez',    'julian.angarita@littlehorizons.edu.co','+57 333 801 2346', 'Física',                '2022-07-18', 'Activo'),
(28, 'CC', '1281901234', 'Luz',              'Marina',     'Barbosa',     'Chaves',    'luz.barbosa@littlehorizons.edu.co',    '+57 334 912 3457', 'Educación Física',      '2016-07-11', 'Activo'),
(29, 'CC', '1292012345', 'Édgar',            'Alfonso',    'Neira',       'Bahos',     'edgar.neira@littlehorizons.edu.co',    '+57 335 023 4568', 'Ciencias Sociales',     '2013-02-18', 'Activo'),
(30, 'CC', '1303123456', 'Dora',             'Inés',       'Mora',        'Buitrago',  'dora.mora@littlehorizons.edu.co',      '+57 336 134 5679', 'Educación Física',      '2019-07-22', 'Activo');

-- ----------------------------------------------------------------------------
-- Students: 30 active students (ids 1-30). Grade assignment:
--   1: Preescolar | 2-4: Primero | 5-7: Segundo | 8-10: Tercero | 11-13: Cuarto
--   14-16: Quinto | 17-19: Sexto | 20-22: Séptimo | 23-26: Octavo
--   27-28: Noveno | 29: Décimo | 30: Once
-- Minors carry a TI (Tarjeta de Identidad) document starting with 10.
-- ----------------------------------------------------------------------------
INSERT INTO estudiantes (id_estudiante, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) VALUES
(1,  'TI', '1001234567', 'Santiago',   'Mateo',        'Restrepo',  'Álvarez',   '2021-03-12', 'M', 'Calle 128 # 15-40, Bogotá',        '+57 312 445 7788', 'santiago.restrepo@littlehorizons.edu.co',   'O+',  'Ninguna conocida', 'Ninguna',                        '2026-01-20', 'Activo'),
(2,  'TI', '1002345678', 'Valentina',  'Sofía',        'Gómez',     'Nuñez',     '2019-05-20', 'F', 'Carrera 24 # 66-32, Bogotá',       '+57 313 556 8899', 'valentina.nunez@littlehorizons.edu.co',     'O+',  'Polen y ácaros',   'Ninguna',                        '2026-01-15', 'Activo'),
(3,  'TI', '1003456789', 'Mateo',      'Antonio',      'Gómez',     'Nuñez',     '2018-08-14', 'M', 'Carrera 24 # 66-32, Bogotá',       '+57 313 556 8899', 'mateo.nunez@littlehorizons.edu.co',         'A+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(4,  'TI', '1004567890', 'Isabella',   'Cristina',     'Torres',    'Molina',    '2019-02-10', 'F', 'Calle 45 # 23-17, Bogotá',         '+57 310 667 9900', 'isabella.torres@littlehorizons.edu.co',     'B+',  'Ninguna',          'Ninguna',                        '2026-01-18', 'Activo'),
(5,  'TI', '1005678901', 'Sebastián',  'José',         'Rodríguez', 'Peña',      '2018-04-03', 'M', 'Carrera 11 # 89-05, Bogotá',       '+57 311 778 0011', 'sebastian.rodriguez@littlehorizons.edu.co', 'O-',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(6,  'TI', '1006789012', 'Laura',      'Fernanda',     'Rodríguez', 'Peña',      '2019-09-30', 'F', 'Carrera 11 # 89-05, Bogotá',       '+57 311 778 0011', 'laura.rodriguez@littlehorizons.edu.co',     'AB+', 'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(7,  'TI', '1007890123', 'Daniel',     'Esteban',      'Vargas',    'Cárdenas',  '2018-01-25', 'M', 'Calle 100 # 52-21, Bogotá',        '+57 314 889 1122', 'daniel.vargas@littlehorizons.edu.co',       'O+',  'Lactosa',          'Ninguna',                        '2026-01-20', 'Activo'),
(8,  'TI', '1008901234', 'Camila',     'Beatriz',      'Martínez',  'Ortiz',     '2017-06-18', 'F', 'Transversal 5 # 34-56, Bogotá',    '+57 315 990 2233', 'camila.martinez@littlehorizons.edu.co',     'A+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(9,  'TI', '1009012345', 'Andrés',     'Felipe',       'Martínez',  'Ortiz',     '2018-11-08', 'M', 'Transversal 5 # 34-56, Bogotá',    '+57 315 990 2233', 'andres.martinez@littlehorizons.edu.co',     'O+',  'Ninguna',          'Asma leve',                      '2026-01-15', 'Activo'),
(10, 'TI', '1010123456', 'Sofía',      'Valeria',      'Martínez',  'Ortiz',     '2019-12-01', 'F', 'Transversal 5 # 34-56, Bogotá',    '+57 315 990 2233', 'sofia.martinez@littlehorizons.edu.co',      'B+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(11, 'TI', '1011234567', 'Nicolás',    'Esteban',      'Suárez',    'Betancur',  '2016-07-22', 'M', 'Diagonal 78 # 40-12, Bogotá',      '+57 316 001 3344', 'nicolas.suarez@littlehorizons.edu.co',      'O+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(12, 'TI', '1012345678', 'Gabriela',   'Antonia',      'Suárez',    'Betancur',  '2017-03-15', 'F', 'Diagonal 78 # 40-12, Bogotá',      '+57 316 001 3344', 'gabriela.suarez@littlehorizons.edu.co',     'A-',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(13, 'TI', '1013456789', 'Julián',     'David',        'Rojas',     'Niño',      '2016-10-05', 'M', 'Calle 63 # 12-08, Bogotá',         '+57 317 112 4455', 'julian.rojas@littlehorizons.edu.co',        'B+',  'Maní y frutos secos', 'Porta autoinyector de epinefrina', '2026-01-19', 'Activo'),
(14, 'TI', '1014567890', 'Mariana',    'Lucía',        'Ospina',    'Duarte',    '2015-05-09', 'F', 'Carrera 68 # 110-45, Bogotá',      '+57 318 223 5566', 'mariana.ospina@littlehorizons.edu.co',      'O+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(15, 'TI', '1015678901', 'Felipe',     'Ignacio',      'Ospina',    'Duarte',    '2016-12-19', 'M', 'Carrera 68 # 110-45, Bogotá',      '+57 318 223 5566', 'felipe.ospina@littlehorizons.edu.co',       'A+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(16, 'TI', '1016789012', 'Valeria',    'Josefina',     'Castro',    'Pardo',     '2015-08-27', 'F', 'Calle 140 # 9-55, Bogotá',         '+57 319 334 6677', 'valeria.castro@littlehorizons.edu.co',      'AB+', 'Ninguna',          'Ninguna',                        '2026-01-18', 'Activo'),
(17, 'TI', '1017890123', 'Emilio',     'Alejandro',    'Herrera',   'Quintero',  '2014-02-14', 'M', 'Avenida 19 # 123-30, Bogotá',      '+57 320 445 7788', 'emilio.herrera@littlehorizons.edu.co',      'O+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(18, 'TI', '1018901234', 'Daniela',    'María',        'Herrera',   'Quintero',  '2015-06-11', 'F', 'Avenida 19 # 123-30, Bogotá',      '+57 320 445 7788', 'daniela.herrera@littlehorizons.edu.co',     'B+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(19, 'TI', '1019012345', 'Tomás',      'Ignacio',      'Herrera',   'Quintero',  '2016-04-28', 'M', 'Avenida 19 # 123-30, Bogotá',      '+57 320 445 7788', 'tomas.herrera@littlehorizons.edu.co',       'O-',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(20, 'TI', '1020123456', 'Manuela',    'Isabel',       'Giraldo',   'Sáenz',     '2014-09-17', 'F', 'Carrera 46 # 84-19, Bogotá',       '+57 321 556 8899', 'manuela.giraldo@littlehorizons.edu.co',     'A+',  'Ninguna',          'Ninguna',                        '2026-01-20', 'Activo'),
(21, 'TI', '1021234567', 'Juan',       'José',         'Pérez',     'Ledesma',   '2013-01-30', 'M', 'Calle 134 # 31-77, Bogotá',        '+57 322 667 9900', 'juan.perez@littlehorizons.edu.co',          'O+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(22, 'TI', '1022345678', 'Sara',       'Lucía',        'Pérez',     'Ledesma',   '2014-11-23', 'F', 'Calle 134 # 31-77, Bogotá',        '+57 322 667 9900', 'sara.perez@littlehorizons.edu.co',          'A+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(23, 'TI', '1023456789', 'Ricardo',    'Andrés',       'Blanco',    'Murillo',   '2013-05-06', 'M', 'Calle 72 # 55-14, Bogotá',         '+57 323 778 0011', 'ricardo.blanco@littlehorizons.edu.co',      'B+',  'Ninguna',          'Ninguna',                        '2026-01-18', 'Activo'),
(24, 'TI', '1024567890', 'Catalina',   'Viviana',      'Jiménez',   'Robayo',    '2012-03-19', 'F', 'Carrera 8 # 101-23, Bogotá',       '+57 324 889 1122', 'catalina.jimenez@littlehorizons.edu.co',    'O+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(25, 'TI', '1025678901', 'Diego',      'Alejandro',    'Jiménez',   'Robayo',    '2013-08-02', 'M', 'Carrera 8 # 101-23, Bogotá',       '+57 324 889 1122', 'diego.jimenez@littlehorizons.edu.co',       'O+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(26, 'TI', '1026789012', 'Samuel',     'Esteban',      'Jiménez',   'Robayo',    '2012-10-15', 'M', 'Carrera 8 # 101-23, Bogotá',       '+57 324 889 1122', 'samuel.jimenez@littlehorizons.edu.co',      'A+',  'Polen',            'Ninguna',                        '2026-01-15', 'Activo'),
(27, 'TI', '1027890123', 'Paola',      'Andrea',       'Frasser',   'Muñoz',     '2011-07-07', 'F', 'Calle 118 # 27-64, Bogotá',        '+57 325 990 2233', 'paola.frasser@littlehorizons.edu.co',       'O+',  'Ninguna',          'Ninguna',                        '2026-01-19', 'Activo'),
(28, 'TI', '1028901234', 'Sebastián',  'Camilo',       'Cifuentes', 'Prieto',    '2011-02-11', 'M', 'Avenida 68 # 95-102, Bogotá',      '+57 326 001 3344', 'sebastian.cifuentes@littlehorizons.edu.co', 'B+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(29, 'TI', '1029012345', 'Antonia',    'María',        'Cifuentes', 'Prieto',    '2011-09-25', 'F', 'Avenida 68 # 95-102, Bogotá',      '+57 326 001 3344', 'antonia.cifuentes@littlehorizons.edu.co',   'O+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo'),
(30, 'TI', '1030123456', 'Valentina',  'Carolina',     'Cárdenas',  'Lozano',    '2009-06-30', 'F', 'Calle 26 # 13-42, Bogotá',         '+57 327 112 4455', 'valentina.cardenas@littlehorizons.edu.co',  'A+',  'Ninguna',          'Ninguna',                        '2026-01-15', 'Activo');

-- ----------------------------------------------------------------------------
-- Guardians: 30 guardians (ids 1-30). Ids 1-18 are the primary (financial)
-- guardians of the families below; ids 19-30 are second parents or secondary
-- contacts. Families with several children enrolled:
--   Guardian 1: students 2, 3 | Guardian 2: 5, 6 | Guardian 3: 8, 9, 10
--   Guardian 4: 11, 12 | Guardian 5: 14, 15 | Guardian 6: 17, 18, 19
--   Guardian 7: 21, 22 | Guardian 8: 24, 25, 26 | Guardian 9: 28, 29
-- ----------------------------------------------------------------------------
INSERT INTO acudientes (id_acudiente, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, correo_electronico, direccion, ocupacion, es_responsable_financiero) VALUES
(1,  'CC', '1014587923', 'Sofía',         'Atropos',    'Nuñez',      NULL,         'Madre',  '+57 310 214 5678', NULL,              'sofia.nunez.atropos@gmail.com',     'Carrera 24 # 66-32, Bogotá',    'Ingeniera de Sistemas',        TRUE),
(2,  'CC', '1023658741', 'Luz',           'Adriana',    'Rodríguez',  'Salas',      'Madre',  '+57 311 325 6789', '+57 311 325 6790', 'luz.rodriguez.s@yahoo.com',         'Carrera 11 # 89-05, Bogotá',    'Contadora Pública',            TRUE),
(3,  'CC', '1032145698', 'Jorge',         'Iván',       'Martínez',   'Cano',       'Padre',  '+57 312 436 7890', NULL,              'jorge.martinez.c@gmail.com',        'Transversal 5 # 34-56, Bogotá', 'Médico Cirujano',              TRUE),
(4,  'CC', '1045723698', 'Diana',         'Marcela',    'Suárez',     'Vélez',      'Madre',  '+57 313 547 8901', '+57 313 547 8902', 'diana.suarez.v@hotmail.com',        'Diagonal 78 # 40-12, Bogotá',   'Abogada',                      TRUE),
(5,  'CC', '1025478963', 'Hernán',        'Alberto',    'Ospina',     'Rueda',      'Padre',  '+57 314 658 9012', NULL,              'hernan.ospina.r@gmail.com',         'Carrera 68 # 110-45, Bogotá',   'Arquitecto',                   TRUE),
(6,  'CC', '1039852147', 'Gloria',        'Inés',       'Herrera',    'Mora',       'Madre',  '+57 315 769 0123', '+57 315 769 0124', 'gloria.herrera.m@gmail.com',        'Avenida 19 # 123-30, Bogotá',   'Docente Universitaria',        TRUE),
(7,  'CC', '1051236478', 'Álvaro',        'Antonio',    'Pérez',      'Domínguez',  'Padre',  '+57 316 870 1234', NULL,              'alvaro.perez.d@hotmail.com',        'Calle 134 # 31-77, Bogotá',     'Empresario',                   TRUE),
(8,  'CE', '5612345678', 'Fabián',        'Eduardo',    'Robayo',     'Salazar',    'Padre',  '+57 317 981 2345', NULL,              'fabian.robayo.s@gmail.com',         'Carrera 8 # 101-23, Bogotá',    'Piloto Comercial',             TRUE),
(9,  'CC', '1062314578', 'Mauricio',      'Andrés',     'Cifuentes',  'Prieto',     'Padre',  '+57 318 092 3456', '+57 318 092 3457', 'mauricio.cifuentes@gmail.com',      'Avenida 68 # 95-102, Bogotá',   'Economista',                   TRUE),
(10, 'CC', '1028945631', 'Sandra',        'Milena',     'Álvarez',    'Ríos',       'Madre',  '+57 319 103 4567', NULL,              'sandra.alvarez.r@gmail.com',        'Calle 128 # 15-40, Bogotá',     'Diseñadora Gráfica',           TRUE),
(11, 'CC', '1037152894', 'Patricia',      'Eugenia',    'Molina',     'Garzón',     'Madre',  '+57 320 214 5678', '+57 320 214 5679', 'patricia.molina.g@yahoo.com',       'Calle 45 # 23-17, Bogotá',      'Enfermera Jefe',               TRUE),
(12, 'CC', '1042963571', 'Rosalba',       'Clemencia',  'Cárdenas',   'Pinto',      'Madre',  '+57 321 325 6789', NULL,              'rosalba.cardenas.p@hotmail.com',    'Calle 100 # 52-21, Bogotá',     'Psicóloga',                    TRUE),
(13, 'CC', '1056341278', 'Óscar',         'Javier',     'Rojas',      'Niño',       'Padre',  '+57 322 436 7890', '+57 322 436 7891', 'oscar.rojas.n@gmail.com',           'Calle 63 # 12-08, Bogotá',      'Administrador de Empresas',    TRUE),
(14, 'CC', '1013624975', 'Beatriz',       'Elena',      'Castro',     'Pardo',      'Madre',  '+57 323 547 8901', NULL,              'beatriz.castro.p@gmail.com',        'Calle 140 # 9-55, Bogotá',      'Nutricionista',                TRUE),
(15, 'CC', '1068471592', 'Pedro',         'Pablo',      'Giraldo',    'Sáenz',      'Padre',  '+57 324 658 9012', '+57 324 658 9013', 'pedro.giraldo.s@gmail.com',         'Carrera 46 # 84-19, Bogotá',    'Ingeniero de Sistemas',        TRUE),
(16, 'CC', '1025197438', 'Yamile',        'Susana',     'Blanco',     'Murillo',    'Madre',  '+57 325 769 0123', NULL,              'yamile.blanco.m@hotmail.com',       'Calle 72 # 55-14, Bogotá',      'Veterinaria',                  TRUE),
(17, 'CC', '1047362589', 'Nidia',         'Rocío',      'Frasser',    'Muñoz',      'Madre',  '+57 326 870 1234', NULL,              'nidia.frasser.m@gmail.com',         'Calle 118 # 27-64, Bogotá',     'Bacterióloga',                 TRUE),
(18, 'CC', '1031527964', 'Marcela',       'Fernanda',   'Lozano',     'de Cárdenas','Madre',  '+57 327 981 2345', '+57 327 981 2346', 'marcela.lozano.c@gmail.com',        'Calle 26 # 13-42, Bogotá',      'Arquitecta',                   TRUE),
(19, 'CC', '1052714369', 'Ana',           'María',      'Ramírez',    'Nieto',      'Madre',  '+57 310 092 3456', NULL,              'ana.ramirez.n@gmail.com',           'Carrera 24 # 66-32, Bogotá',    'Comerciante',                  FALSE),
(20, 'CC', '1019623574', 'Pedro',         NULL,         'Salas',      'Contreras',  'Padre',  '+57 310 092 3456', NULL,              'pedro.salas.c@gmail.com',           'Carrera 11 # 89-05, Bogotá',    'Conductor Ejecutivo',          FALSE),
(21, 'CC', '1064382917', 'María',         'Fernanda',   'Ortiz',      'Guarnizo',   'Madre',  '+57 311 203 4567', NULL,              'fernanda.ortiz.g@gmail.com',        'Transversal 5 # 34-56, Bogotá', 'Odontóloga',                   FALSE),
(22, 'CC', '1023478156', 'Jorge',         'Eduardo',    'Betancur',   'Sáenz',      'Padre',  '+57 312 314 5678', NULL,              'jorge.betancur.s@yahoo.com',        'Diagonal 78 # 40-12, Bogotá',   'Piloto de Avión',              FALSE),
(23, 'CC', '1058931427', 'Claudia',       'Patricia',   'Duarte',     'Mejía',      'Madre',  '+57 313 425 6789', NULL,              'claudia.duarte.m@gmail.com',        'Carrera 68 # 110-45, Bogotá',   'Publicista',                   FALSE),
(24, 'Pasaporte', 'A12345678', 'Óscar',    'Rafael',     'Quintero',   'Valdés',     'Padre',  '+57 314 536 7890', NULL,              'oscar.quintero.v@hotmail.com',      'Avenida 19 # 123-30, Bogotá',   'Consultor',                    FALSE),
(25, 'CC', '1034729681', 'Sandra',        'Milagros',   'Ledesma',    'Arciniegas', 'Madre',  '+57 315 647 8901', NULL,              'sandra.ledesma.a@gmail.com',        'Calle 134 # 31-77, Bogotá',     'Comerciante',                  FALSE),
(26, 'CC', '1045827193', 'Paula',         'Constanza',  'Jiménez',    'Pardo',      'Madre',  '+57 316 758 9012', NULL,              'paula.jimenez.p@gmail.com',         'Carrera 8 # 101-23, Bogotá',    'Fisioterapeuta',               FALSE),
(27, 'CC', '1017523496', 'Verónica',      'Amparo',     'Prieto',     'Mora',       'Madre',  '+57 317 869 0123', NULL,              'veronica.prieto.m@yahoo.com',       'Avenida 68 # 95-102, Bogotá',   'Ingeniera Industrial',         FALSE),
(28, 'CC', '1062145873', 'Óscar',         'Mauricio',   'Restrepo',   'Zuluaga',    'Padre',  '+57 318 970 1234', NULL,              'oscar.restrepo.z@gmail.com',        'Calle 128 # 15-40, Bogotá',     'Comerciante',                  FALSE),
(29, 'CC', '1026893451', 'Andrés',        'Camilo',     'Torres',     'Álvarez',    'Padre',  '+57 319 081 2345', NULL,              'andres.torres.a@gmail.com',         'Calle 45 # 23-17, Bogotá',      'Militar Activo',               FALSE),
(30, 'CE', '5623456789', 'Diego',         'Fernando',   'Vargas',     'Lleras',     'Padre',  '+57 320 192 3456', NULL,              'diego.vargas.ll@hotmail.com',       'Calle 100 # 52-21, Bogotá',     'Abogado',                      FALSE);

-- ----------------------------------------------------------------------------
-- Student-guardian links: every student has at least one primary guardian;
-- second parents are added as secondary contacts for most students.
-- ----------------------------------------------------------------------------
INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) VALUES
(2,  1,  TRUE,  TRUE),
(3,  1,  TRUE,  TRUE),
(2,  19, FALSE, FALSE),
(3,  19, FALSE, TRUE),
(5,  2,  TRUE,  TRUE),
(6,  2,  TRUE,  TRUE),
(5,  20, FALSE, FALSE),
(6,  20, FALSE, TRUE),
(8,  3,  TRUE,  TRUE),
(9,  3,  TRUE,  TRUE),
(10, 3,  TRUE,  TRUE),
(8,  21, FALSE, FALSE),
(9,  21, FALSE, TRUE),
(10, 21, FALSE, FALSE),
(11, 4,  TRUE,  TRUE),
(12, 4,  TRUE,  TRUE),
(11, 22, FALSE, FALSE),
(12, 22, FALSE, FALSE),
(14, 5,  TRUE,  TRUE),
(15, 5,  TRUE,  TRUE),
(14, 23, FALSE, FALSE),
(15, 23, FALSE, TRUE),
(17, 6,  TRUE,  TRUE),
(18, 6,  TRUE,  TRUE),
(19, 6,  TRUE,  TRUE),
(17, 24, FALSE, FALSE),
(18, 24, FALSE, FALSE),
(19, 24, FALSE, FALSE),
(21, 7,  TRUE,  TRUE),
(22, 7,  TRUE,  TRUE),
(21, 25, FALSE, FALSE),
(22, 25, FALSE, FALSE),
(24, 8,  TRUE,  TRUE),
(25, 8,  TRUE,  TRUE),
(26, 8,  TRUE,  TRUE),
(24, 26, FALSE, FALSE),
(25, 26, FALSE, TRUE),
(26, 26, FALSE, FALSE),
(28, 9,  TRUE,  TRUE),
(29, 9,  TRUE,  TRUE),
(28, 27, FALSE, FALSE),
(29, 27, FALSE, FALSE),
(1,  10, TRUE,  TRUE),
(1,  28, FALSE, FALSE),
(4,  11, TRUE,  TRUE),
(4,  29, FALSE, TRUE),
(7,  12, TRUE,  TRUE),
(7,  30, FALSE, FALSE),
(13, 13, TRUE,  TRUE),
(16, 14, TRUE,  TRUE),
(20, 15, TRUE,  TRUE),
(23, 16, TRUE,  TRUE),
(27, 17, TRUE,  TRUE),
(30, 18, TRUE,  TRUE);

-- ----------------------------------------------------------------------------
-- Enrollments: every active student is enrolled in the 2026 academic year,
-- one row per student per grade.
-- ----------------------------------------------------------------------------
INSERT INTO matriculas (id_estudiante, id_grado, año_academico, estado_matricula, fecha_matricula, observaciones) VALUES
(1,  1,  2026, 'Activa', '2026-01-20', 'Estudiante nuevo, ingreso a Preescolar'),
(2,  2,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(3,  2,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(4,  2,  2026, 'Activa', '2026-01-18', 'Estudiante nueva, traslado desde otro colegio'),
(5,  3,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(6,  3,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(7,  3,  2026, 'Activa', '2026-01-20', 'Renovación anual de matrícula'),
(8,  4,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(9,  4,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(10, 4,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(11, 5,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(12, 5,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(13, 5,  2026, 'Activa', '2026-01-19', 'Estudiante nuevo, ingreso 2026'),
(14, 6,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(15, 6,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(16, 6,  2026, 'Activa', '2026-01-18', 'Renovación anual de matrícula'),
(17, 7,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(18, 7,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(19, 7,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(20, 8,  2026, 'Activa', '2026-01-20', 'Renovación anual de matrícula'),
(21, 8,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(22, 8,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(23, 9,  2026, 'Activa', '2026-01-18', 'Renovación anual de matrícula'),
(24, 9,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(25, 9,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(26, 9,  2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(27, 10, 2026, 'Activa', '2026-01-19', 'Estudiante nueva, ingreso 2026'),
(28, 10, 2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(29, 11, 2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula'),
(30, 12, 2026, 'Activa', '2026-01-15', 'Renovación anual de matrícula, grado Once');

-- ----------------------------------------------------------------------------
-- Subjects: 33 subjects spread over the grades. Code scheme: <AREA>-<grade
-- number> (MAT-1 = Matemáticas for Primero, etc.). Preescolar has no subjects.
-- ----------------------------------------------------------------------------
INSERT INTO materias (id_materia, codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) VALUES
(1,  'MAT-1',  'Matemáticas',           'Pensamiento numérico y operaciones básicas',          2,  5, TRUE),
(2,  'ESP-1',  'Español y Literatura',  'Lectoescritura inicial y comprensión lectora',        2,  5, TRUE),
(3,  'ING-1',  'Inglés',                'Vocabulario y expresiones básicas en inglés',         2,  3, TRUE),
(4,  'EDU-1',  'Educación Física',      'Juegos motores y coordinación básica',                2,  2, TRUE),
(5,  'MAT-2',  'Matemáticas',           'Operaciones con números y fracciones básicas',        3,  5, TRUE),
(6,  'ESP-2',  'Español y Literatura',  'Ortografía, redacción y comprensión lectora',         3,  5, TRUE),
(7,  'CIE-2',  'Ciencias Naturales',    'Ser vivo, materia y energía para grados bajos',       3,  3, TRUE),
(8,  'EDU-2',  'Educación Física',      'Motricidad y juegos cooperativos',                    3,  2, TRUE),
(9,  'MAT-3',  'Matemáticas',           'Multiplicación, división y fracciones',               4,  5, TRUE),
(10, 'ESP-3',  'Español y Literatura',  'Comprensión lectora y producción de textos',          4,  5, TRUE),
(11, 'SOC-3',  'Ciencias Sociales',     'Geografía básica de Colombia y ciudadanía',           4,  3, TRUE),
(12, 'MAT-4',  'Matemáticas',           'Fracciones, decimales y geometría básica',            5,  5, TRUE),
(13, 'CIE-4',  'Ciencias Naturales',    'Reino animal, plantas y ecosistemas',                 5,  3, TRUE),
(14, 'ING-4',  'Inglés',                'Comprensión y conversación básica en inglés',         5,  3, TRUE),
(15, 'MAT-5',  'Matemáticas',           'Operaciones combinadas y proporcionalidad',           6,  5, TRUE),
(16, 'ESP-5',  'Español y Literatura',  'Uso de la lengua y literatura infantil',              6,  5, TRUE),
(17, 'SOC-5',  'Ciencias Sociales',     'Historia y geografía de Colombia',                    6,  3, TRUE),
(18, 'MAT-6',  'Matemáticas',           'Números enteros, fracciones y razones',               7,  5, TRUE),
(19, 'CIE-6',  'Ciencias Naturales',    'Cuerpo humano, materia y energía',                    7,  3, TRUE),
(20, 'ING-6',  'Inglés',                'Gramática y vocabulario de nivel elemental',          7,  3, TRUE),
(21, 'MAT-7',  'Matemáticas',           'Álgebra básica y proporcionalidad',                   8,  5, TRUE),
(22, 'ESP-7',  'Español y Literatura',  'Literatura colombiana y producción escrita',          8,  5, TRUE),
(23, 'SOC-7',  'Ciencias Sociales',     'Historia de Colombia y educación ciudadana',          8,  3, TRUE),
(24, 'MAT-8',  'Matemáticas',           'Álgebra, ecuaciones y geometría',                     9,  5, TRUE),
(25, 'CIE-8',  'Ciencias Naturales',    'Química, células y sistemas del cuerpo',              9,  4, TRUE),
(26, 'EDU-8',  'Educación Física',      'Deporte, condición física y salud',                   9,  2, TRUE),
(27, 'MAT-9',  'Matemáticas',           'Álgebra avanzada y estadística',                      10, 5, TRUE),
(28, 'ESP-9',  'Español y Literatura',  'Literatura hispanoamericana y argumentación',         10, 5, TRUE),
(29, 'SOC-9',  'Ciencias Sociales',     'Constitución, democracia y economía',                 10, 3, TRUE),
(30, 'MAT-10', 'Matemáticas',           'Funciones, trigonometría y precálculo',               11, 5, TRUE),
(31, 'CIE-10', 'Ciencias Naturales',    'Física, química y biología de bachillerato',          11, 4, TRUE),
(32, 'ING-11', 'Inglés',                'Inglés conversacional y preparación para pruebas Saber', 12, 3, TRUE),
(33, 'EDU-11', 'Educación Física',      'Proyectos deportivos y proyecto de vida',             12, 2, TRUE);

-- ----------------------------------------------------------------------------
-- Teacher-subject assignments for 2026. Section A is the main section of each
-- subject; teachers 17-30 lead the extra section B of selected subjects, so
-- every one of the 30 teachers has at least one assignment.
-- ----------------------------------------------------------------------------
INSERT INTO asignaciones_profesor (id_profesor, id_materia, año_academico, seccion) VALUES
(1,  1,  2026, 'A'),
(1,  5,  2026, 'A'),
(2,  9,  2026, 'A'),
(2,  12, 2026, 'A'),
(3,  15, 2026, 'A'),
(3,  18, 2026, 'A'),
(4,  21, 2026, 'A'),
(4,  24, 2026, 'A'),
(5,  27, 2026, 'A'),
(5,  30, 2026, 'A'),
(6,  2,  2026, 'A'),
(6,  6,  2026, 'A'),
(7,  10, 2026, 'A'),
(7,  16, 2026, 'A'),
(8,  22, 2026, 'A'),
(8,  28, 2026, 'A'),
(9,  3,  2026, 'A'),
(9,  14, 2026, 'A'),
(9,  20, 2026, 'A'),
(10, 32, 2026, 'A'),
(11, 4,  2026, 'A'),
(11, 8,  2026, 'A'),
(11, 26, 2026, 'A'),
(12, 33, 2026, 'A'),
(13, 7,  2026, 'A'),
(13, 13, 2026, 'A'),
(13, 19, 2026, 'A'),
(14, 25, 2026, 'A'),
(14, 31, 2026, 'A'),
(15, 11, 2026, 'A'),
(15, 17, 2026, 'A'),
(16, 23, 2026, 'A'),
(16, 29, 2026, 'A'),
(17, 1,  2026, 'B'),
(18, 15, 2026, 'B'),
(19, 18, 2026, 'B'),
(20, 27, 2026, 'B'),
(21, 9,  2026, 'B'),
(22, 2,  2026, 'B'),
(23, 7,  2026, 'B'),
(24, 13, 2026, 'B'),
(25, 30, 2026, 'B'),
(26, 22, 2026, 'B'),
(27, 31, 2026, 'B'),
(28, 33, 2026, 'B'),
(29, 11, 2026, 'B'),
(30, 26, 2026, 'B');

-- ----------------------------------------------------------------------------
-- Subject enrollments: each student is enrolled (period I, 2026) in the
-- Mathematics and Language/Science subjects of their grade. Preescolar has no
-- subject enrollments by school policy.
-- ----------------------------------------------------------------------------
INSERT INTO inscripciones_materia (id_estudiante, id_materia, año_academico, periodo_academico, seccion, fecha_inscripcion, estado) VALUES
(2,  1,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(2,  2,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(3,  1,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(3,  2,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(4,  1,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(4,  2,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(5,  5,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(5,  6,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(6,  5,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(6,  6,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(7,  5,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(7,  6,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(8,  9,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(8,  10, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(9,  9,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(9,  10, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(10, 9,  2026, 'I', 'A', '2026-01-25', 'Activa'),
(10, 10, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(11, 12, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(11, 13, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(12, 12, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(12, 13, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(13, 12, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(13, 13, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(14, 15, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(14, 16, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(15, 15, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(15, 16, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(16, 15, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(16, 16, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(17, 18, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(17, 19, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(18, 18, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(18, 19, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(19, 18, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(19, 19, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(20, 21, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(20, 22, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(21, 21, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(21, 22, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(22, 21, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(22, 22, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(23, 24, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(23, 25, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(24, 24, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(24, 25, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(25, 24, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(25, 25, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(26, 24, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(26, 25, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(27, 27, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(27, 28, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(28, 27, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(28, 28, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(29, 30, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(29, 31, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(30, 32, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(30, 33, 2026, 'I', 'A', '2026-01-25', 'Activa');

-- ----------------------------------------------------------------------------
-- Academic records: one grade per student for period I (2026) in Mathematics,
-- plus a set of period II records in their second subject. The grading teacher
-- always matches the official 2026 subject assignment (section A).
-- ----------------------------------------------------------------------------
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, observaciones, fecha_registro) VALUES
(2,  1,  1, 2026, 'I', 4.8, NULL, '2026-03-20'),
(3,  1,  1, 2026, 'I', 3.9, NULL, '2026-03-20'),
(4,  1,  1, 2026, 'I', 4.5, NULL, '2026-03-20'),
(5,  5,  1, 2026, 'I', 4.2, NULL, '2026-03-20'),
(6,  5,  1, 2026, 'I', 4.9, NULL, '2026-03-20'),
(7,  5,  1, 2026, 'I', 2.9, 'Requiere plan de refuerzo', '2026-03-20'),
(8,  9,  2, 2026, 'I', 4.4, NULL, '2026-03-20'),
(9,  9,  2, 2026, 'I', 3.6, NULL, '2026-03-20'),
(10, 9,  2, 2026, 'I', 5.0, 'Desempeño superior', '2026-03-20'),
(11, 12, 2, 2026, 'I', 4.1, NULL, '2026-03-20'),
(12, 12, 2, 2026, 'I', 4.7, NULL, '2026-03-20'),
(13, 12, 2, 2026, 'I', 3.3, 'En proceso de recuperación', '2026-03-20'),
(14, 15, 3, 2026, 'I', 4.6, NULL, '2026-03-20'),
(15, 15, 3, 2026, 'I', 3.8, NULL, '2026-03-20'),
(16, 15, 3, 2026, 'I', 4.9, 'Desempeño superior', '2026-03-20'),
(17, 18, 3, 2026, 'I', 4.0, NULL, '2026-03-20'),
(18, 18, 3, 2026, 'I', 4.8, NULL, '2026-03-20'),
(19, 18, 3, 2026, 'I', 3.5, NULL, '2026-03-20'),
(20, 21, 4, 2026, 'I', 4.3, NULL, '2026-03-20'),
(21, 21, 4, 2026, 'I', 3.9, NULL, '2026-03-20'),
(22, 21, 4, 2026, 'I', 4.6, NULL, '2026-03-20'),
(23, 24, 4, 2026, 'I', 3.2, 'Requiere plan de refuerzo', '2026-03-20'),
(24, 24, 4, 2026, 'I', 4.5, NULL, '2026-03-20'),
(25, 24, 4, 2026, 'I', 4.0, NULL, '2026-03-20'),
(26, 24, 4, 2026, 'I', 4.7, NULL, '2026-03-20'),
(27, 27, 5, 2026, 'I', 4.5, NULL, '2026-03-20'),
(28, 27, 5, 2026, 'I', 3.8, NULL, '2026-03-20'),
(29, 30, 5, 2026, 'I', 4.6, NULL, '2026-03-20'),
(30, 32, 10, 2026, 'I', 4.9, 'Desempeño superior', '2026-03-20'),
(2,  2,  6, 2026, 'II', 4.6, NULL, '2026-06-18'),
(3,  2,  6, 2026, 'II', 4.1, NULL, '2026-06-18'),
(5,  6,  6, 2026, 'II', 4.4, NULL, '2026-06-18'),
(6,  6,  6, 2026, 'II', 5.0, 'Desempeño superior', '2026-06-18'),
(8,  10, 7, 2026, 'II', 4.3, NULL, '2026-06-18'),
(11, 13, 13, 2026, 'II', 4.8, NULL, '2026-06-18'),
(14, 16, 7, 2026, 'II', 4.2, NULL, '2026-06-18'),
(17, 19, 13, 2026, 'II', 3.9, NULL, '2026-06-18'),
(20, 22, 8, 2026, 'II', 4.4, NULL, '2026-06-18'),
(23, 25, 14, 2026, 'II', 3.6, NULL, '2026-06-18'),
(27, 28, 8, 2026, 'II', 4.7, NULL, '2026-06-18'),
(29, 31, 14, 2026, 'II', 4.8, NULL, '2026-06-18');

-- ----------------------------------------------------------------------------
-- Attendance: first week of March 2026 per grade (one main subject per
-- student), including absence patterns: unexcused absences, tardiness and
-- medical excuses, so attendance reports show varied data.
-- ----------------------------------------------------------------------------
INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, observaciones, registrado_por) VALUES
(2,  1,  '2026-03-02', 'Presente',      NULL, 1),
(3,  1,  '2026-03-02', 'Ausente',       'Sin excusa registrada', 1),
(4,  1,  '2026-03-02', 'Presente',      NULL, 1),
(2,  1,  '2026-03-03', 'Presente',      NULL, 1),
(3,  1,  '2026-03-03', 'Tardanza',      NULL, 1),
(4,  1,  '2026-03-03', 'Presente',      NULL, 1),
(2,  2,  '2026-03-02', 'Presente',      NULL, 6),
(3,  2,  '2026-03-02', 'Excusa Médica', 'Cita médica presentada', 6),
(4,  2,  '2026-03-02', 'Presente',      NULL, 6),
(5,  5,  '2026-03-02', 'Presente',      NULL, 1),
(6,  5,  '2026-03-02', 'Presente',      NULL, 1),
(7,  5,  '2026-03-02', 'Ausente',       'Sin excusa registrada', 1),
(5,  5,  '2026-03-04', 'Presente',      NULL, 1),
(6,  5,  '2026-03-04', 'Tardanza',      NULL, 1),
(7,  5,  '2026-03-04', 'Tardanza',      'Llegó 20 minutos tarde', 1),
(8,  9,  '2026-03-02', 'Presente',      NULL, 2),
(9,  9,  '2026-03-02', 'Presente',      NULL, 2),
(10, 9,  '2026-03-02', 'Presente',      NULL, 2),
(8,  9,  '2026-03-03', 'Presente',      NULL, 2),
(9,  9,  '2026-03-03', 'Ausente',       'Sin excusa registrada', 2),
(10, 9,  '2026-03-03', 'Presente',      NULL, 2),
(11, 12, '2026-03-02', 'Presente',      NULL, 2),
(12, 12, '2026-03-02', 'Presente',      NULL, 2),
(13, 12, '2026-03-02', 'Presente',      NULL, 2),
(11, 12, '2026-03-03', 'Presente',      NULL, 2),
(12, 12, '2026-03-03', 'Presente',      NULL, 2),
(13, 12, '2026-03-03', 'Excusa Médica', 'Días de incapacidad médica', 2),
(14, 15, '2026-03-02', 'Presente',      NULL, 3),
(15, 15, '2026-03-02', 'Presente',      NULL, 3),
(16, 15, '2026-03-02', 'Presente',      NULL, 3),
(14, 15, '2026-03-04', 'Presente',      NULL, 3),
(15, 15, '2026-03-04', 'Tardanza',      NULL, 3),
(16, 15, '2026-03-04', 'Presente',      NULL, 3),
(17, 18, '2026-03-02', 'Presente',      NULL, 3),
(18, 18, '2026-03-02', 'Presente',      NULL, 3),
(19, 18, '2026-03-02', 'Ausente',       'Sin excusa registrada', 3),
(17, 18, '2026-03-03', 'Presente',      NULL, 3),
(18, 18, '2026-03-03', 'Presente',      NULL, 3),
(19, 18, '2026-03-03', 'Presente',      NULL, 3),
(20, 21, '2026-03-02', 'Presente',      NULL, 4),
(21, 21, '2026-03-02', 'Presente',      NULL, 4),
(22, 21, '2026-03-02', 'Presente',      NULL, 4),
(20, 21, '2026-03-05', 'Ausente',       'Sin excusa registrada', 4),
(21, 21, '2026-03-05', 'Presente',      NULL, 4),
(22, 21, '2026-03-05', 'Presente',      NULL, 4),
(23, 24, '2026-03-02', 'Presente',      NULL, 4),
(24, 24, '2026-03-02', 'Presente',      NULL, 4),
(25, 24, '2026-03-02', 'Presente',      NULL, 4),
(26, 24, '2026-03-02', 'Presente',      NULL, 4),
(23, 24, '2026-03-05', 'Ausente',       'Sin excusa registrada', 4),
(24, 24, '2026-03-05', 'Tardanza',      NULL, 4),
(25, 24, '2026-03-05', 'Presente',      NULL, 4),
(26, 24, '2026-03-05', 'Presente',      NULL, 4),
(27, 27, '2026-03-02', 'Presente',      NULL, 5),
(28, 27, '2026-03-02', 'Presente',      NULL, 5),
(27, 27, '2026-03-03', 'Tardanza',      NULL, 5),
(28, 27, '2026-03-03', 'Presente',      NULL, 5),
(29, 30, '2026-03-02', 'Presente',      NULL, 5),
(30, 30, '2026-03-02', 'Presente',      NULL, 5),
(29, 30, '2026-03-04', 'Presente',      NULL, 5),
(30, 30, '2026-03-04', 'Tardanza',      NULL, 5);

-- ----------------------------------------------------------------------------
-- Clubs: 30 extracurricular clubs, one supervised by each of the 30 teachers,
-- with varied capacities and weekly schedules.
-- ----------------------------------------------------------------------------
INSERT INTO clubes (id_club, nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, horario_reunion, esta_activo) VALUES
(1,  'Club de Ajedrez',                 'Ajedrez recreativo y competencias internas',              17, 20, 'Martes 3:00-5:00 PM',                          TRUE),
(2,  'Club de Robótica',                'Construcción y programación de robots con kits educativos', 18, 25, 'Miércoles 3:00-5:00 PM',                    TRUE),
(3,  'Club de Oratoria y Debate',       'Técnicas de argumentación y torneos de debate',           19, 18, 'Jueves 3:00-4:30 PM',                          TRUE),
(4,  'Club de Ciencias Naturales',      'Experimentos guiados y feria de ciencia',                 20, 22, 'Martes 3:00-5:00 PM',                          TRUE),
(5,  'Club de Matemáticas Recreativas', 'Juegos lógicos, acertijos y olimpiadas matemáticas',      21, 20, 'Viernes 2:00-4:00 PM',                         TRUE),
(6,  'Club de Teatro',                  'Montaje de obras de teatro escolar',                      22, 25, 'Lunes y Miércoles 3:00-5:00 PM',               TRUE),
(7,  'Club de Poesía y Escritura Creativa', 'Talleres de escritura y recitales de poesía',         23, 15, 'Viernes 2:00-3:30 PM',                         TRUE),
(8,  'Club de Inglés Conversacional',   'Práctica de conversación en inglés',                      24, 20, 'Martes y Jueves 3:00-4:30 PM',                 TRUE),
(9,  'Club de Lectura en Inglés',       'Círculo de lectura de cuentos en inglés',                 25, 16, 'Viernes 2:00-3:30 PM',                         TRUE),
(10, 'Club de Voleibol',                'Entrenamiento y torneos de voleibol',                     26, 24, 'Lunes y Jueves 3:00-5:00 PM',                  TRUE),
(11, 'Equipo de Baloncesto',            'Entrenamiento del equipo representativo',                 27, 20, 'Martes y Jueves 3:00-5:00 PM',                 TRUE),
(12, 'Selección de Fútbol Masculino',   'Selección masculina para torneos intercolegiados',        28, 22, 'Lunes, Miércoles y Viernes 3:00-5:00 PM',      TRUE),
(13, 'Selección de Fútbol Femenino',    'Selección femenina para torneos intercolegiados',         29, 20, 'Martes y Jueves 3:00-5:00 PM',                 TRUE),
(14, 'Club de Atletismo',               'Entrenamiento de pista y campo',                          30, 25, 'Miércoles y Viernes 3:00-4:30 PM',             TRUE),
(15, 'Coro Escolar',                    'Coro representativo del colegio',                         1,  30, 'Lunes y Miércoles 2:00-3:30 PM',               TRUE),
(16, 'Banda Marcial',                   'Banda de guerra y percusión del colegio',                 2,  35, 'Martes y Jueves 2:00-4:00 PM',                 TRUE),
(17, 'Club de Música Andina',           'Tiple, requinto y música colombiana',                     3,  18, 'Viernes 2:00-4:00 PM',                         TRUE),
(18, 'Grupo de Danza Folclórica',       'Danzas tradicionales colombianas',                        4,  25, 'Lunes y Jueves 3:00-5:00 PM',                  TRUE),
(19, 'Club de Artes Plásticas',         'Dibujo, pintura y manualidades',                          5,  20, 'Miércoles 3:00-5:00 PM',                       TRUE),
(20, 'Club de Fotografía',              'Fotografía básica y registro de eventos escolares',       6,  15, 'Viernes 2:00-4:00 PM',                         TRUE),
(21, 'Club de Ecología y Reciclaje',    'Proyectos ambientales y huerta escolar',                  7,  22, 'Martes 3:00-4:30 PM',                          TRUE),
(22, 'Club de Astronomía',              'Observación astronómica y ciencia espacial',              8,  18, 'Jueves 3:00-5:00 PM',                          TRUE),
(23, 'Club de Emprendimiento',          'Proyectos de emprendimiento estudiantil',                 9,  20, 'Viernes 2:00-4:00 PM',                         TRUE),
(24, 'Club de Periodismo Escolar',      'Periódico y boletín escolar',                             10, 16, 'Miércoles 3:00-4:30 PM',                       TRUE),
(25, 'Club de Cocina Saludable',        'Recetas saludables y educación nutricional',              11, 18, 'Viernes 2:00-4:00 PM',                         TRUE),
(26, 'Club de Cinematografía',          'Cine club y cortometrajes escolares',                     12, 15, 'Jueves 3:00-5:00 PM',                          TRUE),
(27, 'Club de Tenis de Mesa',           'Entrenamiento y torneos de ping pong',                    13, 14, 'Martes 3:00-4:30 PM',                          TRUE),
(28, 'Club de Programación Creativa',   'Programación de videojuegos y apps escolares',            14, 18, 'Miércoles 3:00-5:00 PM',                       TRUE),
(29, 'Club de Voluntariado Social',     'Jornadas de servicio a la comunidad',                     15, 25, 'Viernes 2:00-4:00 PM',                         TRUE),
(30, 'Club de Scouts',                  'Grupo scout y actividades al aire libre',                 16, 30, 'Sábados 9:00 AM-12:00 M',                      TRUE);

-- ----------------------------------------------------------------------------
-- Club memberships: 43 memberships; most active, two retired with reasons.
-- ----------------------------------------------------------------------------
INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia, fecha_retiro, motivo_retiro) VALUES
(1,  30, '2026-02-06', 'Activa',   NULL,        NULL),
(2,  15, '2026-02-02', 'Activa',   NULL,        NULL),
(3,  18, '2026-02-02', 'Activa',   NULL,        NULL),
(4,  19, '2026-02-03', 'Activa',   NULL,        NULL),
(4,  30, '2026-02-06', 'Activa',   NULL,        NULL),
(5,  12, '2026-02-02', 'Activa',   NULL,        NULL),
(6,  13, '2026-02-03', 'Activa',   NULL,        NULL),
(7,  1,  '2026-02-03', 'Activa',   NULL,        NULL),
(7,  22, '2026-02-05', 'Retirada', '2026-04-30', 'Cambio de horario incompatible'),
(8,  4,  '2026-02-03', 'Activa',   NULL,        NULL),
(9,  2,  '2026-02-04', 'Activa',   NULL,        NULL),
(9,  10, '2026-02-09', 'Activa',   NULL,        NULL),
(10, 25, '2026-02-06', 'Activa',   NULL,        NULL),
(11, 1,  '2026-02-03', 'Activa',   NULL,        NULL),
(11, 22, '2026-02-05', 'Activa',   NULL,        NULL),
(12, 15, '2026-02-02', 'Activa',   NULL,        NULL),
(13, 10, '2026-02-09', 'Activa',   NULL,        NULL),
(13, 24, '2026-02-11', 'Activa',   NULL,        NULL),
(14, 5,  '2026-02-06', 'Activa',   NULL,        NULL),
(15, 2,  '2026-02-04', 'Activa',   NULL,        NULL),
(15, 12, '2026-02-09', 'Activa',   NULL,        NULL),
(16, 20, '2026-02-06', 'Retirada', '2026-05-29', 'Retiro voluntario del club'),
(16, 21, '2026-02-10', 'Activa',   NULL,        NULL),
(17, 1,  '2026-02-03', 'Activa',   NULL,        NULL),
(17, 22, '2026-02-05', 'Activa',   NULL,        NULL),
(18, 6,  '2026-02-02', 'Activa',   NULL,        NULL),
(19, 18, '2026-02-02', 'Activa',   NULL,        NULL),
(20, 5,  '2026-02-06', 'Activa',   NULL,        NULL),
(20, 3,  '2026-02-11', 'Activa',   NULL,        NULL),
(21, 12, '2026-02-09', 'Activa',   NULL,        NULL),
(22, 13, '2026-02-03', 'Activa',   NULL,        NULL),
(23, 2,  '2026-02-04', 'Activa',   NULL,        NULL),
(23, 27, '2026-02-10', 'Activa',   NULL,        NULL),
(24, 3,  '2026-02-11', 'Activa',   NULL,        NULL),
(24, 21, '2026-02-10', 'Activa',   NULL,        NULL),
(25, 12, '2026-02-09', 'Activa',   NULL,        NULL),
(26, 2,  '2026-02-04', 'Activa',   NULL,        NULL),
(27, 3,  '2026-02-11', 'Activa',   NULL,        NULL),
(28, 8,  '2026-02-05', 'Activa',   NULL,        NULL),
(28, 5,  '2026-02-06', 'Activa',   NULL,        NULL),
(29, 15, '2026-02-02', 'Activa',   NULL,        NULL),
(30, 7,  '2026-02-13', 'Activa',   NULL,        NULL),
(30, 26, '2026-02-12', 'Activa',   NULL,        NULL);

-- ----------------------------------------------------------------------------
-- Events: 10 school events of 2026; seven include competitions with standings.
-- ----------------------------------------------------------------------------
INSERT INTO eventos (id_evento, nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, tiene_competencia, id_organizador, esta_activo) VALUES
(1,  'Olimpiadas Matemáticas Intercolegiadas', 'Competencia de resolución de problemas entre colegios', 'Académico',  '2026-05-14', 'Auditorio Principal',  TRUE,  1,  TRUE),
(2,  'Feria de la Ciencia',                    'Exposición de proyectos científicos por grados',        'Científico', '2026-08-20', 'Cancha Central',       TRUE,  13, TRUE),
(3,  'Torneo de Fútbol Intercolegiado',        'Torneo de fútbol entre instituciones aliadas',          'Deportivo',  '2026-04-24', 'Cancha de Fútbol',     TRUE,  12, TRUE),
(4,  'Festival Cultural Little Horizons',      'Presentaciones artísticas de todos los grados',         'Cultural',   '2026-09-11', 'Teatro Escolar',       FALSE, 6,  TRUE),
(5,  'Día de la Familia',                      'Jornada de integración con familias del colegio',       'Social',     '2026-06-06', 'Patio Central',        FALSE, 11, TRUE),
(6,  'Olimpiadas de Conocimiento',             'Competencia intercolegiada de todas las áreas',         'Académico',  '2026-10-09', 'Auditorio Principal',  TRUE,  15, TRUE),
(7,  'Encuentro de Coros y Bandas',            'Encuentro musical con coros de la ciudad',              'Cultural',   '2026-05-29', 'Teatro Escolar',       TRUE,  16, TRUE),
(8,  'Jornada Deportiva Intercolegios',        'Voleibol y baloncesto entre colegios de la zona',       'Deportivo',  '2026-03-13', 'Coliseo Municipal',    TRUE,  28, TRUE),
(9,  'ExpoRobótica',                           'Exposición y competencia de proyectos de robótica',     'Científico', '2026-09-25', 'Salón Múltiple',       TRUE,  18, TRUE),
(10, 'Recital de Poesía',                      'Recital de poesía propia y de autores colombianos',     'Cultural',   '2026-04-17', 'Biblioteca Escolar',   FALSE, 7,  TRUE);

-- ----------------------------------------------------------------------------
-- Event participation: competition events include 1er/2do/3er Lugar standings.
-- ----------------------------------------------------------------------------
INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, posicion_obtenida, observaciones) VALUES
(1,  20, 'Competidor',  '1er Lugar',  'Puntaje perfecto en la prueba final'),
(1,  11, 'Competidor',  '2do Lugar',  NULL),
(1,  27, 'Competidor',  '3er Lugar',  NULL),
(1,  5,  'Participante', NULL,        NULL),
(2,  8,  'Competidor',  '1er Lugar',  'Proyecto de purificación de agua'),
(2,  23, 'Competidor',  '2do Lugar',  NULL),
(2,  14, 'Competidor',  '3er Lugar',  NULL),
(2,  9,  'Participante', NULL,        NULL),
(3,  21, 'Competidor',  '1er Lugar',  NULL),
(3,  24, 'Competidor',  '2do Lugar',  NULL),
(3,  17, 'Competidor',  '3er Lugar',  NULL),
(3,  28, 'Participante', NULL,        NULL),
(4,  18, 'Participante', NULL,         'Obra de teatro: personaje principal'),
(4,  6,  'Participante', NULL,        NULL),
(4,  30, 'Participante', NULL,        NULL),
(5,  1,  'Asistente',   NULL,         NULL),
(5,  4,  'Asistente',   NULL,         NULL),
(5,  10, 'Asistente',   NULL,         NULL),
(6,  27, 'Competidor',  '1er Lugar',  NULL),
(6,  20, 'Competidor',  '2do Lugar',  NULL),
(6,  11, 'Competidor',  '3er Lugar',  NULL),
(7,  18, 'Competidor',  '1er Lugar',  'Solista del coro escolar'),
(7,  2,  'Participante', NULL,        NULL),
(8,  23, 'Competidor',  '1er Lugar',  NULL),
(8,  25, 'Competidor',  '2do Lugar',  NULL),
(8,  21, 'Competidor',  '3er Lugar',  NULL),
(9,  26, 'Competidor',  '1er Lugar',  'Robot clasificador de residuos'),
(9,  15, 'Competidor',  '2do Lugar',  NULL),
(9,  23, 'Participante', NULL,        NULL),
(10, 30, 'Participante', NULL,        'Lectura de poema propio'),
(10, 7,  'Participante', NULL,        NULL),
(10, 13, 'Participante', NULL,        NULL);

-- ----------------------------------------------------------------------------
-- Payments: 40 payments in COP matching the official concept amounts
-- (Matrícula 500000, Pensión 350000, Uniforme 250000, Transporte 150000,
-- Alimentación 120000, Actividad 50000, Certificado 15000). The paying
-- guardian is always the financial guardian of the family.
-- ----------------------------------------------------------------------------
INSERT INTO pagos (id_pago, id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, metodo_pago, numero_recibo, año_academico, periodo_academico, notas) VALUES
(1,  2,  1,  1, 500000.00, '2026-01-10', 'Transferencia',   'REC-2026-0001', 2026, 'Anual', NULL),
(2,  2,  1,  2, 350000.00, '2026-02-05', 'Transferencia',   'REC-2026-0002', 2026, 'I',     NULL),
(3,  3,  1,  1, 500000.00, '2026-01-10', 'Efectivo',        'REC-2026-0003', 2026, 'Anual', NULL),
(4,  3,  19, 2, 350000.00, '2026-02-06', 'Tarjeta Débito',  'REC-2026-0004', 2026, 'I',     NULL),
(5,  5,  2,  1, 500000.00, '2026-01-12', 'Transferencia',   'REC-2026-0005', 2026, 'Anual', NULL),
(6,  5,  2,  2, 350000.00, '2026-02-05', 'Transferencia',   'REC-2026-0006', 2026, 'I',     NULL),
(7,  6,  2,  1, 500000.00, '2026-01-12', 'Transferencia',   'REC-2026-0007', 2026, 'Anual', NULL),
(8,  1,  10, 1, 500000.00, '2026-01-14', 'Efectivo',        'REC-2026-0008', 2026, 'Anual', NULL),
(9,  1,  10, 5, 120000.00, '2026-02-03', 'Transferencia',   'REC-2026-0009', 2026, 'I',     NULL),
(10, 4,  11, 1, 500000.00, '2026-01-13', 'Tarjeta Crédito', 'REC-2026-0010', 2026, 'Anual', NULL),
(11, 4,  11, 3, 250000.00, '2026-01-13', 'Tarjeta Crédito', 'REC-2026-0011', 2026, 'Anual', 'Juego completo de uniformes'),
(12, 8,  3,  1, 500000.00, '2026-01-11', 'Transferencia',   'REC-2026-0012', 2026, 'Anual', NULL),
(13, 9,  3,  1, 500000.00, '2026-01-11', 'Transferencia',   'REC-2026-0013', 2026, 'Anual', NULL),
(14, 10, 3,  1, 500000.00, '2026-01-11', 'Cheque',          'REC-2026-0014', 2026, 'Anual', 'Cheque cruzado'),
(15, 8,  3,  4, 150000.00, '2026-02-02', 'Transferencia',   'REC-2026-0015', 2026, 'I',     NULL),
(16, 11, 4,  1, 500000.00, '2026-01-09', 'Transferencia',   'REC-2026-0016', 2026, 'Anual', NULL),
(17, 12, 4,  1, 500000.00, '2026-01-09', 'Transferencia',   'REC-2026-0017', 2026, 'Anual', NULL),
(18, 14, 5,  1, 500000.00, '2026-01-08', 'Tarjeta Débito',  'REC-2026-0018', 2026, 'Anual', NULL),
(19, 15, 5,  1, 500000.00, '2026-01-08', 'Tarjeta Débito',  'REC-2026-0019', 2026, 'Anual', NULL),
(20, 14, 5,  5, 120000.00, '2026-02-04', 'Transferencia',   'REC-2026-0020', 2026, 'I',     NULL),
(21, 17, 6,  1, 500000.00, '2026-01-07', 'Efectivo',        'REC-2026-0021', 2026, 'Anual', NULL),
(22, 18, 6,  1, 500000.00, '2026-01-07', 'Efectivo',        'REC-2026-0022', 2026, 'Anual', NULL),
(23, 19, 24, 1, 500000.00, '2026-01-07', 'Transferencia',   'REC-2026-0023', 2026, 'Anual', NULL),
(24, 20, 15, 1, 500000.00, '2026-01-12', 'Transferencia',   'REC-2026-0024', 2026, 'Anual', NULL),
(25, 20, 15, 4, 150000.00, '2026-02-05', 'Transferencia',   'REC-2026-0025', 2026, 'I',     NULL),
(26, 21, 7,  1, 500000.00, '2026-01-06', 'Transferencia',   'REC-2026-0026', 2026, 'Anual', NULL),
(27, 22, 25, 1, 500000.00, '2026-01-06', 'Tarjeta Débito',  'REC-2026-0027', 2026, 'Anual', NULL),
(28, 23, 16, 1, 500000.00, '2026-01-13', 'Efectivo',        'REC-2026-0028', 2026, 'Anual', NULL),
(29, 24, 8,  1, 500000.00, '2026-01-05', 'Transferencia',   'REC-2026-0029', 2026, 'Anual', NULL),
(30, 25, 8,  1, 500000.00, '2026-01-05', 'Transferencia',   'REC-2026-0030', 2026, 'Anual', NULL),
(31, 26, 8,  1, 500000.00, '2026-01-05', 'Cheque',          'REC-2026-0031', 2026, 'Anual', NULL),
(32, 27, 17, 1, 500000.00, '2026-01-14', 'Transferencia',   'REC-2026-0032', 2026, 'Anual', NULL),
(33, 28, 9,  1, 500000.00, '2026-01-04', 'Transferencia',   'REC-2026-0033', 2026, 'Anual', NULL),
(34, 29, 27, 1, 500000.00, '2026-01-04', 'Transferencia',   'REC-2026-0034', 2026, 'Anual', NULL),
(35, 30, 18, 1, 500000.00, '2026-01-04', 'Tarjeta Crédito', 'REC-2026-0035', 2026, 'Anual', NULL),
(36, 7,  12, 6, 50000.00,  '2026-03-10', 'Efectivo',        'REC-2026-0036', 2026, 'I',     'Salida pedagógica al Museo del Oro'),
(37, 13, 13, 6, 50000.00,  '2026-03-11', 'Efectivo',        'REC-2026-0037', 2026, 'I',     'Salida pedagógica al Museo del Oro'),
(38, 16, 14, 7, 15000.00,  '2026-03-15', 'Efectivo',        'REC-2026-0038', 2026, 'I',     'Certificado de estudios del grado anterior'),
(39, 2,  1,  2, 350000.00, '2026-04-06', 'Transferencia',   'REC-2026-0039', 2026, 'II',    NULL),
(40, 5,  2,  2, 350000.00, '2026-04-07', 'Transferencia',   'REC-2026-0040', 2026, 'II',    NULL);

-- ----------------------------------------------------------------------------
-- Electronic invoices (simulated): invoices issued for 10 of the payments.
-- Files are generated as .txt documents, as in the school ERP prototype.
-- ----------------------------------------------------------------------------
INSERT INTO facturas (id_factura, numero_factura, id_pago, fecha_emision, cufe, nombre_archivo_pdf, ruta_archivo) VALUES
(1,  'F-2026-00001', 1,  '2026-01-10', 'cufe-a1f4c9e2b7d304518a2f6e0b93c1d7a4', 'factura_F-2026-00001.txt', '/var/little_horizons/facturas/2026/factura_F-2026-00001.txt'),
(2,  'F-2026-00002', 2,  '2026-02-05', 'cufe-8b2e6d4f0a9c13257e4b8f6d2c0a9e35', 'factura_F-2026-00002.txt', '/var/little_horizons/facturas/2026/factura_F-2026-00002.txt'),
(3,  'F-2026-00003', 3,  '2026-01-10', 'cufe-3d7b1f8c6a2e940b5c8d7e1f3a6b0c29', 'factura_F-2026-00003.txt', '/var/little_horizons/facturas/2026/factura_F-2026-00003.txt'),
(4,  'F-2026-00004', 5,  '2026-01-12', 'cufe-9e5c2a8d4f1b70639b4e2a7c5d8f1b60', 'factura_F-2026-00004.txt', '/var/little_horizons/facturas/2026/factura_F-2026-00004.txt'),
(5,  'F-2026-00005', 8,  '2026-01-14', 'cufe-4a8f1d3b7e6c09284d5f3b8a2e7c1d94', 'factura_F-2026-00005.txt', '/var/little_horizons/facturas/2026/factura_F-2026-00005.txt'),
(6,  'F-2026-00006', 10, '2026-01-13', 'cufe-6b1e9c4a2f7d58036a9b4e1c8f3d7b25', 'factura_F-2026-00006.txt', '/var/little_horizons/facturas/2026/factura_F-2026-00006.txt'),
(7,  'F-2026-00007', 12, '2026-01-11', 'cufe-2c9a7f1e5b3d86407e2a9c6b4d1f8e37', 'factura_F-2026-00007.txt', '/var/little_horizons/facturas/2026/factura_F-2026-00007.txt'),
(8,  'F-2026-00008', 16, '2026-01-09', 'cufe-7d4b2e9a1c8f53096b7e3a2d6c9f4b18', 'factura_F-2026-00008.txt', '/var/little_horizons/facturas/2026/factura_F-2026-00008.txt'),
(9,  'F-2026-00009', 24, '2026-01-12', 'cufe-1e8c3b6d9a4f27501c8e6b3a9f2d7c46', 'factura_F-2026-00009.txt', '/var/little_horizons/facturas/2026/factura_F-2026-00009.txt'),
(10, 'F-2026-00010', 29, '2026-01-05', 'cufe-5f2a8c1e7b9d46083a5f1c8e4b7d2a59', 'factura_F-2026-00010.txt', '/var/little_horizons/facturas/2026/factura_F-2026-00010.txt');

-- ----------------------------------------------------------------------------
-- Resources: 30 inventory items, six per category (LIB-, LAB-, DEP-, TEC-,
-- DID- codes). Items currently on loan are marked as 'Prestado'; one is
-- 'En Mantenimiento' and one 'Dado de Baja'.
-- ----------------------------------------------------------------------------
INSERT INTO recursos (id_recurso, codigo_recurso, nombre_recurso, id_categoria, descripcion, fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES
(1,  'LIB-001', 'Cien Años de Soledad (edición escolar)',        1, 'Novela de Gabriel García Márquez, edición escolar',      '2022-03-15', 'Biblioteca',       'Prestado'),
(2,  'LIB-002', 'Diccionario de la Lengua Española (escolar)',   1, 'Diccionario básico de la RAE para estudiantes',          '2021-08-02', 'Biblioteca',       'Disponible'),
(3,  'LIB-003', 'Enciclopedia Estudiantil - Tomo 3',             1, 'Tomo de ciencias naturales de la enciclopedia',          '2020-02-10', 'Biblioteca',       'Prestado'),
(4,  'LIB-004', 'Atlas Geográfico de Colombia',                  1, 'Atlas de mapas físico y político del país',              '2023-04-18', 'Biblioteca',       'Disponible'),
(5,  'LIB-005', 'Colección de aventuras de lectura (set 1)',     1, 'Set de 12 cuentos de lectura libre',                     '2024-07-22', 'Biblioteca',       'Disponible'),
(6,  'LIB-006', 'Historia de Colombia para niños',               1, 'Libro ilustrado de historia de Colombia',                '2023-09-05', 'Biblioteca',       'Disponible'),
(7,  'LAB-001', 'Microscopio óptico escolar',                    2, 'Microscopio con aumentos 40x, 100x y 400x',              '2021-05-12', 'Laboratorio 1',    'Disponible'),
(8,  'LAB-002', 'Kit de química básica (vasos y pipetas)',       2, 'Vasos de precipitados, pipetas y probetas',              '2022-01-25', 'Laboratorio 1',    'Disponible'),
(9,  'LAB-003', 'Modelo anatómico del cuerpo humano',            2, 'Modelo desmontable de 45 cm',                            '2020-10-19', 'Laboratorio 1',    'Disponible'),
(10, 'LAB-004', 'Kit de óptica (lentes y prismas)',              2, 'Lentes convergentes, divergentes y prismas',             '2022-06-30', 'Laboratorio 2',    'En Mantenimiento'),
(11, 'LAB-005', 'Termómetros de laboratorio (caja de 10)',       2, 'Termómetros de alcohol de -10 a 110 grados',             '2023-02-14', 'Laboratorio 1',    'Disponible'),
(12, 'LAB-006', 'Balanza digital de precisión',                  2, 'Balanza de 0.01 g de precisión',                         '2024-03-08', 'Laboratorio 2',    'Disponible'),
(13, 'DEP-001', 'Balón oficial de fútbol',                       3, 'Balón de fútbol sala número 4',                          '2022-08-17', 'Bodega deportiva', 'Disponible'),
(14, 'DEP-002', 'Red y postes de voleibol portátiles',           3, 'Set de red con postes ajustables',                       '2021-09-23', 'Bodega deportiva', 'Prestado'),
(15, 'DEP-003', 'Juego de balones de baloncesto (set de 6)',     3, 'Seis balones tamaño escolar',                            '2023-01-11', 'Bodega deportiva', 'Disponible'),
(16, 'DEP-004', 'Batons y uniformes de banda (set)',             3, 'Set de batons y casacas de la banda marcial',            '2020-11-27', 'Bodega deportiva', 'Prestado'),
(17, 'DEP-005', 'Conos y vallas de entrenamiento (set)',         3, 'Doce conos y seis vallas de entrenamiento',              '2024-02-06', 'Bodega deportiva', 'Disponible'),
(18, 'DEP-006', 'Raquetas de tenis de mesa (par)',               3, 'Par de raquetas con tres pelotas',                       '2023-05-29', 'Bodega deportiva', 'Disponible'),
(19, 'TEC-001', 'Tableta Samsung Galaxy Tab A8',                 4, 'Tableta de 10.5 pulgadas para sala de lectura',          '2023-03-20', 'Sala de tecnología', 'Disponible'),
(20, 'TEC-002', 'Computador portátil Asus para estudiantes',     4, 'Portátil de préstamo para proyectos escolares',          '2022-10-03', 'Sala de tecnología', 'Prestado'),
(21, 'TEC-003', 'Proyector Epson portátil',                      4, 'Proyector para aulas y eventos',                         '2021-12-08', 'Sala de tecnología', 'Prestado'),
(22, 'TEC-004', 'Cámara fotográfica Canon EOS',                  4, 'Cámara réflex para el club de fotografía',               '2022-04-14', 'Sala de tecnología', 'Prestado'),
(23, 'TEC-005', 'Robot educativo LEGO Spike Prime',              4, 'Kit de robótica educativa',                              '2024-01-30', 'Sala de tecnología', 'Prestado'),
(24, 'TEC-006', 'Sistema de sonido portátil',                    4, 'Parlante con micrófono para eventos',                    '2023-07-12', 'Sala de tecnología', 'Prestado'),
(25, 'DID-001', 'Maleta didáctica de geometría',                 5, 'Figuras geométricas y material de área',                 '2022-05-24', 'Sala de recursos', 'Disponible'),
(26, 'DID-002', 'Juego de regletas numéricas (set de 10)',       5, 'Regletas para pensamiento numérico',                     '2021-06-15', 'Sala de recursos', 'Disponible'),
(27, 'DID-003', 'Mapa mural de Colombia en relieve',             5, 'Mapa en relieve para aulas de sociales',                 '2020-03-09', 'Aula 201',         'Dado de Baja'),
(28, 'DID-004', 'Kit de experimentos de ciencias para primaria', 5, 'Experimentos seguros para grados de primaria',           '2023-10-26', 'Sala de recursos', 'Disponible'),
(29, 'DID-005', 'Bloques lógicos y tangramas (set)',             5, 'Material para pensamiento espacial',                     '2022-11-08', 'Sala de recursos', 'Disponible'),
(30, 'DID-006', 'Títeres y teatrino didáctico',                  5, 'Teatrino con juego de títeres de mano',                  '2024-04-02', 'Sala de recursos', 'Disponible');

-- ----------------------------------------------------------------------------
-- Student loans: mix of returned loans and active ones, three of which are
-- overdue (expected return date already passed).
-- ----------------------------------------------------------------------------
INSERT INTO prestamos_estudiante (id_prestamo, id_recurso, id_estudiante, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(1,  3,  2,  '2026-03-02', '2026-03-16', '2026-03-15', 'Devuelto', NULL),
(2,  25, 5,  '2026-03-10', '2026-03-24', '2026-03-20', 'Devuelto', NULL),
(3,  19, 11, '2026-04-06', '2026-04-20', '2026-04-18', 'Devuelto', NULL),
(4,  4,  14, '2026-05-04', '2026-05-18', '2026-05-16', 'Devuelto', NULL),
(5,  20, 17, '2026-10-05', '2026-10-19', NULL,         'Activo',   'Vencido desde el 19 de octubre; pendiente recordatorio al acudiente'),
(6,  3,  20, '2026-10-12', '2026-10-26', NULL,         'Activo',   'Préstamo vencido, en gestión de devolución'),
(7,  14, 23, '2026-10-20', '2026-11-03', NULL,         'Activo',   'Entrenamiento del equipo de voleibol'),
(8,  25, 26, '2026-09-01', '2026-09-15', '2026-09-14', 'Devuelto', NULL),
(9,  21, 28, '2026-10-28', '2026-11-11', NULL,         'Activo',   'Proyecto de astronomía en curso'),
(10, 1,  8,  '2026-10-15', '2026-10-29', NULL,         'Activo',   'Vencido desde el 29 de octubre');

-- ----------------------------------------------------------------------------
-- Teacher loans: mix of returned loans and active ones, two overdue.
-- ----------------------------------------------------------------------------
INSERT INTO prestamos_profesor (id_prestamo, id_recurso, id_profesor, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(1, 8,  9,  '2026-02-10', '2026-02-24', '2026-02-24', 'Devuelto', NULL),
(2, 7,  13, '2026-03-03', '2026-03-17', '2026-03-15', 'Devuelto', NULL),
(3, 16, 11, '2026-10-01', '2026-10-15', NULL,         'Activo',   'Vencido desde el 15 de octubre'),
(4, 22, 6,  '2026-10-06', '2026-10-20', NULL,         'Activo',   'Vencido desde el 20 de octubre'),
(5, 9,  13, '2026-09-08', '2026-09-22', '2026-09-20', 'Devuelto', NULL),
(6, 23, 2,  '2026-11-02', '2026-11-16', NULL,         'Activo',   'Preparación del club de robótica'),
(7, 10, 15, '2026-08-12', '2026-08-26', '2026-08-25', 'Devuelto', NULL),
(8, 24, 16, '2026-11-03', '2026-11-17', NULL,         'Activo',   NULL);

-- ----------------------------------------------------------------------------
-- Final verification: row counts per table. The last line prints the total
-- number of rows loaded across all tables.
-- ----------------------------------------------------------------------------
SELECT 'grados'                AS tabla, COUNT(*) AS filas FROM grados
UNION ALL SELECT 'estudiantes',            COUNT(*) FROM estudiantes
UNION ALL SELECT 'acudientes',             COUNT(*) FROM acudientes
UNION ALL SELECT 'estudiante_acudiente',   COUNT(*) FROM estudiante_acudiente
UNION ALL SELECT 'matriculas',             COUNT(*) FROM matriculas
UNION ALL SELECT 'materias',               COUNT(*) FROM materias
UNION ALL SELECT 'profesores',             COUNT(*) FROM profesores
UNION ALL SELECT 'asignaciones_profesor',  COUNT(*) FROM asignaciones_profesor
UNION ALL SELECT 'inscripciones_materia',  COUNT(*) FROM inscripciones_materia
UNION ALL SELECT 'registros_academicos',   COUNT(*) FROM registros_academicos
UNION ALL SELECT 'asistencias',            COUNT(*) FROM asistencias
UNION ALL SELECT 'clubes',                 COUNT(*) FROM clubes
UNION ALL SELECT 'membresias_club',        COUNT(*) FROM membresias_club
UNION ALL SELECT 'eventos',                COUNT(*) FROM eventos
UNION ALL SELECT 'participantes_evento',   COUNT(*) FROM participantes_evento
UNION ALL SELECT 'conceptos_pago',         COUNT(*) FROM conceptos_pago
UNION ALL SELECT 'pagos',                  COUNT(*) FROM pagos
UNION ALL SELECT 'facturas',               COUNT(*) FROM facturas
UNION ALL SELECT 'categorias_recurso',     COUNT(*) FROM categorias_recurso
UNION ALL SELECT 'recursos',               COUNT(*) FROM recursos
UNION ALL SELECT 'prestamos_estudiante',   COUNT(*) FROM prestamos_estudiante
UNION ALL SELECT 'prestamos_profesor',     COUNT(*) FROM prestamos_profesor
UNION ALL SELECT 'TOTAL',                  (SELECT COUNT(*) FROM grados
    + (SELECT COUNT(*) FROM estudiantes) + (SELECT COUNT(*) FROM acudientes)
    + (SELECT COUNT(*) FROM estudiante_acudiente) + (SELECT COUNT(*) FROM matriculas)
    + (SELECT COUNT(*) FROM materias) + (SELECT COUNT(*) FROM profesores)
    + (SELECT COUNT(*) FROM asignaciones_profesor) + (SELECT COUNT(*) FROM inscripciones_materia)
    + (SELECT COUNT(*) FROM registros_academicos) + (SELECT COUNT(*) FROM asistencias)
    + (SELECT COUNT(*) FROM clubes) + (SELECT COUNT(*) FROM membresias_club)
    + (SELECT COUNT(*) FROM eventos) + (SELECT COUNT(*) FROM participantes_evento)
    + (SELECT COUNT(*) FROM conceptos_pago) + (SELECT COUNT(*) FROM pagos)
    + (SELECT COUNT(*) FROM facturas) + (SELECT COUNT(*) FROM categorias_recurso)
    + (SELECT COUNT(*) FROM recursos) + (SELECT COUNT(*) FROM prestamos_estudiante)
    + (SELECT COUNT(*) FROM prestamos_profesor));
