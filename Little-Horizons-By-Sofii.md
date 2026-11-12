Proyecto ERP para el Colegio "Little Horizons By Sofii"  
Proyecto ERP para el Colegio "Little Horizons By Sofii"  
Descripción del proyecto  
Introducción: El Desafío de la Gestión Educativa  
El Colegio Internacional "Little Horizons By Sofii"  
es una institución educativa en crecimiento que atiende a estudiantes desde preescolar hasta grado 11°. Con más de 500 estudiantes, 40 profesores y una oferta académica que incluye múltiples materias y actividades extracurriculares, la gestión manual de información se ha vuelto insostenible. Los registros en papel y hojas de cálculo dispersas generan ineficiencias, errores y pérdida de tiempo valioso.  
Como desarrollador, has sido contratado para crear un Sistema de Planificación de Recursos Empresariales (ERP) educativo que centralice y automatice los procesos académicos, administrativos y financieros del colegio. Este sistema transformará la operación diaria, permitiendo decisiones basadas en datos y mejorando la experiencia de toda la comunidad educativa.  
Contexto del Problema: Las Ineficiencias Operacionales  
El día a día en "Little Horizons" está plagado de problemas que este ERP debe resolver:  
Pérdida de Información Académica: Los registros de notas en cuadernos físicos se pierden o dañan, causando conflictos con padres y estudiantes al final del período.  
Desorganización en Actividades Extracurriculares: No existe un registro centralizado de qué estudiantes participan en qué clubes o eventos, generando duplicación de esfuerzos y falta de seguimiento.  
Caos en la Facturación: El proceso manual de generar facturas es lento, propenso a errores de cálculo, y no cumple con la normativa de facturación electrónica de la DIAN.  
Desperdicio de Recursos: Los libros y equipos de laboratorio se "pierden" porque no hay un sistema de préstamos que permita rastrear quién tiene cada recurso.  
Comunicación Ineficiente: Los profesores no tienen visibilidad sobre el rendimiento general de sus estudiantes ni pueden identificar fácilmente quiénes necesitan apoyo adicional.  
Tu misión es construir el sistema nervioso central que conecte todas estas áreas, transformando el caos en un flujo de trabajo eficiente y basado en información.  
Descripción Detallada del Sistema a Desarrollar  
1\. Módulo de Gestión Académica: El Corazón Educativo  
Este módulo gestiona todo el ciclo académico del colegio.  
1.1. Gestión de Estudiantes y Matrícula  
Registro Completo de Estudiantes: El sistema debe permitir registrar estudiantes con toda su información personal (nombre completo, documento de identidad, fecha de nacimiento, dirección, contactos de emergencia).  
Gestión de Acudientes: Cada estudiante debe tener asociado al menos un acudiente (padre, madre o tutor legal) con sus datos de contacto. El sistema debe permitir que un acudiente tenga múltiples estudiantes a su cargo (hermanos).  
Matrícula por Grado: Los estudiantes deben estar matriculados en un grado específico (Preescolar, 1°, 2°... hasta 11°). El sistema debe registrar el año académico de la matrícula y el estado (Activo, Graduado, Retirado).  
Inscripción en Materias: Para cada período académico, el sistema debe permitir inscribir a los estudiantes en las materias correspondientes a su grado. Una materia puede tener múltiples estudiantes, y un estudiante puede estar inscrito en múltiples materias.  
1.2. Gestión de Profesores y Asignación de Carga Académica  
Directorio de Profesores: Registro completo de profesores con su información personal, documento de identidad, especialización y datos de contacto.  
Asignación de Materias: El sistema debe permitir asignar a cada profesor las materias que dicta. Un profesor puede dictar múltiples materias, y una materia puede ser dictada por múltiples profesores (en diferentes grupos o turnos).  
Horarios de Clase: Aunque el módulo principal no gestiona horarios complejos, debe registrar en qué días y horas cada profesor tiene asignada cada materia para evitar conflictos básicos.  
1.3. Registro y Consulta de Calificaciones  
Ingreso de Notas: Los profesores deben poder registrar las calificaciones de sus estudiantes por materia y período académico. Cada registro debe incluir la nota numérica (0.0-5.0 con un decimal) y el período (I, II, III, IV o Final).  
Cálculo Automático de Promedios: El sistema debe calcular automáticamente:  
Promedio por materia de cada estudiante  
Promedio general del estudiante en el período  
Promedio general del curso en cada materia  
Consulta de Historial Académico: Debe ser posible consultar todo el historial de notas de un estudiante desde su ingreso al colegio.  
1.4. Control de Asistencia  
Registro Diario: El sistema debe permitir marcar la asistencia diaria de cada estudiante por materia (Presente, Ausente, Tardanza, Excusa Médica).  
Reportes de Inasistencias: Debe ser posible generar reportes de estudiantes con patrones de inasistencia preocupantes (más de 3 ausencias en una semana, por ejemplo).  
2\. Módulo de Actividades Extracurriculares: Más Allá del Aula  
2.1. Gestión de Clubes  
Catálogo de Clubes: El sistema debe mantener un catálogo de clubes activos (Club de Ciencias, Club de Artes, Club de Deportes, Club de Robótica, etc.) con su descripción, cupo máximo y profesor encargado.  
Inscripción de Estudiantes: Los estudiantes pueden inscribirse en uno o más clubes. El sistema debe validar que no se exceda el cupo máximo.  
Seguimiento de Participación: Debe registrarse la fecha de ingreso del estudiante al club y su estado (Activo, Retirado).  
2.2. Gestión de Eventos  
Calendario de Eventos: El colegio organiza diversos eventos (Feria de Ciencias, Día Cultural, Día del Deporte, etc.). El sistema debe permitir registrar eventos con su nombre, fecha, descripción y tipo (Académico, Cultural, Deportivo, Social).  
Registro de Participantes: Para cada evento, debe ser posible registrar qué estudiantes participaron. Esto es crucial para reportes de participación y para incentivar la vida extracurricular.  
Eventos con Competencia: Algunos eventos tienen competencia (como la Feria de Ciencias). El sistema debe permitir registrar la posición obtenida por cada participante (1er lugar, 2do lugar, 3er lugar, Participante).  
3\. Módulo Financiero: Pagos y Facturación  
3.1. Gestión de Conceptos de Pago  
Catálogo de Conceptos: El colegio cobra por diferentes conceptos: matrícula anual, pensión mensual, uniformes, actividades especiales, salidas pedagógicas, etc. El sistema debe mantener un catálogo actualizado con el nombre del concepto y su valor base.  
Personalización por Estudiante: Algunos conceptos pueden tener valores diferenciados (ej: pensión con descuento por hermanos, becas parciales). El sistema debe permitir ajustar el valor para estudiantes específicos.  
3.2. Registro de Pagos  
Registro Detallado: Cada pago realizado debe registrar: estudiante, concepto de pago, monto pagado, fecha de pago, método de pago (Efectivo, Transferencia, Tarjeta) y número de recibo.  
Historial de Pagos: Debe ser posible consultar todo el historial de pagos de un estudiante o de un acudiente (si tiene varios hijos en el colegio).  
Identificación de Morosos: El sistema debe facilitar la generación de reportes de estudiantes con pagos pendientes por más de un mes.  
3.3. Generación de Facturas Electrónicas  
El sistema debe generar automáticamente una Factura Electrónica visual en formato TXT que cumpla con la estructura académica definida para el proyecto.
Características de la Factura:  
Información del Colegio: Razón social ("Colegio Internacional Little Horizons S.A.S."), NIT, dirección, teléfono, correo electrónico.  
Información del Cliente: Nombre completo del acudiente responsable del pago, documento de identidad, dirección.  
Detalle de la Factura:  
Número de factura consecutivo  
Fecha de emisión  
Lista de conceptos pagados (Descripción, Cantidad, Valor Unitario, Subtotal)  
Subtotal, IVA (si aplica), Total  
Elementos de Cumplimiento DIAN:  
CUFE (Código Único de Factura Electrónica) simulado  
Código QR simulado que "contiene" la información de la factura  
Resolución de habilitación de facturación (simulada)  
Flujo de Generación:  
Al confirmar un pago, el sistema recopila automáticamente todos los datos necesarios desde la base de datos.  
Calcula el CUFE simulado (puede ser un hash del número de factura \+ NIT \+ fecha).  
Genera el archivo TXT con toda la información formateada profesionalmente.
Guarda el TXT en una carpeta del proyecto con nombre único (ej: factura\_000123.txt).
Opcionalmente, muestra un mensaje al usuario con la ruta donde se guardó la factura.  
4\. Módulo de Gestión de Recursos: Biblioteca y Laboratorios  
4.1. Catálogo de Recursos  
Registro de Recursos: El sistema debe mantener un inventario de todos los recursos prestables del colegio: libros de la biblioteca, equipos de laboratorio (microscopios, balanzas), equipos deportivos, dispositivos tecnológicos (tablets, calculadoras científicas), etc.  
Información por Recurso: Cada recurso debe tener un código único, nombre, categoría (Libro, Equipo de Laboratorio, Equipo Deportivo, Dispositivo Tecnológico), estado (Disponible, Prestado, En Mantenimiento, Dado de Baja) y ubicación física.  
4.2. Sistema de Préstamos  
Registro de Préstamo: Cuando un estudiante o profesor solicita un recurso, el sistema debe registrar: quién lo solicita (estudiante o profesor), qué recurso, fecha de préstamo, fecha de devolución programada y estado del préstamo (Activo, Devuelto, Vencido).  
Control de Devoluciones: Al registrar la devolución, se debe actualizar el estado del recurso a "Disponible" y registrar la fecha real de devolución.  
Identificación de Préstamos Vencidos: El sistema debe poder generar reportes de préstamos que no han sido devueltos en la fecha programada, facilitando el seguimiento por parte de la biblioteca o laboratorio.  
5\. Interfaz de Usuario y Arquitectura del Sistema  
5.1. Arquitectura MVC  
El proyecto debe seguir estrictamente una arquitectura Modelo-Vista-Controlador (MVC). La organización del código debe separar claramente las responsabilidades de cada capa.  
Ejemplo de estructura de paquetes:  
src/  
└── com/littlehorizons/erp/  
    ├── model/               // Entidades del dominio  
    ├── view/                // Interfaces de consola  
    ├── controller/          // Lógica de negocio  
    ├── util/                // Utilidades compartidas  
    └── Main.java  
Nota: Esta es una estructura de ejemplo ilustrativa. Los estudiantes pueden organizar los paquetes internos según consideren apropiado (añadir dao/, service/, subdividir por módulos), siempre respetando la separación MVC.  
5.2. Interfaz de Consola Profesional  
Aunque la aplicación es de consola, debe ofrecer una experiencia de usuario clara y profesional:  
Menú Principal Organizado: Un menú principal que agrupe las opciones por módulo.  
Validación de Entradas: Todas las entradas del usuario deben ser validadas. Si ingresa un dato inválido, el sistema debe mostrar un mensaje claro y permitir reintentar.  
Mensajes de Confirmación y Error: Cada operación debe mostrar mensajes claros de éxito o error.  
Navegación Intuitiva: Debe ser fácil regresar al menú anterior o al menú principal desde cualquier punto.  
Ejemplo de Menú Principal:  
╔══════════════════════════════════════════════╗  
║   ERP Colegio Little Horizons               ║  
║   Sistema de Gestión Educativa              ║  
╚══════════════════════════════════════════════╝

1\. Gestión de Estudiantes  
2\. Gestión Académica  
3\. Gestión de Profesores  
4\. Actividades Extracurriculares  
5\. Gestión Financiera  
6\. Gestión de Recursos  
7\. Reportes y Estadísticas  
8\. Salir

Seleccione una opción: \_  
Anexo: Script de Base de Datos  
El script completo DDL de MySQL para crear todas las tablas necesarias está disponible en el siguiente enlace. Este script debe ejecutarse en una base de datos llamada erp\_little\_horizons.  
Enlace al script SQL: [https\://drive.google.com/file/d/1fRaR8EUwcNheOWyMK5YJMkW0NWHo5JK7/view?usp=sharing](https://drive.google.com/file/d/1fRaR8EUwcNheOWyMK5YJMkW0NWHo5JK7/view?usp=sharing)  
Nota importante: El script incluye todas las mejoras técnicas recomendadas:  
Tipos de datos precisos para el sistema educativo colombiano (DECIMAL(2,1) para notas de 0.0 a 5.0)  
Integridad referencial completa con tablas separadas para préstamos a estudiantes (prestamos\_estudiante) y profesores (prestamos\_profesor)  
Todos los nombres en español (base de datos, tablas, campos y comentarios)  
Requiere MySQL 8.0 o superior para que funcionen correctamente los CHECK constraints  
Resultado esperado  
Resultados Esperados  
1\. Código Fuente y Estructura del Proyecto  
IDE y Herramienta de Construcción: El proyecto debe ser desarrollado como una aplicación Java estándar. El IDE o editor de código a utilizar será indicado por el trainer al inicio del proyecto.  
Arquitectura MVC: La organización del código debe seguir estrictamente el patrón Modelo-Vista-Controlador (MVC). La estructura debe separar claramente las responsabilidades de cada capa.  
Estructura general esperada:  
src/  
└── com/littlehorizons/erp/  
    ├── model/               // Entidades del dominio  
    ├── view/                // Interfaces de consola  
    ├── controller/          // Lógica de negocio  
    ├── util/                // Utilidades compartidas  
    └── Main.java            // Punto de entrada  
Nota importante: Esta es una estructura de ejemplo ilustrativa. Los estudiantes pueden organizar los paquetes internos según consideren apropiado (añadir dao/, service/, subdividir por módulos), siempre respetando la separación MVC.  
Lenguaje y Convenciones: Todo el código (clases, variables, métodos, comentarios) debe estar escrito en inglés. Se debe seguir la convención de nomenclatura estándar de Java:  
PascalCase para nombres de clases (ej: StudentController, AcademicRecord)  
camelCase para variables y métodos (ej: firstName, calculateAverage())  
UPPER\_SNAKE\_CASE para constantes (ej: MAX\_STUDENTS, DATABASE\_URL)  
2\. Archivos y Documentación Técnica  
Dentro del repositorio, en una carpeta llamada docs/, se debe incluir el siguiente artefacto:  
2.1. Script de Base de Datos (database.sql)  
Este archivo debe contener dos secciones claramente diferenciadas:  
Sección 1 \- DDL (Data Definition Language):  
El script SQL completo con todas las sentencias CREATE TABLE para construir la estructura de la base de datos desde cero.  
Debe incluir todas las restricciones: llaves primarias, llaves foráneas, índices, checks, etc.  
Debe estar basado en el script proporcionado en el anexo del proyecto (versión corregida con DECIMAL(2,1) para notas y tablas separadas para préstamos).  
Sección 2 \- DML (Data Manipulation Language):  
Un conjunto de sentencias INSERT para poblar la base de datos con datos de prueba coherentes y realistas.  
Se requiere un mínimo de 30 registros en cada tabla principal:  
estudiantes  
acudientes  
profesores  
materias  
registros\_academicos  
pagos  
clubes  
recursos  
La coherencia es clave: cada registro debe respetar la integridad referencial.  
Los datos deben ser realistas: nombres de personas reales, fechas lógicas, montos apropiados para Colombia, etc.  
3\. Formato del Archivo [README.md](http://readme.md/)  
El archivo [README.md](http://readme.md/) es la carta de presentación del proyecto y debe ser exhaustivo, claro y profesional. Debe estar estructurado con las siguientes secciones:  
Estructura de Secciones Requeridas:  
1\. Título del Proyecto  
2\. Descripción  
Resumen del propósito del sistema  
Problemas que resuelve  
Funcionalidades principales  
3\. Autores  
Nombre completo  
Correo electrónico  
Información de contacto relevante  
4\. Tecnologías Utilizadas  
Lenguaje de programación  
IDE utilizado  
Base de datos  
Librerías externas  
5\. Requerimientos Previos  
Software necesario para ejecutar el proyecto  
Versiones específicas requeridas  
Importante: Especificar que se requiere MySQL 8.0 o superior  
6\. Configuración de la Base de Datos  
6.1. Creación de la base de datos  
6.2. Configuración de credenciales  
6.3. Instalación del driver JDBC  
7\. Instalación y Ejecución  
7.1. Clonar el repositorio  
7.2. Abrir el proyecto en el IDE  
7.3. Compilar  
7.4. Ejecutar  
8\. Estructura del Proyecto  
Explicación de la arquitectura MVC  
Descripción de cada paquete principal  
9\. Principios y Patrones de Diseño Aplicados  
9.1. Principios SOLID  
Explicación de cada principio con ejemplos del código  
9.2. Patrones de Diseño  
Patrones implementados (Singleton, DAO, etc.)  
Justificación de su uso  
10\. Funcionalidades Principales  
Lista detallada de funcionalidades por módulo  
11\. Manejo de Errores y Logs  
Sistema de logging implementado  
Ubicación del archivo de logs  
Estrategia de manejo de excepciones  
12\. Capturas de Pantalla (opcional)  
Evidencia visual del funcionamiento  
13\. Problemas Conocidos y Limitaciones (opcional)  
14\. Futuras Mejoras (opcional)  
15\. Licencia  
16\. Contacto  
4\. Entrega Final  
La evaluación se realizará sobre la base de dos elementos que deben ser proporcionados antes de la fecha y hora límite establecida:  
URL del repositorio privado de GitHub  
El repositorio debe ser privado  
El trainer/instructor debe estar agregado como colaborador con permisos de lectura  
El repositorio debe contener todo el código fuente, documentación y archivos necesarios  
Hash completo del último commit  
Debe proporcionarse el hash SHA completo del commit que representa la entrega final  
Este commit debe estar en la rama principal (main o master)  
Cualquier commit posterior a la fecha de entrega no será considerado  
Ejemplo de hash: a3f2b1c9d8e7f6a5b4c3d2e1f0a9b8c7d6e5f4a3  
Formato de entrega en la plataforma:  
URL del repositorio: [https\://github.com/usuario/little-horizons-erp](https://github.com/usuario/little-horizons-erp)  
Hash del commit: a3f2b1c9d8e7f6a5b4c3d2e1f0a9b8c7d6e5f4a3  
5\. Verificación del Proyecto  
Antes de entregar, verifica que tu proyecto cumple con todos los requisitos:  
Checklist de Entrega:  
✅ Código y Estructura  
\[ \] Proyecto Java estándar creado correctamente  
\[ \] Estructura MVC implementada correctamente  
\[ \] Código completamente en inglés (clases, variables, métodos, comentarios)  
\[ \] Convenciones de nomenclatura Java aplicadas consistentemente  
✅ Base de Datos  
\[ \] Base de datos erp\_little\_horizons creada correctamente  
\[ \] Script database.sql incluye DDL completo (basado en versión corregida)  
\[ \] Script incluye DML con mínimo 30 registros por tabla principal  
\[ \] Datos de prueba coherentes y realistas  
\[ \] MySQL 8.0 o superior instalado y funcionando  
✅ Funcionalidades  
\[ \] Todas las funcionalidades principales implementadas y funcionando  
\[ \] Validación de entradas del usuario en todas las vistas  
\[ \] Mensajes de confirmación y error claros  
\[ \] Navegación intuitiva entre menús  
✅ Calidad del Código  
\[ \] Manejo de errores con try-catch  
\[ \] Sistema de logs implementado (archivo error\_log.txt)  
\[ \] Uso de PreparedStatement en todas las consultas SQL  
\[ \] Gestión correcta de recursos con try-with-resources  
\[ \] Aplicación de principios SOLID  
\[ \] Uso de lambdas y Stream API en procesamiento de colecciones  
\[ \] Generación de reportes/facturas TXT funcionando correctamente
✅ Documentación  
\[ \] Carpeta docs/ creada en el repositorio  
\[ \] Archivo database.sql con DDL y DML completos  
\[ \] [README.md](http://readme.md/) completo con todas las secciones requeridas  
✅ GitHub  
\[ \] Repositorio privado creado en GitHub  
\[ \] Trainer/instructor agregado como colaborador  
\[ \] Commits con mensajes claros y descriptivos en inglés  
\[ \] Commits atómicos (cambios relacionados agrupados)  
\[ \] Hash del último commit documentado  
\[ \] Estructura de carpetas organizada  
✅ Pruebas Finales  
\[ \] El proyecto compila sin errores  
\[ \] El proyecto ejecuta correctamente  
\[ \] Conexión a base de datos funciona correctamente  
\[ \] Todas las operaciones CRUD funcionan  
\[ \] No hay errores de ejecución en flujos principales  
\[ \] Los logs se generan correctamente en error\_log.txt  
\[ \] Los reportes/facturas TXT se generan en la carpeta correcta

