package org.delawarex.dbz.raids.models;

public enum WaveStatus {
    PENDING, ACTIVE, COMPLETED, FAILED;

    public String getDisplayName() {
        return switch (this) {
            case PENDING -> "Pendiente";
            case ACTIVE -> "Activa";
            case COMPLETED -> "Completada";
            case FAILED -> "Fallida";
        };
    }
}