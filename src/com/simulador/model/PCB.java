package com.simulador.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bloque de Control de Proceso (PCB).
 * Contiene toda la información administrativa de un proceso.
 */
public class PCB {
    private static final AtomicInteger pidCounter = new AtomicInteger(1);

    private final int pid;
    private String processName;
    private ProcessState state;
    private int priority;          // 1 (máxima) a 10 (mínima)
    private int burstTime;         // Tiempo total de CPU requerido (unidades)
    private int remainingTime;     // Tiempo restante de ejecución (unidades)
    private int memoryRequired;    // Memoria requerida (MB)
    private int memoryAllocated;   // Memoria actualmente asignada (MB)
    private long arrivalTime;      // Timestamp de creación
    private String terminationCause;
    private boolean cpuAssigned;

    public PCB(String processName, int priority, int burstTime, int memoryRequired) {
        this.pid = pidCounter.getAndIncrement();
        this.processName = processName;
        this.state = ProcessState.NEW;
        this.priority = Math.max(1, Math.min(10, priority));
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.memoryRequired = memoryRequired;
        this.memoryAllocated = 0;
        this.arrivalTime = System.currentTimeMillis();
        this.terminationCause = null;
        this.cpuAssigned = false;
    }

    // --- Getters ---
    public int getPid() { return pid; }
    public String getProcessName() { return processName; }
    public ProcessState getState() { return state; }
    public int getPriority() { return priority; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getMemoryRequired() { return memoryRequired; }
    public int getMemoryAllocated() { return memoryAllocated; }
    public long getArrivalTime() { return arrivalTime; }
    public String getTerminationCause() { return terminationCause; }
    public boolean isCpuAssigned() { return cpuAssigned; }

    // --- Setters ---
    public void setProcessName(String processName) { this.processName = processName; }
    public void setState(ProcessState state) { this.state = state; }
    public void setPriority(int priority) { this.priority = Math.max(1, Math.min(10, priority)); }
    public void setRemainingTime(int remainingTime) { this.remainingTime = Math.max(0, remainingTime); }
    public void setMemoryAllocated(int memoryAllocated) { this.memoryAllocated = memoryAllocated; }
    public void setTerminationCause(String cause) { this.terminationCause = cause; }
    public void setCpuAssigned(boolean cpuAssigned) { this.cpuAssigned = cpuAssigned; }

    /**
     * Reinicia el contador de PID (útil para testing).
     */
    public static void resetPidCounter() {
        pidCounter.set(1);
    }

    @Override
    public String toString() {
        return String.format("PCB[PID=%d, %s, Estado=%s, Prior=%d, Burst=%d, Restante=%d, Mem=%dMB]",
                pid, processName, state.getDisplayName(), priority, burstTime, remainingTime, memoryRequired);
    }
}
