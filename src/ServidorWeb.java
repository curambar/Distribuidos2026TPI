import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;

public class ServidorWeb {
    public static void main(String[] args) throws IOException {
        SistemaReservas sistema = new SistemaReservas();
        sistema.agregarEvento("Concierto Rock UNRN", 50); // Evento de prueba

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Endpoint GET: Obtener todos los eventos
        server.createContext("/api/eventos", exchange -> {
            agregarCORS(exchange);
            String json = sistema.getEventosJSON();
            enviarRespuesta(exchange, 200, json);
        });

        // Endpoint POST: Comprar entrada
        server.createContext("/api/comprar", exchange -> {
            agregarCORS(exchange);

            // 1. Atajamos el pre-flight de CORS correctamente
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                enviarRespuesta(exchange, 200, "OK");
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                String idStr = extraerParametro(query, "id");
                String cantStr = extraerParametro(query, "cant");
                String cliente = extraerParametro(query, "cliente");

                // Evitamos NullPointerExceptions si el dato llega vacío
                int id = Integer.parseInt(idStr.isEmpty() ? "0" : idStr);
                int cant = Integer.parseInt(cantStr.isEmpty() ? "0" : cantStr);

                boolean exito = sistema.comprarEntrada(id, cliente, cant);
                enviarRespuesta(exchange, exito ? 200 : 400, exito ? "Exito" : "Sin stock");
            } catch (Exception e) {
                System.err.println("Error en la solicitud: " + e.getMessage());
                enviarRespuesta(exchange, 500, "Error Interno del Servidor");
            }
        });

        // Endpoint POST: Agregar Evento
        server.createContext("/api/admin/agregar", exchange -> {
            agregarCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                enviarRespuesta(exchange, 200, "OK");
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                String nombre = extraerParametro(query, "nombre").replace("%20", " ");
                String entradasStr = extraerParametro(query, "entradas");
                int entradas = Integer.parseInt(entradasStr.isEmpty() ? "0" : entradasStr);

                sistema.agregarEvento(nombre, entradas);
                enviarRespuesta(exchange, 200, "Evento Agregado");
            } catch (Exception e) {
                System.err.println("Error en la solicitud: " + e.getMessage());
                enviarRespuesta(exchange, 500, "Error Interno del Servidor");
            }
        });

        // Endpoint POST: Eliminar Evento
        server.createContext("/api/admin/eliminar", exchange -> {
            agregarCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { enviarRespuesta(exchange, 200, "OK"); return; }
            try {
                int id = Integer.parseInt(extraerParametro(exchange.getRequestURI().getQuery(), "id"));
                sistema.eliminarEvento(id);
                enviarRespuesta(exchange, 200, "Evento Eliminado");
            } catch (Exception e) { enviarRespuesta(exchange, 500, "Error"); }
        });

        // Endpoint POST: Editar Evento
        server.createContext("/api/admin/editar", exchange -> {
            agregarCORS(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { enviarRespuesta(exchange, 200, "OK"); return; }
            try {
                String query = exchange.getRequestURI().getQuery();
                int id = Integer.parseInt(extraerParametro(query, "id"));
                String nombre = extraerParametro(query, "nombre").replace("%20", " ");
                int entradas = Integer.parseInt(extraerParametro(query, "entradas"));

                boolean exito = sistema.editarEvento(id, nombre, entradas);
                if (exito) {
                    enviarRespuesta(exchange, 200, "Evento Editado");
                } else {
                    enviarRespuesta(exchange, 404, "Evento no encontrado");
                }

            } catch (Exception e) {
                System.err.println("Error al editar: " + e.getMessage());
                enviarRespuesta(exchange, 500, "Error");
            }
        });

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("🚀 Servidor Java corriendo en http://localhost:8080");
    }

    private static void enviarRespuesta(HttpExchange exchange, int code, String response) throws IOException {
        // 2. Consumimos la petición entrante para liberar el socket
        InputStream is = exchange.getRequestBody();
        while(is.read() != -1) {
            // Consumimos el cuerpo de la petición para liberar el socket.
        }
        is.close();

        // 3. Forzamos codificación UTF-8 para que los bytes cuadren exacto y evitemos el Error 400
        byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static void agregarCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    // Método blindado contra posibles strings vacíos o mal formados
    private static String extraerParametro(String query, String param) {
        if (query == null) return "";
        for (String par : query.split("&")) {
            String[] split = par.split("=");
            if (split.length == 2 && split[0].equals(param)) {
                return split[1].trim();
            }
        }
        return "";
    }
}