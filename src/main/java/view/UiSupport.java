package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Utilidades visuales comunes para las ventanas Swing de SOFT.GS.
public final class UiSupport {
    
    private static final Locale LOCALE_AR = Locale.forLanguageTag("es-AR");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(LOCALE_AR);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private UiSupport() { }

    public static JTextField campo(int columnas) {
    
        return new JTextField(columnas);
    }

    public static JButton boton(String texto) {
        
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        return b;
    }

    public static DefaultTableModel modelo(String... columnas) {
        
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    public static JPanel formulario(String titulo, String[] etiquetas, JComponent[] controles) {
        
        if (etiquetas.length != controles.length) {
            throw new IllegalArgumentException("La cantidad de etiquetas y controles no coincide.");
        }
        
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < etiquetas.length; i++) {
            c.gridx = 0; c.gridy = i; c.weightx = 0;
            p.add(new JLabel(etiquetas[i] + ":"), c);
            c.gridx = 1; c.weightx = 1;
            p.add(controles[i], c);
        }
        return p;
    }

    public static JPanel acciones(JButton... botones) {
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton b : botones) p.add(b);
        return p;
    }

    public static Integer idSeleccionado(JTable tabla) {
        
        int fila = tabla.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Seleccione un registro de la tabla.");
        int modelo = tabla.convertRowIndexToModel(fila);
        return Integer.valueOf(tabla.getModel().getValueAt(modelo, 0).toString());
    }

    public static int enteroPositivo(String valor, String etiqueta) {
        
        try {
            int n = Integer.parseInt(valor.trim());
            if (n <= 0) throw new IllegalArgumentException(etiqueta + " debe ser mayor que cero.");
            return n;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(etiqueta + " debe ser un número entero válido.");
        }
    }

    public static int enteroNoNegativo(String valor, String etiqueta) {
        
        try {
            int n = Integer.parseInt(valor.trim());
            if (n < 0) throw new IllegalArgumentException(etiqueta + " no puede ser negativo.");
            return n;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(etiqueta + " debe ser un número entero válido.");
        }
    }

    public static BigDecimal importe(String valor, String etiqueta) {
        
        try {
            BigDecimal n = new BigDecimal(valor.trim().replace("$", "").replace(" ", "").replace(",", "."));
            if (n.signum() < 0) throw new IllegalArgumentException(etiqueta + " no puede ser negativo.");
            return n.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(etiqueta + " debe ser un importe válido. Use, por ejemplo, 12500.50.");
        }
    }

    public static String moneda(BigDecimal valor) {
        
        return MONEDA.format(valor == null ? BigDecimal.ZERO : valor);
    }

    public static String fecha(LocalDateTime fecha) {
        
        return fecha == null ? "" : fecha.format(FECHA_HORA);
    }

    public static LocalDate leerFecha(String texto, String campo) {
        
        try {
            return LocalDate.parse(texto.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("La fecha '" + campo + "' debe tener formato AAAA-MM-DD.");
        }
    }

    public static void ejecutar(Component padre, Runnable accion) {
        
        try {
            accion.run();
        } catch (Exception ex) {
            String mensaje = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            JOptionPane.showMessageDialog(padre, mensaje, "SOFT.GS - Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean confirmar(Component padre, String mensaje) {
        
        // Crear botones personalizados en español.
        Object[] opciones = {"Sí", "No"};

        int respuesta = JOptionPane.showOptionDialog(
                padre, mensaje, "Confirmar operación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[1]);

        // Devuelve true solamente cuando se selecciona "Sí".
        return respuesta == 0;
    }

    public static void exito(Component padre, String mensaje) {
        
        JOptionPane.showMessageDialog(padre, mensaje, "SOFT.GS", JOptionPane.INFORMATION_MESSAGE);
    }

    public static LocalDateTime inicioHistorico() {
        
        return LocalDateTime.of(1000, 1, 1, 0, 0);
    }

    public static LocalDateTime finHistorico() {
        
        return LocalDateTime.of(9998, 12, 31, 23, 59, 59);
    }
}