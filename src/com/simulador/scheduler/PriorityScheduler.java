package com.simulador.scheduler;

import com.simulador.model.SimProcess;
import java.util.List;

/**
 * Algoritmo de Planificación por Prioridades.
 * Preemptivo. Selecciona el proceso con la mayor prioridad (menor número).
 * Prioridad 1 = máxima, Prioridad 10 = mínima.
 */
public class PriorityScheduler implements SchedulingAlgorithm {

    @Override
    public String getName() {
        return "Prioridades";
    }

    @Override
    public SimProcess selectNext(List<SimProcess> readyQueue) {
        if (readyQueue.isEmpty()) return null;

        // Seleccionar el proceso con el menor valor de prioridad (mayor prioridad)
        SimProcess highest = readyQueue.get(0);
        for (SimProcess p : readyQueue) {
            if (p.getPriority() < highest.getPriority()) {
                highest = p;
            } else if (p.getPriority() == highest.getPriority()
                    && p.getArrivalTime() < highest.getArrivalTime()) {
                // Desempate por orden de llegada
                highest = p;
            }
        }
        return highest;
    }

    @Override
    public boolean isPreemptive() {
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }
}
