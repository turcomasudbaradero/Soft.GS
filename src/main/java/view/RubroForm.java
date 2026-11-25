package view;

import model.Rubro;
import service.RubroService;
import view.UiSupport;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Pantalla Swing para altas, modificaciones, bajas lógicas y consulta de rubros.
public class RubroForm extends JFrame {
    
    private final RubroService servicio = new RubroService();
    private final JTextField txtCodigo = UiSupport.campo(18);
    private final JTextField txtNombre = UiSupport.campo(24);
    private final JTextField txtDescripcion = UiSupport.campo(24);
    private final DefaultTableModel modelo = UiSupport.modelo("ID", "Código", "Nombre", "Descripción", "Activo");
    private final JTable tabla = new JTable(modelo);

    public RubroForm() {
        super("SOFT.GS - Gestión de rubros");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(850, 560);
        setLocationRelativeTo(null);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel datos = UiSupport.formulario("Datos del rubro",
                new String[]{"Código", "Nombre", "Descripción"},
                new JComponent[]{txtCodigo, txtNombre, txtDescripcion});

        JButton nuevo = UiSupport.boton("Nuevo / Limpiar");
        JButton guardar = UiSupport.boton("Guardar rubro");
        JButton modificar = UiSupport.boton("Modificar seleccionado");
        JButton baja = UiSupport.boton("Dar de baja/alta");
        JButton actualizar = UiSupport.boton("Actualizar lista");

        nuevo.addActionListener(e -> limpiar());
        guardar.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            servicio.crear(txtCodigo.getText(), txtNombre.getText(), txtDescripcion.getText());
            limpiar(); cargar(); UiSupport.exito(this, "Rubro registrado correctamente.");
        }));
        modificar.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            Integer id = UiSupport.idSeleccionado(tabla);
            servicio.modificar(id, txtNombre.getText(), txtDescripcion.getText());
            cargar(); UiSupport.exito(this, "Rubro modificado correctamente.");
        }));
        baja.addActionListener(e -> UiSupport.ejecutar(this, () -> {
            Integer id = UiSupport.idSeleccionado(tabla);
            if (UiSupport.confirmar(this, "¿Desea desactivar/activar el rubro seleccionado?")) {
                servicio.desactivar(id); limpiar(); cargar();
            }
        }));
        actualizar.addActionListener(e -> cargarSeguro());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                int f = tabla.convertRowIndexToModel(tabla.getSelectedRow());
                txtCodigo.setText(valor(f, 1)); txtNombre.setText(valor(f, 2));
                txtDescripcion.setText(valor(f, 3)); txtCodigo.setEnabled(false);
            }
        });

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.add(datos, BorderLayout.CENTER);
        superior.add(UiSupport.acciones(nuevo, guardar, modificar, baja, actualizar), BorderLayout.SOUTH);
        setLayout(new BorderLayout(8, 8));
        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        cargarSeguro();
    }

    private String valor(int fila, int columna) {
        Object v = modelo.getValueAt(fila, columna); return v == null ? "" : v.toString();
    }

    private void cargar() {
        modelo.setRowCount(0);
        for (Rubro r : servicio.listarTodos()) {
            modelo.addRow(new Object[]{r.getId(), r.getCodigo(), r.getNombre(), r.getDescripcion(), r.isActivo()});
        }
    }

    private void cargarSeguro() { UiSupport.ejecutar(this, this::cargar); }

    private void limpiar() {
        tabla.clearSelection(); txtCodigo.setEnabled(true);
        txtCodigo.setText(""); txtNombre.setText(""); txtDescripcion.setText(""); txtCodigo.requestFocus();
    }
}