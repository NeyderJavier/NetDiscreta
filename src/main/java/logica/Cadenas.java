package logica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Operaciones con alfabetos y cadenas.
 * Un alfabeto Σ es un conjunto finito de símbolos (aquí, caracteres)
 * y una cadena es una secuencia finita de símbolos de Σ.
 */
public class Cadenas {

    /** Cadena vacía λ. */
    public static final String LAMBDA = "λ";

    /** Convierte un texto como "0,1" o "a, b, c" en el alfabeto {0, 1}. */
    public static Set<Character> leerAlfabeto(String texto) {
        Set<Character> sigma = new LinkedHashSet<>();
        for (String parte : texto.split(",")) {
            String s = parte.trim();
            if (s.length() != 1) {
                throw new IllegalArgumentException("Cada símbolo del alfabeto debe ser un solo carácter: \"" + s + "\"");
            }
            sigma.add(s.charAt(0));
        }
        if (sigma.isEmpty()) throw new IllegalArgumentException("El alfabeto no puede ser vacío.");
        return sigma;
    }

    public static String mostrarAlfabeto(Set<Character> sigma) {
        List<String> s = new ArrayList<>();
        for (char c : sigma) s.add(String.valueOf(c));
        return "Σ = {" + String.join(", ", s) + "}";
    }

    /** w ∈ Σ*: todos sus símbolos pertenecen a Σ. */
    public static boolean esCadenaSobre(String w, Set<Character> sigma) {
        for (char c : w.toCharArray()) {
            if (!sigma.contains(c)) return false;
        }
        return true;
    }

    /** Primer símbolo que no pertenece al alfabeto, o null si todos pertenecen. */
    public static Character simboloInvalido(String w, Set<Character> sigma) {
        for (char c : w.toCharArray()) {
            if (!sigma.contains(c)) return c;
        }
        return null;
    }

    public static String mostrar(String w) { return w.isEmpty() ? LAMBDA : w; }

    /** |w| */
    public static int longitud(String w) { return w.length(); }

    /** wᴿ: la cadena al revés. */
    public static String inversa(String w) { return new StringBuilder(w).reverse().toString(); }

    /** w·v */
    public static String concatenar(String w, String v) { return w + v; }

    /** wⁿ = w concatenada n veces (w⁰ = λ). */
    public static String potencia(String w, int n) { return w.repeat(Math.max(0, n)); }

    /** Número de apariciones del símbolo c en w. */
    public static int contar(String w, char c) {
        int k = 0;
        for (char x : w.toCharArray()) if (x == c) k++;
        return k;
    }

    public static boolean esPalindromo(String w) { return w.equals(inversa(w)); }

    /** Todos los prefijos de w, desde λ hasta w. */
    public static List<String> prefijos(String w) {
        List<String> r = new ArrayList<>();
        for (int i = 0; i <= w.length(); i++) r.add(mostrar(w.substring(0, i)));
        return r;
    }

    /** Todos los sufijos de w, desde w hasta λ. */
    public static List<String> sufijos(String w) {
        List<String> r = new ArrayList<>();
        for (int i = 0; i <= w.length(); i++) r.add(mostrar(w.substring(i)));
        return r;
    }

    /** Σⁿ: todas las cadenas de longitud n sobre Σ, en orden. |Σⁿ| = |Σ|ⁿ. */
    public static List<String> sigmaN(Set<Character> sigma, int n) {
        List<String> actual = new ArrayList<>(Collections.singletonList(""));
        for (int i = 0; i < n; i++) {
            List<String> siguiente = new ArrayList<>();
            for (String w : actual) {
                for (char c : sigma) siguiente.add(w + c);
            }
            actual = siguiente;
        }
        return actual;
    }
}
