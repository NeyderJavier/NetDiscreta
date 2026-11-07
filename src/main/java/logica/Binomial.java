package logica;

/**
 * Distribución binomial X ~ B(n, p).
 * X = número de bits que llegan con error al enviar n bits,
 * cuando cada bit falla de forma independiente con probabilidad p.
 */
public class Binomial {

    private final int n;
    private final double p;

    public Binomial(int n, double p) {
        if (n < 0) throw new IllegalArgumentException("n debe ser mayor o igual a 0.");
        if (p < 0 || p > 1) throw new IllegalArgumentException("p debe estar entre 0 y 1.");
        this.n = n;
        this.p = p;
    }

    public int getN() { return n; }
    public double getP() { return p; }

    /** Coeficiente binomial C(n, k) = n! / (k! (n − k)!). */
    public static double combinaciones(int n, int k) {
        if (k < 0 || k > n) return 0;
        k = Math.min(k, n - k);
        double r = 1;
        for (int i = 1; i <= k; i++) {
            r = r * (n - k + i) / i;
        }
        return Math.round(r);
    }

    /** P(X = k) = C(n, k) · pᵏ · (1 − p)ⁿ⁻ᵏ */
    public double probabilidad(int k) {
        if (k < 0 || k > n) return 0;
        return combinaciones(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    /** P(X ≤ k) */
    public double acumulada(int k) {
        double s = 0;
        for (int i = 0; i <= Math.min(k, n); i++) s += probabilidad(i);
        return s;
    }

    /** P(a ≤ X ≤ b) */
    public double entre(int a, int b) {
        double s = 0;
        for (int i = Math.max(a, 0); i <= Math.min(b, n); i++) s += probabilidad(i);
        return s;
    }

    /** E[X] = n · p */
    public double media() { return n * p; }

    /** Var(X) = n · p · (1 − p) */
    public double varianza() { return n * p * (1 - p); }

    /** σ = √Var(X) */
    public double desviacion() { return Math.sqrt(varianza()); }

    /** P(X par y X ≥ 2): errores que el bit de paridad NO detecta. */
    public double erroresParesNoDetectados() {
        double s = 0;
        for (int k = 2; k <= n; k += 2) s += probabilidad(k);
        return s;
    }
}
