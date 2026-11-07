package logica;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simula un canal binario con ruido: cada bit se invierte (0 ↔ 1)
 * de forma independiente con probabilidad p.
 */
public class SimuladorCanal {

    private final Random azar;

    public SimuladorCanal() { this.azar = new Random(); }

    /** Con semilla fija los resultados se repiten (útil para pruebas). */
    public SimuladorCanal(long semilla) { this.azar = new Random(semilla); }

    /** Resultado de enviar una trama. */
    public static class Transmision {
        public final String enviada;
        public final String recibida;
        public final List<Integer> posicionesError;   // índices (desde 0) de los bits que cambiaron

        Transmision(String enviada, String recibida, List<Integer> posicionesError) {
            this.enviada = enviada;
            this.recibida = recibida;
            this.posicionesError = posicionesError;
        }

        public int errores() { return posicionesError.size(); }
    }

    /** Envía una trama binaria por el canal. */
    public Transmision transmitir(String trama, double p) {
        StringBuilder r = new StringBuilder();
        List<Integer> pos = new ArrayList<>();
        for (int i = 0; i < trama.length(); i++) {
            char b = trama.charAt(i);
            if (azar.nextDouble() < p) {
                r.append(b == '0' ? '1' : '0');
                pos.add(i);
            } else {
                r.append(b);
            }
        }
        return new Transmision(trama, r.toString(), pos);
    }

    /**
     * Repite N envíos de n bits y cuenta cuántas veces ocurrió cada valor de X.
     * frecuencias[k] = número de envíos con exactamente k errores.
     */
    public int[] simular(int n, double p, int envios) {
        int[] frec = new int[n + 1];
        for (int e = 0; e < envios; e++) {
            int x = 0;
            for (int i = 0; i < n; i++) {
                if (azar.nextDouble() < p) x++;
            }
            frec[x]++;
        }
        return frec;
    }
}
