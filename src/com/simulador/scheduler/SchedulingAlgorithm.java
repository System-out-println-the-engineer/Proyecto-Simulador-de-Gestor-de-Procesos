package com.simulador.scheduler;

import com.simulador.model.SimProcess;
import java.util.List;

/**
 * Interfaz base para los algoritmos de planificación.
 * Cada algoritmo implementa su propia estrategia de selección.
 */
public interface SchedulingAlgorithm {

    /**
     * Nombre del algoritmo para mostrar en la GUI.
     */
    String getName();

    /**
     * Selecciona el siguiente proceso a ejecutar de la cola de listos.
     * @param readyQueue lista de procesos en estado READY
     * @return el proceso seleccionado, o null si la cola está vacía
     */
    SimProcess selectNext(List<SimProcess> readyQueue);

    /**
     * Indica si este algoritmo es preemptivo.
     */
    boolean isPreemptive();

    /**
     * Para Round Robin: retorna el quantum configurado.
     * Para otros algoritmos retorna -1.
     */
    default int getQuantum() {
        return -1;
    }
}
