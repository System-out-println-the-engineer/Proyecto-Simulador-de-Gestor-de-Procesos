package com.simulador.resource;

import com.simulador.model.SimProcess;

/**
 * Fachada para la gestión unificada de recursos (CPU + Memoria).
 * Coordina la solicitud y liberación de ambos recursos.
 */
public class ResourceManager {
    private final MemoryManager memoryManager;
    private final CPUManager cpuManager;

    public ResourceManager() {
        this.memoryManager = new MemoryManager();
        this.cpuManager = new CPUManager();
    }

    /**
     * Intenta asignar memoria a un proceso (al momento de admisión).
     */
    public boolean allocateMemory(SimProcess process) {
        boolean success = memoryManager.allocate(process.getPid(), process.getMemoryRequired());
        if (success) {
            process.setMemoryAllocated(process.getMemoryRequired());
        }
        return success;
    }

    /**
     * Asigna la CPU a un proceso (al momento de ejecución).
     */
    public boolean assignCPU(SimProcess process) {
        return cpuManager.assign(process);
    }

    /**
     * Libera la CPU.
     */
    public void releaseCPU() {
        cpuManager.release();
    }

    /**
     * Libera toda la memoria de un proceso.
     */
    public void releaseMemory(SimProcess process) {
        memoryManager.release(process.getPid());
        process.setMemoryAllocated(0);
    }

    /**
     * Libera todos los recursos de un proceso (CPU + Memoria).
     * Verifica directamente contra el CPUManager en lugar del flag del PCB,
     * ya que terminate() limpia el flag antes de que se llame a este metodo.
     */
    public void releaseAll(SimProcess process) {
        // Verificar si este proceso tiene la CPU asignada en el CPUManager
        SimProcess cpuOwner = cpuManager.getCurrentProcess();
        if (cpuOwner != null && cpuOwner.getPid() == process.getPid()) {
            releaseCPU();
        }
        releaseMemory(process);
    }

    /**
     * Verifica si hay memoria disponible para un proceso.
     */
    public boolean hasMemoryAvailable(int memoryMB) {
        return memoryManager.hasAvailable(memoryMB);
    }

    // --- Acceso a managers individuales ---
    public MemoryManager getMemoryManager() { return memoryManager; }
    public CPUManager getCpuManager() { return cpuManager; }
}
