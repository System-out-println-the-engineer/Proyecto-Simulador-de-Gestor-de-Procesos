package com.simulador.gui;

import com.simulador.resource.ResourceManager;
import com.simulador.resource.MemoryManager;
import com.simulador.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Panel de visualización de recursos (CPU y RAM).
 * Muestra barras de progreso con indicadores numéricos.
 */
public class ResourcePanel extends JPanel {
    private final JProgressBar cpuBar;
    private final JProgressBar ramBar;
    private final JLabel cpuLabel;
    private final JLabel ramLabel;
    private final JLabel processCountLabel;

    public ResourcePanel() {
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_PANEL);
        setBorder(Theme.createPanelBorder("Recursos del Sistema"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // --- CPU ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel cpuTitle = new JLabel("CPU:");
        cpuTitle.setFont(Theme.FONT_BODY);
        cpuTitle.setForeground(Theme.TEXT_PRIMARY);
        add(cpuTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        cpuBar = createStyledProgressBar(Theme.SUCCESS);
        add(cpuBar, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        cpuLabel = new JLabel("Libre");
        cpuLabel.setFont(Theme.FONT_SMALL);
        cpuLabel.setForeground(Theme.SUCCESS);
        cpuLabel.setPreferredSize(new Dimension(100, 20));
        add(cpuLabel, gbc);

        // --- RAM ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel ramTitle = new JLabel("RAM:");
        ramTitle.setFont(Theme.FONT_BODY);
        ramTitle.setForeground(Theme.TEXT_PRIMARY);
        add(ramTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        ramBar = createStyledProgressBar(Theme.ACCENT);
        add(ramBar, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        ramLabel = new JLabel("0 / 4096 MB");
        ramLabel.setFont(Theme.FONT_SMALL);
        ramLabel.setForeground(Theme.ACCENT_LIGHT);
        ramLabel.setPreferredSize(new Dimension(100, 20));
        add(ramLabel, gbc);

        // --- Contador de procesos ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weightx = 1.0;
        processCountLabel = new JLabel("Procesos activos: 0");
        processCountLabel.setFont(Theme.FONT_SMALL);
        processCountLabel.setForeground(Theme.TEXT_SECONDARY);
        processCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(processCountLabel, gbc);
    }

    /**
     * Actualiza los indicadores de recursos.
     */
    public void updateResources(ResourceManager rm, int activeProcessCount) {
        // CPU
        boolean cpuBusy = rm.getCpuManager().isBusy();
        cpuBar.setValue(cpuBusy ? 100 : 0);
        if (cpuBusy) {
            cpuLabel.setText("En uso: P" + rm.getCpuManager().getCurrentProcess().getPid());
            cpuLabel.setForeground(Theme.WARNING);
            cpuBar.setForeground(Theme.WARNING);
        } else {
            cpuLabel.setText("Libre");
            cpuLabel.setForeground(Theme.SUCCESS);
            cpuBar.setForeground(Theme.SUCCESS);
        }

        // RAM
        MemoryManager mem = rm.getMemoryManager();
        int used = mem.getUsedMemory();
        int total = mem.getTotalMemory();
        int percent = (int) (mem.getUsagePercent() * 100);
        ramBar.setValue(percent);
        ramLabel.setText(used + " / " + total + " MB");

        if (percent > 80) {
            ramBar.setForeground(Theme.DANGER);
            ramLabel.setForeground(Theme.DANGER);
        } else if (percent > 50) {
            ramBar.setForeground(Theme.WARNING);
            ramLabel.setForeground(Theme.WARNING);
        } else {
            ramBar.setForeground(Theme.ACCENT);
            ramLabel.setForeground(Theme.ACCENT_LIGHT);
        }

        // Procesos
        processCountLabel.setText("Procesos activos: " + activeProcessCount);
    }

    private JProgressBar createStyledProgressBar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setPreferredSize(new Dimension(200, 22));
        bar.setBackground(Theme.BG_DARK);
        bar.setForeground(color);
        bar.setBorderPainted(false);
        bar.setStringPainted(true);
        bar.setFont(Theme.FONT_SMALL);
        return bar;
    }
}
