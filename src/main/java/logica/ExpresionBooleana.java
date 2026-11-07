package logica;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Expresión del álgebra de Boole con variables de una letra (A–Z).
 *
 * Operadores, de mayor a menor precedencia:
 *   A'  !A  ¬A   negación (NOT)
 *   A·B  A*B  A&B  AB   conjunción (AND)
 *   A⊕B  A^B         o exclusivo (XOR)
 *   A+B  A|B         disyunción (OR)
 * También se aceptan las constantes 0 y 1 y los paréntesis.
 */
public class ExpresionBooleana {

    /** Nodo del árbol de la expresión. */
    private interface Nodo {
        boolean evaluar(Map<Character, Boolean> valores);
    }

    private final String texto;
    private final Nodo raiz;
    private final List<Character> variables;

    // Estado del analizador
    private String s;
    private int pos;
    private final TreeSet<Character> vistas = new TreeSet<>();

    public ExpresionBooleana(String texto) {
        this.texto = texto.trim();
        this.s = this.texto.replace(" ", "").toUpperCase();
        if (s.isEmpty()) throw new IllegalArgumentException("Escribe una expresión.");
        this.pos = 0;
        this.raiz = disyuncion();
        if (pos < s.length()) {
            throw new IllegalArgumentException("Símbolo inesperado '" + s.charAt(pos) + "' en la posición " + (pos + 1) + ".");
        }
        this.variables = new ArrayList<>(vistas);
    }

    public String getTexto() { return texto; }

    public List<Character> getVariables() { return variables; }

    public boolean evaluar(Map<Character, Boolean> valores) { return raiz.evaluar(valores); }

    // ---------------- Analizador (descenso recursivo) ----------------

    private char actual() { return pos < s.length() ? s.charAt(pos) : '\0'; }

    private Nodo disyuncion() {
        Nodo izq = xor();
        while (actual() == '+' || actual() == '|') {
            pos++;
            Nodo a = izq, b = xor();
            izq = v -> a.evaluar(v) | b.evaluar(v);
        }
        return izq;
    }

    private Nodo xor() {
        Nodo izq = conjuncion();
        while (actual() == '^' || actual() == '⊕') {
            pos++;
            Nodo a = izq, b = conjuncion();
            izq = v -> a.evaluar(v) ^ b.evaluar(v);
        }
        return izq;
    }

    private Nodo conjuncion() {
        Nodo izq = unario();
        while (true) {
            char c = actual();
            if (c == '·' || c == '*' || c == '&' || c == '∧') {
                pos++;
            } else if (!iniciaFactor(c)) {
                break;          // si no hay operador ni factor, termina el producto
            }
            Nodo a = izq, b = unario();
            izq = v -> a.evaluar(v) & b.evaluar(v);
        }
        return izq;
    }

    /** Un factor puede empezar con letra, constante, paréntesis o negación prefija (AND implícito: AB). */
    private boolean iniciaFactor(char c) {
        return Character.isLetter(c) || c == '0' || c == '1' || c == '(' || c == '!' || c == '¬';
    }

    private Nodo unario() {
        if (actual() == '!' || actual() == '¬') {
            pos++;
            Nodo a = unario();
            return v -> !a.evaluar(v);
        }
        Nodo n = primario();
        while (actual() == '\'' || actual() == '’') {   // negación posfija: A'
            pos++;
            Nodo a = n;
            n = v -> !a.evaluar(v);
        }
        return n;
    }

    private Nodo primario() {
        char c = actual();
        if (c == '(') {
            pos++;
            Nodo n = disyuncion();
            if (actual() != ')') throw new IllegalArgumentException("Falta cerrar un paréntesis.");
            pos++;
            return n;
        }
        if (c == '0' || c == '1') {
            pos++;
            boolean val = c == '1';
            return v -> val;
        }
        if (Character.isLetter(c) && c >= 'A' && c <= 'Z') {
            pos++;
            vistas.add(c);
            return v -> v.getOrDefault(c, false);
        }
        if (c == '\0') throw new IllegalArgumentException("La expresión termina de forma incompleta.");
        throw new IllegalArgumentException("Símbolo no válido '" + c + "'.");
    }

    // ---------------- Tabla de verdad ----------------

    /** Valores de las variables para la fila i (la primera variable es el bit más significativo). */
    public static Map<Character, Boolean> fila(List<Character> vars, int i) {
        Map<Character, Boolean> m = new TreeMap<>();
        int k = vars.size();
        for (int j = 0; j < k; j++) {
            m.put(vars.get(j), ((i >> (k - 1 - j)) & 1) == 1);
        }
        return m;
    }

    /** Índices de las filas donde la expresión vale 1 (minitérminos). */
    public List<Integer> minterminos() {
        List<Integer> r = new ArrayList<>();
        int filas = 1 << variables.size();
        for (int i = 0; i < filas; i++) {
            if (evaluar(fila(variables, i))) r.add(i);
        }
        return r;
    }

    /** Forma canónica como suma de productos (minitérminos). */
    public String sumaDeProductos() {
        List<Integer> m = minterminos();
        if (m.isEmpty()) return "0";
        if (m.size() == (1 << variables.size())) return "1";
        List<String> terminos = new ArrayList<>();
        for (int i : m) {
            StringBuilder t = new StringBuilder();
            Map<Character, Boolean> f = fila(variables, i);
            for (char v : variables) t.append(v).append(f.get(v) ? "" : "'");
            terminos.add(t.toString());
        }
        return String.join(" + ", terminos);
    }

    /** Dos expresiones son equivalentes si coinciden en todas las filas de la tabla de verdad. */
    public static boolean equivalentes(ExpresionBooleana e1, ExpresionBooleana e2) {
        TreeSet<Character> todas = new TreeSet<>(e1.variables);
        todas.addAll(e2.variables);
        List<Character> vars = new ArrayList<>(todas);
        for (int i = 0; i < (1 << vars.size()); i++) {
            Map<Character, Boolean> f = fila(vars, i);
            if (e1.evaluar(f) != e2.evaluar(f)) return false;
        }
        return true;
    }
}
