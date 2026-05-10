package com.simulador.gui;

import com.simulador.ipc.ProducerConsumerDemo;
import com.simulador.ipc.SharedBuffer;
import com.simulador.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Panel de demostración del Productor-Consumidor.
 */
public class IPCDemoPanel extends JPanel {
    private final ProducerConsumerDemo demo;
    private final JButton startBtn, pauseBtn, stopBtn;
    private final JLabel producedLabel, consumedLabel;
    private final BufferVisualizer bufferVisual;

    public IPCDemoPanel(ProducerConsumerDemo demo) {
        this.demo = demo;
        setLayout(new BorderLayout(5, 5));
        setBackground(Theme.BG_PANEL);
        setBorder(Theme.createPanelBorder("IPC: Productor-Consumidor"));

        bufferVisual = new BufferVisualizer();
        add(bufferVisual, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        controlPanel.setBackground(Theme.BG_PANEL);

        startBtn = Theme.createStyledButton("Iniciar", Theme.SUCCESS);
        pauseBtn = Theme.createStyledButton("Pausar", Theme.WARNING);
        stopBtn = Theme.createStyledButton("Detener", Theme.DANGER);
        pauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);

        startBtn.addActionListener(e -> {
            demo.start();
            startBtn.setEnabled(false);
            pauseBtn.setEnabled(true);
            stopBtn.setEnabled(true);
        });
        pauseBtn.addActionListener(e -> {
            if (demo.isPaused()) { demo.resumeDemo(); pauseBtn.setText("Pausar"); }
            else { demo.pause(); pauseBtn.setText("Reanudar"); }
        });
        stopBtn.addActionListener(e -> {
            demo.stop();
            startBtn.setEnabled(true);
            pauseBtn.setEnabled(false);
            stopBtn.setEnabled(false);
            updateDisplay();
        });

        controlPanel.add(startBtn);
        controlPanel.add(pauseBtn);
        controlPanel.add(stopBtn);

        JPanel counters = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 2));
        counters.setBackground(Theme.BG_PANEL);
        producedLabel = new JLabel("Producidos: 0");
        producedLabel.setFont(Theme.FONT_SMALL);
        producedLabel.setForeground(Theme.SUCCESS);
        consumedLabel = new JLabel("Consumidos: 0");
        consumedLabel.setFont(Theme.FONT_SMALL);
        consumedLabel.setForeground(Theme.INFO);
        counters.add(producedLabel);
        counters.add(consumedLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Theme.BG_PANEL);
        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        bottomPanel.add(counters, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        demo.setOnUpdateCallback(this::updateDisplay);
    }

    public void updateDisplay() {
        producedLabel.setText("Producidos: " + demo.getProducedCount());
        consumedLabel.setText("Consumidos: " + demo.getConsumedCount());
        bufferVisual.updateBuffer(demo.getBufferSize());
    }

    private static class BufferVisualizer extends JPanel {
        private int filledSlots = 0;
        BufferVisualizer() {
            setBackground(Theme.BG_PANEL);
            setPreferredSize(new Dimension(300, 60));
            setMinimumSize(new Dimension(200, 50));
            setOpaque(true);
        }
        void updateBuffer(int filled) { this.filledSlots = filled; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int total = SharedBuffer.BUFFER_SIZE, sw = 40, sh = 30, gap = 6;
            int tw = total * (sw + gap) - gap;
            int sx = (getWidth() - tw) / 2, y = (getHeight() - sh) / 2;
            g2.setColor(Theme.TEXT_SECONDARY); g2.setFont(Theme.FONT_SMALL);
            g2.drawString("Buffer:", sx - 55, y + 20);
            for (int i = 0; i < total; i++) {
                int x = sx + i * (sw + gap); boolean filled = i < filledSlots;
                g2.setColor(filled ? new Color(99,102,241,50) : new Color(55,55,75,40));
                g2.fillRoundRect(x, y, sw, sh, 8, 8);
                g2.setColor(filled ? Theme.ACCENT : Theme.BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(x, y, sw, sh, 8, 8);
                g2.setFont(Theme.FONT_BODY);
                g2.setColor(filled ? Theme.ACCENT_LIGHT : Theme.TEXT_MUTED);
                String icon = filled ? "■" : "□";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icon, x + (sw - fm.stringWidth(icon))/2, y + 21);
            }
            g2.setColor(Theme.TEXT_SECONDARY); g2.setFont(Theme.FONT_SMALL);
            g2.drawString(filledSlots + "/" + total, sx + tw + 10, y + 20);
            g2.dispose();
        }
    }
}
