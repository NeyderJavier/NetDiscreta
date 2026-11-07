package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import modelo.Grafo;

/**
 * Ventana principal: un JTabbedPane con una pestaña por módulo.
 * Todas las pestañas comparten el mismo objeto Grafo (la red).
 */
public class VentanaPrincipal extends JFrame {

    private final Grafo red = new Grafo();

    public VentanaPrincipal() {
        super("NetDiscreta — Simulador de envío de mensajes en una red");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("1. Red", new PanelRed(red));
        pestanas.addTab("2. Grupos", new PanelPendiente("Grupos", "Conjuntos", "Fase 2"));
        pestanas.addTab("3. Conexiones", new PanelPendiente("Conexiones", "Producto cartesiano", "Fase 2"));
        pestanas.addTab("4. Mensajes", new PanelPendiente("Mensajes", "Alfabetos, cadenas y lenguajes", "Fase 2"));
        pestanas.addTab("5. Canal", new PanelPendiente("Canal", "Espacio muestral, eventos, variable aleatoria y binomial", "Fase 3"));
        pestanas.addTab("6. Verificador", new PanelPendiente("Verificador", "Álgebra booleana", "Fase 3"));

        JLabel pie = new JLabel("Matemáticas Discretas · Universidad Simón Bolívar");
        pie.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pestanas, BorderLayout.CENTER);
        getContentPane().add(pie, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }
}
