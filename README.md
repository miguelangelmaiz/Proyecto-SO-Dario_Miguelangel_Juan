ÁvilaOS - Simulador de Sistema Operativo Distribuido

Proyecto 1 - Sistemas Operativos (Trimestre 2627-1)

ÁvilaOS es un simulador de sistema operativo distribuido desarrollado en Java. El sistema modela la gestión de procesos, la planificación de la CPU bajo múltiples políticas y la sincronización mediante semáforos (productor-consumidor) en un clúster de computadoras, manteniendo un estricto diseño por capas.

## 👥 Integrantes
*   Juan Moreira
*   Miguel Angel Maíz
*   Dario Fernandez

## ⚙️ Requisitos del Sistema
*   **Java JDK 21** o superior.
*   **Apache NetBeans** (estrictamente requerido por la cátedra).
*   **Maven** para la gestión de dependencias.
*   *Librerías externas utilizadas:* `JFreeChart` (gráficos) y `Gson` (JSON).

## 🚀 ¿Cómo compilar y ejecutar?
1. Clona este repositorio en tu máquina local.
2. Abre el proyecto en **NetBeans**.
3. NetBeans descargará automáticamente las dependencias declaradas en el `pom.xml` (JFreeChart y Gson).
4. Dirígete al paquete `com.mycompany.so_miguelangel_juan_dario` y ejecuta la clase **`main.java`**.
5. La interfaz gráfica (Vista 1 y Vista 2) se abrirá automáticamente.

**💡 Nota para probar Productores/Consumidores:**
Si deseas crear procesos de tipo `PRODUCTOR` o `CONSUMIDOR`, primero debes hacer clic en el botón **"BUFFER"** en la interfaz principal y crear al menos un buffer en el computador deseado.

## 🧩 Características Principales

*   **Gestión de Procesos:** Modelado a través de la clase `PCB` (Bloque de Control de Proceso). Implementa un modelo de 5 estados (Nuevo, Listo, Ejecución, Bloqueado, Terminado) con muerte por vencimiento de *Deadline*.
*   **Planificación de CPU (Corto Plazo):** 4 políticas implementadas mediante el patrón Strategy (interfaz `PoliticaPlanificacion`):
    *   FCFS (First-Come, First-Served)
    *   Round Robin (Quantum configurable en tiempo real)
    *   EDF (Earliest Deadline First)
    *   Prioridades Apropiativas
*   **Planificación a Largo Plazo:** Admisión de procesos basada en la disponibilidad de Memoria Principal (RAM).
*   **Sincronización (Productor-Consumidor):** Algoritmo de buffer acotado de Stallings implementado con semáforos propios (Mutex, Vacíos, Llenos) y soporte para múltiples productores/consumidores.
*   **Clúster Distribuido:** Soporta múltiples computadores (mínimo 2) con soporte para *acceso remoto a buffers* y penalización por latencia de red.
*   **Interfaz Gráfica (GUI):** Interfaz en Java Swing que permite crear procesos, observar las colas en tiempo real, visualizar los semáforos, leer un log de eventos y guardar/cargar la configuración del sistema en archivos JSON.
*   **Métricas:** Cálculo de Throughput, Tiempo de Respuesta Promedio y gráfico de Utilización de CPU en tiempo real con JFreeChart.

## 🏗️ Arquitectura y Estructura de Paquetes

El proyecto está diseñado bajo un modelo de capas donde la capa superior conoce a la inferior, pero nunca al revés.

*   **`com.mycompany.so_miguelangel_juan_dario` (Ejecuta):** Clase `main`. El punto de entrada al programa.
*   **`hardware` (Obedece):** Clases `Computador`, `CPU`, `MemoriaPrincipal` y `RelojGlobal`. No toman decisiones lógicas.
*   **`sistemaOperativo` (Decide):** Clases `Cluster`, `Nucleo`, `PCB`, `Semaforo`, `Buffer` y `GestorBuffers`. Aquí vive el corazón lógico del simulador.
*   **`planificacionSO` (Contratos):** Interfaz `PoliticaPlanificacion`, `Criterio` y sus implementaciones (FCFS, RoundRobin, EDF, PrioridadApropiativa).
*   **`estructuraDeDatos` (Estructuras Propias):** Clases `Cola`, `ListaSimple`, `Nodo` y `ColaPrioridad`. **Nota:** En estricto apego a las reglas, no se utilizaron colecciones de `java.util`.
*   **`losEnums` (Conjuntos cerrados):** `EstadoProceso`, `ModoEjecucion`, `TipoProceso` y `MotivoBloqueo`.
*   **`gui` (Observa):** Interfaces visuales (`Interfaz`, `ventanaBuffer`, `VentanaGrafico`).

## 🧠 Decisiones de Diseño Destacadas

*   **Hilos Reales vs. Procesos Simulados:** El programa cuenta con 3 hilos reales de Java (Hilo Principal, Hilo del Reloj y EDT de Swing). Los procesos (`PCB`) **no son hilos de Java**, son objetos gestionados por el planificador propio. Esto garantiza que se evalúen las políticas del proyecto y no las del sistema operativo anfitrión.
*   **Semáforos de Java:** Se implementó `java.util.concurrent.Semaphore` dentro de la clase `Cola` para garantizar la exclusión mutua. Esto evita colisiones (como `ConcurrentModificationException`) entre el hilo que actualiza el reloj y el hilo que pinta la interfaz gráfica.
*   **Enlace de Cola:** Para permitir que un `PCB` se mueva libremente entre las colas de Nuevos, Listos y Bloqueados, el enlace (puntero siguiente) se maneja en
  el `Nodo`, dejando al `PCB` independiente de la estructura de la cola.
*   **Prevención de Inanición:** En la admisión de memoria, si el primer proceso de la cola de nuevos no cabe en RAM, el sistema detiene el barrido. Esto evita que los procesos grandes mueran de inanición por procesos pequeños, priorizando la integridad de la memoria.
  
## 👥 Aporte cada integrante al proyecto

* **Juan Moreira:** Primitivas, Hardware y enums
* **Miguelangel Maíz:** Primitivas, SO y enums
* **Dario Fernandez:** Semaforo y Buffers.
* **En conjunto:** La interfaz Grafica de Usuario
  
---
*Desarrollado para la asignatura de Sistemas Operativos - Universidad Metropolitana.*
