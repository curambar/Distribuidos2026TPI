/**
 * Representa un evento o función.
 */
public class Evento {
    public int id;
    public String nombre;
    public int entradasDisponibles;

    public Evento(int id, String nombre, int entradasDisponibles) {
        this.id = id;
        this.nombre = nombre;
        this.entradasDisponibles = entradasDisponibles;
    }
}