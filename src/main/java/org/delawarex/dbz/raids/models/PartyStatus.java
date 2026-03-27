package org.delawarex.dbz.raids.models;

public enum PartyStatus {
    WAITING, IN_RAID, DISBANDED;

    public String getDisplayName() {
        return switch (this) {
            case WAITING -> "Esperando";
            case IN_RAID -> "En Raid";
            case DISBANDED -> "Disuelta";
        };
    }
}