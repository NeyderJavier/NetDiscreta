package logica;

import java.util.LinkedHashSet;
import java.util.Set;
import modelo.Conjunto;
import modelo.Nodo;

/**
 * Operaciones entre conjuntos de equipos.
 */
public class OperacionesConjuntos {

    /** Las operaciones disponibles, con su símbolo y definición. */
    public enum Operacion {
        UNION("A ∪ B", "Unión", "{x | x ∈ A ∨ x ∈ B}"),
        INTERSECCION("A ∩ B", "Intersección", "{x | x ∈ A ∧ x ∈ B}"),
        DIFERENCIA_AB("A − B", "Diferencia A − B", "{x | x ∈ A ∧ x ∉ B}"),
        DIFERENCIA_BA("B − A", "Diferencia B − A", "{x | x ∈ B ∧ x ∉ A}"),
        SIMETRICA("A Δ B", "Diferencia simétrica", "(A − B) ∪ (B − A)"),
        COMPLEMENTO_A("A'", "Complemento de A", "{x ∈ U | x ∉ A}"),
        COMPLEMENTO_B("B'", "Complemento de B", "{x ∈ U | x ∉ B}");

        public final String simbolo;
        public final String nombre;
        public final String definicion;

        Operacion(String simbolo, String nombre, String definicion) {
            this.simbolo = simbolo;
            this.nombre = nombre;
            this.definicion = definicion;
        }

        @Override
        public String toString() { return simbolo + "  (" + nombre + ")"; }
    }

    public static Conjunto aplicar(Operacion op, Conjunto a, Conjunto b, Conjunto u) {
        return switch (op) {
            case UNION -> union(a, b);
            case INTERSECCION -> interseccion(a, b);
            case DIFERENCIA_AB -> diferencia(a, b);
            case DIFERENCIA_BA -> diferencia(b, a);
            case SIMETRICA -> diferenciaSimetrica(a, b);
            case COMPLEMENTO_A -> complemento(a, u);
            case COMPLEMENTO_B -> complemento(b, u);
        };
    }

    public static Conjunto union(Conjunto a, Conjunto b) {
        Set<Nodo> r = new LinkedHashSet<>(a.getElementos());
        r.addAll(b.getElementos());
        return new Conjunto(a + " ∪ " + b, r);
    }

    public static Conjunto interseccion(Conjunto a, Conjunto b) {
        Set<Nodo> r = new LinkedHashSet<>(a.getElementos());
        r.retainAll(b.getElementos());
        return new Conjunto(a + " ∩ " + b, r);
    }

    public static Conjunto diferencia(Conjunto a, Conjunto b) {
        Set<Nodo> r = new LinkedHashSet<>(a.getElementos());
        r.removeAll(b.getElementos());
        return new Conjunto(a + " − " + b, r);
    }

    public static Conjunto diferenciaSimetrica(Conjunto a, Conjunto b) {
        Set<Nodo> r = new LinkedHashSet<>(diferencia(a, b).getElementos());
        r.addAll(diferencia(b, a).getElementos());
        return new Conjunto(a + " Δ " + b, r);
    }

    public static Conjunto complemento(Conjunto a, Conjunto u) {
        Set<Nodo> r = new LinkedHashSet<>(u.getElementos());
        r.removeAll(a.getElementos());
        return new Conjunto(a + "'", r);
    }

    /** A ⊆ B: todo elemento de A está en B. */
    public static boolean esSubconjunto(Conjunto a, Conjunto b) {
        return b.getElementos().containsAll(a.getElementos());
    }

    /** A y B son disjuntos si A ∩ B = ∅. */
    public static boolean sonDisjuntos(Conjunto a, Conjunto b) {
        return interseccion(a, b).esVacio();
    }
}
