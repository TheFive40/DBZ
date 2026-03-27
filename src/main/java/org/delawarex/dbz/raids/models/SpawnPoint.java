package org.delawarex.dbz.raids.models;

import org.bukkit.Location;

public class SpawnPoint {

    private Location location;
    private String npcName;
    private int npcTab;
    private int quantity;
    private int aliveCount;

    public SpawnPoint(Location location, String npcName, int npcTab, int quantity) {
        this.location = location;
        this.npcName = npcName;
        this.npcTab = npcTab;
        this.quantity = quantity;
        this.aliveCount = quantity;
    }

    public void resetAliveCount() { this.aliveCount = quantity; }
    public void decrementAliveCount() { if (aliveCount > 0) aliveCount--; }
    public boolean allDefeated() { return aliveCount <= 0; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public String getNpcName() { return npcName; }
    public void setNpcName(String npcName) { this.npcName = npcName; }
    public int getNpcTab() { return npcTab; }
    public void setNpcTab(int npcTab) { this.npcTab = npcTab; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getAliveCount() { return aliveCount; }
    public void setAliveCount(int aliveCount) { this.aliveCount = aliveCount; }
}