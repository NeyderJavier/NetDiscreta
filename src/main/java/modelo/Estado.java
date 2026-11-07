package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Estado compartido por todos los módulos: la red, los grupos,
 * el mensaje que se envía y el resultado del último envío por el canal.
 */
public class Estado {

    private final Grafo red = new Grafo();
    private final List<Conjunto> grupos = new ArrayList<>();
    private String mensaje = "1011001";

    // Último envío simulado en el módulo Canal (lo usa el Verificador)
    private double probabilidadError = 0.05;
    private String tramaEnviada;     // mensaje + bit de paridad
    private String tramaRecibida;    // lo que llegó, posiblemente con errores
    private int erroresReales = -1;  // -1 = todavía no se ha enviado nada

    public Grafo getRed() { return red; }

    public List<Conjunto> getGrupos() {
        // Si se eliminaron equipos en el módulo Red, se quitan de los grupos
        for (Conjunto c : grupos) c.retenerSolo(red.getNodos());
        return grupos;
    }

    public Conjunto buscarGrupo(String nombre) {
        for (Conjunto c : grupos) {
            if (c.getNombre().equalsIgnoreCase(nombre)) return c;
        }
        return null;
    }

    /** Conjunto universal U: todos los equipos de la red. */
    public Conjunto universo() {
        return new Conjunto("U", red.getNodos());
    }

    public String getMensaje() { return mensaje; }

    public void setMensaje(String mensaje) {
        if (!mensaje.equals(this.mensaje)) {
            // Un mensaje nuevo invalida el último envío
            tramaEnviada = null;
            tramaRecibida = null;
            erroresReales = -1;
        }
        this.mensaje = mensaje;
    }

    public double getProbabilidadError() { return probabilidadError; }
    public void setProbabilidadError(double p) { this.probabilidadError = p; }

    public void registrarEnvio(String enviada, String recibida, int errores) {
        this.tramaEnviada = enviada;
        this.tramaRecibida = recibida;
        this.erroresReales = errores;
    }

    public String getTramaEnviada() { return tramaEnviada; }
    public String getTramaRecibida() { return tramaRecibida; }
    public int getErroresReales() { return erroresReales; }
}
