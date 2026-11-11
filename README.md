# Little Horizons ERP

Sistema de Planificación de Recursos Empresariales (ERP) educativo para el Colegio Internacional "Little Horizons By Sofii".

---

## 1. Título del Proyecto

**Little Horizons ERP** — Sistema de Gestión Educativa para el Colegio Internacional "Little Horizons By Sofii"

Aplicación de consola en Java con arquitectura MVC y base de datos MySQL 8.0.

---

## 2. Descripción

### 2.1. Resumen del propósito del sistema

Little Horizons ERP centraliza y automatiza los procesos académicos, administrativos y financieros del colegio: gestión de estudiantes y matrícula, calificaciones y asistencia, actividades extracurriculares, facturación electrónica de pagos y control de recursos (biblioteca y laboratorios). El sistema reemplaza los registros en papel y las hojas de cálculo dispersas, permitiendo decisiones basadas en datos.

### 2.2. Problemas que resuelve

- **Pérdida de información académica**: las notas dejan de registrarse en cuadernos físicos y quedan persistidas en base de datos con integridad referencial.
- **Desorganización en actividades extracurriculares**: registro centralizado de clubes (con control de cupos), eventos y participantes.
- **Caos en la facturación**: generación automática de facturas electrónicas (formato `.txt`) con CUFE simulado, numeración consecutiva y datos del acudiente, siguiendo la estructura exigida por la normativa de la DIAN.
- **Desperdicio de recursos**: sistema de préstamos con rastreo por recurso, estados y reporte de préstamos vencidos, tanto para estudiantes como para profesores.
- **Comunicación ineficiente**: reportes y estadísticas que permiten identificar estudiantes con bajo rendimiento, patrones de inasistencia y morosidad en pagos.

### 2.3. Funcionalidades principales

| Módulo | Funcionalidades |
|---|---|
| Gestión de Estudiantes | CRUD de estudiantes y acudientes, vinculación estudiante-acudiente (hermanos con acudiente común), matrícula por grado y año académico con estados |
| Gestión Académica | CRUD de materias, inscripciones por período, registro de notas (0.0–5.0, un decimal), cálculo automático de promedios (por materia, por período, por curso), historial académico completo, control de asistencia con reportes de patrones de inasistencia |
| Gestión de Profesores | Directorio de profesores, asignación de materias por año y sección, consulta de carga académica |
| Actividades Extracurriculares | Catálogo de clubes con cupo máximo validado, membresías, eventos con competencia y registro de posiciones (1er/2do/3er Lugar) |
| Gestión Financiera | Catálogo de conceptos de pago con valores personalizables, registro de pagos con recibo, historial por estudiante/acudiente, generación automática de factura electrónica `.txt` (CUFE simulado + QR ASCII), reporte de morosos (>1 mes sin pagar) |
| Gestión de Recursos | Inventario por categorías y estados, préstamos a estudiantes y profesores (tablas separadas), control de devoluciones, reporte de préstamos vencidos |
| Reportes y Estadísticas | Dashboard general, promedios y alertas académicas (<3.0), inasistencias (>3 en una semana), recaudo por concepto y mes, morosidad, ocupación de clubes, préstamos vencidos |

---

## 3. Autores

| Campo | Información |
|---|---|
| **Nombre completo** | Sofía — Little Horizons By Sofii |
| **Correo electrónico** | lorensofian23@gmail.com |
| **Información de contacto** | GitHub: [lorensofian-nu](https://github.com/lorensofian-nu) |

---

## 4. Tecnologías Utilizadas

- **Lenguaje de programación**: Java 25 (compatible con Java 17+)
- **IDE utilizado**: Visual Studio Code / IntelliJ IDEA / NetBeans (cualquiera compatible con proyectos Java estándar)
- **Base de datos**: MySQL 8.0+
- **Driver JDBC**: MySQL Connector/J 8.4.0 (incluido en `lib/mysql-connector-j.jar`)
- **Librerías externas**: ninguna adicional — la conexión JDBC y la generación de facturas (`.txt`) se hacen con la librería estándar de Java
- **Herramienta de construcción**: compilación directa con `javac` (proyecto Java estándar, sin Maven/Gradle)

---

## 5. Requerimientos Previos

- **Java Development Kit (JDK) 17 o superior** (probado con JDK 25).
- **MySQL 8.0 o superior** — **obligatorio**: el script DDL usa `CHECK` constraints y sintaxis disponible solo desde MySQL 8.
- **MySQL Connector/J 8.x** — ya incluido en el repositorio (`lib/mysql-connector-j.jar`); si se desea actualizar, descargarlo desde [Maven Central](https://central.sonatype.com/artifact/com.mysql/mysql-connector-j).

---

## 6. Configuración de la Base de Datos

### 6.1. Creación de la base de datos

Ejecutar el script completo `docs/database.sql`, que crea la base de datos `erp_little_horizons`, todas las tablas (DDL) y los datos de prueba (DML, mínimo 30 registros por tabla principal):

```bash
mysql -u root -p < docs/database.sql
```

O desde el cliente de MySQL:

```sql
SOURCE /ruta/al/proyecto/docs/database.sql;
```

### 6.2. Configuración de credenciales

Editar el archivo `db.properties` en la raíz del proyecto:

```properties
db.host=localhost
db.port=3306
db.name=erp_little_horizons
db.user=root
db.password=TU_CONTRASEÑA
```

### 6.3. Instalación del driver JDBC

El driver MySQL Connector/J ya está incluido en `lib/mysql-connector-j.jar`. No se requiere instalación adicional: se agrega al classpath al compilar y ejecutar (ver sección 7). Si usas un IDE, agrégalo como librería externa del proyecto (*Add External JARs* en Eclipse/IntelliJ, o en la lista de dependencias de NetBeans).

---

## 7. Instalación y Ejecución

### 7.1. Clonar el repositorio

```bash
git clone https://github.com/lorensofian-nu/little-horizons-by-sofii.git
cd little-horizons-by-sofii
```

### 7.2. Abrir el proyecto en el IDE

Importar la carpeta como proyecto Java estándar. La carpeta de código fuente es `src/`.

### 7.3. Compilar

Desde la terminal, en la raíz del proyecto:

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

En Windows (PowerShell):

```powershell
Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName | Out-File sources.txt
javac -encoding UTF-8 -d out @sources.txt
```

### 7.4. Ejecutar

```bash
java -cp "out:lib/mysql-connector-j.jar" com.littlehorizons.erp.Main
```

En Windows (usar `;` en lugar de `:`):

```powershell
java -cp "out;lib/mysql-connector-j.jar" com.littlehorizons.erp.Main
```

Al iniciar, el sistema valida la conexión a la base de datos y muestra el menú principal:

```
╔══════════════════════════════════════════════════╗
║   ERP Colegio Little Horizons                    ║
║   Sistema de Gestión Educativa                   ║
╚══════════════════════════════════════════════════╝

  1. Gestión de Estudiantes
  2. Gestión Académica
  3. Gestión de Profesores
  4. Actividades Extracurriculares
  5. Gestión Financiera
  6. Gestión de Recursos
  7. Reportes y Estadísticas
  8. Salir

Seleccione una opción: _
```

---

## 8. Estructura del Proyecto

```
little-horizons-by-sofii/
├── src/
│   └── com/littlehorizons/erp/
│       ├── Main.java              # Punto de entrada y menú principal
│       ├── model/                 # Entidades del dominio y enums
│       ├── view/                  # Interfaces de consola (menús)
│       ├── controller/            # Lógica de negocio y validaciones
│       ├── dao/                   # Contratos de acceso a datos (DAO)
│       │   └── impl/              # Implementaciones JDBC (MySQL)
│       └── util/                  # Utilidades compartidas
├── docs/
│   └── database.sql               # DDL + DML (datos de prueba)
├── lib/
│   └── mysql-connector-j.jar      # Driver JDBC de MySQL
├── invoices/                      # Facturas .txt generadas (runtime)
├── db.properties                  # Credenciales de la base de datos
└── error_log.txt                  # Log de errores (runtime)
```

### Descripción de cada paquete (arquitectura MVC)

- **`model`** — Entidades del dominio: clases de datos puras (`Student`, `Teacher`, `Payment`, `Resource`, ...) con sus enums. No conocen la base de datos ni la consola.
- **`view`** — Capa de presentación: menús de consola (`StudentView`, `AcademicView`, `TeacherView`, `ExtracurricularView`, `FinancialView`, `ResourceView`, `ReportsView`). Solo leen entrada y muestran salida mediante `ConsoleUtils`.
- **`controller`** — Capa de lógica de negocio: validaciones (cupos, notas 0.0–5.0, documentos únicos), cálculos (promedios, morosidad) y orquestación entre vistas y DAOs.
- **`dao` / `dao.impl`** — Capa de acceso a datos: contratos `XxxDao` e implementaciones JDBC `XxxDaoImpl` que usan exclusivamente `PreparedStatement` y try-with-resources.
- **`util`** — Utilidades compartidas: `DatabaseConnection` (conexión), `ConsoleUtils` (entrada/salida validada), `LoggerUtil` (log de errores), `InvoiceGenerator` (facturas `.txt`), `AppConstants` (constantes del colegio).
- **`Main`** — Punto de entrada: valida la conexión, muestra el menú principal y delega a las vistas.

El flujo de una operación es siempre: **View → Controller → DAO → MySQL**, devolviendo los datos por el mismo camino (Model).

---

## 9. Principios y Patrones de Diseño Aplicados

### 9.1. Principios SOLID

- **S — Single Responsibility**: cada clase tiene un único motivo de cambio. Ej.: `StudentDaoImpl` solo ejecuta SQL de estudiantes; `AcademicRecordController` solo contiene reglas de calificaciones; `InvoiceGenerator` solo construye el documento de factura.
- **O — Open/Closed**: las vistas y controladores dependen de **interfaces** DAO (`StudentDao`, `PaymentDao`), por lo que se puede cambiar la implementación (otro motor de BD, otra estrategia) sin modificar el código que la usa.
- **L — Liskov Substitution**: las implementaciones `XxxDaoImpl` cumplen el contrato completo de sus interfaces `XxxDao`; cualquier implementación alternativa es intercambiable.
- **I — Interface Segregation**: los contratos DAO son específicos por entidad y solo exponen las operaciones que la entidad necesita (`ClubMembershipDao` expone `countActiveMembers`, que no existe en otros DAOs).
- **D — Dependency Inversion**: los controladores reciben sus DAOs **por constructor** (inyección de dependencias manual) apuntando a la abstracción, no a la implementación concreta. Ej.: `new StudentController(new StudentDaoImpl(), ...)`.

### 9.2. Patrones de Diseño

- **Singleton** — `DatabaseConnection`: una única instancia de configuración de conexión, con *double-checked locking*. *Justificación*: garantiza una única fuente de configuración de BD leída una sola vez de `db.properties`.
- **DAO (Data Access Object)** — Todo acceso a datos pasa por interfaces `dao/` implementadas en `dao/impl/`. *Justificación*: aísla el SQL del resto del sistema, facilita pruebas y cumple el principio de inversión de dependencias.
- **MVC (Model-View-Controller)** — Separación estricta de responsabilidades en los paquetes `model`, `view` y `controller`. *Justificación*: requisito del proyecto y base del desacoplamiento de la lógica de presentación.
- **Factory Method (implícito)** — `DatabaseConnection.getConnection()` actúa como fábrica de conexiones `DriverManager.getConnection(...)`. *Justificación*: centraliza la creación de recursos costosos.
- **Data Transfer Object / Value Object** — Clases de resumen (`StudentSummary`, `TeacherSummary`, `LoanSummary`, filas de reporte en el módulo de reportes) transportan datos entre capas sin exponer entidades completas. *Justificación*: listings y JOINs livianos sin acoplar módulos.

---

## 10. Funcionalidades Principales (por módulo)

**1. Gestión de Estudiantes** — Registro completo con datos personales y médicos; acudientes con parentesco y responsable financiero; vinculación muchos-a-muchos (un acudiente, varios hijos); matrícula por grado y año (`Activa/Completada/Retirada`); baja lógica (estado `Retirado`) con borrado físico confirmado.

**2. Gestión Académica** — Materias por grado con código único; inscripción por período validando matrícula activa; registro de notas por estudiante/materia/profesor/período con validación 0.0–5.0 a un decimal; promedios calculados con Stream API (por materia, por período y por curso); historial académico completo; asistencia diaria (`Presente/Ausente/Tardanza/Excusa Médica`) y reportes de patrones (>3 ausencias en una semana ISO).

**3. Gestión de Profesores** — Directorio con documento y correo únicos; asignación de materias por año y sección (A/B/C) con validación de unicidad; consulta de carga académica por profesor y por materia; cambio de estado (`Activo/Inactivo/Licencia`) preferido sobre el borrado.

**4. Actividades Extracurriculares** — Clubes con cupo máximo validado en el momento de la inscripción; membresías con fecha de ingreso/retiro y motivo; eventos por tipo (`Académico/Cultural/Deportivo/Social/Científico`); participantes con rol y, en eventos con competencia, posición obtenida; reportes de participación y podio.

**5. Gestión Financiera** — Conceptos de pago con valores base y recurrentes; pago con monto personalizado por estudiante (descuentos/becas), método de pago y recibo consecutivo; **factura electrónica en `.txt`** generada automáticamente en `invoices/` con: datos del colegio, resolución DIAN (simulada), datos del acudiente, numeración consecutiva `FAC-000001`, detalle (descripción, cantidad, valor unitario, subtotal), IVA 0% y total, **CUFE simulado** (SHA-256 de `número|NIT|fecha`) y **QR ASCII simulado**; historial de pagos por estudiante y por acudiente (todos sus hijos); reporte de morosos (>30 días sin pagar, ordenado por antigüedad).

**6. Gestión de Recursos** — Inventario por categorías (Libro, Laboratorio, Deportivo, Tecnológico, Didáctico) con código único, ubicación y estados (`Disponible/Prestado/En Mantenimiento/Dado de Baja`); préstamos a estudiantes y profesores en **tablas separadas** (`prestamos_estudiante` / `prestamos_profesor`) con transacción atómica (préstamo ↔ estado del recurso); devoluciones con fecha real; reporte de vencidos.

**7. Reportes y Estadísticas** — Dashboard general; promedios por estudiante/materia con alertas <3.0 y top 5; inasistencias por semana ISO y % de asistencia; recaudo por concepto y por mes con morosidad; ocupación de clubes y podio de eventos; préstamos vencidos e inventario por categoría/estado.

---

## 11. Manejo de Errores y Logs

- **Sistema de logging**: clase `LoggerUtil` — cada error capturado se registra con marca de tiempo (`yyyy-MM-dd HH:mm:ss`), contexto y tipo de excepción en el archivo **`error_log.txt`** en la raíz del proyecto.
- **Ubicación del archivo de logs**: `error_log.txt` (se crea automáticamente al primer error).
- **Estrategia de manejo de excepciones**:
  - `SQLException` se captura en la capa de controladores: se muestra un mensaje claro al usuario (`ConsoleUtils.printError`) y se registra la traza (`LoggerUtil.logError`), sin propagar la excepción hasta romper la ejecución.
  - `IOException` en generación de facturas y logging se captura y reporta sin detener la aplicación.
  - Las entradas del usuario nunca lanzan excepciones: `ConsoleUtils` valida con bucles de reintento (números, fechas `yyyy-MM-dd`, opciones de listas, confirmaciones).
  - `try-with-resources` en **todas** las conexiones, statements y result sets para liberar recursos siempre.
  - Fallo de conexión al inicio: mensaje guiado (verificar MySQL, `db.properties`, ejecutar `docs/database.sql`) y cierre limpio.

---

## 12. Capturas de Pantalla

_(Opcional — agregar capturas del menú principal, registro de notas, factura `.txt` generada y reportes al ejecutar el sistema.)_

---

## 13. Problemas Conocidos y Limitaciones

- El **CUFE, el código QR y la resolución DIAN son simulados** (fines académicos): el CUFE es un hash SHA-256 y el QR se dibuja en ASCII. No existe conexión real con la DIAN.
- Las facturas se generan en **formato `.txt`** (decisión del proyecto), no en PDF.
- El control de horarios de profesores es básico: la asignación muestra la carga actual del profesor para evitar solapamientos, pero no hay una malla de horarios (el esquema de BD no incluye columnas de horario).
- La aplicación requiere consola con soporte UTF-8 para mostrar correctamente los caracteres de caja (`╔ ═ ╚`) y las tildes.
- El script `docs/database.sql` reinicia la base de datos (`DROP DATABASE`), por lo que no debe ejecutarse sobre datos reales.

---

## 14. Futuras Mejoras

- Interfaz gráfica (JavaFX/Swing) o API REST reutilizando la capa de DAO/Controller.
- Facturación en PDF real con librería (OpenPDF/iText) e integración con un proveedor de facturación electrónica habilitado ante la DIAN.
- Módulo de horarios con validación automática de solapamientos.
- Autenticación de usuarios y roles (administrador, profesor, secretaría).
- Pruebas unitarias (JUnit 5) para la capa de controladores y cálculo de promedios.

---

## 15. Licencia

Este proyecto se desarrolla con fines académicos. Todos los derechos reservados © 2026 — Colegio Internacional "Little Horizons By Sofii".

---

## 16. Contacto

- **Repositorio**: https://github.com/lorensofian-nu/little-horizons-by-sofii
- **Correo**: lorensofian23@gmail.com
- **Contacto**: ver sección [Autores](#3-autores).
