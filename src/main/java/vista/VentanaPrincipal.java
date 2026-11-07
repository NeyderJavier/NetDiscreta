package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import modelo.Estado;

/**
 * Ventana principal: un JTabbedPane con una pestaña por módulo.
 * Todas las pestañas comparten el mismo Estado (red, grupos y mensaje).
 */
public class VentanaPrincipal extends JFrame {

    private final Estado estado = new Estado();

    public VentanaPrincipal() {
        super("NetDiscreta — Simulador de envío de mensajes en una red");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("1. Red", new PanelRed(estado.getRed()));
        pestanas.addTab("2. Grupos", new PanelGrupos(estado));
        pestanas.addTab("3. Conexiones", new PanelConexiones(estado));
        pestanas.addTab("4. Mensajes", new PanelMensajes(estado));
        pestanas.addTab("5. Canal", new PanelPendiente("Canal", "Espacio muestral, eventos, variable aleatoria y binomial", "Fase 3"));
        pestanas.addTab("6. Verificador", new PanelPendiente("Verificador", "Álgebra booleana", "Fase 3"));

        // Al cambiar de pestaña, el módulo se actualiza con los cambios hechos en los otros
        pestanas.addChangeListener(e -> {
            Component actual = pestanas.getSelectedComponent();
            if (actual instanceof Refrescable r) r.refrescar();
        });

        JLabel pie = new JLabel("Matemáticas Discretas · Universidad Simón Bolívar");
        pie.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pestanas, BorderLayout.CENTER);
        getContentPane().add(pie, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }
}
