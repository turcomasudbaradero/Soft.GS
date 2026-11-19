package view;

import model.Proveedor;
import service.ProveedorService;
import view.UiSupport;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Pantalla Swing para administrar proveedores.
public class ProveedorForm extends JFrame {
    
    private final ProveedorService servicio = new ProveedorService();
    private final JTextField txtCodigo = UiSupport.campo(15);
    private final JTextField txtCuit = UiSupport.campo(15);
    private final JTextField txtRazon = UiSupport.campo(22);
    private final JTextField txtDireccion = UiSupport.campo(22);
    private final JTextField txtTelefono = UiSupport.campo(15);
    private final JTextField txtEmail = UiSupport.campo(22);
    private final DefaultTableModel modelo = UiSupport.modelo("ID", "Código", "CUIT/CUIL", "Razón social", "Dirección", "Teléfono", "Email", "Activo");
    private final JTable tabla = new JTable(modelo);

    public ProveedorForm() {
        super("SOFT.GS - Gestión de proveedores");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1050, 620); setLocationRelativeTo(null);
        tabla.setAutoCreateRowSorter(true); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel datos = UiSupport.formulario("Datos del proveedor",
                new String[]{"Código", "CUIT/CUIL", "Razón social", "Dirección", "Teléfono", "Email"},
                new JComponent[]{txtCodigo, txtCuit, txtRazon, txtDireccion, txtTelefono, txtEmail});
        JButton nuevo = UiSupport.boton("Nuevo / Limpiar");
        JButton guardar = UiSupport.boton("Guardar proveedor");
        JButton modificar = UiSupport.boton("Modificar seleccionado");
        JButton baja = UiSupport.boton("Dar de baja");
        JButton actualizar = UiSupport.boton("Actualizar lista");

        nuevo.addActionListener(e -> limpiar());
        guardar.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            servicio.crear(txtCodigo.getText(), txtCuit.getText(), txtRazon.getText(),
                    txtDireccion.getText(), txtTelefono.getText(), txtEmail.getText());
            limpiar(); cargar(); UiSupport.exito(this, "Proveedor registrado correctamente.");
        }));
        modificar.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            servicio.modificar(UiSupport.idSeleccionado(tabla), txtRazon.getText(),
                    txtDireccion.getText(), txtTelefono.getText(), txtEmail.getText());
            cargar(); UiSupport.exito(this, "Proveedor modificado correctamente.");
        }));
        baja.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            Integer id = UiSupport.idSeleccionado(tabla);
            if (UiSupport.confirmar(this, "¿Desea desactivar el proveedor seleccionado?")) {
                servicio.desactivar(id); limpiar(); cargar();
            }
        }));
        actualizar.addActionListener(e -> cargarSeguro());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                int f = tabla.convertRowIndexToModel(tabla.getSelectedRow());
                txtCodigo.setText(v(f, 1)); txtCuit.setText(v(f, 2)); txtRazon.setText(v(f, 3));
                txtDireccion.setText(v(f, 4)); txtTelefono.setText(v(f, 5)); txtEmail.setText(v(f, 6));
                txtCodigo.setEnabled(false); txtCuit.setEnabled(false);
            }
        });

        JPanel norte = new JPanel(new BorderLayout(8, 8));
        norte.add(datos, BorderLayout.CENTER);
        norte.add(UiSupport.acciones(nuevo, guardar, modificar, baja, actualizar), BorderLayout.SOUTH);
        setLayout(new BorderLayout(8, 8)); add(norte, BorderLayout.NORTH); add(new JScrollPane(tabla), BorderLayout.CENTER);
        cargarSeguro();
    }

    private String v(int f, int c) { Object x = modelo.getValueAt(f, c); return x == null ? "" : x.toString(); }
    private void cargar() {
        modelo.setRowCount(0);
        for (Proveedor p : servicio.listarTodos()) modelo.addRow(new Object[]{p.getId(), p.getCodigo(),
                p.getCuitCuil(), p.getRazonSocial(), p.getDireccion(), p.getTelefono(), p.getEmail(), p.isActivo()});
    }
    private void cargarSeguro() { UiSupport.ejecutar(this, this::cargar); }
    private void limpiar() {
        tabla.clearSelection(); txtCodigo.setEnabled(true); txtCuit.setEnabled(true);
        for (JTextField t : new JTextField[]{txtCodigo, txtCuit, txtRazon, txtDireccion, txtTelefono, txtEmail}) t.setText("");
        txtCodigo.requestFocus();
    }
}