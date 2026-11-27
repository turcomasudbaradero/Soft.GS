package view;

import model.Articulo;
import notification.StockNotifier;
import notification.StockObserver;
import service.ArticuloService;
import service.StockService;
import util.JpaUtil;
import view.ArticuloList;
import view.ClienteForm;
import view.CompraForm;
import view.FacturaForm;
import view.ProveedorForm;
import view.ReporteStockBajo;
import view.RubroForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Menú principal SOFT.GS: abre cada módulo de la interfaz en su ventana independiente.
public class MenuPrincipal extends JFrame {
    
    private final StockObserver observadorStock = this::mostrarAlertaStock;

    public MenuPrincipal() {
        super("SOFT.GS - Software de Gestión de Stock");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(650, 620));
        setSize(670, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));
        add(encabezado(), BorderLayout.NORTH);
        add(panelNavegacion(), BorderLayout.CENTER);
        add(pie(), BorderLayout.SOUTH);
        StockNotifier.getInstancia().registrar(observadorStock);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (UiSupport.confirmar(MenuPrincipal.this, "¿Desea salir de SOFT.GS?")) {
                    StockNotifier.getInstancia().quitar(observadorStock);
                    JpaUtil.cerrar(); dispose(); System.exit(0);
                }
            }
        });
    }

    private JComponent encabezado() {

        // Color de fondo claro del encabezado.
        Color fondo = new Color(230, 238, 246);
        Color textoPrincipal = new Color(28, 48, 75);
        Color textoSecundario = new Color(64, 86, 111);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(fondo);
        p.setBorder(BorderFactory.createEmptyBorder(15, 28, 15, 28));

        // Cargar el logo desde la carpeta resources.
        java.net.URL recurso = getClass().getResource("/imagenes/logo.png");

        JLabel imagen = new JLabel();

        if (recurso != null) {
            ImageIcon icono = new ImageIcon(recurso);

            // Ajustar el tamaño del logo.
            Image imagenEscalada = icono.getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH);

            imagen.setIcon(new ImageIcon(imagenEscalada));
        }

        // Panel que contiene el logo y los textos.
        JPanel encabezado = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        encabezado.setBackground(fondo);

        // Título principal.
        JLabel titulo = new JLabel("SOFT.GS");
        titulo.setForeground(textoPrincipal);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 30f));

        // Subtítulo.
        JLabel subtitulo = new JLabel("Software de Gestión de Stock | Mayorista de artículos de computación");
        subtitulo.setForeground(textoSecundario);

        // Panel para organizar los textos verticalmente.
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setBackground(fondo);
        textos.add(titulo);
        textos.add(subtitulo);

        // Agregar logo y textos al encabezado.
        encabezado.add(imagen);
        encabezado.add(textos);

        p.add(encabezado, BorderLayout.CENTER);

        return p;
    }

    private JComponent panelNavegacion() {
        
        JPanel contenedor = new JPanel(new BorderLayout(10, 18));
        contenedor.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        // Título de la sección.
        JLabel titulo = new JLabel("Seleccione la SECCIÓN que desea utilizar");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        // Grilla de secciones: dos columnas.
        JPanel grilla = new JPanel(new GridLayout(0, 2, 10, 10));

        grilla.add(contenedorTarjeta(tarjeta("Rubros", "Administrar categorías de artículos", () -> new RubroForm().setVisible(true))));
        grilla.add(contenedorTarjeta(tarjeta("Proveedores", "Registrar y consultar proveedores", () -> new ProveedorForm().setVisible(true))));
        grilla.add(contenedorTarjeta(tarjeta("Clientes", "Registrar y consultar clientes", () -> new ClienteForm().setVisible(true))));
        grilla.add(contenedorTarjeta(tarjeta("Artículos", "Altas, modificaciones y control del inventario", () -> new ArticuloList().setVisible(true))));
        grilla.add(contenedorTarjeta(tarjeta("Compras", "Registrar ingresos de mercadería", () -> new CompraForm().setVisible(true))));
        grilla.add(contenedorTarjeta(tarjeta("Facturación", "Crear, emitir, anular e imprimir facturas", () -> new FacturaForm().setVisible(true))));
        grilla.add(contenedorTarjeta(tarjeta("Stock bajo y reportes", "Consultar mínimos, compras y ventas por fecha", () -> new ReporteStockBajo().setVisible(true))));

        // No establecer un tamaño fijo para la grilla.
        // El JScrollPane permitirá desplazarse por todas las tarjetas.
        JScrollPane scroll = new JScrollPane(grilla);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        contenedor.add(titulo, BorderLayout.NORTH);
        contenedor.add(scroll, BorderLayout.CENTER);

        return contenedor;
        
    }


    private JPanel tarjeta(String titulo, String descripcion, Runnable accion) {

        JPanel tarjeta = new JPanel(new BorderLayout(6, 3));

        // Borde exterior y márgenes internos.
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 213, 222)),
                BorderFactory.createEmptyBorder(5, 9, 5, 9)));

        // Panel izquierdo: título y descripción.
        JPanel informacion = new JPanel(new BorderLayout(3, 3));

        JLabel nombre = new JLabel(titulo);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 13f));

        JLabel texto = new JLabel("<html><body style='width:170px'>" + descripcion + "</body></html>");

        texto.setForeground(new Color(85, 85, 85));
        texto.setFont(texto.getFont().deriveFont(10f));

        informacion.add(nombre, BorderLayout.NORTH);
        informacion.add(texto, BorderLayout.CENTER);

        JButton abrir = UiSupport.boton("Abrir");

        abrir.setPreferredSize(new Dimension(70, 38));
        abrir.setMinimumSize(new Dimension(70, 38));

        abrir.addActionListener(e -> {
            if (accion != null) {
                accion.run();
            }
        });

        tarjeta.add(informacion, BorderLayout.CENTER);
        tarjeta.add(abrir, BorderLayout.EAST);

        // Todas las tarjetas tendrán las mismas dimensiones.
        Dimension tamano = new Dimension(270, 78);

        tarjeta.setPreferredSize(tamano);
        tarjeta.setMinimumSize(tamano);
        tarjeta.setMaximumSize(tamano);

        return tarjeta;
    }

    private JPanel contenedorTarjeta(JPanel tarjeta) {

        // El FlowLayout mantiene la tarjeta en su tamaño preferido.
        JPanel contenedor = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        contenedor.setOpaque(false);
        contenedor.add(tarjeta);

        return contenedor;
    }

    
    private JComponent pie() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(8, 24, 12, 24));
        JLabel estado = new JLabel("Aplicación creada por ©GRUPO 1: Castelli; Masud; Ortiz; Piñeyro - Para POO - UNSAdA - 2026");
        estado.setForeground(new Color(95, 95, 95)); p.add(estado, BorderLayout.WEST); 
        return p;
    }

    private void mostrarAlertaStock(Articulo articulo, int anterior, int nuevo) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                "El artículo " + articulo.getCodigo() + " - " + articulo.getDescripcion()
                        + " alcanzó el stock mínimo.\nStock anterior: " + anterior
                        + "\nStock actual: " + nuevo + "\nStock mínimo: " + articulo.getStockMinimo(),
                "Alerta de stock mínimo", JOptionPane.WARNING_MESSAGE));
    }
}