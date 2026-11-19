package view;

import model.Cliente;
import service.ClienteService;
import view.UiSupport;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Pantalla Swing para altas, modificaciones, bajas lógicas y consulta de clientes.
public class ClienteForm extends JFrame {
    
    private final ClienteService servicio = new ClienteService();
    private final JTextField txtCodigo = UiSupport.campo(14);
    private final JTextField txtApellido = UiSupport.campo(18);
    private final JTextField txtNombre = UiSupport.campo(18);
    private final JTextField txtDni = UiSupport.campo(14);
    private final JTextField txtDireccion = UiSupport.campo(22);
    private final JTextField txtTelefono = UiSupport.campo(14);
    private final JTextField txtEmail = UiSupport.campo(22);
    private final DefaultTableModel modelo = UiSupport.modelo("ID", "Código", "Apellido", "Nombre", "DNI", "Dirección", "Teléfono", "Email", "Activo");
    private final JTable tabla = new JTable(modelo);

    public ClienteForm() {
        super("SOFT.GS - Gestión de clientes");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1100, 630); setLocationRelativeTo(null);
        tabla.setAutoCreateRowSorter(true); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel datos = UiSupport.formulario("Datos del cliente",
                new String[]{"Código", "Apellido", "Nombre", "DNI", "Dirección", "Teléfono", "Email"},
                new JComponent[]{txtCodigo, txtApellido, txtNombre, txtDni, txtDireccion, txtTelefono, txtEmail});
        JButton nuevo = UiSupport.boton("Nuevo / Limpiar"), guardar = UiSupport.boton("Guardar cliente"),
                modificar = UiSupport.boton("Modificar seleccionado"), baja = UiSupport.boton("Dar de baja"),
                actualizar = UiSupport.boton("Actualizar lista");

        nuevo.addActionListener(e -> limpiar());
        guardar.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            servicio.crear(txtCodigo.getText(), txtApellido.getText(), txtNombre.getText(), txtDni.getText(),
                    txtDireccion.getText(), txtTelefono.getText(), txtEmail.getText());
            limpiar(); cargar(); UiSupport.exito(this, "Cliente registrado correctamente.");
        }));
        modificar.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            servicio.modificar(UiSupport.idSeleccionado(tabla), txtApellido.getText(), txtNombre.getText(),
                    txtDireccion.getText(), txtTelefono.getText(), txtEmail.getText());
            cargar(); UiSupport.exito(this, "Cliente modificado correctamente.");
        }));
        baja.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            Integer id = UiSupport.idSeleccionado(tabla);
            if (UiSupport.confirmar(this, "¿Desea desactivar el cliente seleccionado?")) {
                servicio.desactivar(id); limpiar(); cargar();
            }
        }));
        actualizar.addActionListener(e -> cargarSeguro());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                int f = tabla.convertRowIndexToModel(tabla.getSelectedRow());
                txtCodigo.setText(v(f, 1)); txtApellido.setText(v(f, 2)); txtNombre.setText(v(f, 3));
                txtDni.setText(v(f, 4)); txtDireccion.setText(v(f, 5)); txtTelefono.setText(v(f, 6));
                txtEmail.setText(v(f, 7)); txtCodigo.setEnabled(false); txtDni.setEnabled(false);
            }
        });
        JPanel norte = new JPanel(new BorderLayout(8, 8)); norte.add(datos, BorderLayout.CENTER);
        norte.add(UiSupport.acciones(nuevo, guardar, modificar, baja, actualizar), BorderLayout.SOUTH);
        setLayout(new BorderLayout(8, 8)); add(norte, BorderLayout.NORTH); add(new JScrollPane(tabla), BorderLayout.CENTER);
        cargarSeguro();
    }

    private String v(int f, int c) { Object x = modelo.getValueAt(f, c); return x == null ? "" : x.toString(); }
    private void cargar() {
        modelo.setRowCount(0);
        for (Cliente c : servicio.listarTodos()) modelo.addRow(new Object[]{c.getId(), c.getCodigo(), c.getApellido(),
                c.getNombre(), c.getDni(), c.getDireccion(), c.getTelefono(), c.getEmail(), c.isActivo()});
    }
    private void cargarSeguro() { UiSupport.ejecutar(this, this::cargar); }
    private void limpiar() {
        tabla.clearSelection(); txtCodigo.setEnabled(true); txtDni.setEnabled(true);
        for (JTextField t : new JTextField[]{txtCodigo, txtApellido, txtNombre, txtDni, txtDireccion, txtTelefono, txtEmail}) t.setText("");
        txtCodigo.requestFocus();
    }
}