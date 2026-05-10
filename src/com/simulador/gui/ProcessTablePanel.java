package com.simulador.gui;

import com.simulador.model.ProcessState;
import com.simulador.model.SimProcess;
import com.simulador.util.Theme;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel con tabla de procesos activos.
 * Muestra PID, nombre, estado, prioridad, burst time, tiempo restante y memoria.
 */
public class ProcessTablePanel extends JPanel {
    private final JTable table;
    private final ProcessTableModel tableModel;

    public ProcessTablePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PANEL);
        setBorder(Theme.createPanelBorder("Tabla de Procesos"));

        tableModel = new ProcessTableModel();
        table = new JTable(tableModel);
        table.setBackground(Theme.BG_PANEL);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setGridColor(Theme.BORDER_COLOR);
        table.setSelectionBackground(new Color(70, 130, 180));  // Azul acero, alto contraste
        table.setSelectionForeground(Color.WHITE);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        // Header styling - forzar colores para que sean visibles con Nimbus
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.BG_PANEL_LIGHT);
        header.setForeground(Color.WHITE);
        header.setFont(Theme.FONT_BUTTON);
        header.setOpaque(true);
        header.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));

        // Nimbus overrides para el header
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                label.setBackground(Theme.BG_PANEL_LIGHT);
                label.setForeground(Color.WHITE);
                label.setFont(Theme.FONT_BUTTON);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, Theme.BORDER_COLOR),
                        BorderFactory.createEmptyBorder(4, 6, 4, 6)));
                return label;
            }
        });

        // Renderer para la columna de estado (coloreada)
        table.getColumnModel().getColumn(2).setCellRenderer(new StateCellRenderer());

        // Ancho de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(45);  // PID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Nombre
        table.getColumnModel().getColumn(2).setPreferredWidth(90);  // Estado
        table.getColumnModel().getColumn(3).setPreferredWidth(55);  // Prioridad
        table.getColumnModel().getColumn(4).setPreferredWidth(55);  // Burst
        table.getColumnModel().getColumn(5).setPreferredWidth(65);  // Restante
        table.getColumnModel().getColumn(6).setPreferredWidth(70);  // Memoria

        // Default renderer para alineación central
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBackground(Theme.BG_PANEL);
        centerRenderer.setForeground(Theme.TEXT_PRIMARY);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 2) { // No sobrescribir el renderer de estado
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        JScrollPane scrollPane = Theme.styledScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Actualiza la tabla con la lista actual de procesos.
     */
    public void updateProcesses(List<SimProcess> processes) {
        // Preservar la fila seleccionada antes de actualizar el modelo
        int selectedRow = table.getSelectedRow();
        tableModel.setProcesses(processes);
        // Restaurar la selección si la fila aún existe
        if (selectedRow >= 0 && selectedRow < table.getRowCount()) {
            table.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }

    /**
     * Retorna el proceso seleccionado actualmente.
     */
    public SimProcess getSelectedProcess(List<SimProcess> allProcesses) {
        int row = table.getSelectedRow();
        if (row < 0 || row >= allProcesses.size()) return null;
        return tableModel.getProcessAt(row);
    }

    public JTable getTable() {
        return table;
    }

    // --- Modelo de tabla ---

    private static class ProcessTableModel extends AbstractTableModel {
        private final String[] columns = {"PID", "Nombre", "Estado", "Prioridad", "Burst", "Restante", "Memoria"};
        private List<SimProcess> processes = new ArrayList<>();

        public void setProcesses(List<SimProcess> processes) {
            this.processes = new ArrayList<>(processes);
            fireTableDataChanged();
        }

        public SimProcess getProcessAt(int row) {
            if (row >= 0 && row < processes.size()) {
                return processes.get(row);
            }
            return null;
        }

        @Override
        public int getRowCount() { return processes.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Celdas no editables, pero filas seleccionables
        }

        @Override
        public Object getValueAt(int row, int col) {
            SimProcess p = processes.get(row);
            switch (col) {
                case 0: return "P" + p.getPid();
                case 1: return p.getName();
                case 2: return p.getState().getDisplayName();
                case 3: return p.getPriority();
                case 4: return p.getBurstTime();
                case 5: return p.getRemainingTime();
                case 6: return p.getMemoryRequired() + " MB";
                default: return "";
            }
        }
    }

    // --- Renderer para columna de estado ---

    private static class StateCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);

            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(Theme.FONT_BUTTON);
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(new Color(70, 130, 180));
                label.setForeground(Color.WHITE);
                return label;
            } else {
                label.setBackground(Theme.BG_PANEL);
            }

            // Color basado en el estado
            String stateText = value != null ? value.toString() : "";
            for (ProcessState state : ProcessState.values()) {
                if (state.getDisplayName().equals(stateText)) {
                    label.setForeground(state.getColor());
                    break;
                }
            }

            return label;
        }
    }
}
