package com.simulador.scheduler;

import com.simulador.model.ProcessState;
import com.simulador.model.SimProcess;
import com.simulador.resource.ResourceManager;
import com.simulador.util.EventLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Motor de ejecución del planificador.
 * Corre en un hilo independiente y simula el ciclo de vida de los procesos.
 */
public class SchedulerEngine implements Runnable {

    private final List<SimProcess> allProcesses;
    private final ResourceManager resourceManager;
    private SchedulingAlgorithm currentAlgorithm;

    private volatile boolean running = false;
    private volatile boolean paused = false;
    private int tickIntervalMs = 1000;  // Intervalo entre ticks (ms)
    private int currentQuantumCount = 0; // Contador de ticks para RR

    private Thread engineThread;
    private Runnable onTickCallback; // Callback para actualizar GUI

    public SchedulerEngine(ResourceManager resourceManager, SchedulingAlgorithm initialAlgorithm) {
        this.allProcesses = new CopyOnWriteArrayList<>();
        this.resourceManager = resourceManager;
        this.currentAlgorithm = initialAlgorithm;
    }

    /**
     * Agrega un nuevo proceso al sistema.
     * Intenta asignar memoria: si hay disponible → READY, si no → permanece en NEW.
     * Los procesos en NEW serán reintentados en cada tick por checkNewProcesses().
     */
    public synchronized void addProcess(SimProcess process) {
        allProcesses.add(process);

        if (resourceManager.allocateMemory(process)) {
            process.admit(); // NEW → READY
        } else {
            // No hay memoria: el proceso permanece en NEW hasta que haya recursos
            EventLog.log("CONFLICTO: Memoria insuficiente para P" + process.getPid()
                    + " (" + process.getName() + ") - solicitado: "
                    + process.getMemoryRequired() + "MB. Proceso en espera de admision.");
        }
    }

    /**
     * Suspende un proceso.
     */
    public synchronized void suspendProcess(SimProcess process) {
        if (process.getState() == ProcessState.TERMINATED) return;

        ProcessState currentState = process.getState();
        if (currentState == ProcessState.RUNNING) {
            resourceManager.releaseCPU();
            currentQuantumCount = 0;
        }
        process.suspend();
    }

    /**
     * Reanuda un proceso suspendido.
     */
    public synchronized void resumeProcess(SimProcess process) {
        if (process.getState() != ProcessState.SUSPENDED) return;
        process.resume();
    }

    /**
     * Termina forzosamente un proceso.
     */
    public synchronized void forceTerminateProcess(SimProcess process) {
        if (process.getState() == ProcessState.TERMINATED) return;

        process.forceTerminate();
        resourceManager.releaseAll(process);
        currentQuantumCount = 0;

        // Intentar admitir procesos en espera
        checkPendingProcesses();
    }

    /**
     * Ciclo principal del motor de planificación.
     */
    @Override
    public void run() {
        EventLog.log("Motor de planificacion iniciado (" + currentAlgorithm.getName() + ")");
        running = true;

        while (running) {
            if (!paused) {
                synchronized (this) {
                    tick();
                }
                // Notificar GUI
                if (onTickCallback != null) {
                    javax.swing.SwingUtilities.invokeLater(onTickCallback);
                }
            }

            try {
                Thread.sleep(tickIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        EventLog.log("Motor de planificacion detenido");
    }

    /**
     * Un tick de simulación.
     */
    private void tick() {
        // 0. Intentar admitir procesos pendientes (NEW o WAITING)
        checkPendingProcesses();

        // 1. Buscar proceso actualmente en ejecución
        SimProcess runningProcess = findRunningProcess();

        // 2. Si hay un proceso ejecutándose
        if (runningProcess != null) {
            // Decrementar tiempo restante
            runningProcess.decrementRemainingTime(1);

            // ¿Terminó?
            if (runningProcess.isFinished()) {
                runningProcess.terminate("Finalización normal");
                resourceManager.releaseAll(runningProcess);
                currentQuantumCount = 0;
                checkPendingProcesses();
                return;
            }

            // ¿Es preemptivo y se acabó el quantum?
            if (currentAlgorithm.isPreemptive()) {
                currentQuantumCount++;
                int quantum = currentAlgorithm.getQuantum();

                if (quantum > 0 && currentQuantumCount >= quantum) {
                    // Round Robin: quantum expirado
                    runningProcess.preempt();
                    resourceManager.releaseCPU();
                    currentQuantumCount = 0;
                    EventLog.log("Quantum expirado para P" + runningProcess.getPid());
                } else if (quantum < 0) {
                    // Prioridades: verificar si hay un proceso con mayor prioridad
                    List<SimProcess> readyQueue = getReadyProcesses();
                    if (!readyQueue.isEmpty()) {
                        SimProcess next = currentAlgorithm.selectNext(readyQueue);
                        if (next != null && next.getPriority() < runningProcess.getPriority()) {
                            runningProcess.preempt();
                            resourceManager.releaseCPU();
                            currentQuantumCount = 0;
                        }
                    }
                }
            }
            return;
        }

        // 3. No hay proceso ejecutándose → seleccionar siguiente
        List<SimProcess> readyQueue = getReadyProcesses();
        if (readyQueue.isEmpty()) return;

        SimProcess next = currentAlgorithm.selectNext(readyQueue);
        if (next == null) return;

        // Asignar CPU
        if (resourceManager.assignCPU(next)) {
            next.execute(); // READY → RUNNING
            currentQuantumCount = 0;
        }
    }

    /**
     * Verifica procesos pendientes de admision y los admite si hay memoria disponible.
     * Revisa procesos en estado NEW (rechazados en admision) y WAITING (bloqueados por recurso).
     */
    private void checkPendingProcesses() {
        for (SimProcess p : allProcesses) {
            ProcessState state = p.getState();
            if (state == ProcessState.NEW) {
                // Proceso que nunca fue admitido por falta de memoria
                if (resourceManager.allocateMemory(p)) {
                    p.admit(); // NEW → READY
                    EventLog.log("Proceso P" + p.getPid() + " (" + p.getName()
                            + ") admitido tras liberacion de memoria.");
                }
            } else if (state == ProcessState.WAITING) {
                // Proceso que fue bloqueado durante la ejecucion
                if (resourceManager.allocateMemory(p)) {
                    p.unblock(); // WAITING → READY
                }
            }
        }
    }

    /**
     * Encuentra el proceso actualmente en ejecución.
     */
    private SimProcess findRunningProcess() {
        for (SimProcess p : allProcesses) {
            if (p.getState() == ProcessState.RUNNING) {
                return p;
            }
        }
        return null;
    }

    /**
     * Retorna la lista de procesos en estado READY.
     */
    public List<SimProcess> getReadyProcesses() {
        List<SimProcess> ready = new ArrayList<>();
        for (SimProcess p : allProcesses) {
            if (p.getState() == ProcessState.READY) {
                ready.add(p);
            }
        }
        return ready;
    }

    // --- Control del motor ---

    public void start() {
        if (engineThread != null && engineThread.isAlive()) return;
        running = true;
        paused = false;
        engineThread = new Thread(this, "SchedulerEngine");
        engineThread.setDaemon(true);
        engineThread.start();
    }

    public void pause() {
        paused = true;
        EventLog.log("Motor de planificacion pausado");
    }

    public void resumeEngine() {
        paused = false;
        EventLog.log("Motor de planificacion reanudado");
    }

    public void stop() {
        running = false;
        paused = false;
        if (engineThread != null) {
            engineThread.interrupt();
        }
    }

    // --- Configuración ---

    public synchronized void setAlgorithm(SchedulingAlgorithm algorithm) {
        this.currentAlgorithm = algorithm;
        this.currentQuantumCount = 0;
        EventLog.log("Algoritmo cambiado a: " + algorithm.getName());
    }

    public void setTickInterval(int ms) {
        this.tickIntervalMs = Math.max(100, ms);
    }

    public void setOnTickCallback(Runnable callback) {
        this.onTickCallback = callback;
    }

    // --- Getters ---

    public List<SimProcess> getAllProcesses() {
        return Collections.unmodifiableList(new ArrayList<>(allProcesses));
    }

    public SchedulingAlgorithm getCurrentAlgorithm() {
        return currentAlgorithm;
    }

    public boolean isRunning() { return running && !paused; }
    public boolean isPaused() { return paused; }
    public boolean isStopped() { return !running; }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
