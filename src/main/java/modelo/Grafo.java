package modelo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * La red de computadores modelada como un grafo G = (V, E)
 * no dirigido y ponderado.
 */
public class Grafo {

    private final List<Nodo> nodos = new ArrayList<>();
    private final List<Arista> aristas = new ArrayList<>();

    // ---------------- Vértices ----------------

    public Nodo agregarNodo(String nombre, double x, double y) {
        if (buscarNodo(nombre) != null) {
            throw new IllegalArgumentException("Ya existe un equipo llamado " + nombre);
        }
        Nodo n = new Nodo(nombre, x, y);
        nodos.add(n);
        return n;
    }

    public void eliminarNodo(Nodo n) {
        aristas.removeIf(a -> a.contiene(n));
        nodos.remove(n);
    }

    public Nodo buscarNodo(String nombre) {
        for (Nodo n : nodos) {
            if (n.getNombre().equalsIgnoreCase(nombre)) return n;
        }
        return null;
    }

    public List<Nodo> getNodos() { return Collections.unmodifiableList(nodos); }

    // ---------------- Aristas ----------------

    public void agregarArista(Nodo a, Nodo b, int peso) {
        if (a == b) throw new IllegalArgumentException("No se permiten lazos (un nodo consigo mismo).");
        if (peso <= 0) throw new IllegalArgumentException("El peso debe ser mayor que 0.");
        Arista existente = buscarArista(a, b);
        if (existente != null) {
            existente.setPeso(peso);   // si ya existe, se actualiza el peso
        } else {
            aristas.add(new Arista(a, b, peso));
        }
    }

    public void eliminarArista(Nodo a, Nodo b) {
        aristas.removeIf(ar -> ar.une(a, b));
    }

    public Arista buscarArista(Nodo a, Nodo b) {
        for (Arista ar : aristas) {
            if (ar.une(a, b)) return ar;
        }
        return null;
    }

    public List<Arista> getAristas() { return Collections.unmodifiableList(aristas); }

    // ---------------- Propiedades ----------------

    /** Nodos adyacentes a n. */
    public List<Nodo> vecinos(Nodo n) {
        List<Nodo> v = new ArrayList<>();
        for (Arista ar : aristas) {
            if (ar.contiene(n)) v.add(ar.otro(n));
        }
        return v;
    }

    /** Grado de un nodo: número de aristas que inciden en él. */
    public int grado(Nodo n) { return vecinos(n).size(); }

    /**
     * Matriz de adyacencia: M[i][j] = 1 si hay arista entre el nodo i y el j.
     * El orden de filas y columnas es el de getNodos().
     */
    public int[][] matrizAdyacencia() {
        int k = nodos.size();
        int[][] m = new int[k][k];
        for (Arista ar : aristas) {
            int i = nodos.indexOf(ar.getA());
            int j = nodos.indexOf(ar.getB());
            m[i][j] = 1;
            m[j][i] = 1;
        }
        return m;
    }

    /** Un grafo es conexo si desde cualquier nodo se alcanzan todos los demás (recorrido BFS). */
    public boolean esConexo() {
        if (nodos.isEmpty()) return true;
        Set<Nodo> visitados = new HashSet<>();
        Deque<Nodo> cola = new ArrayDeque<>();
        cola.add(nodos.get(0));
        visitados.add(nodos.get(0));
        while (!cola.isEmpty()) {
            Nodo actual = cola.poll();
            for (Nodo v : vecinos(actual)) {
                if (visitados.add(v)) cola.add(v);
            }
        }
        return visitados.size() == nodos.size();
    }

    /** Suma de grados = 2|E| (teorema del apretón de manos). */
    public int sumaGrados() {
        int s = 0;
        for (Nodo n : nodos) s += grado(n);
        return s;
    }

    public void limpiar() {
        aristas.clear();
        nodos.clear();
    }
}
