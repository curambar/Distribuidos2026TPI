# Sistema de Reserva de Entradas Concurrente - TP Final Integrador

Este repositorio contiene el código fuente correspondiente al Trabajo Práctico Final Integrador de la asignatura Sistemas Distribuidos y Paralelos de la Universidad Nacional de Río Negro (UNRN).

## Descripción del Proyecto

El proyecto consiste en una aplicación web con arquitectura Cliente-Servidor diseñada para demostrar el manejo de la concurrencia y la protección de regiones críticas. 

El sistema modela una plataforma de venta de entradas para eventos, enfrentando el problema clásico de la "sobreventa" (overselling). Cuando múltiples usuarios intentan adquirir los mismos tickets de forma simultánea a través de la red, se genera una potencial condición de carrera (Race Condition) que podría resultar en un inventario negativo o inconsistente.

### Solución Teórica Implementada

Para resolver este problema, la lógica de negocio se centralizó en un Monitor dentro del servidor. Se utilizó la primitiva de sincronización nativa de Java (`synchronized`) para aislar la lectura y modificación de las entradas disponibles. Esto garantiza la exclusión mutua, asegurando que si el servidor recibe múltiples peticiones HTTP concurrentes (despachadas en distintos hilos), el acceso a la región crítica se realice de manera secuencial y atómica.

## Arquitectura

El proyecto está completamente desacoplado y no utiliza frameworks pesados, minimizando las dependencias:

* **Backend (API REST):** Desarrollado en Java puro. Utiliza la librería nativa `com.sun.net.httpserver.HttpServer` para levantar un servidor web asíncrono que asigna dinámicamente un hilo (Thread) del pool a cada petición HTTP entrante. Escucha en el puerto 8080.
* **Frontend:** Construido con HTML, CSS (Bootstrap 5 vía CDN) y Vanilla JavaScript. Consume los datos del servidor utilizando la API Fetch.

## Instrucciones de Ejecución

Para evaluar el sistema localmente, siga los siguientes pasos:

### 1. Iniciar el Servidor (Backend)
1. Abra el proyecto en su Entorno de Desarrollo Integrado (IDE) preferido para Java (IntelliJ IDEA, Eclipse, etc.).
2. Localice y ejecute la clase principal: `ServidorWeb.java`.
3. Aguarde a que la consola del IDE confirme que el servidor está en línea y escuchando en el puerto 8080. 
*(Nota: El servidor actúa únicamente como API de datos. Si ingresa a http://localhost:8080 desde el navegador, recibirá un error 404 esperado, ya que la interfaz gráfica se encuentra separada).*

### 2. Iniciar la Interfaz Gráfica (Frontend)
1. Localice el archivo `index.html` provisto en el directorio del proyecto.
2. Ábralo directamente haciendo doble clic sobre el archivo, lo cual iniciará la aplicación en su navegador web predeterminado (Google Chrome, Firefox, Edge). No es necesario montar un servidor local Apache o Nginx para el frontend.

## Guía de Uso y Demostración

La interfaz cuenta con dos modos principales:

* **Modo Cliente:** Vista predeterminada. Permite a los usuarios consultar los eventos vigentes y ejecutar solicitudes de compra.
* **Modo Administrador:** Accesible desde la pestaña superior. (Contraseña de acceso: **admin123**). Habilita un panel para realizar Altas, Bajas y Modificaciones (ABM) sobre el catálogo de eventos en tiempo real.

### Prueba de Concurrencia
Para verificar el correcto funcionamiento de los bloqueos y la integridad de los datos, se sugiere la siguiente prueba:
1. Abrir el archivo `index.html` en dos o más ventanas separadas del navegador y posicionarlas lado a lado.
2. Identificar un evento que cuente con un número muy reducido de entradas disponibles (por ejemplo, 1 entrada).
3. Intentar realizar la compra simultáneamente desde las distintas ventanas.
4. El sistema garantizará que solo la primera petición que logre adquirir el cerrojo del monitor podrá completar la transacción, rechazando las peticiones posteriores y evitando inconsistencias en la base de datos temporal.
