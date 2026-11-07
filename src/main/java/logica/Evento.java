package logica;

/**
 * Eventos sobre la variable aleatoria X = número de bits con error.
 * Cada evento es un subconjunto del espacio muestral.
 */
public enum Evento {

    SIN_ERRORES("Llega sin errores", "X = 0"),
    EXACTAMENTE("Exactamente k errores", "X = k"),
    A_LO_SUMO("A lo sumo k errores", "X ≤ k"),
    AL_MENOS("Al menos k errores", "X ≥ k"),
    ALGUN_ERROR("Al menos un error", "X ≥ 1");

    public final String nombre;
    public final String condicion;

    Evento(String nombre, String condicion) {
        this.nombre = nombre;
        this.condicion = condicion;
    }

    /** Indica si el valor x de la variable aleatoria hace que ocurra el evento. */
    public boolean ocurre(int x, int k) {
        return switch (this) {
            case SIN_ERRORES -> x == 0;
            case EXACTAMENTE -> x == k;
            case A_LO_SUMO -> x <= k;
            case AL_MENOS -> x >= k;
            case ALGUN_ERROR -> x >= 1;
        };
    }

    /** Usa k en la condición, por ejemplo "X ≤ 2". */
    public String condicion(int k) {
        return condicion.replace("k", String.valueOf(k));
    }

    /** Indica si el evento usa el parámetro k. */
    public boolean usaK() {
        return this == EXACTAMENTE || this == A_LO_SUMO || this == AL_MENOS;
    }

    /** P(evento) a partir de la distribución binomial. */
    public double probabilidad(Binomial b, int k) {
        double s = 0;
        for (int x = 0; x <= b.getN(); x++) {
            if (ocurre(x, k)) s += b.probabilidad(x);
        }
        return s;
    }

    @Override
    public String toString() { return nombre + " (" + condicion + ")"; }
}
