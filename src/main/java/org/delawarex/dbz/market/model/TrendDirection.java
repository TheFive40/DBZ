package org.delawarex.dbz.market.model;

public enum TrendDirection {
    RISING, FALLING, STABLE;

    public String getDisplay() {
        return switch (this) {
            case RISING  -> "&a↑ Subiendo";
            case FALLING -> "&c↓ Bajando";
            case STABLE  -> "&e→ Estable";
        };
    }

    public String getSymbol() {
        return switch (this) {
            case RISING  -> "&a↑";
            case FALLING -> "&c↓";
            case STABLE  -> "&e→";
        };
    }
}
