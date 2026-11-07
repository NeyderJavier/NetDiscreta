package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import vista.VentanaPrincipal;

/**
 * Punto de entrada de NetDiscreta.
 */
public class NetDiscreta {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, se usa el estilo por defecto de Swing
        }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
