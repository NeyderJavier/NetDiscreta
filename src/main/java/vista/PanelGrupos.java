package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import logica.OperacionesConjuntos;
import logica.OperacionesConjuntos.Operacion;
import modelo.Conjunto;
import modelo.Estado;
import modelo.Grafo;
import modelo.Nodo;
import vista.graficos.LienzoVenn;

/**
 * Módulo 2 — Grupos (Conjuntos).
 * Se crean grupos de equipos y se operan como conjuntos, con diagrama de Venn.
 */
public class PanelGrupos extends JPanel implements Refrescable {

    private final Estado estado;
    private final LienzoVenn venn = new LienzoVenn();

    private final JTextField txtNombre = new JTextField();
    private final DefaultListModel<Nodo> modeloNodos = new DefaultListModel<>();
    private final JList<Nodo> lstNodos = new JList<>(modeloNodos);
    private final DefaultListModel<Conjunto> modeloGrupos = new DefaultListModel<>();
    private final JList<Conjunto> lstGrupos = new JList<>(modeloGrupos);

    private final JComboBox<Conjunto> cbA = new JComboBox<>();
    private final JComboBox<Conjunto> cbB = new JComboBox<>();
    private final JComboBox<Operacion> cbOp = new JComboBox<>(Operacion.values());
    private final JTextArea salida = Estilo.areaSalida(9);

    public PanelGrupos(Estado estado) {
        this.estado = estado;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(venn, BorderLayout.CENTER);
        add(construirControles(), BorderLayout.EAST);
        refrescar();
    }

    private JPanel construirControles() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setPreferredSize(new Dimension(330, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;
        int f = 0;

        // --- Editor de grupos ---
        c.gridy = f++; p.add(Estilo.titulo("Crear o editar grupo"), c);
        c.gridy = f++; p.add(Estilo.fila("Nombre:", txtNombre), c);
        lstNodos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstNodos.setVisibleRowCount(5);
        lstNodos.setToolTipText("Ctrl + clic para elegir varios equipos");
        c.gridy = f++; p.add(Estilo.fila("Elementos:", new JScrollPane(lstNodos)), c);
        JButton btnGuardar = new JButton("Guardar grupo");
        JButton btnEliminar = new JButton("Eliminar grupo");
        c.gridy = f++; p.add(Estilo.botones(btnGuardar, btnEliminar), c);

        lstGrupos.setVisibleRowCount(4);
        lstGrupos.setCellRenderer((list, valor, i, sel, foco) -> {
            javax.swing.JLabel l = new javax.swing.JLabel(valor.notacion());
            l.setOpaque(true);
            l.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            l.setBackground(sel ? list.getSelectionBackground() : list.getBackground());
            l.setForeground(sel ? list.getSelectionForeground() : list.getForeground());
            return l;
        });
        c.gridy = f++; p.add(Estilo.titulo("Grupos creados"), c);
        c.gridy = f++; p.add(new JScrollPane(lstGrupos), c);

        // --- Operaciones ---
        c.gridy = f++; p.add(Estilo.titulo("Operación entre conjuntos"), c);
        c.gridy = f++; p.add(Estilo.fila("A:", cbA), c);
        c.gridy = f++; p.add(Estilo.fila("B:", cbB), c);
        c.gridy = f++; p.add(Estilo.fila("Operación:", cbOp), c);
        JButton btnCalcular = new JButton("Calcular");
        JButton btnEjemplo = new JButton("Grupos de ejemplo");
        c.gridy = f++; p.add(Estilo.botones(btnCalcular, btnEjemplo), c);

        c.gridy = f++; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        p.add(new JScrollPane(salida), c);

        // --- Eventos ---
        btnGuardar.addActionListener(e -> guardarGrupo());
        btnEliminar.addActionListener(e -> eliminarGrupo());
        btnCalcular.addActionListener(e -> calcular());
        btnEjemplo.addActionListener(e -> cargarEjemplo());
        cbOp.addActionListener(e -> calcular());
        cbA.addActionListener(e -> calcular());
        cbB.addActionListener(e -> calcular());
        lstGrupos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) editarSeleccionado();
        });
        return p;
    }

    // ---------------- Acciones ----------------

    private void guardarGrupo() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) { Estilo.aviso(this, "Escribe un nombre para el grupo (ej. A, Servidores)."); return; }
        List<Nodo> elegidos = lstNodos.getSelectedValuesList();
        Conjunto existente = estado.buscarGrupo(nombre);
        if (existente != null) {
            existente.setElementos(elegidos);
        } else {
            estado.getGrupos().add(new Conjunto(nombre, elegidos));
        }
        refrescar();
        salida.setText("Grupo guardado:\n" + estado.buscarGrupo(nombre).notacion());
    }

    private void eliminarGrupo() {
        Conjunto sel = lstGrupos.getSelectedValue();
        if (sel == null) { Estilo.aviso(this, "Selecciona un grupo de la lista."); return; }
        estado.getGrupos().remove(sel);
        refrescar();
    }

    private void editarSeleccionado() {
        Conjunto sel = lstGrupos.getSelectedValue();
        if (sel == null) return;
        txtNombre.setText(sel.getNombre());
        lstNodos.clearSelection();
        for (int i = 0; i < modeloNodos.size(); i++) {
            if (sel.contiene(modeloNodos.get(i))) lstNodos.addSelectionInterval(i, i);
        }
    }

    private void calcular() {
        Conjunto a = (Conjunto) cbA.getSelectedItem();
        Conjunto b = (Conjunto) cbB.getSelectedItem();
        Operacion op = (Operacion) cbOp.getSelectedItem();
        if (a == null || b == null) {
            venn.mostrar(null, null, null, null);
            return;
        }
        Conjunto u = estado.universo();
        Conjunto r = OperacionesConjuntos.aplicar(op, a, b, u);
        venn.mostrar(a, b, u, op);

        // Se usan marcadores para no dañar nombres que contengan las letras A o B
        String sim = op.simbolo.replace("A", "\u0001").replace("B", "\u0002")
                .replace("\u0001", a.getNombre()).replace("\u0002", b.getNombre());
        Conjunto inter = OperacionesConjuntos.interseccion(a, b);
        int cardUnion = OperacionesConjuntos.union(a, b).cardinalidad();

        StringBuilder sb = new StringBuilder();
        sb.append(u.notacion()).append("\n")
          .append(a.notacion()).append("\n")
          .append(b.notacion()).append("\n\n")
          .append(op.nombre).append(": ").append(op.definicion).append("\n")
          .append(sim).append(" = ").append(r.extension()).append("\n")
          .append("|").append(sim).append("| = ").append(r.cardinalidad()).append("\n\n")
          .append("Propiedades:\n")
          .append("• ").append(a).append(" ⊆ ").append(b).append(": ")
          .append(OperacionesConjuntos.esSubconjunto(a, b) ? "sí" : "no").append("\n")
          .append("• ").append(b).append(" ⊆ ").append(a).append(": ")
          .append(OperacionesConjuntos.esSubconjunto(b, a) ? "sí" : "no").append("\n")
          .append("• Disjuntos (").append(a).append(" ∩ ").append(b).append(" = ∅): ")
          .append(inter.esVacio() ? "sí" : "no").append("\n")
          .append("• |A ∪ B| = |A| + |B| − |A ∩ B| → ")
          .append(cardUnion).append(" = ").append(a.cardinalidad()).append(" + ")
          .append(b.cardinalidad()).append(" − ").append(inter.cardinalidad()).append(" ✔");
        salida.setText(sb.toString());
        salida.setCaretPosition(0);
    }

    private void cargarEjemplo() {
        Grafo red = estado.getRed();
        if (red.getNodos().isEmpty()) red.cargarEjemplo();
        estado.getGrupos().clear();
        estado.getGrupos().add(new Conjunto("Norte", nodos(red, "PC1", "PC2", "R1", "R3")));
        estado.getGrupos().add(new Conjunto("Sur", nodos(red, "R2", "R3", "SRV")));
        refrescar();
        cbA.setSelectedIndex(0);
        cbB.setSelectedIndex(1);
        cbOp.setSelectedItem(Operacion.UNION);
        calcular();
    }

    private static List<Nodo> nodos(Grafo red, String... nombres) {
        java.util.ArrayList<Nodo> r = new java.util.ArrayList<>();
        for (String n : nombres) {
            Nodo x = red.buscarNodo(n);
            if (x != null) r.add(x);
        }
        return r;
    }

    // ---------------- Refresco ----------------

    @Override
    public void refrescar() {
        modeloNodos.clear();
        for (Nodo n : estado.getRed().getNodos()) modeloNodos.addElement(n);

        modeloGrupos.clear();
        for (Conjunto c : estado.getGrupos()) modeloGrupos.addElement(c);

        Object selA = cbA.getSelectedItem(), selB = cbB.getSelectedItem();
        cbA.removeAllItems();
        cbB.removeAllItems();
        for (Conjunto c : estado.getGrupos()) {
            cbA.addItem(c);
            cbB.addItem(c);
        }
        if (selA != null && estado.getGrupos().contains(selA)) cbA.setSelectedItem(selA);
        if (selB != null && estado.getGrupos().contains(selB)) cbB.setSelectedItem(selB);
        else if (cbB.getItemCount() > 1) cbB.setSelectedIndex(1);
        calcular();
    }
}
