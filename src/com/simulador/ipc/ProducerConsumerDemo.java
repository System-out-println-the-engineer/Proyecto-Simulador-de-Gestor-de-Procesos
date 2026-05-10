package com.simulador.ipc;

import com.simulador.util.EventLog;

/**
 * Demostración del problema del Productor-Consumidor.
 * Crea hilos productor y consumidor que interactúan con un buffer compartido.
 */
public class ProducerConsumerDemo {

    private final SharedBuffer buffer;
    private Thread producerThread;
    private Thread consumerThread;
    private volatile boolean running = false;
    private volatile boolean paused = false;

    private int producerDelayMs = 1500;  // Velocidad de producción
    private int consumerDelayMs = 2000;  // Velocidad de consumo
    private int producedCount = 0;
    private int consumedCount = 0;

    private Runnable onUpdateCallback;

    public ProducerConsumerDemo() {
        this.buffer = new SharedBuffer();
    }

    /**
     * Inicia la demostración creando los hilos productor y consumidor.
     */
    public void start() {
        if (running) return;
        running = true;
        paused = false;
        producedCount = 0;
        consumedCount = 0;

        EventLog.log("IPC: Demo Productor-Consumidor iniciada");

        // Hilo Productor
        producerThread = new Thread(() -> {
            int itemNum = 1;
            while (running) {
                if (!paused) {
                    try {
                        String item = "Item-" + itemNum;
                        buffer.produce(item);
                        itemNum++;
                        producedCount++;
                        notifyUpdate();
                        Thread.sleep(producerDelayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    try { Thread.sleep(100); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "Productor");
        producerThread.setDaemon(true);

        // Hilo Consumidor
        consumerThread = new Thread(() -> {
            while (running) {
                if (!paused) {
                    try {
                        buffer.consume();
                        consumedCount++;
                        notifyUpdate();
                        Thread.sleep(consumerDelayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    try { Thread.sleep(100); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "Consumidor");
        consumerThread.setDaemon(true);

        producerThread.start();
        consumerThread.start();
    }

    /**
     * Pausa la demostración.
     */
    public void pause() {
        paused = true;
        EventLog.log("IPC: Demo pausada");
    }

    /**
     * Reanuda la demostración.
     */
    public void resumeDemo() {
        paused = false;
        EventLog.log("IPC: Demo reanudada");
    }

    /**
     * Detiene la demostración y finaliza los hilos.
     */
    public void stop() {
        running = false;
        paused = false;

        if (producerThread != null) producerThread.interrupt();
        if (consumerThread != null) consumerThread.interrupt();

        EventLog.log("IPC: Demo Productor-Consumidor detenida " +
                "(Producidos: " + producedCount + ", Consumidos: " + consumedCount + ")");
    }

    private void notifyUpdate() {
        if (onUpdateCallback != null) {
            javax.swing.SwingUtilities.invokeLater(onUpdateCallback);
        }
    }

    // --- Configuración ---

    public void setProducerDelay(int ms) { this.producerDelayMs = Math.max(200, ms); }
    public void setConsumerDelay(int ms) { this.consumerDelayMs = Math.max(200, ms); }
    public void setOnUpdateCallback(Runnable callback) { this.onUpdateCallback = callback; }

    // --- Getters ---

    public SharedBuffer getBuffer() { return buffer; }
    public int getProducedCount() { return producedCount; }
    public int getConsumedCount() { return consumedCount; }
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
    public int getBufferSize() { return buffer.getCurrentSize(); }
}
