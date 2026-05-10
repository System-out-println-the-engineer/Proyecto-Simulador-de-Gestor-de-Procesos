package com.simulador.ipc;

import com.simulador.util.EventLog;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 * Buffer compartido para el problema del Productor-Consumidor.
 * Utiliza 3 semáforos: empty, full y mutex para la sincronización.
 */
public class SharedBuffer {
    public static final int BUFFER_SIZE = 5;

    private final Queue<String> buffer;
    private final Semaphore empty;   // Cuenta espacios vacíos
    private final Semaphore full;    // Cuenta elementos disponibles
    private final Semaphore mutex;   // Exclusión mutua

    public SharedBuffer() {
        this.buffer = new LinkedList<>();
        this.empty = new Semaphore(BUFFER_SIZE);
        this.full = new Semaphore(0);
        this.mutex = new Semaphore(1);
    }

    /**
     * Produce un elemento y lo coloca en el buffer.
     * Se bloquea si el buffer está lleno.
     */
    public void produce(String item) throws InterruptedException {
        empty.acquire();     // Esperar espacio disponible
        mutex.acquire();     // Sección crítica
        try {
            buffer.add(item);
            EventLog.log("PRODUCTOR: Produjo \"" + item + "\" [Buffer: " + buffer.size() + "/" + BUFFER_SIZE + "]");
        } finally {
            mutex.release();     // Salir sección crítica
            full.release();      // Señalar nuevo elemento disponible
        }
    }

    /**
     * Consume un elemento del buffer.
     * Se bloquea si el buffer está vacío.
     */
    public String consume() throws InterruptedException {
        full.acquire();      // Esperar elemento disponible
        mutex.acquire();     // Sección crítica
        try {
            String item = buffer.poll();
            EventLog.log("CONSUMIDOR: Consumio \"" + item + "\" [Buffer: " + buffer.size() + "/" + BUFFER_SIZE + "]");
            return item;
        } finally {
            mutex.release();     // Salir sección crítica
            empty.release();     // Señalar espacio disponible
        }
    }

    /**
     * Retorna el tamaño actual del buffer (para visualización).
     */
    public int getCurrentSize() {
        return buffer.size();
    }

    /**
     * Retorna una copia del contenido del buffer.
     */
    public String[] getContents() {
        synchronized (buffer) {
            return buffer.toArray(new String[0]);
        }
    }
}
