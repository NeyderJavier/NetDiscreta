package logica;

import java.util.ArrayList;
import java.util.List;
import modelo.Conjunto;
import modelo.Grafo;
import modelo.Nodo;

/**
 * Producto cartesiano A × B = {(a, b) | a ∈ A ∧ b ∈ B}
 * y la relación R ⊆ A × B "tiene enlace directo en la red".
 */
public class ProductoCartesiano {

    /** Un par ordenado (a, b). */
    public static class Par {
        public final Nodo a;
        public final Nodo b;
        public final boolean enRelacion;   // true si (a, b) ∈ R

        Par(Nodo a, Nodo b, boolean enRelacion) {
            this.a = a;
            this.b = b;
            this.enRelacion = enRelacion;
        }

        @Override
        public String toString() { return "(" + a + ", " + b + ")"; }
    }

    /** Genera todos los pares de A × B, marcando los que están en R. */
    public static List<Par> generar(Conjunto a, Conjunto b, Grafo red) {
        List<Par> pares = new ArrayList<>();
        for (Nodo x : a.getElementos()) {
            for (Nodo y : b.getElementos()) {
                boolean enlace = x != y && red.buscarArista(x, y) != null;
                pares.add(new Par(x, y, enlace));
            }
        }
        return pares;
    }

    /** Solo los pares que pertenecen a la relación R. */
    public static List<Par> relacion(List<Par> pares) {
        List<Par> r = new ArrayList<>();
        for (Par p : pares) if (p.enRelacion) r.add(p);
        return r;
    }
}
