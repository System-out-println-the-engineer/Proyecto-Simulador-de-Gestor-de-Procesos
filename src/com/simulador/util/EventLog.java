package com.simulador.util;

import javax.swing.SwingUtilities;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Sistema de logging centralizado para el simulador.
 * Registra eventos con timestamp y notifica a la GUI.
 */
public class EventLog {
    private static final List<String> logs = new ArrayList<>();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static Consumer<String> listener;

    /**
     * Registra un listener para recibir mensajes de log en el EDT.
     */
    public static void setListener(Consumer<String> logListener) {
        listener = logListener;
    }

    /**
     * Registra un mensaje de log con timestamp.
     */
    public static synchronized void log(String message) {
        String timestamped = "[" + LocalTime.now().format(TIME_FORMAT) + "] " + message;
        logs.add(timestamped);

        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.accept(timestamped));
        }
    }

    /**
     * Retorna una copia inmutable de todos los logs.
     */
    public static synchronized List<String> getLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logs));
    }

    /**
     * Limpia todos los logs.
     */
    public static synchronized void clear() {
        logs.clear();
    }
}
