package vista.graficos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import logica.ProductoCartesiano.Par;
import modelo.Conjunto;
import modelo.Nodo;
import vista.Estilo;

/**
 * Representa A × B en el plano cartesiano: los elementos de A en el eje X,
 * los de B en el eje Y y un punto por cada par ordenado.
 * Los pares de la relación R se pintan rellenos.
 */
public class LienzoPlano extends JPanel {

    private Conjunto a, b;
    private List<Par> pares = new ArrayList<>();

    public LienzoPlano() {
        setBackground(Estilo.FONDO);
        setPreferredSize(new Dimension(560, 420));
    }

    public void mostrar(Conjunto a, Conjunto b, List<Par> pares) {
        this.a = a;
        this.b = b;
        this.pares = pares;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (a == null || b == null || a.esVacio() || b.esVacio()) {
            g.setColor(Color.GRAY);
            g.drawString("Elige dos grupos no vacíos y genera A × B.", 20, 30);
            return;
        }

        List<Nodo> xs = new ArrayList<>(a.getElementos());
        List<Nodo> ys = new ArrayList<>(b.getElementos());
        int izq = 80, abajo = 60, arriba = 30, der = 30;
        int ox = izq, oy = getHeight() - abajo;
        double pasoX = (getWidth() - izq - der) / (double) (xs.size() + 1);
        double pasoY = (getHeight() - abajo - arriba) / (double) (ys.size() + 1);

        // Cuadrícula y marcas
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{4f, 4f}, 0f));
        for (int i = 0; i < xs.size(); i++) {
            int x = (int) (ox + (i + 1) * pasoX);
            g.setColor(new Color(225, 229, 235));
            g.drawLine(x, oy, x, arriba);
            g.setColor(Color.DARK_GRAY);
            String s = xs.get(i).getNombre();
            g.drawString(s, x - fm.stringWidth(s) / 2, oy + 18);
        }
        for (int j = 0; j < ys.size(); j++) {
            int y = (int) (oy - (j + 1) * pasoY);
            g.setColor(new Color(225, 229, 235));
            g.drawLine(ox, y, getWidth() - der, y);
            g.setColor(Color.DARK_GRAY);
            String s = ys.get(j).getNombre();
            g.drawString(s, ox - 8 - fm.stringWidth(s), y + 4);
        }

        // Ejes
        g.setStroke(new BasicStroke(2f));
        g.setColor(Estilo.AZUL);
        g.drawLine(ox, oy, getWidth() - der, oy);
        g.drawLine(ox, oy, ox, arriba);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.drawString(a.getNombre() + " (eje X)", getWidth() - der - 90, oy + 40);
        g.drawString(b.getNombre() + " (eje Y)", 8, arriba - 10 < 12 ? 14 : arriba - 10);

        // Puntos
        int rad = 7;
        for (Par p : pares) {
            int x = (int) (ox + (xs.indexOf(p.a) + 1) * pasoX);
            int y = (int) (oy - (ys.indexOf(p.b) + 1) * pasoY);
            if (p.enRelacion) {
                g.setColor(Estilo.NARANJA);
                g.fillOval(x - rad, y - rad, 2 * rad, 2 * rad);
            } else {
                g.setColor(Color.WHITE);
                g.fillOval(x - rad, y - rad, 2 * rad, 2 * rad);
                g.setColor(Estilo.GRIS);
                g.drawOval(x - rad, y - rad, 2 * rad, 2 * rad);
            }
        }

        // Leyenda
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        int lx = getWidth() - der - 210, ly = arriba + 4;
        g.setColor(Estilo.NARANJA);
        g.fillOval(lx, ly, 12, 12);
        g.setColor(Color.DARK_GRAY);
        g.drawString("(a, b) ∈ R: enlace directo", lx + 18, ly + 11);
        g.setColor(Estilo.GRIS);
        g.drawOval(lx, ly + 18, 12, 12);
        g.setColor(Color.DARK_GRAY);
        g.drawString("(a, b) ∉ R", lx + 18, ly + 29);
    }
}
