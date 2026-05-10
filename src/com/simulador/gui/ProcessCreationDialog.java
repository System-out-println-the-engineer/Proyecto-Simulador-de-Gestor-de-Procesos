package com.simulador.gui;

import com.simulador.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para crear nuevos procesos.
 * Permite definir nombre, prioridad, burst time y memoria.
 */
public class ProcessCreationDialog extends JDialog {
    private final JTextField nameField;
    private final JSpinner prioritySpinner;
    private final JSpinner burstSpinner;
    private final JSpinner memorySpinner;
    private boolean confirmed = false;

    public ProcessCreationDialog(JFrame parent) {
        super(parent, "Crear Nuevo Proceso", true);
        setSize(380, 320);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout(10, 10));

        // --- Panel de formulario ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_PANEL);
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(createLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        nameField = new JTextField("Proceso");
        nameField.setBackground(Theme.BG_INPUT);
        nameField.setForeground(Theme.TEXT_PRIMARY);
        nameField.setCaretColor(Theme.TEXT_PRIMARY);
        nameField.setFont(Theme.FONT_BODY);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        form.add(nameField, gbc);

        // Prioridad
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(createLabel("Prioridad (1-10):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        prioritySpinner = createStyledSpinner(5, 1, 10, 1);
        form.add(prioritySpinner, gbc);

        // Burst Time
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(createLabel("Burst Time (ticks):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        burstSpinner = createStyledSpinner(5, 1, 100, 1);
        form.add(burstSpinner, gbc);

        // Memoria
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(createLabel("Memoria (MB):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        memorySpinner = createStyledSpinner(256, 64, 2048, 64);
        form.add(memorySpinner, gbc);

        add(form, BorderLayout.CENTER);

        // --- Botones ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Theme.BG_PANEL);

        JButton cancelBtn = Theme.createStyledButton("Cancelar", Theme.BG_HOVER);
        JButton createBtn = Theme.createStyledButton("Crear Proceso", Theme.ACCENT);

        cancelBtn.addActionListener(e -> { confirmed = false; dispose(); });
        createBtn.addActionListener(e -> { confirmed = true; dispose(); });

        btnPanel.add(cancelBtn);
        btnPanel.add(createBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Título
        JLabel title = new JLabel("  Nuevo Proceso", SwingConstants.LEFT);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_LIGHT);
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
        title.setBackground(Theme.BG_PANEL);
        title.setOpaque(true);
        add(title, BorderLayout.NORTH);
    }

    public boolean isConfirmed() { return confirmed; }
    public String getProcessName() { return nameField.getText().trim(); }
    public int getPriority() { return (int) prioritySpinner.getValue(); }
    public int getBurstTime() { return (int) burstSpinner.getValue(); }
    public int getMemoryRequired() { return (int) memorySpinner.getValue(); }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BODY);
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }

    private JSpinner createStyledSpinner(int value, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        spinner.setFont(Theme.FONT_BODY);
        spinner.setBackground(Theme.BG_INPUT);
        spinner.setForeground(Theme.TEXT_PRIMARY);
        spinner.setOpaque(true);
        spinner.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor defaultEditor = (JSpinner.DefaultEditor) editor;
            JTextField tf = defaultEditor.getTextField();
            tf.setBackground(Theme.BG_INPUT);
            tf.setForeground(Theme.TEXT_PRIMARY);
            tf.setCaretColor(Theme.TEXT_PRIMARY);
            tf.setOpaque(true);
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

            // Forzar colores sobre Nimbus que los sobreescribe
            tf.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
            javax.swing.UIDefaults overrides = new javax.swing.UIDefaults();
            overrides.put("TextField.background", Theme.BG_INPUT);
            overrides.put("TextField.foreground", Theme.TEXT_PRIMARY);
            tf.putClientProperty("Nimbus.Overrides", overrides);
        }
        return spinner;
    }
}
