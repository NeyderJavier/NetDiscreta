package vista.graficos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.JPanel;
import logica.Paridad;
import vista.Estilo;

/**
 * Dibuja el circuito de paridad: una cadena de compuertas XOR
 * donde cada compuerta combina el resultado anterior con el siguiente bit.
 * En cada cable se escribe su valor (0 o 1); los cables en 1 se pintan en naranja.
 */
public class LienzoCircuito extends JPanel {

    private static final int MAX_BITS = 16;
    private String bits = "";
    private String titulo = "";
    private String salida = "P";

    public LienzoCircuito() {
        setBackground(Estilo.FONDO);
        setPreferredSize(new Dimension(560, 320));
    }

    /**
     * @param bits   bits de entrada del circuito
     * @param titulo texto superior
     * @param salida nombre de la salida (P en el emisor, S en el receptor)
     */
    public void mostrar(String bits, String titulo, String salida) {
        this.bits = bits;
        this.titulo = titulo;
        this.salida = salida;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Estilo.AZUL);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.drawString(titulo, 16, 22);

        if (bits.isEmpty()) {
            g.setColor(Color.GRAY);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.drawString("Escribe un mensaje binario para ver el circuito.", 16, 50);
            return;
        }
        if (bits.length() > MAX_BITS) {
            g.setColor(Color.GRAY);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.drawString("El circuito se dibuja para tramas de hasta " + MAX_BITS + " bits.", 16, 50);
            return;
        }

        int n = bits.length();
        int x0 = 70, arriba = 50, abajo = 20;
        double dy = (getHeight() - arriba - abajo) / (double) n;
        double dx = (getWidth() - x0 - 110) / (double) Math.max(1, n - 1);
        dx = Math.min(dx, 120);
        int gw = (int) Math.min(46, dx * 0.6);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();

        // Altura (y) de cada bit de entrada
        double[] y = new double[n];
        for (int i = 0; i < n; i++) y[i] = arriba + dy * (i + 0.5);

        // Etiquetas de entrada
        for (int i = 0; i < n; i++) {
            int b = Paridad.bit(bits.charAt(i));
            g.setColor(Color.DARK_GRAY);
            String et = "b" + (i + 1) + " = " + b;
            g.drawString(et, 8, (int) y[i] + 4);
        }

        if (n == 1) {
            int b = Paridad.bit(bits.charAt(0));
            cable(g, x0, y[0], getWidth() - 80, y[0], b);
            etiquetaSalida(g, getWidth() - 75, y[0], b);
            return;
        }

        // Cadena de compuertas
        double ySalidaAnterior = y[0];
        int valorAnterior = Paridad.bit(bits.charAt(0));
        double xSalidaAnterior = x0;
        for (int i = 1; i < n; i++) {
            int bi = Paridad.bit(bits.charAt(i));
            double gx = x0 + 20 + (i - 1) * dx;
            double yTop = ySalidaAnterior, yBot = y[i];
            double cy = (yTop + yBot) / 2;
            double h = (yBot - yTop) + 18;

            // Entrada superior: resultado anterior (o b1)
            cable(g, xSalidaAnterior, yTop, gx + 4, yTop, valorAnterior);
            // Entrada inferior: bit i
            cable(g, x0, yBot, gx + 4, yBot, bi);

            int nuevo = valorAnterior ^ bi;
            compuertaXor(g, gx, cy - h / 2, gw, h);

            valorAnterior = nuevo;
            ySalidaAnterior = cy;
            xSalidaAnterior = gx + gw;

            // Valor intermedio junto a la salida de la compuerta
            g.setColor(nuevo == 1 ? Estilo.NARANJA : Color.GRAY);
            g.drawString(String.valueOf(nuevo), (int) (gx + gw + 4), (int) cy - 4);
        }

        // Salida final
        double xFin = Math.min(getWidth() - 80, xSalidaAnterior + 40);
        cable(g, xSalidaAnterior, ySalidaAnterior, xFin, ySalidaAnterior, valorAnterior);
        etiquetaSalida(g, xFin + 4, ySalidaAnterior, valorAnterior);
    }

    private void cable(Graphics2D g, double x1, double y1, double x2, double y2, int valor) {
        g.setStroke(new BasicStroke(valor == 1 ? 2.5f : 1.5f));
        g.setColor(valor == 1 ? Estilo.NARANJA : Estilo.GRIS);
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    private void compuertaXor(Graphics2D g, double x, double y, double w, double h) {
        Path2D cuerpo = new Path2D.Double();
        cuerpo.moveTo(x, y);
        cuerpo.quadTo(x + w * 0.55, y, x + w, y + h / 2);
        cuerpo.quadTo(x + w * 0.55, y + h, x, y + h);
        cuerpo.quadTo(x + w * 0.3, y + h / 2, x, y);
        g.setColor(Color.WHITE);
        g.fill(cuerpo);
        g.setStroke(new BasicStroke(2f));
        g.setColor(Estilo.AZUL);
        g.draw(cuerpo);

        Path2D curvaXor = new Path2D.Double();
        curvaXor.moveTo(x - 7, y);
        curvaXor.quadTo(x - 7 + w * 0.3, y + h / 2, x - 7, y + h);
        g.draw(curvaXor);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.drawString("⊕", (int) (x + w * 0.35), (int) (y + h / 2 + 4));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    }

    private void etiquetaSalida(Graphics2D g, double x, double y, int valor) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(valor == 1 ? Estilo.NARANJA : Estilo.AZUL);
        g.drawString(salida + " = " + valor, (int) x, (int) y + 6);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    }
}
