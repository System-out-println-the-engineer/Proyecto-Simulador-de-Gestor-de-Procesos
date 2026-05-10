package com.simulador.gui;

import com.simulador.util.EventLog;
import com.simulador.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Panel de logs de eventos del simulador.
 * Muestra mensajes con timestamp en un JTextArea con scroll automático.
 */
public class LogPanel extends JPanel {
    private final JTextArea logArea;

    public LogPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PANEL);
        setBorder(Theme.createPanelBorder("Log de Eventos"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(Theme.FONT_MONO);
        logArea.setBackground(Theme.BG_DARK);
        logArea.setForeground(Theme.TEXT_PRIMARY);
        logArea.setCaretColor(Theme.ACCENT);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = Theme.styledScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        add(scrollPane, BorderLayout.CENTER);

        // Barra de botones
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        buttonBar.setBackground(Theme.BG_PANEL);

        // Botón copiar al portapapeles
        JButton copyBtn = Theme.createStyledButton("Copiar Log", Theme.ACCENT);
        copyBtn.setPreferredSize(new Dimension(110, 28));
        copyBtn.addActionListener(e -> {
            String text = logArea.getText();
            if (text != null && !text.isEmpty()) {
                java.awt.datatransfer.StringSelection sel =
                        new java.awt.datatransfer.StringSelection(text);
                java.awt.datatransfer.Clipboard clipboard =
                        Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(sel, null);

                // Feedback visual: cambiar texto brevemente
                String originalText = copyBtn.getText();
                copyBtn.setText("Copiado!");
                Timer feedbackTimer = new Timer(1500, evt -> copyBtn.setText(originalText));
                feedbackTimer.setRepeats(false);
                feedbackTimer.start();
            }
        });
        buttonBar.add(copyBtn);

        // Botón limpiar
        JButton clearBtn = Theme.createStyledButton("Limpiar", Theme.BG_HOVER);
        clearBtn.setPreferredSize(new Dimension(90, 28));
        clearBtn.addActionListener(e -> {
            logArea.setText("");
            EventLog.clear();
        });
        buttonBar.add(clearBtn);
        add(buttonBar, BorderLayout.SOUTH);

        // Registrar listener del EventLog
        EventLog.setListener(this::appendLog);
    }

    /**
     * Agrega un mensaje al log (llamado desde el EDT).
     */
    public void appendLog(String message) {
        logArea.append(message + "\n");
        // Auto-scroll al final
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
