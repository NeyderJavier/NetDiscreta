package modelo;

/**
 * Un equipo de la red (vértice del grafo).
 * Guarda una posición (x, y) para poder dibujarlo.
 */
public class Nodo {

    private final String nombre;
    private double x;
    private double y;

    public Nodo(String nombre, double x, double y) {
        this.nombre = nombre;
        this.x = x;
        this.y = y;
    }

    public String getNombre() { return nombre; }
    public double getX() { return x; }
    public double getY() { return y; }

    public void setPosicion(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() { return nombre; }
}
