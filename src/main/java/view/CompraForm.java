package view;

import model.Articulo;
import model.Compra;
import model.DetalleCompra;
import model.Proveedor;
import enums.EstadoCompra;
import service.ArticuloService;
import service.CompraService;
import service.ProveedorService;
import view.UiSupport;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Ventana para crear borradores de compra, administrar sus detalles y confirmar ingresos de stock.
public class CompraForm extends JFrame {
    
    private final CompraService compraService = new CompraService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final ArticuloService articuloService = new ArticuloService();

    private final JTextField txtNumero = UiSupport.campo(16);
    private final JTextField txtObservaciones = UiSupport.campo(25);
    private final JTextField txtIdCompra = UiSupport.campo(10);
    private final JComboBox<Proveedor> cbProveedor = new JComboBox<>();
    private final JComboBox<Articulo> cbArticulo = new JComboBox<>();
    private final JTextField txtCantidad = UiSupport.campo(8);
    private final JTextField txtPrecioCompra = UiSupport.campo(10);
    private final JLabel lblEstado = new JLabel("Sin compra seleccionada");
    private final JLabel lblTotal = new JLabel("Total: $ 0,00");
    private final DefaultTableModel modelo = UiSupport.modelo("ID artículo", "Código", "Descripción", "Cantidad", "Precio compra", "Subtotal");
    private final JTable tabla = new JTable(modelo);
    private Integer idActual;

    public CompraForm() {
        super("SOFT.GS - Compras a proveedores");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1100, 700); setLocationRelativeTo(null);
        tabla.setAutoCreateRowSorter(true); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cargarCombos();

        JPanel cabecera = UiSupport.formulario("Datos de la compra",
                new String[]{"Número de comprobante", "Proveedor", "Observaciones", "ID compra para recuperar"},
                new JComponent[]{txtNumero, cbProveedor, txtObservaciones, txtIdCompra});
        JButton crear = UiSupport.boton("Crear borrador"), cargar = UiSupport.boton("Cargar compra"),
                agregar = UiSupport.boton("Agregar artículo"), quitar = UiSupport.boton("Quitar detalle"),
                confirmar = UiSupport.boton("Confirmar compra"), anular = UiSupport.boton("Anular compra"),
                limpiar = UiSupport.boton("Nueva operación");

        JPanel renglonDetalle = UiSupport.formulario("Artículo de la compra",
                new String[]{"Artículo", "Cantidad", "Precio unitario de compra"},
                new JComponent[]{cbArticulo, txtCantidad, txtPrecioCompra});
        crear.addActionListener(e -> UiSupport.ejecutar(this, this::crearBorrador));
        cargar.addActionListener(e -> UiSupport.ejecutar(this, this::cargarCompraExistente));
        agregar.addActionListener(e -> UiSupport.ejecutar(this, this::agregarDetalle));
        quitar.addActionListener(e -> UiSupport.ejecutar(this, this::quitarDetalle));
        confirmar.addActionListener(e -> UiSupport.ejecutar(this, this::confirmar));
        anular.addActionListener(e -> UiSupport.ejecutar(this, this::anular));
        limpiar.addActionListener(e -> limpiarPantalla());

        JPanel estado = new JPanel(new BorderLayout());
        estado.add(lblEstado, BorderLayout.WEST); estado.add(lblTotal, BorderLayout.EAST);
        JPanel operaciones = new JPanel(new BorderLayout(6, 6));
        operaciones.add(renglonDetalle, BorderLayout.CENTER);
        operaciones.add(UiSupport.acciones(agregar, quitar), BorderLayout.SOUTH);
        JPanel norte = new JPanel(new BorderLayout(6, 6));
        norte.add(cabecera, BorderLayout.CENTER);
        norte.add(UiSupport.acciones(crear, cargar, confirmar, anular, limpiar), BorderLayout.SOUTH);
        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(operaciones, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        setLayout(new BorderLayout(8, 8));
        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
    }

    private void cargarCombos() {
        try {
            for (Proveedor p : proveedorService.listarTodos()) if (p.isActivo()) cbProveedor.addItem(p);
            for (Articulo a : articuloService.listarTodos()) if (a.isActivo()) cbArticulo.addItem(a);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar proveedores y artículos. Verifique la conexión con MySQL.\n" + ex.getMessage(),
                    "Error al cargar datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearBorrador() {
        Proveedor proveedor = (Proveedor) cbProveedor.getSelectedItem();
        if (proveedor == null) throw new IllegalArgumentException("Debe registrar y seleccionar un proveedor activo.");
        idActual = compraService.crearBorrador(txtNumero.getText(), proveedor.getId(), txtObservaciones.getText());
        txtIdCompra.setText(idActual.toString());
        bloquearCabecera(true);
        modelo.setRowCount(0); actualizarEstado(EstadoCompra.BORRADOR, BigDecimal.ZERO);
        UiSupport.exito(this, "Borrador de compra creado. ID: " + idActual + ". Agregue los artículos y luego confirme.");
    }

    private void cargarCompraExistente() {
        Integer id = UiSupport.enteroPositivo(txtIdCompra.getText(), "ID de compra");
        Compra compra = buscarCompraCompleta(id);
        idActual = compra.getId();
        txtNumero.setText(compra.getNumeroComprobante());
        txtObservaciones.setText(compra.getObservaciones());
        seleccionarProveedor(compra.getProveedor().getId());
        modelo.setRowCount(0);
        for (DetalleCompra d : compra.getDetalles()) agregarFila(d);
        bloquearCabecera(true);
        actualizarEstado(compra.getEstado(), compra.getTotal());
        UiSupport.exito(this, "Compra cargada. Estado: " + compra.getEstado());
    }

    private Compra buscarCompraCompleta(Integer id) {
        LocalDateTime desde = UiSupport.inicioHistorico();
        LocalDateTime hasta = UiSupport.finHistorico();
        return compraService.listarComprasPorFecha(desde, hasta).stream()
                .filter(c -> c.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la compra " + id));
    }

    private void agregarDetalle() {
        exigirBorrador();
        Articulo articulo = (Articulo) cbArticulo.getSelectedItem();
        if (articulo == null) throw new IllegalArgumentException("Debe seleccionar un artículo activo.");
        int cantidad = UiSupport.enteroPositivo(txtCantidad.getText(), "Cantidad");
        BigDecimal precio = UiSupport.importe(txtPrecioCompra.getText(), "Precio de compra");
        compraService.agregarDetalle(idActual, articulo.getId(), cantidad, precio);
        refrescarDetalles();
        txtCantidad.setText(""); txtPrecioCompra.setText("");
    }

    private void quitarDetalle() {
        exigirBorrador();
        int fila = tabla.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Seleccione un detalle para quitar.");
        int modeloFila = tabla.convertRowIndexToModel(fila);
        Integer idArticulo = Integer.valueOf(modelo.getValueAt(modeloFila, 0).toString());
        compraService.quitarDetalle(idActual, idArticulo);
        refrescarDetalles();
    }

    private void refrescarDetalles() {
        Compra compra = buscarCompraCompleta(idActual);
        modelo.setRowCount(0);
        for (DetalleCompra d : compra.getDetalles()) agregarFila(d);
        actualizarEstado(compra.getEstado(), compra.getTotal());
    }

    private void agregarFila(DetalleCompra d) {
        Articulo a = d.getArticulo();
        modelo.addRow(new Object[]{a.getId(), a.getCodigo(), a.getDescripcion(), d.getCantidad(),
                UiSupport.moneda(d.getPrecioUnitarioCompra()), UiSupport.moneda(d.calcularSubtotal())});
    }

    private void confirmar() {
        exigirBorrador();
        if (UiSupport.confirmar(this, "¿Confirmar la compra? Esta operación incrementará el stock de todos sus artículos.")) {
            compraService.confirmarCompra(idActual);
            refrescarDetalles();
            UiSupport.exito(this, "Compra confirmada. El stock fue incrementado.");
        }
    }

    private void anular() {
        exigirCompra();
        if (UiSupport.confirmar(this, "¿Anular la compra? Si estaba confirmada se intentará revertir el ingreso de stock.")) {
            compraService.anularCompra(idActual);
            refrescarDetalles();
            UiSupport.exito(this, "Compra anulada.");
        }
    }

    private void exigirCompra() {
        if (idActual == null) throw new IllegalStateException("Primero cree o cargue una compra.");
    }

    private void exigirBorrador() {
        exigirCompra();
        Compra c = buscarCompraCompleta(idActual);
        if (c.getEstado() != EstadoCompra.BORRADOR)
            throw new IllegalStateException("La compra no está en estado BORRADOR.");
    }

    private void seleccionarProveedor(Integer id) {
        for (int i = 0; i < cbProveedor.getItemCount(); i++) {
            if (cbProveedor.getItemAt(i).getId().equals(id)) { cbProveedor.setSelectedIndex(i); return; }
        }
    }

    private void actualizarEstado(EstadoCompra estado, BigDecimal total) {
        lblEstado.setText("Compra ID " + idActual + " | Estado: " + estado);
        lblTotal.setText("Total: " + UiSupport.moneda(total));
    }

    private void bloquearCabecera(boolean bloquear) {
        txtNumero.setEnabled(!bloquear); txtObservaciones.setEnabled(!bloquear); cbProveedor.setEnabled(!bloquear);
    }

    private void limpiarPantalla() {
        idActual = null; txtNumero.setEnabled(true); txtObservaciones.setEnabled(true); cbProveedor.setEnabled(true);
        txtNumero.setText(""); txtObservaciones.setText(""); txtIdCompra.setText("");
        txtCantidad.setText(""); txtPrecioCompra.setText(""); modelo.setRowCount(0);
        lblEstado.setText("Sin compra seleccionada"); lblTotal.setText("Total: $ 0,00");
    }
}