package com.simulador.gui;

import com.simulador.model.ProcessState;
import com.simulador.model.SimProcess;
import com.simulador.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel de visualización de la cola de planificación.
 * Muestra los procesos en cola como bloques coloreados por estado.
 */
public class QueuePanel extends JPanel {
    private List<SimProcess> queuedProcesses;
    private SimProcess runningProcess;

    public QueuePanel() {
        setBackground(Theme.BG_PANEL);
        setBorder(Theme.createPanelBorder("Cola de Planificacion"));
        queuedProcesses = List.of();
    }

    /**
     * Actualiza los datos de la cola.
     */
    public void updateQueue(List<SimProcess> ready, SimProcess running) {
        this.queuedProcesses = ready;
        this.runningProcess = running;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 15;
        int chipWidth = 70;
        int chipHeight = 36;
        int gap = 8;
        int arrowWidth = 18;

        // Centrar verticalmente los chips en el espacio disponible
        java.awt.Insets insets = getInsets();
        int availableHeight = getHeight() - insets.top - insets.bottom;
        int y = insets.top + Math.max(5, (availableHeight - chipHeight) / 2);

        // Dibujar proceso en ejecución primero
        if (runningProcess != null) {
            drawProcessChip(g2, x, y, chipWidth, chipHeight, runningProcess, true);
            x += chipWidth + gap;

            // Flecha
            if (!queuedProcesses.isEmpty()) {
                drawArrow(g2, x, y + chipHeight / 2, arrowWidth);
                x += arrowWidth + gap;
            }
        }

        // Dibujar procesos en cola
        for (int i = 0; i < queuedProcesses.size(); i++) {
            SimProcess p = queuedProcesses.get(i);
            if (x + chipWidth > getWidth() - 15) break; // No sobresalir

            drawProcessChip(g2, x, y, chipWidth, chipHeight, p, false);
            x += chipWidth + gap;

            // Flecha entre procesos
            if (i < queuedProcesses.size() - 1 && x + arrowWidth + chipWidth < getWidth()) {
                drawArrow(g2, x, y + chipHeight / 2, arrowWidth);
                x += arrowWidth + gap;
            }
        }

        if (runningProcess == null && queuedProcesses.isEmpty()) {
            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(Theme.FONT_BODY);
            g2.drawString("Cola vacía — No hay procesos listos", 15, y + 22);
        }

        g2.dispose();
    }

    private void drawProcessChip(Graphics2D g2, int x, int y, int w, int h,
                                  SimProcess process, boolean isRunning) {
        Color bg = isRunning ? ProcessState.RUNNING.getColor() : ProcessState.READY.getColor();

        // Fondo con transparencia
        g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 40));
        g2.fillRoundRect(x, y, w, h, 10, 10);

        // Borde
        g2.setColor(bg);
        g2.setStroke(new BasicStroke(isRunning ? 2f : 1.5f));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        // Texto PID
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.setFont(Theme.FONT_BUTTON);
        String label = "P" + process.getPid();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (w - fm.stringWidth(label)) / 2, y + 15);

        // Texto prioridad
        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.TEXT_SECONDARY);
        String detail = "Pri:" + process.getPriority();
        fm = g2.getFontMetrics();
        g2.drawString(detail, x + (w - fm.stringWidth(detail)) / 2, y + 30);
    }

    private void drawArrow(Graphics2D g2, int x, int y, int width) {
        g2.setColor(Theme.TEXT_MUTED);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(x, y, x + width - 5, y);
        // Punta de flecha
        g2.drawLine(x + width - 5, y, x + width - 10, y - 4);
        g2.drawLine(x + width - 5, y, x + width - 10, y + 4);
    }
}
