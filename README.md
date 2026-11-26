# ÁvilaOS - Simulador de Sistema Operativo

**Proyecto 2 - Sistemas Operativos (Trimestre 2627-1)**

ÁvilaOS es un simulador de sistema operativo desarrollado en Java. En esta segunda etapa el sistema se extiende con un **disco simulado**, un **sistema de archivos jerárquico**, un **modelo de siete estados con suspensión**, planificación a **mediano plazo con swap**, y **modos administrador/usuario con permisos**. Se mantiene el estricto diseño por capas del Proyecto 1 y se conservan las cuatro políticas de CPU, la memoria principal y las métricas.

👥 Integrantes
- Juan Moreira
- Miguel Angel Maíz
- Dario Fernandez

⚙️ Requisitos del Sistema
- Java JDK 21 o superior.
- Apache NetBeans (estrictamente requerido por la cátedra).
- Maven para la gestión de dependencias.
- Librerías externas utilizadas: JFreeChart (gráficos) y Gson (JSON).
- El proyecto debe abrir y ejecutarse desde un **clon limpio** del repositorio en NetBeans.

🚀 ¿Cómo compilar y ejecutar?
1. Clona este repositorio en tu máquina local.
2. Abre el proyecto en NetBeans.
3. NetBeans descargará automáticamente las dependencias declaradas en el `pom.xml` (JFreeChart y Gson).
4. Dirígete al paquete `com.mycompany.so_miguelangel_juan_dario` y ejecuta la clase `main.java`.
5. La interfaz gráfica se abrirá automáticamente.

💡 **Nota obligatoria antes de iniciar:** La interfaz exige cargar **ambos archivos JSON** (`globalConfig` y `diskConfig`/`fileSystem`) antes de habilitar el reloj global. El botón **Iniciar** permanece deshabilitado hasta que las dos configuraciones estén cargadas.  
Los antiguos procesos **PRODUCTOR/CONSUMIDOR** y los buffers ya no forman parte del proyecto. Toda operación CRUD se realiza mediante un **proceso de E/S**, nunca directamente desde la ventana.

🧩 Características Principales
- **Gestión de Procesos:** Modelado mediante la clase `PCB`. Implementa un **modelo de 7 estados** (Nuevo, Listo, Ejecución, Bloqueado, Bloqueado-Suspendido, Listo-Suspendido, Terminado) con muerte por vencimiento de Deadline.
- **Planificación de CPU (Corto Plazo):** 4 políticas implementadas mediante el patrón Strategy (`PoliticaPlanificacion`): FCFS, Round Robin (quantum configurable en tiempo real), EDF y Prioridades Apropiativas.
- **Planificación a Mediano Plazo:** Suspensión y reactivación de procesos según `PCB.ciclosBloqueado`, con área de intercambio (**swap**). Un proceso suspendido ocupa en swap tantos bloques como unidades de RAM tenga.
- **Planificación a Largo Plazo:** Admisión de procesos basada en la disponibilidad de Memoria Principal (RAM).
- **Disco Simulado:** Bloques numerados y un cabezal que se mueve. El disco atiende una solicitud a la vez y avanza exactamente un ciclo de trabajo por cada tick del reloj. Planificación de disco con **FIFO, SSTF, SCAN y C-SCAN** como mínimo.
- **Sistema de Archivos Jerárquico:** Árbol de directorios visualizado en un `JTree`, con **asignación encadenada** y estructura propia (`NodoFS` con una `ListaSimple` de hijos). Solo se modela la estructura y los bloques; no el contenido de los archivos.
- **Modo Administrador y Modo Usuario:** Dueños y permisos sobre archivos y directorios.
- **Sincronización:** Se mantienen los `Semaphore` de Java, ahora también para la **cola de E/S del disco**, el **mapa de bloques** y el **árbol de archivos**, evitando colisiones entre el hilo del reloj y el hilo de Swing.
- **Interfaz Gráfica (GUI):** Interfaz en Java Swing con **un solo computador**. Permite crear procesos, observar colas, visualizar el cabezal del disco, navegar el árbol de archivos, leer un log acotado y cargar/guardar la configuración en JSON.
- **Métricas:** Cálculo de Throughput, Tiempo de Respuesta Promedio y Tiempo de Retorno Promedio, más gráfico de Utilización de CPU en tiempo real con JFreeChart.

🏗️ Arquitectura y Estructura de Paquetes
El proyecto mantiene el modelo de capas donde la capa superior conoce a la inferior, pero nunca al revés.

- **`com.mycompany.so_miguelangel_juan_dario` (Ejecuta):** Clase `main`. Punto de entrada al programa.
- **`hardware` (Obedece):** Clases `Computador`, `CPU`, `MemoriaPrincipal`, `RelojGlobal`, `Disco` y `Bloque`. No toman decisiones lógicas; el disco implementa la interfaz de componente que reacciona al reloj.
- **`sistemaOperativo` (Decide):** Clases `Nucleo`, `PCB`, `Semaforo`, `SistemaArchivos`, `SolicitudES`, `Metricas` y `Log`. Aquí vive el corazón lógico del simulador.
- **`planificacionSO` (Contratos):** Interfaz `PoliticaPlanificacion` (CPU), interfaz `PoliticaDisco` (FIFO, SSTF, SCAN, C-SCAN), `Criterio` y sus implementaciones.
- **`estructuraDeDatos` (Estructuras Propias):** Clases `Cola`, `ListaSimple`, `Nodo` y `ColaPrioridad`. En estricto apego a las reglas, **no se utilizan colecciones de `java.util`** (`ArrayList`, `Queue`, `Stack`, `Vector`, etc.).
- **`losEnums` (Conjuntos cerrados):** `EstadoProceso`, `ModoEjecucion`, `TipoProceso` y `MotivoBloqueo`.
- **`gui` (Observa):** Interfaces visuales (`Interfaz`, `ventanaDisco`, `ventanaArchivos`, `VentanaGrafico`). La GUI solo valida forma y muestra resultados; no calcula métricas ni construye PCB.

🌿 Ramas y Flujo de Trabajo
- Rama base: `develop`.
- Ramas por funcionalidad: `feat/disk`, `feat/filesystem`, `feat/swap`.
- Rama de correcciones del Proyecto 1: `fix/p1-base`, que se fusiona a `develop` antes de abrir las ramas nuevas.
- Se exigen **issues** y **PR comentados** para cada integración.

🧠 Decisiones de Diseño Destacadas
- **Hilos Reales vs. Procesos Simulados:** El programa cuenta con 3 hilos reales de Java (Hilo Principal, Hilo del Reloj y EDT de Swing). Los procesos (`PCB`) no son hilos de Java: son objetos gestionados por el planificador propio. Esto garantiza que se evalúen las políticas del proyecto y no las del sistema operativo anfitrión.
- **Semáforos de Java:** Se implementó `java.util.concurrent.Semaphore` dentro de las colas y estructuras compartidas para garantizar la exclusión mutua. Esto evita colisiones entre el hilo que actualiza el reloj y el hilo que pinta la interfaz gráfica.
- **Disco que Obedece:** El `Disco` es hardware y no conoce FIFO, SSTF, SCAN ni C-SCAN. En cada ciclo pregunta al SO hacia dónde mover el cabezal. La interfaz `PoliticaDisco` devuelve un **destino** (posición y solicitud si la hay), no solo la siguiente solicitud, porque SCAN y C-SCAN a veces deben llevar el cabezal a un extremo donde no hay nada.
- **Reevaluación del Disco:** Mientras el cabezal se mueve, el disco vuelve a preguntar el destino cada ciclo; solo la transferencia no se interrumpe. Esto permite que SCAN y C-SCAN coincidan también cuando las solicitudes llegan una por ciclo.
- **Enlace de Cola:** Para permitir que un `PCB` se mueva libremente entre las colas de Nuevos, Listos, Bloqueados y Suspendidos, el enlace (puntero siguiente) se maneja en el `Nodo`, dejando al `PCB` independiente de la estructura de la cola.
- **Prevención de Inanición:** En la admisión de memoria, si el primer proceso de la cola de nuevos no cabe en RAM, el sistema detiene el barrido. Esto evita que los procesos grandes mueran de inanición por procesos pequeños.
- **Métricas y Log en el SO:** El cálculo de throughput, tiempos promedios y utilización de CPU vive en `Metricas`; el log acotado con semáforo vive en `Log`. La GUI solo lee y pinta. Si se borra el paquete `gui`, ÁvilaOS debe poder ejecutarse desde un `main` de consola y mostrar sus métricas.
- **Dos JSON Obligatorios:** La configuración global y la configuración del disco/sistema de archivos se cargan por separado y son requisitos para habilitar el reloj. Esto reemplaza el antiguo `cargarJson` que solo llenaba el formulario con el primer proceso.

👥 Aporte de cada integrante al proyecto
- **Juan Moreira:** Disco, Bloque, las 4 políticas de disco, sus pruebas y la pestaña Disco.
- **Miguelangel Maíz:** PCB, Nucleo, procesos de E/S, planificador a mediano plazo, 7 estados y la pestaña Computador.
- **Dario Fernandez:** SistemaArchivos, permisos, JSON, semáforos nuevos y la pestaña Archivos.
- **En conjunto:** La interfaz gráfica de usuario.

---

Desarrollado para la asignatura de Sistemas Operativos - Universidad Metropolitana.
