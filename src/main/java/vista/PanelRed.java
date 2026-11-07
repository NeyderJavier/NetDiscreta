package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import logica.Dijkstra;
import modelo.Grafo;
import modelo.Nodo;
import vista.graficos.LienzoGrafo;

/**
 * Módulo 1 — Red (Grafos).
 * Crear equipos y conexiones, ver propiedades del grafo y la ruta más corta.
 */
public class PanelRed extends JPanel {

    private final Grafo grafo;
    private final LienzoGrafo lienzo;

    private final JTextField txtNombre = new JTextField(8);
    private final JComboBox<Nodo> cbA = new JComboBox<>();
    private final JComboBox<Nodo> cbB = new JComboBox<>();
    private final JSpinner spPeso = new JSpinner(new SpinnerNumberModel(5, 1, 999, 1));
    private final JComboBox<Nodo> cbOrigen = new JComboBox<>();
    private final JComboBox<Nodo> cbDestino = new JComboBox<>();
    private final JTextArea salida = new JTextArea(8, 24);

    public PanelRed(Grafo grafo) {
        this.grafo = grafo;
        this.lienzo = new LienzoGrafo(grafo);
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(lienzo, BorderLayout.CENTER);
        add(construirControles(), BorderLayout.EAST);
        refrescar();
    }

    private JPanel construirControles() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setPreferredSize(new Dimension(300, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        int fila = 0;

        // --- Equipos ---
        c.gridy = fila++; p.add(titulo("Equipos (vértices)"), c);
        c.gridy = fila++; p.add(fila("Nombre:", txtNombre), c);
        JButton btnAgregar = new JButton("Agregar equipo");
        JButton btnEliminar = new JButton("Eliminar equipo A");
        c.gridy = fila++; p.add(dos(btnAgregar, btnEliminar), c);

        // --- Conexiones ---
        c.gridy = fila++; p.add(titulo("Conexiones (aristas)"), c);
        c.gridy = fila++; p.add(fila("A:", cbA), c);
        c.gridy = fila++; p.add(fila("B:", cbB), c);
        c.gridy = fila++; p.add(fila("Latencia (ms):", spPeso), c);
        JButton btnConectar = new JButton("Conectar A — B");
        JButton btnDesconectar = new JButton("Quitar A — B");
        c.gridy = fila++; p.add(dos(btnConectar, btnDesconectar), c);

        // --- Ruta ---
        c.gridy = fila++; p.add(titulo("Ruta más corta (Dijkstra)"), c);
        c.gridy = fila++; p.add(fila("Origen:", cbOrigen), c);
        c.gridy = fila++; p.add(fila("Destino:", cbDestino), c);
        JButton btnRuta = new JButton("Calcular ruta");
        c.gridy = fila++; p.add(btnRuta, c);

        // --- Propiedades ---
        c.gridy = fila++; p.add(titulo("Propiedades"), c);
        JButton btnProp = new JButton("Grados");
        btnProp.setToolTipText("Grado de cada nodo y si el grafo es conexo");
        JButton btnMatriz = new JButton("Matriz");
        btnMatriz.setToolTipText("Matriz de adyacencia");
        c.gridy = fila++; p.add(dos(btnProp, btnMatriz), c);
        JButton btnEjemplo = new JButton("Red de ejemplo");
        JButton btnLimpiar = new JButton("Limpiar");
        c.gridy = fila++; p.add(dos(btnEjemplo, btnLimpiar), c);

        salida.setEditable(false);
        salida.setLineWrap(true);
        salida.setWrapStyleWord(true);
        c.gridy = fila++; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        p.add(new JScrollPane(salida), c);

        // --- Eventos ---
        btnAgregar.addActionListener(e -> agregarEquipo());
        txtNombre.addActionListener(e -> agregarEquipo());
        btnEliminar.addActionListener(e -> eliminarEquipo());
        btnConectar.addActionListener(e -> conectar());
        btnDesconectar.addActionListener(e -> desconectar());
        btnRuta.addActionListener(e -> calcularRuta());
        btnProp.addActionListener(e -> mostrarPropiedades());
        btnMatriz.addActionListener(e -> mostrarMatriz());
        btnEjemplo.addActionListener(e -> cargarEjemplo());
        btnLimpiar.addActionListener(e -> { grafo.limpiar(); refrescar(); salida.setText(""); });
        return p;
    }

    // ---------------- Acciones ----------------

    private void agregarEquipo() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) { error("Escribe un nombre para el equipo."); return; }
        try {
            // Se ubica en un círculo; luego el usuario lo puede arrastrar
            int k = grafo.getNodos().size();
            double ang = k * (2 * Math.PI / 8);
            double cx = Math.max(lienzo.getWidth(), 600) / 2.0;
            double cy = Math.max(lienzo.getHeight(), 450) / 2.0;
            double r = 150 + 30 * (k / 8);
            grafo.agregarNodo(nombre, cx + r * Math.cos(ang), cy + r * Math.sin(ang));
            txtNombre.setText("");
            refrescar();
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void eliminarEquipo() {
        Nodo a = (Nodo) cbA.getSelectedItem();
        if (a == null) return;
        grafo.eliminarNodo(a);
        refrescar();
    }

    private void conectar() {
        Nodo a = (Nodo) cbA.getSelectedItem(), b = (Nodo) cbB.getSelectedItem();
        if (a == null || b == null) { error("Selecciona los equipos A y B."); return; }
        try {
            grafo.agregarArista(a, b, (Integer) spPeso.getValue());
            refrescar();
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void desconectar() {
        Nodo a = (Nodo) cbA.getSelectedItem(), b = (Nodo) cbB.getSelectedItem();
        if (a == null || b == null) return;
        grafo.eliminarArista(a, b);
        refrescar();
    }

    private void calcularRuta() {
        Nodo o = (Nodo) cbOrigen.getSelectedItem(), d = (Nodo) cbDestino.getSelectedItem();
        if (o == null || d == null) { error("Selecciona origen y destino."); return; }
        Dijkstra.Resultado r = Dijkstra.calcular(grafo, o, d);
        if (!r.existe()) {
            lienzo.setCamino(List.of());
            salida.setText("No existe camino entre " + o + " y " + d + ".\nEl grafo no es conexo.");
            return;
        }
        lienzo.setCamino(r.camino);
        String ruta = r.camino.stream().map(Nodo::getNombre).collect(Collectors.joining(" → "));
        salida.setText("Ruta más corta:\n" + ruta
                + "\n\nLatencia total: " + r.distancia + " ms"
                + "\nSaltos: " + (r.camino.size() - 1));
    }

    private void mostrarPropiedades() {
        StringBuilder sb = new StringBuilder();
        sb.append("|V| = ").append(grafo.getNodos().size())
          .append("   |E| = ").append(grafo.getAristas().size()).append("\n\nGrados:\n");
        for (Nodo n : grafo.getNodos()) {
            sb.append("  gr(").append(n).append(") = ").append(grafo.grado(n)).append("\n");
        }
        sb.append("\nSuma de grados = ").append(grafo.sumaGrados())
          .append(" = 2|E| ✔\n")
          .append("\n¿Es conexo? ").append(grafo.esConexo() ? "Sí" : "No");
        salida.setText(sb.toString());
    }

    private void mostrarMatriz() {
        List<Nodo> ns = grafo.getNodos();
        if (ns.isEmpty()) return;
        int[][] m = grafo.matrizAdyacencia();
        String[] cols = new String[ns.size() + 1];
        cols[0] = "";
        for (int i = 0; i < ns.size(); i++) cols[i + 1] = ns.get(i).getNombre();
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (int i = 0; i < ns.size(); i++) {
            Object[] filaDatos = new Object[ns.size() + 1];
            filaDatos[0] = ns.get(i).getNombre();
            for (int j = 0; j < ns.size(); j++) filaDatos[j + 1] = m[i][j];
            modelo.addRow(filaDatos);
        }
        JTable tabla = new JTable(modelo);
        JScrollPane sp = new JScrollPane(tabla);
        sp.setPreferredSize(new Dimension(Math.min(80 * (ns.size() + 1), 700), Math.min(25 * (ns.size() + 2), 400)));
        JOptionPane.showMessageDialog(this, sp, "Matriz de adyacencia", JOptionPane.PLAIN_MESSAGE);
    }

    private void cargarEjemplo() {
        grafo.limpiar();
        Nodo pc1 = grafo.agregarNodo("PC1", 90, 120);
        Nodo pc2 = grafo.agregarNodo("PC2", 90, 330);
        Nodo r1 = grafo.agregarNodo("R1", 250, 220);
        Nodo r2 = grafo.agregarNodo("R2", 400, 110);
        Nodo r3 = grafo.agregarNodo("R3", 400, 340);
        Nodo srv = grafo.agregarNodo("SRV", 540, 220);
        grafo.agregarArista(pc1, r1, 4);
        grafo.agregarArista(pc2, r1, 3);
        grafo.agregarArista(r1, r2, 7);
        grafo.agregarArista(r1, r3, 2);
        grafo.agregarArista(r3, r2, 3);
        grafo.agregarArista(r2, srv, 4);
        grafo.agregarArista(r3, srv, 9);
        refrescar();
        salida.setText("Red de ejemplo cargada.\nPrueba la ruta PC1 → SRV.");
    }

    // ---------------- Utilidades ----------------

    /** Actualiza listas desplegables y dibujo. Lo pueden llamar otros módulos. */
    public final void refrescar() {
        for (JComboBox<Nodo> cb : List.of(cbA, cbB, cbOrigen, cbDestino)) {
            Object sel = cb.getSelectedItem();
            cb.removeAllItems();
            for (Nodo n : grafo.getNodos()) cb.addItem(n);
            if (sel != null && grafo.getNodos().contains(sel)) cb.setSelectedItem(sel);
        }
        if (cbB.getItemCount() > 1 && cbB.getSelectedIndex() == 0) cbB.setSelectedIndex(1);
        if (cbDestino.getItemCount() > 1 && cbDestino.getSelectedIndex() == 0) {
            cbDestino.setSelectedIndex(cbDestino.getItemCount() - 1);
        }
        lienzo.setCamino(List.of());
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    private static JLabel titulo(String t) {
        JLabel l = new JLabel(t);
        l.setFont(l.getFont().deriveFont(java.awt.Font.BOLD, 13f));
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));
        return l;
    }

    private static JPanel fila(String etiqueta, java.awt.Component comp) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        JLabel l = new JLabel(etiqueta);
        l.setPreferredSize(new Dimension(95, 22));
        p.add(l, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private static JPanel dos(JButton a, JButton b) {
        JPanel p = new JPanel(new java.awt.GridLayout(1, 2, 4, 0));
        p.add(a);
        p.add(b);
        return p;
    }
}
