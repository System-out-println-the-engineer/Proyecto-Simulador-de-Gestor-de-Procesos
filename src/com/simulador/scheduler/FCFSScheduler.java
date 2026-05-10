package com.simulador.scheduler;

import com.simulador.model.SimProcess;
import java.util.List;

/**
 * Algoritmo First Come First Served (FCFS).
 * No preemptivo. Selecciona el proceso que llegó primero.
 */
public class FCFSScheduler implements SchedulingAlgorithm {

    @Override
    public String getName() {
        return "FCFS (First Come First Served)";
    }

    @Override
    public SimProcess selectNext(List<SimProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;

        // Seleccionar el proceso con el menor arrivalTime (el más antiguo)
        SimProcess oldest = readyQueue.get(0);
        for (SimProcess p : readyQueue) {
            if (p.getArrivalTime() < oldest.getArrivalTime()) {
                oldest = p;
            }
        }
        return oldest;
    }

    @Override
    public boolean isPreemptive() {
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }
}
