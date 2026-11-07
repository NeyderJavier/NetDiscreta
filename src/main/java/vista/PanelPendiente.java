package vista;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel temporal para los módulos que aún no se han desarrollado.
 * Se reemplaza por el panel real en cada fase.
 */
public class PanelPendiente extends JPanel {

    public PanelPendiente(String modulo, String tema, String fase) {
        setLayout(new BorderLayout());
        JLabel l = new JLabel("<html><div style='text-align:center'>"
                + "<h2>" + modulo + "</h2>"
                + "<p>Tema: " + tema + "</p><br>"
                + "<p style='color:gray'>En desarrollo — " + fase + "</p>"
                + "</div></html>", SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 14f));
        add(l, BorderLayout.CENTER);
    }
}
