package logica;

/**
 * Un lenguaje L ⊆ Σ* definido por una propiedad.
 * Cada lenguaje sabe decir si una cadena le pertenece y por qué.
 */
public abstract class Lenguaje {

    /** Resultado de validar una cadena. */
    public static class Veredicto {
        public final boolean pertenece;
        public final String explicacion;

        public Veredicto(boolean pertenece, String explicacion) {
            this.pertenece = pertenece;
            this.explicacion = explicacion;
        }
    }

    public abstract String nombre();

    /** Definición en notación de conjuntos. */
    public abstract String definicion();

    public abstract Veredicto validar(String w);

    @Override
    public String toString() { return nombre(); }

    // ---------------- Lenguajes del proyecto ----------------

    /** L = {w ∈ Σ* | |w| = n}: mensajes de tamaño fijo. */
    public static Lenguaje longitudFija(int n) {
        return new Lenguaje() {
            public String nombre() { return "Longitud fija (|w| = " + n + ")"; }
            public String definicion() { return "L = {w ∈ Σ* | |w| = " + n + "}"; }
            public Veredicto validar(String w) {
                int k = w.length();
                return new Veredicto(k == n, "|w| = " + k + (k == n ? " = " : " ≠ ") + n);
            }
        };
    }

    /** L = {w ∈ {0,1}* | w tiene un número par de unos}: paridad par. */
    public static Lenguaje paridadPar() {
        return new Lenguaje() {
            public String nombre() { return "Paridad par (cantidad par de 1)"; }
            public String definicion() { return "L = {w ∈ Σ* | la cantidad de 1 en w es par}"; }
            public Veredicto validar(String w) {
                int unos = Cadenas.contar(w, '1');
                boolean par = unos % 2 == 0;
                return new Veredicto(par, "w tiene " + unos + " unos → " + (par ? "par" : "impar"));
            }
        };
    }

    /** L = {w ∈ Σ* | w empieza con p y termina con s}: cabecera y cola del paquete. */
    public static Lenguaje cabeceraYCola(String p, String s) {
        return new Lenguaje() {
            public String nombre() { return "Empieza con \"" + p + "\" y termina con \"" + s + "\""; }
            public String definicion() { return "L = {w ∈ Σ* | w = " + p + "·x·" + s + ", x ∈ Σ*}"; }
            public Veredicto validar(String w) {
                boolean ini = w.startsWith(p);
                boolean fin = w.endsWith(s);
                boolean ok = ini && fin && w.length() >= p.length() + s.length();
                return new Veredicto(ok, "¿Empieza con " + p + "? " + (ini ? "sí" : "no")
                        + " · ¿Termina con " + s + "? " + (fin ? "sí" : "no"));
            }
        };
    }

    /** L = {w ∈ Σ* | w = wᴿ}: palíndromos. */
    public static Lenguaje palindromos() {
        return new Lenguaje() {
            public String nombre() { return "Palíndromos (w = wᴿ)"; }
            public String definicion() { return "L = {w ∈ Σ* | w = wᴿ}"; }
            public Veredicto validar(String w) {
                String r = Cadenas.inversa(w);
                boolean ok = w.equals(r);
                return new Veredicto(ok, "wᴿ = " + Cadenas.mostrar(r) + (ok ? " = w" : " ≠ w"));
            }
        };
    }
}
