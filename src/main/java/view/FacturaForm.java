package view;

import model.Articulo;
import model.Cliente;
import model.DetalleFactura;
import model.Factura;
import enums.EstadoFactura;
import service.ArticuloService;
import service.ClienteService;
import service.FacturaService;
import view.UiSupport;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.time.LocalDateTime;

// Ventana para crear, editar detalles, emitir, anular y preparar la impresión de facturas.
public class FacturaForm extends JFrame {
    
    private final FacturaService facturaService = new FacturaService();
    private final ClienteService clienteService = new ClienteService();
    private final ArticuloService articuloService = new ArticuloService();

    private final JTextField txtNumero = UiSupport.campo(16);
    private final JTextField txtObservaciones = UiSupport.campo(24);
    private final JTextField txtIdFactura = UiSupport.campo(10);
    private final JComboBox<Cliente> cbCliente = new JComboBox<>();
    private final JComboBox<Articulo> cbArticulo = new JComboBox<>();
    private final JTextField txtCantidad = UiSupport.campo(8);
    private final JLabel lblEstado = new JLabel("Sin factura seleccionada");
    private final JLabel lblTotal = new JLabel("Total: $ 0,00");
    private final DefaultTableModel modelo = UiSupport.modelo("ID artículo", "Código", "Descripción", "Cantidad", "Precio unitario", "Subtotal");
    private final JTable tabla = new JTable(modelo);
    private Integer idActual;

    public FacturaForm() {
        super("SOFT.GS - Facturación de ventas");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1120, 700); setLocationRelativeTo(null);
        tabla.setAutoCreateRowSorter(true); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cargarCombos();

        JPanel cabecera = UiSupport.formulario("Datos de la factura",
                new String[]{"Número de factura", "Cliente", "Observaciones", "ID factura para recuperar"},
                new JComponent[]{txtNumero, cbCliente, txtObservaciones, txtIdFactura});
        JPanel renglon = UiSupport.formulario("Artículo de la venta",
                new String[]{"Artículo", "Cantidad"}, new JComponent[]{cbArticulo, txtCantidad});
        JButton crear = UiSupport.boton("Crear borrador"), cargar = UiSupport.boton("Cargar factura"),
                agregar = UiSupport.boton("Agregar artículo"), quitar = UiSupport.boton("Quitar detalle"),
                emitir = UiSupport.boton("Emitir factura"), anular = UiSupport.boton("Anular factura"),
                imprimir = UiSupport.boton("Vista previa / Imprimir"), limpiar = UiSupport.boton("Nueva operación");

        crear.addActionListener(e -> UiSupport.ejecutar(this, this::crearBorrador));
        cargar.addActionListener(e -> UiSupport.ejecutar(this, this::cargarExistente));
        agregar.addActionListener(e -> UiSupport.ejecutar(this, this::agregarDetalle));
        quitar.addActionListener(e -> UiSupport.ejecutar(this, this::quitarDetalle));
        emitir.addActionListener(e -> UiSupport.ejecutar(this, this::emitir));
        anular.addActionListener(e -> UiSupport.ejecutar(this, this::anular));
        imprimir.addActionListener(e -> UiSupport.ejecutar(this, this::vistaPrevia));
        limpiar.addActionListener(e -> limpiarPantalla());

        JPanel cabeceraPanel = new JPanel(new BorderLayout(6, 6));
        cabeceraPanel.add(cabecera, BorderLayout.CENTER);
        cabeceraPanel.add(UiSupport.acciones(crear, cargar, emitir, anular, imprimir, limpiar), BorderLayout.SOUTH);
        JPanel detallePanel = new JPanel(new BorderLayout(6, 6));
        detallePanel.add(renglon, BorderLayout.CENTER);
        detallePanel.add(UiSupport.acciones(agregar, quitar), BorderLayout.SOUTH);
        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(detallePanel, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel estado = new JPanel(new BorderLayout()); estado.add(lblEstado, BorderLayout.WEST); estado.add(lblTotal, BorderLayout.EAST);
        estado.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        setLayout(new BorderLayout(8, 8)); add(cabeceraPanel, BorderLayout.NORTH); add(centro, BorderLayout.CENTER); add(estado, BorderLayout.SOUTH);
    }

    private void cargarCombos() {
        try {
            for (Cliente c : clienteService.listarTodos()) if (c.isActivo()) cbCliente.addItem(c);
            for (Articulo a : articuloService.listarTodos()) if (a.isActivo()) cbArticulo.addItem(a);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar clientes y artículos. Verifique la conexión con MySQL.\n" + ex.getMessage(),
                    "Error al cargar datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void crearBorrador() {
        Cliente cliente = (Cliente) cbCliente.getSelectedItem();
        if (cliente == null) throw new IllegalArgumentException("Debe registrar y seleccionar un cliente activo.");
        idActual = facturaService.crearBorrador(txtNumero.getText(), cliente.getId(), txtObservaciones.getText());
        txtIdFactura.setText(idActual.toString()); bloquearCabecera(true); modelo.setRowCount(0);
        actualizarEstado(EstadoFactura.BORRADOR, java.math.BigDecimal.ZERO);
        UiSupport.exito(this, "Borrador creado. ID: " + idActual + ". Agregue los artículos y luego emita la factura.");
    }

    private void cargarExistente() {
        Integer id = UiSupport.enteroPositivo(txtIdFactura.getText(), "ID de factura");
        Factura f = facturaService.prepararImpresion(id);
        idActual = f.getId(); txtNumero.setText(f.getNumeroFactura()); txtObservaciones.setText(f.getObservaciones());
        seleccionarCliente(f.getCliente().getId()); modelo.setRowCount(0);
        for (DetalleFactura d : f.getDetalles()) agregarFila(d);
        bloquearCabecera(true); actualizarEstado(f.getEstado(), f.getTotal());
        UiSupport.exito(this, "Factura cargada. Estado: " + f.getEstado());
    }

    private void agregarDetalle() {
        exigirBorrador();
        Articulo a = (Articulo) cbArticulo.getSelectedItem();
        if (a == null) throw new IllegalArgumentException("Seleccione un artículo activo.");
        int cantidad = UiSupport.enteroPositivo(txtCantidad.getText(), "Cantidad");
        facturaService.agregarDetalle(idActual, a.getId(), cantidad);
        refrescarDetalles(); txtCantidad.setText("");
    }

    private void quitarDetalle() {
        exigirBorrador();
        int fila = tabla.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Seleccione un detalle para quitar.");
        int mf = tabla.convertRowIndexToModel(fila);
        Integer idArticulo = Integer.valueOf(modelo.getValueAt(mf, 0).toString());
        facturaService.quitarDetalle(idActual, idArticulo); refrescarDetalles();
    }

    private void refrescarDetalles() {
        Factura f = facturaService.prepararImpresion(idActual);
        modelo.setRowCount(0); for (DetalleFactura d : f.getDetalles()) agregarFila(d);
        actualizarEstado(f.getEstado(), f.getTotal());
    }

    private void agregarFila(DetalleFactura d) {
        Articulo a = d.getArticulo();
        modelo.addRow(new Object[]{a.getId(), a.getCodigo(), d.getDescripcionArticulo(), d.getCantidad(),
                UiSupport.moneda(d.getPrecioUnitario()), UiSupport.moneda(d.calcularSubtotal())});
    }

    private void emitir() {
        exigirBorrador();
        if (UiSupport.confirmar(this, "¿Emitir la factura? Se descontará el stock de todos los artículos.")) {
            facturaService.emitirFactura(idActual); refrescarDetalles();
            UiSupport.exito(this, "Factura emitida correctamente. El stock fue descontado.");
        }
    }

    private void anular() {
        exigirFactura();
        if (UiSupport.confirmar(this, "¿Anular la factura? Si ya estaba emitida, se repondrá el stock.")) {
            facturaService.anularFactura(idActual); refrescarDetalles();
            UiSupport.exito(this, "Factura anulada correctamente.");
        }
    }

    private void vistaPrevia() {
        exigirFactura();
        Factura f = facturaService.prepararImpresion(idActual);
        JTextArea documento = new JTextArea(textoFactura(f), 25, 72);
        documento.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); documento.setEditable(false);
        JScrollPane scroll = new JScrollPane(documento);
        scroll.setPreferredSize(new Dimension(760, 480));
        int opcion = JOptionPane.showConfirmDialog(this, scroll,
                "Factura " + f.getNumeroFactura() + " - Vista previa", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (opcion == JOptionPane.OK_OPTION) {
            try { documento.print(); }
            catch (PrinterException ex) { throw new IllegalStateException("No se pudo imprimir la factura: " + ex.getMessage(), ex); }
        }
    }

    private String textoFactura(Factura f) {
        StringBuilder b = new StringBuilder();
        b.append("SOFT.GS - FACTURA DE VENTA\n");
        b.append("Número: ").append(f.getNumeroFactura()).append("\n");
        b.append("Fecha: ").append(UiSupport.fecha(f.getFechaEmision())).append("\n");
        b.append("Cliente: ").append(f.getCliente().getNombreCompleto()).append("\n");
        b.append("Estado: ").append(f.getEstado()).append("\n\nDETALLE\n");
        b.append(String.format("%-12s %-28s %8s %14s %14s%n", "Código", "Artículo", "Cant.", "Precio", "Subtotal"));
        b.append("-".repeat(82)).append("\n");
        for (DetalleFactura d : f.getDetalles()) {
            String codigo = d.getArticulo().getCodigo();
            String descripcion = d.getDescripcionArticulo();
            if (descripcion.length() > 28) descripcion = descripcion.substring(0, 28);
            b.append(String.format("%-12s %-28s %8d %14s %14s%n", codigo, descripcion, d.getCantidad(),
                    UiSupport.moneda(d.getPrecioUnitario()), UiSupport.moneda(d.calcularSubtotal())));
        }
        b.append("\nTOTAL: ").append(UiSupport.moneda(f.getTotal())).append("\n");
        if (f.getObservaciones() != null && !f.getObservaciones().isBlank())
            b.append("Observaciones: ").append(f.getObservaciones()).append("\n");
        return b.toString();
    }

    private void exigirFactura() { if (idActual == null) throw new IllegalStateException("Primero cree o cargue una factura."); }
    private void exigirBorrador() {
        exigirFactura();
        Factura f = facturaService.prepararImpresion(idActual);
        if (f.getEstado() != EstadoFactura.BORRADOR) throw new IllegalStateException("Solo se puede modificar una factura en BORRADOR.");
    }
    private void seleccionarCliente(Integer id) {
        for (int i = 0; i < cbCliente.getItemCount(); i++)
            if (cbCliente.getItemAt(i).getId().equals(id)) { cbCliente.setSelectedIndex(i); return; }
    }
    private void actualizarEstado(EstadoFactura estado, java.math.BigDecimal total) {
        lblEstado.setText("Factura ID " + idActual + " | Estado: " + estado);
        lblTotal.setText("Total: " + UiSupport.moneda(total));
    }
    private void bloquearCabecera(boolean bloquear) {
        txtNumero.setEnabled(!bloquear); txtObservaciones.setEnabled(!bloquear); cbCliente.setEnabled(!bloquear);
    }
    private void limpiarPantalla() {
        idActual = null; txtNumero.setEnabled(true); txtObservaciones.setEnabled(true); cbCliente.setEnabled(true);
        txtNumero.setText(""); txtObservaciones.setText(""); txtIdFactura.setText(""); txtCantidad.setText("");
        modelo.setRowCount(0); lblEstado.setText("Sin factura seleccionada"); lblTotal.setText("Total: $ 0,00");
    }
}