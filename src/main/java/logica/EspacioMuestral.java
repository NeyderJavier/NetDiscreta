package logica;

import java.util.ArrayList;
import java.util.List;

/**
 * Espacio muestral Ω de enviar n bits: cada bit llega Correcto (C) o con Error (E).
 * Ω = {C, E}ⁿ y |Ω| = 2ⁿ.
 */
public class EspacioMuestral {

    /** Un resultado del experimento, por ejemplo "CEC". */
    public static class Resultado {
        public final String valor;
        public final int errores;        // X(ω): número de E
        public final double probabilidad; // pˣ · (1 − p)ⁿ⁻ˣ

        Resultado(String valor, int errores, double probabilidad) {
            this.valor = valor;
            this.errores = errores;
            this.probabilidad = probabilidad;
        }
    }

    public static long tamano(int n) { return 1L << n; }

    /** Lista todos los resultados de Ω con su valor de X y su probabilidad. */
    public static List<Resultado> generar(int n, double p) {
        List<Resultado> r = new ArrayList<>();
        java.util.Set<Character> ce = new java.util.LinkedHashSet<>(List.of('C', 'E'));
        for (String w : Cadenas.sigmaN(ce, n)) {
            int x = Cadenas.contar(w, 'E');
            r.add(new Resultado(w, x, Math.pow(p, x) * Math.pow(1 - p, n - x)));
        }
        return r;
    }
}
