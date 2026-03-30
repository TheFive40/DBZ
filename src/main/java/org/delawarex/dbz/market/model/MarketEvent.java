package org.delawarex.dbz.market.model;

public class MarketEvent {

    private final String id;
    private final String name;
    private final String description;
    private final double priceMultiplier;
    private final long startTime;
    private final long durationMillis;
    private final String affectedItemId;

    public MarketEvent(String id, String name, String description, double priceMultiplier, long durationMillis, String affectedItemId) {
        this.id              = id;
        this.name            = name;
        this.description     = description;
        this.priceMultiplier = priceMultiplier;
        this.durationMillis  = durationMillis;
        this.affectedItemId  = affectedItemId;
        this.startTime       = System.currentTimeMillis();
    }

    public boolean isActive() {
        return System.currentTimeMillis() < startTime + durationMillis;
    }

    public boolean affects(String itemId) {
        return affectedItemId == null || affectedItemId.equalsIgnoreCase(itemId);
    }

    public long getRemainingMillis() {
        return Math.max(0, (startTime + durationMillis) - System.currentTimeMillis());
    }

    public String getId()              { return id; }
    public String getName()            { return name; }
    public String getDescription()     { return description; }
    public double getPriceMultiplier() { return priceMultiplier; }
    public long   getDurationMillis()  { return durationMillis; }
    public String getAffectedItemId()  { return affectedItemId; }
    public long   getStartTime()       { return startTime; }
}
