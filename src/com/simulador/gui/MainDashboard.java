package com.simulador.gui;

import com.simulador.ipc.ProducerConsumerDemo;
import com.simulador.model.ProcessState;
import com.simulador.model.SimProcess;
import com.simulador.resource.ResourceManager;
import com.simulador.scheduler.*;
import com.simulador.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dashboard principal del Simulador de Gestor de Procesos.
 * Integra todos los paneles y coordina la interacción del usuario.
 */
public class MainDashboard extends JFrame {

    private final SchedulerEngine engine;
    private final ResourceManager resourceManager;
    private final ProducerConsumerDemo ipcDemo;

    // Paneles
    private ProcessTablePanel processTablePanel;
    private ResourcePanel resourcePanel;
    private QueuePanel queuePanel;
    private LogPanel logPanel;
    private IPCDemoPanel ipcPanel;

    // Controles del toolbar
    private JComboBox<String> algorithmCombo;
    private JSpinner quantumSpinner;
    private JSpinner speedSpinner;
    private JButton startBtn, pauseBtn, stopBtn;
    private JLabel statusLabel;

    // Algoritmos
    private final SchedulingAlgorithm[] algorithms = {
        new FCFSScheduler(),
        new SJFScheduler(),
        new RoundRobinScheduler(3),
        new PriorityScheduler()
    };

    public MainDashboard() {
        resourceManager = new ResourceManager();
        engine = new SchedulerEngine(resourceManager, algorithms[0]);
        ipcDemo = new ProducerConsumerDemo();

        initUI();
        setupTimerRefresh();
    }

    private void initUI() {
        setTitle("Simulador de Gestor de Procesos  -  UAT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // Padding global alrededor de todo el contenido
        JPanel rootPanel = new JPanel(new BorderLayout(6, 6));
        rootPanel.setBackground(Theme.BG_DARK);
        rootPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        rootPanel.add(createToolbar(), BorderLayout.NORTH);

        // Panel superior (Tabla + Columna Derecha)
        JPanel topContent = createCenterPanel();

        // Panel inferior (Log de Eventos)
        logPanel = new LogPanel();
        logPanel.setPreferredSize(new Dimension(0, 200));
        logPanel.setMinimumSize(new Dimension(0, 80));
        logPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(6, 0, 0, 0),
                logPanel.getBorder()));

        // JSplitPane vertical: el usuario puede ajustar el balance arrastrando
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topContent, logPanel);
        verticalSplit.setResizeWeight(0.65);     // 65% arriba, 35% abajo
        verticalSplit.setDividerSize(6);
        verticalSplit.setBackground(Theme.BG_DARK);
        verticalSplit.setBorder(null);
        verticalSplit.setContinuousLayout(true);
        rootPanel.add(verticalSplit, BorderLayout.CENTER);

        add(rootPanel, BorderLayout.CENTER);

        // Forzar posicion inicial del divisor una vez la ventana sea visible
        SwingUtilities.invokeLater(() -> verticalSplit.setDividerLocation(0.65));

        // Callback del engine
        engine.setOnTickCallback(this::refreshAll);
    }

    // ===== TOOLBAR (Controles Globales del Simulador) =====
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(Theme.BG_PANEL_LIGHT);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                new EmptyBorder(4, 10, 4, 10)
        ));

        // Titulo
        JLabel title = new JLabel("Simulador de Procesos");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_LIGHT);
        toolbar.add(title);
        toolbar.add(createSeparator());

        // Selector de algoritmo
        toolbar.add(styledLabel("Algoritmo:"));
        algorithmCombo = new JComboBox<>(new String[]{"FCFS", "SJF", "Round Robin", "Prioridades"});
        algorithmCombo.setBackground(Theme.BG_INPUT);
        algorithmCombo.setForeground(Theme.TEXT_PRIMARY);
        algorithmCombo.setFont(Theme.FONT_BODY);
        algorithmCombo.addActionListener(e -> onAlgorithmChanged());
        toolbar.add(algorithmCombo);

        // Quantum spinner
        toolbar.add(styledLabel("Quantum:"));
        quantumSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
        styleSpinner(quantumSpinner);
        quantumSpinner.setPreferredSize(new Dimension(60, 28));
        quantumSpinner.addChangeListener(e -> onQuantumChanged());
        toolbar.add(quantumSpinner);

        // Velocidad
        toolbar.add(styledLabel("Velocidad (ms):"));
        speedSpinner = new JSpinner(new SpinnerNumberModel(1000, 200, 5000, 100));
        styleSpinner(speedSpinner);
        speedSpinner.setPreferredSize(new Dimension(75, 28));
        speedSpinner.addChangeListener(e -> engine.setTickInterval((int) speedSpinner.getValue()));
        toolbar.add(speedSpinner);

        toolbar.add(createSeparator());

        // Botones de control global
        startBtn = Theme.createStyledButton("Iniciar", Theme.SUCCESS);
        pauseBtn = Theme.createStyledButton("Pausar", Theme.WARNING);
        stopBtn = Theme.createStyledButton("Detener", Theme.DANGER);
        pauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);

        startBtn.addActionListener(e -> startEngine());
        pauseBtn.addActionListener(e -> togglePause());
        stopBtn.addActionListener(e -> stopEngine());

        toolbar.add(startBtn);
        toolbar.add(pauseBtn);
        toolbar.add(stopBtn);

        toolbar.add(createSeparator());

        statusLabel = new JLabel("Detenido");
        statusLabel.setFont(Theme.FONT_BUTTON);
        statusLabel.setForeground(Theme.TEXT_MUTED);
        toolbar.add(statusLabel);

        // Forzar la logica inicial para el estado del quantumSpinner
        onAlgorithmChanged();

        return toolbar;
    }

    // ===== CENTER PANEL =====
    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(Theme.BG_DARK);
        center.setBorder(new EmptyBorder(6, 0, 0, 0));

        // LEFT: Tabla de procesos + botones de accion individual
        JPanel leftPanel = new JPanel(new BorderLayout(0, 6));
        leftPanel.setBackground(Theme.BG_DARK);

        processTablePanel = new ProcessTablePanel();
        leftPanel.add(processTablePanel, BorderLayout.CENTER);
        leftPanel.add(createActionButtons(), BorderLayout.SOUTH);

        // RIGHT: GridBagLayout estricto para proporciones perfectas
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Theme.BG_DARK);
        rightPanel.setBorder(new EmptyBorder(0, 6, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new java.awt.Insets(0, 0, 6, 0);

        // Panel Superior: Recursos del Sistema (NO crece verticalmente)
        resourcePanel = new ResourcePanel();
        resourcePanel.setPreferredSize(new Dimension(0, 120));
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(resourcePanel, gbc);

        // Panel Central: Cola de Planificacion (absorbe todo el espacio extra)
        queuePanel = new QueuePanel();
        queuePanel.setPreferredSize(new Dimension(0, 100));
        queuePanel.setMinimumSize(new Dimension(0, 100));
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(queuePanel, gbc);

        // Panel Inferior: IPC Productor-Consumidor (altura fija, suficiente para buffer + botones)
        ipcPanel = new IPCDemoPanel(ipcDemo);
        ipcPanel.setPreferredSize(new Dimension(0, 140));
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        rightPanel.add(ipcPanel, gbc);

        // Split pane: 60% tabla, 40% paneles derecha
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.6);     // 60% para la tabla
        splitPane.setDividerLocation(0.6);
        splitPane.setDividerSize(6);
        splitPane.setBackground(Theme.BG_DARK);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);

        // Forzar el divider en 60% una vez el frame se muestra
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                splitPane.setDividerLocation(0.6);
            }
        });

        center.add(splitPane, BorderLayout.CENTER);
        return center;
    }

    // ===== ACTION BUTTONS (Controles Individuales, anclados bajo la tabla) =====
    private JPanel createActionButtons() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG_PANEL);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // Etiqueta descriptiva
        JLabel hint = new JLabel("Acciones sobre el proceso seleccionado:");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        wrapper.add(hint, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new GridLayout(1, 5, 8, 0));
        btnRow.setBackground(Theme.BG_PANEL);

        JButton createBtn     = Theme.createStyledButton("Crear Proceso", Theme.ACCENT);
        JButton suspendBtn    = Theme.createStyledButton("Suspender", Theme.WARNING);
        JButton resumeBtn     = Theme.createStyledButton("Reanudar", Theme.INFO);
        JButton terminateBtn  = Theme.createStyledButton("Terminar", Theme.DANGER);
        JButton randomBtn     = Theme.createStyledButton("Aleatorio", Theme.BG_HOVER);

        createBtn.setToolTipText("Crear un nuevo proceso manualmente");
        suspendBtn.setToolTipText("Suspender el proceso seleccionado");
        resumeBtn.setToolTipText("Reanudar el proceso seleccionado");
        terminateBtn.setToolTipText("Forzar la terminacion del proceso seleccionado");
        randomBtn.setToolTipText("Crear un proceso con valores aleatorios");

        createBtn.addActionListener(e -> showCreateDialog());
        suspendBtn.addActionListener(e -> onSuspend());
        resumeBtn.addActionListener(e -> onResume());
        terminateBtn.addActionListener(e -> onTerminate());
        randomBtn.addActionListener(e -> createRandomProcess());

        btnRow.add(createBtn);
        btnRow.add(suspendBtn);
        btnRow.add(resumeBtn);
        btnRow.add(terminateBtn);
        btnRow.add(randomBtn);

        wrapper.add(btnRow, BorderLayout.CENTER);
        return wrapper;
    }

    // ===== ENGINE CONTROL =====
    private void startEngine() {
        engine.start();
        startBtn.setEnabled(false);
        pauseBtn.setEnabled(true);
        stopBtn.setEnabled(true);
        statusLabel.setText("Ejecutando");
        statusLabel.setForeground(Theme.SUCCESS);
    }

    private void togglePause() {
        if (engine.isPaused()) {
            engine.resumeEngine();
            pauseBtn.setText("Pausar");
            statusLabel.setText("Ejecutando");
            statusLabel.setForeground(Theme.SUCCESS);
        } else {
            engine.pause();
            pauseBtn.setText("Reanudar");
            statusLabel.setText("Pausado");
            statusLabel.setForeground(Theme.WARNING);
        }
    }

    private void stopEngine() {
        engine.stop();
        startBtn.setEnabled(true);
        pauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);
        pauseBtn.setText("Pausar");
        statusLabel.setText("Detenido");
        statusLabel.setForeground(Theme.TEXT_MUTED);
    }

    // ===== PROCESS ACTIONS =====
    private void showCreateDialog() {
        ProcessCreationDialog dialog = new ProcessCreationDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            String name = dialog.getProcessName().isEmpty() ? "Proceso" : dialog.getProcessName();
            SimProcess p = new SimProcess(name, dialog.getPriority(),
                    dialog.getBurstTime(), dialog.getMemoryRequired());
            engine.addProcess(p);
            refreshAll();
        }
    }

    private void createRandomProcess() {
        String[] names = {"Chrome", "VSCode", "Spotify", "Discord", "Java", "Steam", "Word", "Excel"};
        String name = names[(int)(Math.random() * names.length)];
        int priority = (int)(Math.random() * 10) + 1;
        int burst = (int)(Math.random() * 15) + 1;
        int memory = ((int)(Math.random() * 8) + 1) * 128;

        SimProcess p = new SimProcess(name, priority, burst, memory);
        engine.addProcess(p);
        refreshAll();
    }

    private void onSuspend() {
        SimProcess selected = getSelectedProcess();
        if (selected == null) { showWarning("Selecciona un proceso en la tabla."); return; }
        try { engine.suspendProcess(selected); refreshAll(); }
        catch (Exception ex) { showWarning(ex.getMessage()); }
    }

    private void onResume() {
        SimProcess selected = getSelectedProcess();
        if (selected == null) { showWarning("Selecciona un proceso en la tabla."); return; }
        try { engine.resumeProcess(selected); refreshAll(); }
        catch (Exception ex) { showWarning(ex.getMessage()); }
    }

    private void onTerminate() {
        SimProcess selected = getSelectedProcess();
        if (selected == null) { showWarning("Selecciona un proceso en la tabla."); return; }
        engine.forceTerminateProcess(selected);
        refreshAll();
    }

    private SimProcess getSelectedProcess() {
        return processTablePanel.getSelectedProcess(engine.getAllProcesses());
    }

    // ===== ALGORITHM CHANGES =====
    private void onAlgorithmChanged() {
        int idx = algorithmCombo.getSelectedIndex();
        if (idx >= 0 && idx < algorithms.length) {
            engine.setAlgorithm(algorithms[idx]);
            quantumSpinner.setEnabled(idx == 2); // Solo para Round Robin
        }
    }

    private void onQuantumChanged() {
        int q = (int) quantumSpinner.getValue();
        if (algorithms[2] instanceof RoundRobinScheduler) {
            ((RoundRobinScheduler) algorithms[2]).setQuantum(q);
        }
    }

    // ===== UI REFRESH =====
    private void refreshAll() {
        List<SimProcess> allProcs = engine.getAllProcesses();
        processTablePanel.updateProcesses(allProcs);

        long activeCount = allProcs.stream()
                .filter(p -> p.getState() != ProcessState.TERMINATED)
                .count();
        resourcePanel.updateResources(resourceManager, (int) activeCount);

        List<SimProcess> readyQueue = engine.getReadyProcesses();
        SimProcess running = allProcs.stream()
                .filter(p -> p.getState() == ProcessState.RUNNING)
                .findFirst().orElse(null);
        queuePanel.updateQueue(readyQueue, running);
    }

    private void setupTimerRefresh() {
        Timer refreshTimer = new Timer(300, e -> refreshAll());
        refreshTimer.start();
    }

    // ===== HELPERS =====
    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BODY);
        label.setForeground(Theme.TEXT_SECONDARY);
        return label;
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        sep.setForeground(Theme.BORDER_COLOR);
        return sep;
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Estiliza un JSpinner para que sea legible en el tema oscuro.
     * Fuerza colores sobre Nimbus usando client properties.
     */
    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(Theme.FONT_BODY);
        spinner.setBackground(Theme.BG_INPUT);
        spinner.setForeground(Theme.TEXT_PRIMARY);
        spinner.setOpaque(true);
        spinner.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(Theme.BG_INPUT);
            tf.setForeground(Theme.TEXT_PRIMARY);
            tf.setCaretColor(Theme.TEXT_PRIMARY);
            tf.setOpaque(true);
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

            tf.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
            javax.swing.UIDefaults overrides = new javax.swing.UIDefaults();
            overrides.put("TextField.background", Theme.BG_INPUT);
            overrides.put("TextField.foreground", Theme.TEXT_PRIMARY);
            tf.putClientProperty("Nimbus.Overrides", overrides);
        }
    }
}
