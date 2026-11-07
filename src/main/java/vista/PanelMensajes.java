package vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import logica.Cadenas;
import logica.Lenguaje;
import modelo.Estado;

/**
 * Módulo 4 — Mensajes (Alfabetos, cadenas y lenguajes).
 * El mensaje es una cadena w sobre Σ; se analiza y se valida contra un lenguaje L.
 */
public class PanelMensajes extends JPanel implements Refrescable {

    private static final String[] TIPOS = {
        "Longitud fija", "Paridad par", "Cabecera y cola", "Palíndromos"
    };

    private final Estado estado;

    private final JTextField txtAlfabeto = new JTextField("0, 1");
    private final JTextField txtW = new JTextField();
    private final JTextField txtV = new JTextField("01");
    private final JSpinner spPotencia = new JSpinner(new SpinnerNumberModel(2, 0, 6, 1));

    private final JComboBox<String> cbLenguaje = new JComboBox<>(TIPOS);
    private final JPanel parametros = new JPanel(new CardLayout());
    private final JSpinner spLongitud = new JSpinner(new SpinnerNumberModel(8, 0, 64, 1));
    private final JTextField txtCabecera = new JTextField("10", 5);
    private final JTextField txtCola = new JTextField("01", 5);
    private final JLabel lblVeredicto = new JLabel(" ", SwingConstants.CENTER);

    private final JSpinner spN = new JSpinner(new SpinnerNumberModel(3, 0, 8, 1));
    private final JCheckBox chkSoloL = new JCheckBox("Solo las que pertenecen a L");

    private final JTextArea salida = Estilo.areaSalida(20);

    public PanelMensajes(Estado estado) {
        this.estado = estado;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        salida.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        add(construirControles(), BorderLayout.WEST);
        add(new JScrollPane(salida), BorderLayout.CENTER);
        refrescar();
    }

    private JPanel construirControles() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setPreferredSize(new Dimension(360, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;
        int f = 0;

        // --- Cadena ---
        c.gridy = f++; p.add(Estilo.titulo("Alfabeto y mensaje"), c);
        c.gridy = f++; p.add(Estilo.fila("Alfabeto Σ:", txtAlfabeto), c);
        c.gridy = f++; p.add(Estilo.fila("Mensaje w:", txtW), c);
        c.gridy = f++; p.add(Estilo.fila("Otra cadena v:", txtV), c);
        c.gridy = f++; p.add(Estilo.fila("Potencia n:", spPotencia), c);
        JButton btnAnalizar = new JButton("Analizar w");
        c.gridy = f++; p.add(btnAnalizar, c);

        // --- Lenguaje ---
        c.gridy = f++; p.add(Estilo.titulo("Validar contra un lenguaje L"), c);
        c.gridy = f++; p.add(Estilo.fila("Lenguaje:", cbLenguaje), c);
        parametros.add(Estilo.fila("n =", spLongitud), TIPOS[0]);
        parametros.add(new JLabel("Sin parámetros: cuenta los 1 del mensaje."), TIPOS[1]);
        JPanel pc = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        pc.add(new JLabel("Empieza con"));
        pc.add(txtCabecera);
        pc.add(new JLabel("termina con"));
        pc.add(txtCola);
        parametros.add(pc, TIPOS[2]);
        parametros.add(new JLabel("Sin parámetros: w debe ser igual a wᴿ."), TIPOS[3]);
        c.gridy = f++; p.add(parametros, c);
        JButton btnValidar = new JButton("Validar w ∈ L");
        JButton btnUsar = new JButton("Usar como mensaje");
        btnUsar.setToolTipText("Guarda w como el mensaje que se enviará en los módulos Canal y Verificador");
        c.gridy = f++; p.add(Estilo.botones(btnValidar, btnUsar), c);
        lblVeredicto.setFont(lblVeredicto.getFont().deriveFont(Font.BOLD, 18f));
        lblVeredicto.setOpaque(true);
        lblVeredicto.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        c.gridy = f++; p.add(lblVeredicto, c);

        // --- Σⁿ ---
        c.gridy = f++; p.add(Estilo.titulo("Generar Σⁿ"), c);
        c.gridy = f++; p.add(Estilo.fila("n =", spN), c);
        c.gridy = f++; p.add(chkSoloL, c);
        JButton btnSigma = new JButton("Generar Σⁿ");
        c.gridy = f++; p.add(btnSigma, c);

        c.gridy = f++; c.weighty = 1;
        p.add(new JPanel(), c);

        // --- Eventos ---
        btnAnalizar.addActionListener(e -> analizar());
        txtW.addActionListener(e -> analizar());
        btnValidar.addActionListener(e -> validar());
        btnUsar.addActionListener(e -> usarComoMensaje());
        btnSigma.addActionListener(e -> generarSigmaN());
        cbLenguaje.addActionListener(e -> {
            ((CardLayout) parametros.getLayout()).show(parametros, (String) cbLenguaje.getSelectedItem());
            lblVeredicto.setText(" ");
            lblVeredicto.setBackground(getBackground());
        });
        return p;
    }

    // ---------------- Lectura de datos ----------------

    private Set<Character> alfabeto() {
        return Cadenas.leerAlfabeto(txtAlfabeto.getText());
    }

    private Lenguaje lenguaje() {
        return switch (cbLenguaje.getSelectedIndex()) {
            case 0 -> Lenguaje.longitudFija((Integer) spLongitud.getValue());
            case 1 -> Lenguaje.paridadPar();
            case 2 -> Lenguaje.cabeceraYCola(txtCabecera.getText().trim(), txtCola.getText().trim());
            default -> Lenguaje.palindromos();
        };
    }

    /** Lee w y verifica que sea una cadena sobre Σ. Devuelve null si no lo es. */
    private String leerW(Set<Character> sigma) {
        String w = txtW.getText().trim();
        Character malo = Cadenas.simboloInvalido(w, sigma);
        if (malo != null) {
            Estilo.aviso(this, "El símbolo '" + malo + "' no pertenece a " + Cadenas.mostrarAlfabeto(sigma)
                    + ".\nPor eso w ∉ Σ*.");
            return null;
        }
        return w;
    }

    // ---------------- Acciones ----------------

    private void analizar() {
        try {
            Set<Character> sigma = alfabeto();
            String w = leerW(sigma);
            if (w == null) return;
            String v = txtV.getText().trim();
            int n = (Integer) spPotencia.getValue();

            StringBuilder sb = new StringBuilder();
            sb.append(Cadenas.mostrarAlfabeto(sigma)).append("\n");
            sb.append("w = ").append(Cadenas.mostrar(w)).append("   (w ∈ Σ* ✔)\n\n");
            sb.append("Longitud      |w| = ").append(Cadenas.longitud(w)).append("\n");
            sb.append("Inversa       wᴿ = ").append(Cadenas.mostrar(Cadenas.inversa(w))).append("\n");
            if (Cadenas.esCadenaSobre(v, sigma)) {
                sb.append("Concatenación w·v = ").append(Cadenas.mostrar(w)).append(" · ")
                  .append(Cadenas.mostrar(v)).append(" = ").append(Cadenas.mostrar(Cadenas.concatenar(w, v)))
                  .append("   (|w·v| = ").append(w.length() + v.length()).append(")\n");
            } else {
                sb.append("Concatenación: v no es una cadena sobre Σ\n");
            }
            sb.append("Potencia      w^").append(n).append(" = ")
              .append(Cadenas.mostrar(Cadenas.potencia(w, n))).append("\n");
            sb.append("¿Palíndromo?  ").append(Cadenas.esPalindromo(w) ? "sí" : "no").append("\n\n");

            sb.append("Apariciones de cada símbolo:\n");
            for (char s : sigma) {
                sb.append("   ").append(s).append(": ").append(Cadenas.contar(w, s)).append("\n");
            }
            sb.append("\nPrefijos: ").append(String.join(", ", Cadenas.prefijos(w))).append("\n");
            sb.append("Sufijos:  ").append(String.join(", ", Cadenas.sufijos(w))).append("\n");
            salida.setText(sb.toString());
            salida.setCaretPosition(0);
        } catch (IllegalArgumentException ex) {
            Estilo.aviso(this, ex.getMessage());
        }
    }

    private void validar() {
        try {
            Set<Character> sigma = alfabeto();
            String w = leerW(sigma);
            if (w == null) return;
            Lenguaje l = lenguaje();
            Lenguaje.Veredicto v = l.validar(w);

            lblVeredicto.setText(v.pertenece ? "w ∈ L  ✔  mensaje válido" : "w ∉ L  ✘  mensaje rechazado");
            lblVeredicto.setBackground(v.pertenece ? new java.awt.Color(229, 244, 228) : new java.awt.Color(251, 235, 235));
            lblVeredicto.setForeground(v.pertenece ? Estilo.VERDE : Estilo.ROJO);

            salida.setText(Cadenas.mostrarAlfabeto(sigma) + "\n"
                    + l.definicion() + "\n\n"
                    + "w = " + Cadenas.mostrar(w) + "\n"
                    + v.explicacion + "\n\n"
                    + "Conclusión: w " + (v.pertenece ? "∈" : "∉") + " L");
        } catch (IllegalArgumentException ex) {
            Estilo.aviso(this, ex.getMessage());
        }
    }

    private void usarComoMensaje() {
        try {
            Set<Character> sigma = alfabeto();
            String w = leerW(sigma);
            if (w == null) return;
            if (!Cadenas.esCadenaSobre(w, Set.of('0', '1')) || w.isEmpty()) {
                Estilo.aviso(this, "El mensaje que se envía por la red debe ser binario y no vacío (solo 0 y 1).");
                return;
            }
            estado.setMensaje(w);
            salida.setText("Mensaje guardado para el envío: " + w
                    + "\n\nSe usará en los módulos Canal (probabilidad de error)\ny Verificador (bit de paridad).");
        } catch (IllegalArgumentException ex) {
            Estilo.aviso(this, ex.getMessage());
        }
    }

    private void generarSigmaN() {
        try {
            Set<Character> sigma = alfabeto();
            int n = (Integer) spN.getValue();
            double total = Math.pow(sigma.size(), n);
            if (total > 4096) {
                Estilo.aviso(this, "|Σⁿ| = " + sigma.size() + "^" + n + " = " + (long) total
                        + " cadenas. Son demasiadas para mostrar; usa un n menor.");
                return;
            }
            List<String> todas = Cadenas.sigmaN(sigma, n);
            Lenguaje l = lenguaje();
            List<String> mostradas = new ArrayList<>();
            int enL = 0;
            for (String w : todas) {
                boolean pertenece = l.validar(w).pertenece;
                if (pertenece) enL++;
                if (!chkSoloL.isSelected() || pertenece) {
                    mostradas.add(Cadenas.mostrar(w) + (pertenece ? "  ∈ L" : ""));
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(Cadenas.mostrarAlfabeto(sigma)).append("\n");
            sb.append("Σ^").append(n).append(": cadenas de longitud ").append(n).append("\n");
            sb.append("|Σ^").append(n).append("| = |Σ|^").append(n).append(" = ")
              .append(sigma.size()).append("^").append(n).append(" = ").append(todas.size()).append("\n");
            sb.append(l.definicion()).append("\n");
            sb.append("|L ∩ Σ^").append(n).append("| = ").append(enL).append("\n\n");
            for (String s : mostradas) sb.append(s).append("\n");
            salida.setText(sb.toString());
            salida.setCaretPosition(0);
        } catch (IllegalArgumentException ex) {
            Estilo.aviso(this, ex.getMessage());
        }
    }

    @Override
    public void refrescar() {
        if (txtW.getText().isBlank()) txtW.setText(estado.getMensaje());
    }
}
