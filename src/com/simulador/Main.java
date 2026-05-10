package com.simulador;

import com.simulador.gui.MainDashboard;
import com.simulador.util.Theme;

import javax.swing.*;

/**
 * Punto de entrada del Simulador de Gestor de Procesos.
 * Configura el tema visual y lanza el dashboard principal.
 */
public class Main {
    public static void main(String[] args) {
        // Aplicar tema oscuro antes de crear componentes
        Theme.applyGlobalTheme();

        SwingUtilities.invokeLater(() -> {
            try {
                // Intentar usar Nimbus como base (mejor que Metal)
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // Usar L&F por defecto si Nimbus no está disponible
            }

            // Re-aplicar tema oscuro sobre Nimbus
            Theme.applyGlobalTheme();

            MainDashboard dashboard = new MainDashboard();
            dashboard.setVisible(true);
        });
    }
}
