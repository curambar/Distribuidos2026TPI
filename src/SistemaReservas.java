import java.util.HashMap;
import java.util.Map;

/**
 * Describe un Sistema de reservas para eventos.
 */
public class SistemaReservas {
    private final Map<Integer, Evento> eventos = new HashMap<>();
    private int contadorIds = 1;

    // Funciones de Admin
    public synchronized void agregarEvento(String nombre, int entradas) {
        eventos.put(contadorIds, new Evento(contadorIds, nombre, entradas));
        contadorIds++;
    }

    public synchronized boolean editarEvento(int id, String nuevoNombre, int nuevasEntradas) {
        Evento ev = eventos.get(id);
        if (ev != null) {
            ev.nombre = nuevoNombre;
            ev.entradasDisponibles = nuevasEntradas;
            System.out.println("[EDITAR] Evento " + id + " modificado.");
            return true;
        }
        return false;
    }

    public synchronized void eliminarEvento(int id) {
        if(eventos.remove(id) != null) {
            System.out.println("[ELIMINAR] Evento " + id + " eliminado.");
        }
    }

    // Funciones de Cliente
    public synchronized boolean comprarEntrada(int idEvento, String cliente, int cantidad) {
        Evento evento = eventos.get(idEvento);
        if(evento != null && evento.entradasDisponibles >= cantidad) {
            //Simula latencia
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                Thread.currentThread().interrupt();
            }
            evento.entradasDisponibles -= cantidad;
            System.out.println("[COMPRA] " + cliente + " compró " + cantidad + " de " + evento.nombre);
            return true;
        }
        System.out.println("[RECHAZO] " + cliente + " intentó comprar pero falló.");
        return false;
    }
    public synchronized String getEventosJSON() {
        // Generamos un JSON simple a mano para no usar librerías externas
        StringBuilder json = new StringBuilder("[");
        for (Evento ev : eventos.values()) {
            json.append(String.format("{\"id\":%d, \"nombre\":\"%s\", \"entradas\":%d},",
                    ev.id, ev.nombre, ev.entradasDisponibles));
        }
        if (json.length() > 1) json.setLength(json.length() - 1); // Quitar última coma
        json.append("]");
        return json.toString();
    }
}