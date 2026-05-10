package com.simulador.model;

import com.simulador.util.EventLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidad Proceso que encapsula el PCB y gestiona las transiciones de estado.
 * Todas las transiciones son validadas y registradas en el log de eventos.
 */
public class SimProcess {
    private final PCB pcb;
    private final List<String> stateHistory;
    private ProcessState previousState; // Estado antes de suspensión

    public SimProcess(String name, int priority, int burstTime, int memoryRequired) {
        this.pcb = new PCB(name, priority, burstTime, memoryRequired);
        this.stateHistory = new ArrayList<>();
        this.previousState = null;
        logTransition(null, ProcessState.NEW);
    }

    // --- Transiciones de Estado ---

    /** NEW → READY: proceso admitido con memoria asignada */
    public synchronized void admit() {
        validateTransition(ProcessState.NEW, ProcessState.READY);
        changeState(ProcessState.READY);
        EventLog.log("Proceso P" + getPid() + " (" + getName() + ") admitido → LISTO");
    }

    /** READY → RUNNING: planificador selecciona el proceso */
    public synchronized void execute() {
        validateTransition(ProcessState.READY, ProcessState.RUNNING);
        pcb.setCpuAssigned(true);
        changeState(ProcessState.RUNNING);
        EventLog.log("Proceso P" + getPid() + " → EJECUTANDO");
    }

    /** RUNNING → READY: preempción (quantum expirado o proceso de mayor prioridad) */
    public synchronized void preempt() {
        validateTransition(ProcessState.RUNNING, ProcessState.READY);
        pcb.setCpuAssigned(false);
        changeState(ProcessState.READY);
        EventLog.log("Proceso P" + getPid() + " preemptado → LISTO");
    }

    /** RUNNING → WAITING: proceso solicita recurso no disponible */
    public synchronized void block() {
        validateTransition(ProcessState.RUNNING, ProcessState.WAITING);
        pcb.setCpuAssigned(false);
        changeState(ProcessState.WAITING);
        EventLog.log("Proceso P" + getPid() + " bloqueado → ESPERANDO");
    }

    /** WAITING → READY: recurso disponible */
    public synchronized void unblock() {
        validateTransition(ProcessState.WAITING, ProcessState.READY);
        changeState(ProcessState.READY);
        EventLog.log("Proceso P" + getPid() + " desbloqueado → LISTO");
    }

    /** READY|WAITING → SUSPENDED: usuario suspende el proceso */
    public synchronized void suspend() {
        ProcessState current = pcb.getState();
        if (current == ProcessState.RUNNING) {
            pcb.setCpuAssigned(false);
        }
        if (current != ProcessState.READY && current != ProcessState.WAITING && current != ProcessState.RUNNING) {
            throw new IllegalStateException(
                    "No se puede suspender proceso en estado " + current.getDisplayName());
        }
        previousState = current;
        changeState(ProcessState.SUSPENDED);
        EventLog.log("Proceso P" + getPid() + " suspendido (estaba " + previousState.getDisplayName() + ")");
    }

    /** SUSPENDED → READY: usuario reanuda el proceso */
    public synchronized void resume() {
        validateTransition(ProcessState.SUSPENDED, ProcessState.READY);
        changeState(ProcessState.READY);
        EventLog.log("Proceso P" + getPid() + " reanudado → LISTO");
    }

    /** Cualquier estado activo → TERMINATED: terminación normal */
    public synchronized void terminate(String cause) {
        ProcessState current = pcb.getState();
        if (current == ProcessState.TERMINATED) {
            return; // Ya terminado
        }
        pcb.setCpuAssigned(false);
        pcb.setTerminationCause(cause);
        changeState(ProcessState.TERMINATED);
        EventLog.log("Proceso P" + getPid() + " TERMINADO (" + cause + ")");
    }

    /** Terminación forzada por el usuario */
    public synchronized void forceTerminate() {
        terminate("Terminación forzada por usuario");
    }

    /** Decrementa el tiempo restante de ejecución */
    public synchronized void decrementRemainingTime(int amount) {
        pcb.setRemainingTime(pcb.getRemainingTime() - amount);
    }

    /** Verifica si el proceso ha completado su ejecución */
    public synchronized boolean isFinished() {
        return pcb.getRemainingTime() <= 0;
    }

    // --- Delegación al PCB (Getters) ---
    public int getPid() { return pcb.getPid(); }
    public String getName() { return pcb.getProcessName(); }
    public ProcessState getState() { return pcb.getState(); }
    public int getPriority() { return pcb.getPriority(); }
    public int getBurstTime() { return pcb.getBurstTime(); }
    public int getRemainingTime() { return pcb.getRemainingTime(); }
    public int getMemoryRequired() { return pcb.getMemoryRequired(); }
    public int getMemoryAllocated() { return pcb.getMemoryAllocated(); }
    public long getArrivalTime() { return pcb.getArrivalTime(); }
    public String getTerminationCause() { return pcb.getTerminationCause(); }
    public boolean isCpuAssigned() { return pcb.isCpuAssigned(); }
    public PCB getPcb() { return pcb; }
    public List<String> getStateHistory() { return Collections.unmodifiableList(stateHistory); }

    public void setMemoryAllocated(int amount) { pcb.setMemoryAllocated(amount); }

    // --- Helpers ---

    private void validateTransition(ProcessState expected, ProcessState target) {
        ProcessState current = pcb.getState();
        if (current != expected) {
            throw new IllegalStateException(
                    String.format("Transición inválida: %s → %s (estado actual: %s)",
                            expected.getDisplayName(), target.getDisplayName(), current.getDisplayName()));
        }
    }

    private void changeState(ProcessState newState) {
        ProcessState oldState = pcb.getState();
        pcb.setState(newState);
        logTransition(oldState, newState);
    }

    private void logTransition(ProcessState from, ProcessState to) {
        String entry = String.format("[P%d] %s → %s",
                pcb.getPid(),
                from != null ? from.getDisplayName() : "CREADO",
                to.getDisplayName());
        stateHistory.add(entry);
    }

    @Override
    public String toString() {
        return pcb.toString();
    }
}
