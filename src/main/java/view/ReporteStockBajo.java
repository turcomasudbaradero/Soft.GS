package view;

import model.Articulo;
import model.Compra;
import model.Factura;
import service.CompraService;
import service.FacturaService;
import service.StockService;
import view.UiSupport;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Consultas de stock bajo, compras por fecha y ventas por fecha.
public class ReporteStockBajo extends JFrame {
    
    private final StockService stockService = new StockService();
    private final CompraService compraService = new CompraService();
    private final FacturaService facturaService = new FacturaService();

    private final DefaultTableModel stockModel = UiSupport.modelo("ID", "Código", "Descripción", "Stock actual", "Stock mínimo", "Unidades faltantes");
    private final DefaultTableModel comprasModel = UiSupport.modelo("ID", "Comprobante", "Fecha", "Proveedor", "Total", "Estado");
    private final DefaultTableModel ventasModel = UiSupport.modelo("ID", "Factura", "Fecha", "Cliente", "Total", "Estado");
    private final JTextField compraDesde = UiSupport.campo(10), compraHasta = UiSupport.campo(10);
    private final JTextField ventaDesde = UiSupport.campo(10), ventaHasta = UiSupport.campo(10);

    public ReporteStockBajo() {
        super("SOFT.GS - Consultas y reportes");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1050, 650); setLocationRelativeTo(null);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Stock bajo", panelStock());
        tabs.addTab("Compras por fecha", panelCompras());
        tabs.addTab("Ventas por fecha", panelVentas());
        setLayout(new BorderLayout()); add(tabs, BorderLayout.CENTER);
        cargarStockSeguro();
    }

    private JPanel panelStock() {
        JTable tabla = new JTable(stockModel); tabla.setAutoCreateRowSorter(true);
        JButton actualizar = UiSupport.boton("Actualizar artículos con stock bajo");
        actualizar.addActionListener(e -> cargarStockSeguro());
        JLabel ayuda = new JLabel("Se muestran los artículos activos cuyo stock actual es menor o igual al stock mínimo.");
        JPanel p = new JPanel(new BorderLayout(8, 8)); p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(ayuda, BorderLayout.NORTH); p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        p.add(actualizar, BorderLayout.SOUTH); return p;
    }

    private JPanel panelCompras() {
        inicializarFechas(compraDesde, compraHasta);
        JTable tabla = new JTable(comprasModel); tabla.setAutoCreateRowSorter(true);
        JButton consultar = UiSupport.boton("Consultar compras");
        consultar.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            LocalDate d = UiSupport.leerFecha(compraDesde.getText(), "Desde");
            LocalDate h = UiSupport.leerFecha(compraHasta.getText(), "Hasta");
            if (d.isAfter(h)) throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
            LocalDateTime desde = d.atStartOfDay(), hasta = h.atTime(23, 59, 59);
            comprasModel.setRowCount(0);
            for (Compra c : compraService.listarComprasPorFecha(desde, hasta)) {
                comprasModel.addRow(new Object[]{c.getId(), c.getNumeroComprobante(), UiSupport.fecha(c.getFechaCompra()),
                        c.getProveedor().getRazonSocial(), UiSupport.moneda(c.getTotal()), c.getEstado()});
            }
        }));
        JPanel filtro = filtroFechas(compraDesde, compraHasta, consultar);
        JPanel p = new JPanel(new BorderLayout(8, 8)); p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(filtro, BorderLayout.NORTH); p.add(new JScrollPane(tabla), BorderLayout.CENTER); return p;
    }

    private JPanel panelVentas() {
        inicializarFechas(ventaDesde, ventaHasta);
        JTable tabla = new JTable(ventasModel); tabla.setAutoCreateRowSorter(true);
        JButton consultar = UiSupport.boton("Consultar facturas");
        consultar.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            LocalDate d = UiSupport.leerFecha(ventaDesde.getText(), "Desde");
            LocalDate h = UiSupport.leerFecha(ventaHasta.getText(), "Hasta");
            if (d.isAfter(h)) throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
            ventasModel.setRowCount(0);
            for (Factura f : facturaService.listarPorFecha(d.atStartOfDay(), h.atTime(23, 59, 59))) {
                ventasModel.addRow(new Object[]{f.getId(), f.getNumeroFactura(), UiSupport.fecha(f.getFechaEmision()),
                        f.getCliente().getNombreCompleto(), UiSupport.moneda(f.getTotal()), f.getEstado()});
            }
        }));
        JPanel filtro = filtroFechas(ventaDesde, ventaHasta, consultar);
        JPanel p = new JPanel(new BorderLayout(8, 8)); p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(filtro, BorderLayout.NORTH); p.add(new JScrollPane(tabla), BorderLayout.CENTER); return p;
    }

    private JPanel filtroFechas(JTextField desde, JTextField hasta, JButton consultar) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Desde (AAAA-MM-DD):")); p.add(desde);
        p.add(new JLabel("Hasta (AAAA-MM-DD):")); p.add(hasta); p.add(consultar); return p;
    }

    private void inicializarFechas(JTextField desde, JTextField hasta) {
        LocalDate hoy = LocalDate.now();
        desde.setText(hoy.withDayOfMonth(1).toString()); hasta.setText(hoy.toString());
        desde.setToolTipText("Formato AAAA-MM-DD"); hasta.setToolTipText("Formato AAAA-MM-DD");
    }

    private void cargarStock() {
        stockModel.setRowCount(0);
        for (Articulo a : stockService.listarStockBajo()) {
            stockModel.addRow(new Object[]{a.getId(), a.getCodigo(), a.getDescripcion(), a.getStockActual(),
                    a.getStockMinimo(), a.getStockMinimo() - a.getStockActual()});
        }
    }

    private void cargarStockSeguro() { UiSupport.ejecutar(this, this::cargarStock); }
}