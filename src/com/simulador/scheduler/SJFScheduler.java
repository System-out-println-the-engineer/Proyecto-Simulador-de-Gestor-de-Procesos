package com.simulador.scheduler;

import com.simulador.model.SimProcess;
import java.util.List;

/**
 * Algoritmo Shortest Job First (SJF).
 * No preemptivo. Selecciona el proceso con el menor burst time restante.
 */
public class SJFScheduler implements SchedulingAlgorithm {

    @Override
    public String getName() {
        return "SJF (Shortest Job First)";
    }

    @Override
    public SimProcess selectNext(List<SimProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;

        // Seleccionar el proceso con el menor remainingTime
        SimProcess shortest = readyQueue.get(0);
        for (SimProcess p : readyQueue) {
            if (p.getRemainingTime() < shortest.getRemainingTime()) {
                shortest = p;
            } else if (p.getRemainingTime() == shortest.getRemainingTime()
                    && p.getArrivalTime() < shortest.getArrivalTime()) {
                // Desempate por orden de llegada
                shortest = p;
            }
        }
        return shortest;
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
