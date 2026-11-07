package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import logica.Binomial;
import logica.EspacioMuestral;
import logica.Evento;
import logica.Paridad;
import logica.SimuladorCanal;
import modelo.Estado;
import vista.graficos.LienzoBarras;

/**
 * Módulo 5 — Canal (Espacio muestral, eventos, variable aleatoria y binomial).
 * Cada bit enviado puede llegar con error con probabilidad p.
 * X = número de bits con error sigue una distribución binomial B(n, p).
 */
public class PanelCanal extends JPanel implements Refrescable {

    private final Estado estado;
    private final SimuladorCanal canal = new SimuladorCanal();
    private final LienzoBarras barras = new LienzoBarras();

    private final JLabel lblMensaje = new JLabel();
    private final JSpinner spN = new JSpinner(new SpinnerNumberModel(8, 1, 60, 1));
    private final JSpinner spP = new JSpinner(new SpinnerNumberModel(0.05, 0.0, 1.0, 0.01));
    private final JComboBox<Evento> cbEvento = new JComboBox<>(Evento.values());
    private final JSpinner spK = new JSpinner(new SpinnerNumberModel(1, 0, 60, 1));
    private final JSpinner spEnvios = new JSpinner(new SpinnerNumberModel(1000, 10, 1_000_000, 100));
    private final JTextArea salida = Estilo.areaSalida(12);

    private double[] simulada;   // última simulación (frecuencias relativas)

    public PanelCanal(Estado estado) {
        this.estado = estado;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        salida.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        ((JSpinner.NumberEditor) spP.getEditor()).getFormat().applyPattern("0.00");

        JSplitPane centro = new JSplitPane(JSplitPane.VERTICAL_SPLIT, barras, new JScrollPane(salida));
        centro.setResizeWeight(0.55);
        centro.setBorder(null);
        add(centro, BorderLayout.CENTER);
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

        c.gridy = f++; p.add(Estilo.titulo("Canal"), c);
        c.gridy = f++; p.add(Estilo.fila("Trama:", lblMensaje), c);
        c.gridy = f++; p.add(Estilo.fila("n (bits):", spN), c);
        c.gridy = f++; p.add(Estilo.fila("p (error/bit):", spP), c);
        JButton btnUsarTrama = new JButton("n = tamaño de la trama");
        btnUsarTrama.setToolTipText("n = |mensaje| + 1 (bit de paridad)");
        c.gridy = f++; p.add(btnUsarTrama, c);

        c.gridy = f++; p.add(Estilo.titulo("Evento"), c);
        c.gridy = f++; p.add(Estilo.fila("Evento:", cbEvento), c);
        c.gridy = f++; p.add(Estilo.fila("k =", spK), c);
        JButton btnOmega = new JButton("Ver espacio muestral Ω");
        c.gridy = f++; p.add(btnOmega, c);

        c.gridy = f++; p.add(Estilo.titulo("Simulación"), c);
        c.gridy = f++; p.add(Estilo.fila("N envíos:", spEnvios), c);
        JButton btnSimular = new JButton("Simular N envíos");
        c.gridy = f++; p.add(btnSimular, c);

        c.gridy = f++; p.add(Estilo.titulo("Enviar el mensaje"), c);
        JButton btnEnviar = new JButton("Enviar trama una vez");
        btnEnviar.setToolTipText("Envía mensaje + bit de paridad. El resultado pasa al Verificador.");
        c.gridy = f++; p.add(btnEnviar, c);

        c.gridy = f++; c.weighty = 1;
        p.add(new JPanel(), c);

        // Eventos: cualquier cambio de parámetros recalcula
        spN.addChangeListener(e -> { simulada = null; calcular(); });
        spP.addChangeListener(e -> { simulada = null; estado.setProbabilidadError(p()); calcular(); });
        cbEvento.addActionListener(e -> calcular());
        spK.addChangeListener(e -> calcular());
        btnUsarTrama.addActionListener(e -> spN.setValue(estado.getMensaje().length() + 1));
        btnOmega.addActionListener(e -> verOmega());
        btnSimular.addActionListener(e -> simular());
        btnEnviar.addActionListener(e -> enviar());
        return p;
    }

    private int n() { return (Integer) spN.getValue(); }
    private double p() { return ((Number) spP.getValue()).doubleValue(); }
    private int k() { return Math.min((Integer) spK.getValue(), n()); }
    private Evento evento() { return (Evento) cbEvento.getSelectedItem(); }

    // ---------------- Acciones ----------------

    /** Distribución completa, medidas y probabilidad del evento elegido. */
    private void calcular() {
        Binomial b = new Binomial(n(), p());
        Evento ev = evento();
        int k = k();
        spK.setEnabled(ev.usaK());
        barras.mostrar(b, ev, k, simulada);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("X = número de bits con error al enviar n = %d bits%n", n()));
        sb.append(String.format("X ~ B(n = %d, p = %.2f)%n", n(), p()));
        sb.append("P(X = k) = C(n, k) · p^k · (1 − p)^(n − k)\n\n");

        sb.append(String.format("Evento: %s  →  %s%n", ev.nombre, ev.condicion(k)));
        sb.append(String.format("P(%s) = %.6f  (%.2f %%)%n", ev.condicion(k), ev.probabilidad(b, k), 100 * ev.probabilidad(b, k)));
        if (ev == Evento.ALGUN_ERROR) {
            sb.append(String.format("   = 1 − P(X = 0) = 1 − %.6f%n", b.probabilidad(0)));
        }
        sb.append("\n");

        sb.append(String.format("E[X] = n·p = %.4f   Var(X) = n·p·(1−p) = %.4f   σ = %.4f%n%n",
                b.media(), b.varianza(), b.desviacion()));

        sb.append("  k   C(n,k)        P(X = k)    P(X ≤ k)");
        if (simulada != null) sb.append("   simulada");
        sb.append("\n");
        for (int i = 0; i <= n(); i++) {
            sb.append(String.format("%3d   %-12.0f  %.6f    %.6f", i, Binomial.combinaciones(n(), i),
                    b.probabilidad(i), b.acumulada(i)));
            if (simulada != null) sb.append(String.format("   %.6f", simulada[i]));
            sb.append(ev.ocurre(i, k) ? "   ◀" : "").append("\n");
        }
        salida.setText(sb.toString());
        salida.setCaretPosition(0);
    }

    private void verOmega() {
        int n = n();
        double p = p();
        if (n > 5) {
            salida.setText(String.format("Ω = {C, E}^%d   (C = bit correcto, E = bit con error)%n|Ω| = 2^%d = %d resultados.%n%n"
                    + "Son demasiados para listarlos; baja n a 5 o menos para verlos todos.%n%n"
                    + "Cada resultado ω con x errores tiene probabilidad p^x · (1 − p)^(n − x),%n"
                    + "y hay C(n, x) resultados con x errores. Por eso P(X = x) = C(n, x) · p^x · (1 − p)^(n − x).",
                    n, n, EspacioMuestral.tamano(n)));
            return;
        }
        List<EspacioMuestral.Resultado> omega = EspacioMuestral.generar(n, p);
        Evento ev = evento();
        int k = k();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Ω = {C, E}^%d   (C = correcto, E = error)   |Ω| = 2^%d = %d%n", n, n, omega.size()));
        sb.append(String.format("Evento A: %s → %s%n%n", ev.nombre, ev.condicion(k)));
        sb.append("  ω        X(ω)   P(ω)         ¿ω ∈ A?\n");
        double suma = 0;
        int enA = 0;
        for (EspacioMuestral.Resultado r : omega) {
            boolean ocurre = ev.ocurre(r.errores, k);
            if (ocurre) { suma += r.probabilidad; enA++; }
            sb.append(String.format("  %-8s %-6d %.6f     %s%n", r.valor, r.errores, r.probabilidad, ocurre ? "sí" : ""));
        }
        sb.append(String.format("%n|A| = %d resultados%nP(A) = suma de P(ω) con ω ∈ A = %.6f%n", enA, suma));
        salida.setText(sb.toString());
        salida.setCaretPosition(0);
    }

    private void simular() {
        int envios = (Integer) spEnvios.getValue();
        int[] frec = canal.simular(n(), p(), envios);
        simulada = new double[frec.length];
        for (int i = 0; i < frec.length; i++) simulada[i] = frec[i] / (double) envios;
        calcular();

        Binomial b = new Binomial(n(), p());
        Evento ev = evento();
        int k = k();
        int casos = 0;
        double media = 0;
        for (int i = 0; i < frec.length; i++) {
            if (ev.ocurre(i, k)) casos += frec[i];
            media += i * (double) frec[i];
        }
        media /= envios;
        String resumen = String.format("Simulación de %d envíos%n"
                + "Frecuencia relativa de %s: %d / %d = %.4f   (teórica %.4f)%n"
                + "Promedio de errores observado: %.4f   (E[X] = %.4f)%n"
                + "Con más envíos, la frecuencia se acerca a la probabilidad teórica.%n"
                + "─────────────────────────────────────────────%n",
                envios, ev.condicion(k), casos, envios, casos / (double) envios, ev.probabilidad(b, k), media, b.media());
        salida.setText(resumen + salida.getText());
        salida.setCaretPosition(0);
    }

    private void enviar() {
        String mensaje = estado.getMensaje();
        String trama = Paridad.agregarParidad(mensaje);
        SimuladorCanal.Transmision t = canal.transmitir(trama, p());
        estado.registrarEnvio(t.enviada, t.recibida, t.errores());

        StringBuilder marcas = new StringBuilder();
        for (int i = 0; i < trama.length(); i++) marcas.append(t.posicionesError.contains(i) ? '^' : ' ');

        Binomial b = new Binomial(trama.length(), p());
        salida.setText(String.format("Mensaje w        = %s%n"
                + "Bit de paridad P = %s%n%n"
                + "Enviada          = %s%n"
                + "Recibida         = %s%n"
                + "                   %s%n"
                + "Bits con error: %d %s%n%n"
                + "Para una trama de n = %d bits con p = %.2f:%n"
                + "P(X = %d) = %.6f%n%n"
                + "Ve al módulo 6 (Verificador) para comprobar si el receptor detecta el error.",
                mensaje, Paridad.bitParidad(mensaje), t.enviada, t.recibida, marcas,
                t.errores(), t.posicionesError.isEmpty() ? "" : "(posiciones " + posiciones(t.posicionesError) + ")",
                trama.length(), p(), t.errores(), b.probabilidad(t.errores())));
    }

    private static String posiciones(List<Integer> pos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pos.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(pos.get(i) + 1);
        }
        return sb.toString();
    }

    @Override
    public void refrescar() {
        String m = estado.getMensaje();
        lblMensaje.setText(m + " + P  (" + (m.length() + 1) + " bits)");
        calcular();
    }
}
