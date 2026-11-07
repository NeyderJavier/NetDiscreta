package modelo;

/**
 * Conexión entre dos equipos (arista no dirigida con peso).
 * El peso representa la latencia del enlace en milisegundos.
 */
public class Arista {

    private final Nodo a;
    private final Nodo b;
    private int peso;

    public Arista(Nodo a, Nodo b, int peso) {
        this.a = a;
        this.b = b;
        this.peso = peso;
    }

    public Nodo getA() { return a; }
    public Nodo getB() { return b; }
    public int getPeso() { return peso; }
    public void setPeso(int peso) { this.peso = peso; }

    /** Indica si la arista toca al nodo dado. */
    public boolean contiene(Nodo n) { return a == n || b == n; }

    /** Indica si la arista une exactamente a estos dos nodos (en cualquier orden). */
    public boolean une(Nodo x, Nodo y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    /** Devuelve el extremo opuesto a n. */
    public Nodo otro(Nodo n) { return n == a ? b : a; }

    @Override
    public String toString() { return a + " — " + b + " (" + peso + " ms)"; }
}
