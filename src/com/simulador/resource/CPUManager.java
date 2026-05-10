package com.simulador.resource;

import com.simulador.model.SimProcess;
import com.simulador.util.EventLog;

/**
 * Gestor de CPU.
 * Administra 1 CPU disponible para el simulador.
 */
public class CPUManager {
    private SimProcess currentProcess;

    public CPUManager() {
        this.currentProcess = null;
    }

    /**
     * Asigna la CPU a un proceso.
     * @return true si la asignación fue exitosa
     */
    public synchronized boolean assign(SimProcess process) {
        if (currentProcess != null) {
            EventLog.log("CPU: Ocupada por P" + currentProcess.getPid() +
                    ", no se puede asignar a P" + process.getPid());
            return false;
        }
        currentProcess = process;
        EventLog.log("CPU: Asignada a P" + process.getPid() + " (" + process.getName() + ")");
        return true;
    }

    /**
     * Libera la CPU.
     */
    public synchronized void release() {
        if (currentProcess != null) {
            EventLog.log("CPU: Liberada de P" + currentProcess.getPid());
            currentProcess = null;
        }
    }

    /**
     * Verifica si la CPU está disponible.
     */
    public synchronized boolean isAvailable() {
        return currentProcess == null;
    }

    public synchronized boolean isBusy() {
        return currentProcess != null;
    }

    public synchronized SimProcess getCurrentProcess() {
        return currentProcess;
    }
}
