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
        setMinimumSize(new Dimension(850, 560)); setSize(1000, 650); setLocationRelativeTo(null);
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
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(28, 48, 75));
        p.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));
        JLabel titulo = new JLabel("SOFT.GS"); titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 30f));
        JLabel subtitulo = new JLabel("Sistema de Gestión de Stock | Mayorista de artículos de computación");
        subtitulo.setForeground(new Color(220, 230, 242));
        p.add(titulo, BorderLayout.NORTH); p.add(subtitulo, BorderLayout.SOUTH); return p;
    }

    private JComponent panelNavegacion() {
        JPanel contenedor = new JPanel(new BorderLayout(10, 10));
        contenedor.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel titulo = new JLabel("Seleccione el módulo que desea utilizar");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JPanel grilla = new JPanel(new GridLayout(0, 2, 14, 14));
        grilla.add(tarjeta("Rubros", "Administrar categorías de artículos", () -> new RubroForm().setVisible(true)));
        grilla.add(tarjeta("Proveedores", "Registrar y consultar proveedores", () -> new ProveedorForm().setVisible(true)));
        grilla.add(tarjeta("Clientes", "Registrar y consultar clientes", () -> new ClienteForm().setVisible(true)));
        grilla.add(tarjeta("Artículos", "Altas, modificaciones y control del inventario", () -> new ArticuloList().setVisible(true)));
        grilla.add(tarjeta("Compras", "Registrar ingresos de mercadería", () -> new CompraForm().setVisible(true)));
        grilla.add(tarjeta("Facturación", "Crear, emitir, anular e imprimir facturas", () -> new FacturaForm().setVisible(true)));
        grilla.add(tarjeta("Stock bajo y reportes", "Consultar mínimos, compras y ventas por fecha", () -> new ReporteStockBajo().setVisible(true)));
        contenedor.add(titulo, BorderLayout.NORTH); contenedor.add(grilla, BorderLayout.CENTER); return contenedor;
    }

    private JPanel tarjeta(String titulo, String descripcion, Runnable accion) {
        JPanel tarjeta = new JPanel(new BorderLayout(8, 8));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 213, 222)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel nombre = new JLabel(titulo); nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 17f));
        JLabel texto = new JLabel("<html><body style='width:260px'>" + descripcion + "</body></html>");
        JButton abrir = UiSupport.boton("Abrir módulo"); abrir.addActionListener(e -> accion.run());
        tarjeta.add(nombre, BorderLayout.NORTH); tarjeta.add(texto, BorderLayout.CENTER); tarjeta.add(abrir, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JComponent pie() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(8, 24, 12, 24));
        JLabel estado = new JLabel("Aplicación creada por GRUPO 1: Castelli; Masud; Ortiz; Piñeyro - Para POO");
        estado.setForeground(new Color(95, 95, 95)); p.add(estado, BorderLayout.WEST); return p;
    }

    private void mostrarAlertaStock(Articulo articulo, int anterior, int nuevo) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                "El artículo " + articulo.getCodigo() + " - " + articulo.getDescripcion()
                        + " alcanzó el stock mínimo.\nStock anterior: " + anterior
                        + "\nStock actual: " + nuevo + "\nStock mínimo: " + articulo.getStockMinimo(),
                "Alerta de stock mínimo", JOptionPane.WARNING_MESSAGE));
    }
}