package com.simulador.scheduler;

import com.simulador.model.SimProcess;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Algoritmo Round Robin.
 * Preemptivo. Cada proceso recibe un quantum de tiempo fijo.
 * Si no termina, se mueve al final de la cola.
 */
public class RoundRobinScheduler implements SchedulingAlgorithm {
    private int quantum;
    private final Queue<Integer> roundRobinOrder; // PIDs en orden circular

    public RoundRobinScheduler(int quantum) {
        this.quantum = Math.max(1, quantum);
        this.roundRobinOrder = new LinkedList<>();
    }

    @Override
    public String getName() {
        return "Round Robin (Q=" + quantum + ")";
    }

    @Override
    public SimProcess selectNext(List<SimProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;

        // Añadir nuevos procesos al orden RR si no están
        for (SimProcess p : readyQueue) {
            if (!roundRobinOrder.contains(p.getPid())) {
                roundRobinOrder.add(p.getPid());
            }
        }

        // Limpiar PIDs que ya no están en ready
        roundRobinOrder.removeIf(pid ->
                readyQueue.stream().noneMatch(p -> p.getPid() == pid));

        if (roundRobinOrder.isEmpty()) return null;

        // Tomar el siguiente PID en la rotación
        int nextPid = roundRobinOrder.poll();
        roundRobinOrder.add(nextPid); // Mover al final

        // Buscar el proceso correspondiente
        for (SimProcess p : readyQueue) {
            if (p.getPid() == nextPid) {
                return p;
            }
        }

        // Fallback: retornar el primero
        return readyQueue.get(0);
    }

    @Override
    public boolean isPreemptive() {
        return true;
    }

    @Override
    public int getQuantum() {
        return quantum;
    }

    public void setQuantum(int quantum) {
        this.quantum = Math.max(1, quantum);
    }

    @Override
    public String toString() {
        return getName();
    }
}
