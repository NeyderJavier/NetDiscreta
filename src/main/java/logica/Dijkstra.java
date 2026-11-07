package logica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import modelo.Arista;
import modelo.Grafo;
import modelo.Nodo;

/**
 * Algoritmo de Dijkstra: camino de menor peso (latencia) entre dos nodos.
 */
public class Dijkstra {

    /** Resultado del cálculo: el camino y su costo total. */
    public static class Resultado {
        public final List<Nodo> camino;   // vacío si no hay camino
        public final int distancia;       // -1 si no hay camino

        Resultado(List<Nodo> camino, int distancia) {
            this.camino = camino;
            this.distancia = distancia;
        }

        public boolean existe() { return distancia >= 0; }
    }

    public static Resultado calcular(Grafo g, Nodo origen, Nodo destino) {
        Map<Nodo, Integer> dist = new HashMap<>();
        Map<Nodo, Nodo> previo = new HashMap<>();
        Set<Nodo> visitados = new HashSet<>();

        for (Nodo n : g.getNodos()) dist.put(n, Integer.MAX_VALUE);
        dist.put(origen, 0);

        while (visitados.size() < g.getNodos().size()) {
            // 1. Elegir el nodo no visitado con menor distancia
            Nodo u = null;
            for (Nodo n : g.getNodos()) {
                if (!visitados.contains(n) && (u == null || dist.get(n) < dist.get(u))) u = n;
            }
            if (u == null || dist.get(u) == Integer.MAX_VALUE) break; // el resto es inalcanzable
            visitados.add(u);
            if (u == destino) break;

            // 2. Relajar las aristas que salen de u
            for (Arista ar : g.getAristas()) {
                if (!ar.contiene(u)) continue;
                Nodo v = ar.otro(u);
                if (visitados.contains(v)) continue;
                int nueva = dist.get(u) + ar.getPeso();
                if (nueva < dist.get(v)) {
                    dist.put(v, nueva);
                    previo.put(v, u);
                }
            }
        }

        if (dist.get(destino) == Integer.MAX_VALUE) {
            return new Resultado(Collections.emptyList(), -1);
        }

        // 3. Reconstruir el camino desde el destino hacia atrás
        List<Nodo> camino = new ArrayList<>();
        for (Nodo n = destino; n != null; n = previo.get(n)) camino.add(n);
        Collections.reverse(camino);
        return new Resultado(camino, dist.get(destino));
    }
}
