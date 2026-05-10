package com.simulador.resource;

import com.simulador.util.EventLog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de Memoria RAM.
 * Administra 4096 MB (4 GB) de memoria con asignación por proceso.
 */
public class MemoryManager {
    public static final int TOTAL_MEMORY_MB = 4096;

    private int availableMemory;
    private final Map<Integer, Integer> allocations; // PID → MB asignados

    public MemoryManager() {
        this.availableMemory = TOTAL_MEMORY_MB;
        this.allocations = new ConcurrentHashMap<>();
    }

    /**
     * Intenta asignar memoria a un proceso.
     * @return true si la asignación fue exitosa
     */
    public synchronized boolean allocate(int pid, int memoryMB) {
        if (memoryMB <= 0) return false;
        if (memoryMB > availableMemory) {
            EventLog.log("MEMORIA: Insuficiente para P" + pid +
                    " (solicitado: " + memoryMB + "MB, disponible: " + availableMemory + "MB)");
            return false;
        }

        availableMemory -= memoryMB;
        allocations.put(pid, allocations.getOrDefault(pid, 0) + memoryMB);
        EventLog.log("MEMORIA: Asignada " + memoryMB + "MB a P" + pid +
                " (disponible: " + availableMemory + "MB)");
        return true;
    }

    /**
     * Libera toda la memoria asignada a un proceso.
     */
    public synchronized void release(int pid) {
        Integer allocated = allocations.remove(pid);
        if (allocated != null && allocated > 0) {
            availableMemory += allocated;
            EventLog.log("MEMORIA: Liberada " + allocated + "MB de P" + pid +
                    " (disponible: " + availableMemory + "MB)");
        }
    }

    /**
     * Verifica si hay memoria suficiente para una cantidad dada.
     */
    public synchronized boolean hasAvailable(int memoryMB) {
        return memoryMB <= availableMemory;
    }

    public synchronized int getAvailableMemory() { return availableMemory; }
    public synchronized int getUsedMemory() { return TOTAL_MEMORY_MB - availableMemory; }
    public synchronized int getTotalMemory() { return TOTAL_MEMORY_MB; }

    /**
     * Retorna una copia del mapa de asignaciones (PID → MB).
     */
    public synchronized Map<Integer, Integer> getAllocationsSnapshot() {
        return new ConcurrentHashMap<>(allocations);
    }

    /**
     * Porcentaje de memoria utilizada (0.0 a 1.0).
     */
    public synchronized double getUsagePercent() {
        return (double) getUsedMemory() / TOTAL_MEMORY_MB;
    }
}
