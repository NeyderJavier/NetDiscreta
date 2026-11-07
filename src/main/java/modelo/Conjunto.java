package modelo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Un grupo de equipos de la red, tratado como conjunto matemático
 * (sin elementos repetidos y sin importar el orden).
 */
public class Conjunto {

    private final String nombre;
    private final Set<Nodo> elementos = new LinkedHashSet<>();

    public Conjunto(String nombre) {
        this.nombre = nombre;
    }

    public Conjunto(String nombre, Collection<Nodo> elementos) {
        this(nombre);
        this.elementos.addAll(elementos);
    }

    public String getNombre() { return nombre; }

    public Set<Nodo> getElementos() { return Collections.unmodifiableSet(elementos); }

    public void setElementos(Collection<Nodo> nuevos) {
        elementos.clear();
        elementos.addAll(nuevos);
    }

    public boolean contiene(Nodo n) { return elementos.contains(n); }

    /** Cardinalidad |A|. */
    public int cardinalidad() { return elementos.size(); }

    public boolean esVacio() { return elementos.isEmpty(); }

    /** Quita los equipos que ya no existen en la red. */
    public void retenerSolo(Collection<Nodo> existentes) { elementos.retainAll(existentes); }

    /** Notación por extensión: A = {PC1, PC2}. Vacío: ∅. */
    public String notacion() {
        return nombre + " = " + extension();
    }

    public String extension() {
        if (elementos.isEmpty()) return "∅";
        return elementos.stream().map(Nodo::getNombre).collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public String toString() { return nombre; }
}
