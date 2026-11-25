package view;

import model.Articulo;
import model.Proveedor;
import model.Rubro;
import service.ArticuloService;
import service.ProveedorService;
import service.RubroService;
import service.StockService;
import view.UiSupport;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

// Listado y administración de artículos; abre ArticuloForm para altas y modificaciones.
public class ArticuloList extends JFrame {
    
    private final ArticuloService articuloService = new ArticuloService();
    private final RubroService rubroService = new RubroService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final StockService stockService = new StockService();
    private final DefaultTableModel modelo = UiSupport.modelo("ID", "Código", "Descripción", "Rubro", "Proveedor", "Stock actual", "Stock mínimo", "Precio", "Activo");
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = UiSupport.campo(24);

    public ArticuloList() {
        super("SOFT.GS - Artículos");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1120, 650); setLocationRelativeTo(null);
        tabla.setAutoCreateRowSorter(true); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        JButton nuevo = UiSupport.boton("Nuevo artículo");
        JButton modificar = UiSupport.boton("Modificar");
        JButton baja = UiSupport.boton("Dar de baja/alta");
        JButton ingreso = UiSupport.boton("Ingresar stock");
        JButton salida = UiSupport.boton("Retirar stock");
        JButton actualizar = UiSupport.boton("Actualizar");
        JButton buscar = UiSupport.boton("Buscar");

        nuevo.addActionListener(e -> new ArticuloForm(this, null, this::cargarSeguro).setVisible(true));
        modificar.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            Integer id = UiSupport.idSeleccionado(tabla);
            Articulo seleccionado = articuloService.listarTodos().stream()
                    .filter(a -> a.getId().equals(id)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el artículo."));
            new ArticuloForm(this, seleccionado, this::cargarSeguro).setVisible(true);
        }));
        baja.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            Integer id = UiSupport.idSeleccionado(tabla);
            if (UiSupport.confirmar(this, "¿Desea desactivar/activar el artículo seleccionado?")) {
                articuloService.desactivar(id); cargar();
            }
        }));
        ingreso.addActionListener(e -> ajustarStock(true));
        salida.addActionListener(e -> ajustarStock(false));
        actualizar.addActionListener(e -> cargarSeguro());
        buscar.addActionListener(e -> filtrar(sorter));
        txtBuscar.addActionListener(e -> filtrar(sorter));

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Buscar por código o descripción:")); filtro.add(txtBuscar); filtro.add(buscar);
        JPanel acciones = UiSupport.acciones(nuevo, modificar, baja, ingreso, salida, actualizar);
        JPanel superior = new JPanel(new BorderLayout()); superior.add(filtro, BorderLayout.NORTH); superior.add(acciones, BorderLayout.SOUTH);
        setLayout(new BorderLayout(8, 8)); add(superior, BorderLayout.NORTH); add(new JScrollPane(tabla), BorderLayout.CENTER);
        cargarSeguro();
    }

    private void filtrar(TableRowSorter<DefaultTableModel> sorter) {
        String texto = txtBuscar.getText().trim();
        sorter.setRowFilter(texto.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), 1, 2));
    }

    private void ajustarStock(boolean incrementar) {
        UiSupport.ejecutar(this, () -> {
            Integer id = UiSupport.idSeleccionado(tabla);
            String entrada = JOptionPane.showInputDialog(this, "Cantidad a " + (incrementar ? "ingresar" : "retirar") + ":");
            if (entrada == null) return;
            int cantidad = UiSupport.enteroPositivo(entrada, "Cantidad");
            stockService.ajustar(id, cantidad, incrementar);
            cargar();
            UiSupport.exito(this, "Ajuste de stock registrado correctamente.");
        });
    }

    private void cargar() {
        Map<Integer, String> rubros = new HashMap<>();
        for (Rubro r : rubroService.listarTodos()) rubros.put(r.getId(), r.getCodigo() + " - " + r.getNombre());
        Map<Integer, String> proveedores = new HashMap<>();
        for (Proveedor p : proveedorService.listarTodos()) proveedores.put(p.getId(), p.getRazonSocial());
        modelo.setRowCount(0);
        for (Articulo a : articuloService.listarTodos()) {
            Integer idRubro = a.getRubro() == null ? null : a.getRubro().getId();
            Integer idProveedor = a.getProveedorHabitual() == null ? null : a.getProveedorHabitual().getId();
            modelo.addRow(new Object[]{a.getId(), a.getCodigo(), a.getDescripcion(),
                    rubros.getOrDefault(idRubro, String.valueOf(idRubro)),
                    proveedores.getOrDefault(idProveedor, String.valueOf(idProveedor)),
                    a.getStockActual(), a.getStockMinimo(), UiSupport.moneda(a.getPrecioUnitario()), a.isActivo()});
        }
    }

    private void cargarSeguro() { 
        UiSupport.ejecutar(this, this::cargar); 
    }
}