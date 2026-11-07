package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import logica.Binomial;
import logica.ExpresionBooleana;
import logica.Paridad;
import modelo.Estado;
import vista.graficos.LienzoCircuito;

/**
 * Módulo 6 — Verificador (Álgebra booleana).
 * El emisor calcula un bit de paridad con XOR y el receptor comprueba la trama.
 * Incluye tablas de verdad y comprobación de equivalencias (leyes del álgebra de Boole).
 */
public class PanelVerificador extends JPanel implements Refrescable {

    /** Leyes de ejemplo: nombre, lado izquierdo, lado derecho. */
    private static final String[][] LEYES = {
        {"Paridad de 3 bits", "A ⊕ B ⊕ C", "A'B'C + A'BC' + AB'C' + ABC"},
        {"De Morgan (1)", "(A + B)'", "A'·B'"},
        {"De Morgan (2)", "(A·B)'", "A' + B'"},
        {"Distributiva", "A·(B + C)", "A·B + A·C"},
        {"Absorción", "A + A·B", "A"},
        {"XOR con AND/OR", "A ⊕ B", "A·B' + A'·B"},
        {"Contraejemplo", "A + B·C", "(A + B)·C"},
    };

    private final Estado estado;
    private final LienzoCircuito circuito = new LienzoCircuito();

    private final JTextField txtMensaje = new JTextField();
    private final JTextField txtRecibida = new JTextField();
    private final JRadioButton rbEmisor = new JRadioButton("Emisor (P)", true);
    private final JRadioButton rbReceptor = new JRadioButton("Receptor (S)");
    private final JLabel lblResultado = new JLabel(" ", SwingConstants.CENTER);

    private final JComboBox<String> cbLey = new JComboBox<>();
    private final JTextField txtExpr1 = new JTextField();
    private final JTextField txtExpr2 = new JTextField();

    private final JTextArea salida = Estilo.areaSalida(12);

    public PanelVerificador(Estado estado) {
        this.estado = estado;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        salida.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JSplitPane centro = new JSplitPane(JSplitPane.VERTICAL_SPLIT, circuito, new JScrollPane(salida));
        centro.setResizeWeight(0.55);
        centro.setBorder(null);
        add(construirControles(), BorderLayout.WEST);
        add(centro, BorderLayout.CENTER);
        refrescar();
    }

    private JPanel construirControles() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setPreferredSize(new Dimension(350, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;
        int f = 0;

        // --- Paridad ---
        c.gridy = f++; p.add(Estilo.titulo("Bit de paridad (XOR)"), c);
        c.gridy = f++; p.add(Estilo.fila("Mensaje w:", txtMensaje), c);
        JButton btnParidad = new JButton("Calcular P");
        c.gridy = f++; p.add(btnParidad, c);
        c.gridy = f++; p.add(Estilo.fila("Recibida r:", txtRecibida), c);
        JButton btnTraer = new JButton("Traer del Canal");
        btnTraer.setToolTipText("Usa la última trama enviada en el módulo 5");
        JButton btnVerificar = new JButton("Verificar r");
        c.gridy = f++; p.add(Estilo.botones(btnTraer, btnVerificar), c);
        lblResultado.setFont(lblResultado.getFont().deriveFont(Font.BOLD, 15f));
        lblResultado.setOpaque(true);
        lblResultado.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        c.gridy = f++; p.add(lblResultado, c);

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbEmisor);
        grupo.add(rbReceptor);
        JPanel radios = new JPanel(new java.awt.GridLayout(1, 2));
        radios.add(rbEmisor);
        radios.add(rbReceptor);
        c.gridy = f++; p.add(Estilo.fila("Circuito:", radios), c);

        // --- Expresiones ---
        c.gridy = f++; p.add(Estilo.titulo("Álgebra de Boole"), c);
        for (String[] ley : LEYES) cbLey.addItem(ley[0]);
        c.gridy = f++; p.add(Estilo.fila("Ejemplo:", cbLey), c);
        c.gridy = f++; p.add(Estilo.fila("Expresión 1:", txtExpr1), c);
        c.gridy = f++; p.add(Estilo.fila("Expresión 2:", txtExpr2), c);
        JButton btnTabla = new JButton("Tabla de verdad");
        JButton btnEquiv = new JButton("¿Equivalentes?");
        c.gridy = f++; p.add(Estilo.botones(btnTabla, btnEquiv), c);
        JLabel ayuda = new JLabel("<html><small>Operadores: A' (NOT) · AB o A·B (AND) · A+B (OR) · "
                + "A⊕B o A^B (XOR) · paréntesis</small></html>");
        c.gridy = f++; p.add(ayuda, c);

        c.gridy = f++; c.weighty = 1;
        p.add(new JPanel(), c);

        // --- Eventos ---
        btnParidad.addActionListener(e -> calcularParidad());
        txtMensaje.addActionListener(e -> calcularParidad());
        btnTraer.addActionListener(e -> traerDelCanal());
        btnVerificar.addActionListener(e -> verificar());
        txtRecibida.addActionListener(e -> verificar());
        rbEmisor.addActionListener(e -> dibujarCircuito());
        rbReceptor.addActionListener(e -> dibujarCircuito());
        cbLey.addActionListener(e -> {
            String[] ley = LEYES[cbLey.getSelectedIndex()];
            txtExpr1.setText(ley[1]);
            txtExpr2.setText(ley[2]);
        });
        btnTabla.addActionListener(e -> tablaDeVerdad());
        btnEquiv.addActionListener(e -> equivalencia());
        cbLey.setSelectedIndex(0);
        return p;
    }

    // ---------------- Paridad ----------------

    private String leerBinario(JTextField campo, String nombre) {
        String s = campo.getText().trim();
        if (s.isEmpty() || !s.matches("[01]+")) {
            Estilo.aviso(this, nombre + " debe ser una cadena binaria no vacía (solo 0 y 1).");
            return null;
        }
        return s;
    }

    private void calcularParidad() {
        String w = leerBinario(txtMensaje, "El mensaje");
        if (w == null) return;
        int pb = Paridad.bitParidad(w);
        String trama = w + pb;
        rbEmisor.setSelected(true);
        dibujarCircuito();
        lblResultado.setText("P = " + pb + "   →   trama = " + trama);
        lblResultado.setBackground(new java.awt.Color(231, 241, 251));
        lblResultado.setForeground(Estilo.AZUL);
        salida.setText("EMISOR — paridad par\n"
                + "P = b1 ⊕ b2 ⊕ … ⊕ bn\n"
                + "P = " + Paridad.expresion(w) + "\n\n"
                + "Paso a paso:\n" + Paridad.pasoAPaso(w) + "\n"
                + "Cantidad de unos en w: " + countUnos(w) + " → P = " + pb
                + (pb == 0 ? " (ya era par)" : " (se agrega un 1 para que sea par)") + "\n"
                + "Trama enviada = w · P = " + trama + "  (tiene " + countUnos(trama) + " unos: par)");
        salida.setCaretPosition(0);
    }

    private void traerDelCanal() {
        if (estado.getTramaRecibida() == null) {
            Estilo.aviso(this, "Todavía no hay envíos. Ve al módulo 5 (Canal) y pulsa \"Enviar trama una vez\".");
            return;
        }
        txtMensaje.setText(estado.getMensaje());
        txtRecibida.setText(estado.getTramaRecibida());
        verificar();
    }

    private void verificar() {
        String r = leerBinario(txtRecibida, "La trama recibida");
        if (r == null) return;
        int s = Paridad.sindrome(r);
        rbReceptor.setSelected(true);
        dibujarCircuito();

        lblResultado.setText(s == 0 ? "S = 0  ✔  no se detectan errores" : "S = 1  ✘  ¡error detectado!");
        lblResultado.setBackground(s == 0 ? new java.awt.Color(229, 244, 228) : new java.awt.Color(251, 235, 235));
        lblResultado.setForeground(s == 0 ? Estilo.VERDE : Estilo.ROJO);

        StringBuilder sb = new StringBuilder();
        sb.append("RECEPTOR — síndrome S = r1 ⊕ r2 ⊕ … ⊕ r(n+1)\n");
        sb.append("S = ").append(Paridad.expresion(r)).append("\n");
        sb.append(s == 0 ? "S = 0 → la trama tiene paridad par: se acepta.\n"
                         : "S = 1 → la paridad no coincide: hay un número impar de bits con error.\n");

        // Comparación con lo que realmente pasó en el canal
        String enviada = estado.getTramaEnviada();
        if (enviada != null && r.equals(estado.getTramaRecibida())) {
            int reales = estado.getErroresReales();
            sb.append("\nLo que realmente ocurrió en el canal:\n");
            sb.append("  enviada  = ").append(enviada).append("\n");
            sb.append("  recibida = ").append(r).append("\n");
            sb.append("  errores reales = ").append(reales).append("\n");
            if (reales == 0) {
                sb.append("  ✔ Correcto: no hubo errores y no se detectó ninguno.\n");
            } else if (reales % 2 == 1) {
                sb.append("  ✔ Correcto: hubo ").append(reales).append(" error(es), número impar, y se detectó.\n");
            } else {
                sb.append("  ✘ Error NO detectado: hubo ").append(reales)
                  .append(" errores (número par) y la paridad no cambia.\n");
            }
            sb.append("  Mensaje que lee el receptor: ").append(r, 0, r.length() - 1)
              .append(r.substring(0, r.length() - 1).equals(enviada.substring(0, enviada.length() - 1))
                      ? "  (igual al original)" : "  (distinto al original)").append("\n");
        }

        // Probabilidad de que un error pase sin detectarse
        Binomial b = new Binomial(r.length(), estado.getProbabilidadError());
        sb.append(String.format("%nLimitación del bit de paridad (n = %d, p = %.2f):%n", r.length(), b.getP()));
        sb.append(String.format("  P(error no detectado) = P(X = 2) + P(X = 4) + … = %.6f%n", b.erroresParesNoDetectados()));
        sb.append(String.format("  P(error detectado)    = P(X impar)            = %.6f%n",
                1 - b.probabilidad(0) - b.erroresParesNoDetectados()));
        salida.setText(sb.toString());
        salida.setCaretPosition(0);
    }

    private void dibujarCircuito() {
        if (rbReceptor.isSelected()) {
            String r = txtRecibida.getText().trim();
            circuito.mostrar(r.matches("[01]+") ? r : "", "Receptor: S = r1 ⊕ r2 ⊕ … ⊕ r" + r.length(), "S");
        } else {
            String w = txtMensaje.getText().trim();
            circuito.mostrar(w.matches("[01]+") ? w : "", "Emisor: P = b1 ⊕ b2 ⊕ … ⊕ b" + w.length(), "P");
        }
    }

    private static int countUnos(String s) {
        int k = 0;
        for (char ch : s.toCharArray()) if (ch == '1') k++;
        return k;
    }

    // ---------------- Álgebra de Boole ----------------

    private void tablaDeVerdad() {
        try {
            ExpresionBooleana e = new ExpresionBooleana(txtExpr1.getText());
            List<Character> vars = e.getVariables();
            if (vars.size() > 6) {
                Estilo.aviso(this, "Usa máximo 6 variables para que la tabla quepa en pantalla.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("F = ").append(e.getTexto()).append("\n\n");
            sb.append("fila  ");
            for (char v : vars) sb.append(v).append("  ");
            sb.append("│ F\n");
            sb.append("─".repeat(6 + 3 * vars.size())).append("┼───\n");
            int filas = 1 << vars.size();
            for (int i = 0; i < filas; i++) {
                Map<Character, Boolean> fila = ExpresionBooleana.fila(vars, i);
                sb.append(String.format("%3d   ", i));
                for (char v : vars) sb.append(fila.get(v) ? '1' : '0').append("  ");
                boolean val = e.evaluar(fila);
                sb.append("│ ").append(val ? '1' : '0').append(val ? "   ← m" + i : "").append("\n");
            }
            sb.append("\nMinitérminos: Σm").append(e.minterminos()).append("\n");
            sb.append("Suma de productos canónica:\nF = ").append(e.sumaDeProductos()).append("\n");
            salida.setText(sb.toString());
            salida.setCaretPosition(0);
        } catch (IllegalArgumentException ex) {
            Estilo.aviso(this, "Expresión 1: " + ex.getMessage());
        }
    }

    private void equivalencia() {
        try {
            ExpresionBooleana e1 = new ExpresionBooleana(txtExpr1.getText());
            ExpresionBooleana e2 = new ExpresionBooleana(txtExpr2.getText());
            java.util.TreeSet<Character> todas = new java.util.TreeSet<>(e1.getVariables());
            todas.addAll(e2.getVariables());
            List<Character> vars = new java.util.ArrayList<>(todas);
            if (vars.size() > 6) {
                Estilo.aviso(this, "Usa máximo 6 variables en total.");
                return;
            }
            boolean eq = ExpresionBooleana.equivalentes(e1, e2);

            StringBuilder sb = new StringBuilder();
            sb.append("F1 = ").append(e1.getTexto()).append("\n");
            sb.append("F2 = ").append(e2.getTexto()).append("\n\n");
            for (char v : vars) sb.append(v).append("  ");
            sb.append("│ F1  F2\n");
            sb.append("─".repeat(3 * vars.size())).append("┼────────\n");
            for (int i = 0; i < (1 << vars.size()); i++) {
                Map<Character, Boolean> fila = ExpresionBooleana.fila(vars, i);
                for (char v : vars) sb.append(fila.get(v) ? '1' : '0').append("  ");
                boolean a = e1.evaluar(fila), b = e2.evaluar(fila);
                sb.append("│ ").append(a ? '1' : '0').append("   ").append(b ? '1' : '0')
                  .append(a != b ? "   ← distintas" : "").append("\n");
            }
            sb.append("\n").append(eq
                    ? "F1 ≡ F2: coinciden en todas las filas, son equivalentes."
                    : "F1 ≢ F2: hay filas donde difieren, NO son equivalentes.");
            lblResultado.setText(eq ? "F1 ≡ F2  ✔  equivalentes" : "F1 ≢ F2  ✘  no equivalentes");
            lblResultado.setBackground(eq ? new java.awt.Color(229, 244, 228) : new java.awt.Color(251, 235, 235));
            lblResultado.setForeground(eq ? Estilo.VERDE : Estilo.ROJO);
            salida.setText(sb.toString());
            salida.setCaretPosition(0);
        } catch (IllegalArgumentException ex) {
            Estilo.aviso(this, ex.getMessage());
        }
    }

    @Override
    public void refrescar() {
        txtMensaje.setText(estado.getMensaje());
        if (estado.getTramaRecibida() != null) {
            txtRecibida.setText(estado.getTramaRecibida());
        } else if (txtRecibida.getText().isBlank() || !txtRecibida.getText().startsWith(estado.getMensaje())) {
            txtRecibida.setText(Paridad.agregarParidad(estado.getMensaje()));
        }
        dibujarCircuito();
    }
}
