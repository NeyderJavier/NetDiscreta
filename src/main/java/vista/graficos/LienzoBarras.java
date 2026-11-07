package vista.graficos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import logica.Binomial;
import logica.Evento;
import vista.Estilo;

/**
 * Gráfica de barras de la distribución binomial P(X = k).
 * Las barras del evento elegido se pintan en naranja y, si hay simulación,
 * se marca con un rombo la frecuencia relativa observada.
 */
public class LienzoBarras extends JPanel {

    private static final Color BARRA = new Color(160, 180, 210);

    private Binomial binomial;
    private Evento evento;
    private int k;
    private double[] simulada;   // frecuencias relativas, o null

    public LienzoBarras() {
        setBackground(Estilo.FONDO);
        setPreferredSize(new Dimension(560, 300));
    }

    public void mostrar(Binomial b, Evento evento, int k, double[] simulada) {
        this.binomial = b;
        this.evento = evento;
        this.k = k;
        this.simulada = simulada;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (binomial == null) return;
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int n = binomial.getN();
        int izq = 60, der = 20, arriba = 40, abajo = 45;
        int ancho = getWidth() - izq - der, alto = getHeight() - arriba - abajo;
        int oy = arriba + alto;

        // Escala vertical: el mayor valor redondeado hacia arriba a un múltiplo de 0.1
        double max = 0;
        for (int i = 0; i <= n; i++) max = Math.max(max, binomial.probabilidad(i));
        if (simulada != null) for (double v : simulada) max = Math.max(max, v);
        max = Math.min(1.0, Math.ceil(max * 10 + 1e-9) / 10.0);
        if (max == 0) max = 0.1;

        // Título
        g.setColor(Estilo.AZUL);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.drawString(String.format("X ~ B(n = %d, p = %.2f)   ·   P(X = k)", n, binomial.getP()), izq, 22);

        // Líneas guía del eje Y
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i <= 5; i++) {
            double v = max * i / 5;
            int y = (int) (oy - alto * v / max);
            g.setColor(new Color(228, 232, 238));
            g.drawLine(izq, y, izq + ancho, y);
            g.setColor(Color.DARK_GRAY);
            String s = String.format("%.2f", v);
            g.drawString(s, izq - 8 - fm.stringWidth(s), y + 4);
        }

        // Barras
        double paso = ancho / (double) (n + 1);
        int anchoBarra = (int) Math.max(4, paso * 0.6);
        for (int i = 0; i <= n; i++) {
            double v = binomial.probabilidad(i);
            int cx = (int) (izq + paso * (i + 0.5));
            int h = (int) Math.round(alto * v / max);
            boolean enEvento = evento != null && evento.ocurre(i, k);
            g.setColor(enEvento ? Estilo.NARANJA : BARRA);
            g.fillRect(cx - anchoBarra / 2, oy - h, anchoBarra, h);

            // Valor sobre la barra si hay espacio (por encima del rombo de la simulación)
            if (n <= 12 && v >= 0.0005) {
                int yTexto = oy - h - 4;
                if (simulada != null && i < simulada.length) {
                    yTexto = Math.min(yTexto, (int) (oy - alto * simulada[i] / max) - 9);
                }
                g.setColor(Color.DARK_GRAY);
                String s = String.format("%.3f", v);
                g.drawString(s, cx - fm.stringWidth(s) / 2, yTexto);
            }
            // Etiqueta k
            if (n <= 25 || i % 5 == 0) {
                g.setColor(Color.DARK_GRAY);
                String s = String.valueOf(i);
                g.drawString(s, cx - fm.stringWidth(s) / 2, oy + 15);
            }
            // Frecuencia simulada
            if (simulada != null && i < simulada.length) {
                int sy = (int) (oy - alto * simulada[i] / max);
                Polygon rombo = new Polygon(new int[]{cx, cx + 6, cx, cx - 6}, new int[]{sy - 6, sy, sy + 6, sy}, 4);
                g.setColor(Color.BLACK);
                g.fillPolygon(rombo);
            }
        }

        // Eje X
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(Estilo.AZUL);
        g.drawLine(izq, oy, izq + ancho, oy);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.drawString("k = número de bits con error", izq + ancho / 2 - 80, oy + 35);

        // Leyenda
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        int lx = izq + ancho - 230, ly = 30;
        if (evento != null) {
            g.setColor(Estilo.NARANJA);
            g.fillRect(lx, ly, 12, 10);
            g.setColor(Color.DARK_GRAY);
            g.drawString("Evento " + evento.condicion(k), lx + 18, ly + 10);
        }
        if (simulada != null) {
            g.setColor(Color.BLACK);
            g.fillPolygon(new int[]{lx + 120, lx + 126, lx + 120, lx + 114}, new int[]{ly - 1, ly + 5, ly + 11, ly + 5}, 4);
            g.setColor(Color.DARK_GRAY);
            g.drawString("Simulación", lx + 132, ly + 10);
        }
    }
}
