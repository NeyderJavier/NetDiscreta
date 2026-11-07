package vista.graficos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import logica.OperacionesConjuntos.Operacion;
import modelo.Conjunto;
import modelo.Nodo;
import vista.Estilo;

/**
 * Diagrama de Venn de dos conjuntos dentro del universo U.
 * Coloca cada elemento en su región y sombrea el resultado de la operación.
 */
public class LienzoVenn extends JPanel {

    private Conjunto a, b, u;
    private Operacion operacion;

    public LienzoVenn() {
        setBackground(Estilo.FONDO);
        setPreferredSize(new Dimension(560, 420));
    }

    public void mostrar(Conjunto a, Conjunto b, Conjunto u, Operacion op) {
        this.a = a;
        this.b = b;
        this.u = u;
        this.operacion = op;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (a == null || b == null) {
            g.setColor(Color.GRAY);
            g.drawString("Crea dos grupos y elige una operación.", 20, 30);
            return;
        }

        int w = getWidth(), h = getHeight();
        int margen = 30;
        Rectangle2D rectU = new Rectangle2D.Double(margen, margen, w - 2 * margen, h - 2 * margen);
        double r = Math.min(rectU.getWidth() / 3.6, rectU.getHeight() / 2.6);
        double cy = rectU.getCenterY();
        double cxA = rectU.getCenterX() - r * 0.6, cxB = rectU.getCenterX() + r * 0.6;
        Ellipse2D circA = new Ellipse2D.Double(cxA - r, cy - r, 2 * r, 2 * r);
        Ellipse2D circB = new Ellipse2D.Double(cxB - r, cy - r, 2 * r, 2 * r);

        // Región sombreada según la operación
        if (operacion != null) {
            Area areaA = new Area(circA), areaB = new Area(circB);
            Area res = switch (operacion) {
                case UNION -> { Area x = new Area(areaA); x.add(areaB); yield x; }
                case INTERSECCION -> { Area x = new Area(areaA); x.intersect(areaB); yield x; }
                case DIFERENCIA_AB -> { Area x = new Area(areaA); x.subtract(areaB); yield x; }
                case DIFERENCIA_BA -> { Area x = new Area(areaB); x.subtract(areaA); yield x; }
                case SIMETRICA -> { Area x = new Area(areaA); x.exclusiveOr(areaB); yield x; }
                case COMPLEMENTO_A -> { Area x = new Area(rectU); x.subtract(areaA); yield x; }
                case COMPLEMENTO_B -> { Area x = new Area(rectU); x.subtract(areaB); yield x; }
            };
            g.setColor(new Color(230, 126, 34, 90));
            g.fill(res);
        }

        // Contornos
        g.setStroke(new BasicStroke(2f));
        g.setColor(Estilo.GRIS);
        g.draw(rectU);
        g.setColor(Estilo.AZUL);
        g.draw(circA);
        g.draw(circB);

        // Etiquetas de U, A y B
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(Color.DARK_GRAY);
        g.drawString("U", (int) rectU.getX() + 8, (int) rectU.getY() + 20);
        g.setColor(Estilo.AZUL);
        g.drawString(a.getNombre(), (int) (cxA - r * 0.7), (int) (cy - r * 0.8));
        g.drawString(b.getNombre(), (int) (cxB + r * 0.55), (int) (cy - r * 0.8));

        // Elementos en cada región
        List<String> soloA = new ArrayList<>(), ambos = new ArrayList<>(), soloB = new ArrayList<>(), fuera = new ArrayList<>();
        for (Nodo n : u.getElementos()) {
            boolean enA = a.contiene(n), enB = b.contiene(n);
            if (enA && enB) ambos.add(n.getNombre());
            else if (enA) soloA.add(n.getNombre());
            else if (enB) soloB.add(n.getNombre());
            else fuera.add(n.getNombre());
        }
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.setColor(Color.BLACK);
        escribirColumna(g, soloA, cxA - r * 0.5, cy);
        escribirColumna(g, ambos, (cxA + cxB) / 2, cy);
        escribirColumna(g, soloB, cxB + r * 0.5, cy);
        escribirFila(g, fuera, rectU.getCenterX(), rectU.getMaxY() - 12);
    }

    private void escribirColumna(Graphics2D g, List<String> items, double cx, double cy) {
        FontMetrics fm = g.getFontMetrics();
        int alto = fm.getHeight();
        double y0 = cy - (items.size() - 1) * alto / 2.0;
        for (int i = 0; i < items.size(); i++) {
            String s = items.get(i);
            g.drawString(s, (int) (cx - fm.stringWidth(s) / 2.0), (int) (y0 + i * alto + fm.getAscent() / 2.0));
        }
    }

    private void escribirFila(Graphics2D g, List<String> items, double cx, double y) {
        if (items.isEmpty()) return;
        String s = String.join("   ", items);
        g.drawString(s, (int) (cx - g.getFontMetrics().stringWidth(s) / 2.0), (int) y);
    }
}
