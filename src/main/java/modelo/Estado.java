package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Estado compartido por todos los módulos: la red, los grupos
 * y el mensaje que se va a enviar.
 */
public class Estado {

    private final Grafo red = new Grafo();
    private final List<Conjunto> grupos = new ArrayList<>();
    private String mensaje = "1011001";

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
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
