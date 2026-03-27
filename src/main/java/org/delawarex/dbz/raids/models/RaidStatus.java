package org.delawarex.dbz.raids.models;

public enum RaidStatus {
    IDLE, IN_PROGRESS, COMPLETED, FAILED;

    public String getDisplayName() {
        return switch (this) {
            case IDLE -> "Inactivo";
            case IN_PROGRESS -> "En Progreso";
            case COMPLETED -> "Completada";
            case FAILED -> "Fallida";
        };
    }
}