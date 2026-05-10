package com.simulador.model;

import java.awt.Color;

/**
 * Enum que representa los posibles estados de un proceso en el simulador.
 * Cada estado tiene un nombre para mostrar en la GUI y un color asociado.
 */
public enum ProcessState {
    NEW("Nuevo", new Color(156, 163, 175)),
    READY("Listo", new Color(59, 130, 246)),
    RUNNING("Ejecutando", new Color(34, 197, 94)),
    WAITING("Esperando", new Color(245, 158, 11)),
    SUSPENDED("Suspendido", new Color(168, 85, 247)),
    TERMINATED("Terminado", new Color(239, 68, 68));

    private final String displayName;
    private final Color color;

    ProcessState(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getColor() {
        return color;
    }
}
