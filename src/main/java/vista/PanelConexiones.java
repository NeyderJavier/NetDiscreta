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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import logica.ProductoCartesiano;
import logica.ProductoCartesiano.Par;
import modelo.Conjunto;
import modelo.Estado;
import vista.graficos.LienzoPlano;

/**
 * Módulo 3 — Conexiones (Producto cartesiano).
 * Genera A × B entre dos grupos, lo grafica en el plano y marca
 * la relación R = {(a, b) ∈ A × B | a y b tienen enlace directo}.
 */
public class PanelConexiones extends JPanel implements Refrescable {

    private final Estado estado;
    private final LienzoPlano plano = new LienzoPlano();

    private final JComboBox<Conjunto> cbA = new JComboBox<>();
    private final JComboBox<Conjunto> cbB = new JComboBox<>();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(new String[]{"#", "Par (a, b)", "¿Enlace directo?"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTextArea salida = Estilo.areaSalida(8);

    public PanelConexiones(Estado estado) {
        this.estado = estado;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(plano, BorderLayout.CENTER);
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

        c.gridy = f++; p.add(Estilo.titulo("Producto cartesiano A × B"), c);
        c.gridy = f++; p.add(Estilo.fila("A:", cbA), c);
        c.gridy = f++; p.add(Estilo.fila("B:", cbB), c);
        JButton btnGenerar = new JButton("Generar A × B");
        JButton btnInvertir = new JButton("Ver B × A");
        btnInvertir.setToolTipText("Intercambia A y B para comprobar que A × B ≠ B × A");
        c.gridy = f++; p.add(Estilo.botones(btnGenerar, btnInvertir), c);

        c.gridy = f++; p.add(Estilo.titulo("Pares ordenados"), c);
        JTable tabla = new JTable(modeloTabla);
        tabla.getColumnModel().getColumn(0).setMaxWidth(35);
        JScrollPane spTabla = new JScrollPane(tabla);
        spTabla.setPreferredSize(new Dimension(300, 200));
        c.gridy = f++; c.weighty = 0.5; c.fill = GridBagConstraints.BOTH;
        p.add(spTabla, c);

        c.gridy = f++; c.weighty = 0.5;
        p.add(new JScrollPane(salida), c);

        btnGenerar.addActionListener(e -> generar());
        btnInvertir.addActionListener(e -> {
            Object a = cbA.getSelectedItem();
            cbA.setSelectedItem(cbB.getSelectedItem());
            cbB.setSelectedItem(a);
            generar();
        });
        return p;
    }

    private void generar() {
        Conjunto a = (Conjunto) cbA.getSelectedItem();
        Conjunto b = (Conjunto) cbB.getSelectedItem();
        modeloTabla.setRowCount(0);
        if (a == null || b == null) {
            plano.mostrar(null, null, List.of());
            salida.setText("Primero crea al menos un grupo en el módulo 2 (Grupos).");
            return;
        }
        List<Par> pares = ProductoCartesiano.generar(a, b, estado.getRed());
        List<Par> rel = ProductoCartesiano.relacion(pares);
        plano.mostrar(a, b, pares);

        int i = 1;
        for (Par par : pares) modeloTabla.addRow(new Object[]{i++, par.toString(), par.enRelacion ? "Sí" : "No"});

        String nA = a.getNombre(), nB = b.getNombre();
        String extR = rel.isEmpty() ? "∅"
                : rel.stream().map(Par::toString).collect(Collectors.joining(", ", "{", "}"));
        salida.setText(a.notacion() + "\n" + b.notacion() + "\n\n"
                + nA + " × " + nB + " = {(a, b) | a ∈ " + nA + " ∧ b ∈ " + nB + "}\n"
                + "|" + nA + " × " + nB + "| = |" + nA + "| · |" + nB + "| = "
                + a.cardinalidad() + " · " + b.cardinalidad() + " = " + pares.size() + "\n\n"
                + "Relación R ⊆ " + nA + " × " + nB + ": (a, b) ∈ R si a y b tienen enlace directo.\n"
                + "R = " + extR + "\n|R| = " + rel.size());
        salida.setCaretPosition(0);
    }

    @Override
    public void refrescar() {
        Object selA = cbA.getSelectedItem(), selB = cbB.getSelectedItem();
        cbA.removeAllItems();
        cbB.removeAllItems();
        for (Conjunto g : estado.getGrupos()) {
            cbA.addItem(g);
            cbB.addItem(g);
        }
        if (selA != null && estado.getGrupos().contains(selA)) cbA.setSelectedItem(selA);
        if (selB != null && estado.getGrupos().contains(selB)) cbB.setSelectedItem(selB);
        else if (cbB.getItemCount() > 1) cbB.setSelectedIndex(1);
        generar();
    }
}
