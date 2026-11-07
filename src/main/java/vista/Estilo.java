package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Colores y pequeños componentes que comparten todos los paneles.
 */
public final class Estilo {

    public static final Color AZUL = new Color(31, 58, 95);
    public static final Color NARANJA = new Color(230, 126, 34);
    public static final Color VERDE = new Color(39, 140, 80);
    public static final Color ROJO = new Color(192, 57, 43);
    public static final Color GRIS = new Color(160, 170, 185);
    public static final Color FONDO = new Color(250, 251, 253);

    private Estilo() { }

    public static JLabel titulo(String t) {
        JLabel l = new JLabel(t);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));
        return l;
    }

    public static JPanel fila(String etiqueta, Component comp) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        JLabel l = new JLabel(etiqueta);
        l.setPreferredSize(new Dimension(95, 22));
        p.add(l, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    public static JPanel botones(JButton... bs) {
        JPanel p = new JPanel(new GridLayout(1, bs.length, 4, 0));
        for (JButton b : bs) p.add(b);
        return p;
    }

    public static JTextArea areaSalida(int filas) {
        JTextArea a = new JTextArea(filas, 24);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        return a;
    }

    public static void aviso(Component padre, String msg) {
        JOptionPane.showMessageDialog(padre, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }
}
