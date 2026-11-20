package com.gestionstock;

/**
 *
 * @author GRUPO 1: CASTELLI; MASUD; ORTIZ; PIÑEYRO
 */

import view.MenuPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Punto de entrada de SOFT.GS
public class SoftwareGestionStock {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("No se pudo aplicar el estilo del sistema: " + ex.getMessage());
            }
            new MenuPrincipal().setVisible(true);
        });
    }
}