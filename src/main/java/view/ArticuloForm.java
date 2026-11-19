package view;

import model.Articulo;
import model.Proveedor;
import model.Rubro;
import service.ArticuloService;
import service.ProveedorService;
import service.RubroService;
import view.UiSupport;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

// Formulario modal para crear o modificar un artículo.
public class ArticuloForm extends JDialog {
    private final ArticuloService articuloService = new ArticuloService();
    private final RubroService rubroService = new RubroService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final Articulo existente;
    private final Runnable alGuardar;

    private final JTextField txtCodigo = UiSupport.campo(20);
    private final JTextField txtDescripcion = UiSupport.campo(28);
    private final JComboBox<Rubro> cbRubro = new JComboBox<>();
    private final JComboBox<Proveedor> cbProveedor = new JComboBox<>();
    private final JTextField txtStock = UiSupport.campo(10);
    private final JTextField txtMinimo = UiSupport.campo(10);
    private final JTextField txtPrecio = UiSupport.campo(12);

    public ArticuloForm(Window propietario, Articulo articulo, Runnable alGuardar) {
        super(propietario, articulo == null ? "Nuevo artículo" : "Modificar artículo",
                ModalityType.APPLICATION_MODAL);
        this.existente = articulo;
        this.alGuardar = alGuardar == null ? () -> { } : alGuardar;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(570, 470); setLocationRelativeTo(propietario);

        cargarCombos();
        if (existente != null) cargarDatosExistentes();

        txtStock.setToolTipText(existente == null
                ? "Stock inicial del artículo" : "El stock se modifica mediante los ajustes de inventario.");
        if (existente != null) {
            txtCodigo.setEnabled(false);
            txtStock.setEnabled(false);
            txtStock.setToolTipText("Solo lectura. Use los botones de ajuste de stock en la lista de artículos.");
        }

        JPanel datos = UiSupport.formulario("Datos del artículo",
                new String[]{"Código", "Descripción", "Rubro", "Proveedor habitual",
                    existente == null ? "Stock inicial" : "Stock actual (solo lectura)",
                    "Stock mínimo", "Precio unitario"},
                new JComponent[]{txtCodigo, txtDescripcion, cbRubro, cbProveedor,
                    txtStock, txtMinimo, txtPrecio});
        JLabel ayuda = new JLabel("El stock actual se modifica con compras o ajustes de inventario.");
        ayuda.setForeground(new Color(90, 90, 90));
        JButton guardar = UiSupport.boton("Guardar");
        JButton cancelar = UiSupport.boton("Cancelar");
        guardar.addActionListener(e -> UiSupport.ejecutar(this, this::guardar));
        cancelar.addActionListener(e -> dispose());

        JPanel pie = new JPanel(new BorderLayout());
        pie.add(ayuda, BorderLayout.CENTER);
        pie.add(UiSupport.acciones(guardar, cancelar), BorderLayout.SOUTH);
        setLayout(new BorderLayout(10, 10));
        add(datos, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }

    private void cargarCombos() {
        try {
            List<Rubro> rubros = rubroService.listarTodos();
            for (Rubro r : rubros) if (r.isActivo()) cbRubro.addItem(r);
            List<Proveedor> proveedores = proveedorService.listarTodos();
            for (Proveedor p : proveedores) if (p.isActivo()) cbProveedor.addItem(p);
            if (cbRubro.getItemCount() == 0 || cbProveedor.getItemCount() == 0) {
                throw new IllegalStateException("Primero debe registrar al menos un rubro y un proveedor activos.");
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudieron cargar las listas",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cargarDatosExistentes() {
        txtCodigo.setText(existente.getCodigo());
        txtDescripcion.setText(existente.getDescripcion());
        txtStock.setText(String.valueOf(existente.getStockActual()));
        txtMinimo.setText(String.valueOf(existente.getStockMinimo()));
        txtPrecio.setText(existente.getPrecioUnitario().toPlainString());
        seleccionarRubro(existente.getRubro() == null ? null : existente.getRubro().getId());
        seleccionarProveedor(existente.getProveedorHabitual() == null ? null : existente.getProveedorHabitual().getId());
    }

    private void seleccionarRubro(Integer id) {
        for (int i = 0; i < cbRubro.getItemCount(); i++) {
            Rubro r = cbRubro.getItemAt(i);
            if (id != null && id.equals(r.getId())) { cbRubro.setSelectedIndex(i); return; }
        }
    }

    private void seleccionarProveedor(Integer id) {
        for (int i = 0; i < cbProveedor.getItemCount(); i++) {
            Proveedor p = cbProveedor.getItemAt(i);
            if (id != null && id.equals(p.getId())) { cbProveedor.setSelectedIndex(i); return; }
        }
    }

    private void guardar() {
        Rubro rubro = (Rubro) cbRubro.getSelectedItem();
        Proveedor proveedor = (Proveedor) cbProveedor.getSelectedItem();
        if (rubro == null || proveedor == null) {
            throw new IllegalArgumentException("Seleccione un rubro y un proveedor.");
        }
        BigDecimal precio = UiSupport.importe(txtPrecio.getText(), "Precio unitario");
        int minimo = UiSupport.enteroNoNegativo(txtMinimo.getText(), "Stock mínimo");

        if (existente == null) {
            int stockInicial = UiSupport.enteroNoNegativo(txtStock.getText(), "Stock inicial");
            articuloService.crear(txtCodigo.getText(), txtDescripcion.getText(),
                    rubro.getId(), proveedor.getId(), stockInicial, minimo, precio);
            UiSupport.exito(this, "Artículo registrado correctamente.");
        } else {
            articuloService.modificar(existente.getId(), txtDescripcion.getText(),
                    rubro.getId(), proveedor.getId(), minimo, precio);
            UiSupport.exito(this, "Artículo modificado correctamente.");
        }
        alGuardar.run();
        dispose();
    }
}