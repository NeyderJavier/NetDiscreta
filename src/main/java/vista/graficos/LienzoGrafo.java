package vista.graficos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import modelo.Arista;
import modelo.Grafo;
import modelo.Nodo;

/**
 * Dibuja el grafo con Graphics2D. Los nodos se pueden arrastrar con el mouse
 * y se puede resaltar un camino (por ejemplo, el resultado de Dijkstra).
 */
public class LienzoGrafo extends JPanel {

    private static final int RADIO = 24;
    private static final Color FONDO = new Color(250, 251, 253);
    private static final Color NODO = new Color(31, 58, 95);
    private static final Color ARISTA = new Color(160, 170, 185);
    private static final Color RESALTE = new Color(230, 126, 34);

    private final Grafo grafo;
    private List<Nodo> camino = new ArrayList<>();
    private Nodo arrastrado;

    public LienzoGrafo(Grafo grafo) {
        this.grafo = grafo;
        setBackground(FONDO);
        setPreferredSize(new Dimension(600, 450));

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { arrastrado = nodoEn(e.getX(), e.getY()); }
            @Override public void mouseReleased(MouseEvent e) { arrastrado = null; }
            @Override public void mouseDragged(MouseEvent e) {
                if (arrastrado != null) {
                    arrastrado.setPosicion(e.getX(), e.getY());
                    repaint();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    /** Resalta un camino; una lista vacía quita el resaltado. */
    public void setCamino(List<Nodo> camino) {
        this.camino = new ArrayList<>(camino);
        repaint();
    }

    private Nodo nodoEn(int x, int y) {
        for (Nodo n : grafo.getNodos()) {
            double dx = n.getX() - x, dy = n.getY() - y;
            if (dx * dx + dy * dy <= RADIO * RADIO) return n;
        }
        return null;
    }

    private boolean aristaEnCamino(Arista ar) {
        for (int i = 0; i + 1 < camino.size(); i++) {
            if (ar.une(camino.get(i), camino.get(i + 1))) return true;
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (grafo.getNodos().isEmpty()) {
            g.setColor(Color.GRAY);
            g.drawString("Agrega equipos o carga la red de ejemplo.", 20, 30);
            return;
        }

        // Aristas con su peso
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (Arista ar : grafo.getAristas()) {
            boolean enCamino = aristaEnCamino(ar);
            g.setColor(enCamino ? RESALTE : ARISTA);
            g.setStroke(new BasicStroke(enCamino ? 4f : 2f));
            int x1 = (int) ar.getA().getX(), y1 = (int) ar.getA().getY();
            int x2 = (int) ar.getB().getX(), y2 = (int) ar.getB().getY();
            g.drawLine(x1, y1, x2, y2);

            String peso = String.valueOf(ar.getPeso());
            FontMetrics fm = g.getFontMetrics();
            int mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
            int w = fm.stringWidth(peso) + 8;
            g.setColor(Color.WHITE);
            g.fillRoundRect(mx - w / 2, my - 9, w, 18, 8, 8);
            g.setColor(enCamino ? RESALTE : Color.DARK_GRAY);
            g.drawString(peso, mx - fm.stringWidth(peso) / 2, my + 5);
        }

        // Nodos
        g.setStroke(new BasicStroke(2f));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (Nodo n : grafo.getNodos()) {
            int x = (int) n.getX() - RADIO, y = (int) n.getY() - RADIO;
            g.setColor(camino.contains(n) ? RESALTE : NODO);
            g.fillOval(x, y, RADIO * 2, RADIO * 2);
            g.setColor(Color.WHITE);
            g.drawOval(x, y, RADIO * 2, RADIO * 2);
            FontMetrics fm = g.getFontMetrics();
            String s = n.getNombre();
            g.drawString(s, (int) n.getX() - fm.stringWidth(s) / 2, (int) n.getY() + 5);
        }
    }
}
