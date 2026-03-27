package org.delawarex.dbz.raids.models;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

public class Raid {

    private String raidId;
    private String raidName;
    private String description;
    private Location playerSpawnPoint;
    private List<Wave> waves;
    private RaidStatus status;
    private long cooldownSeconds;
    private int minPlayers;
    private int maxPlayers;
    private boolean enabled;

    public Raid(String raidId, String raidName) {
        this.raidId = raidId;
        this.raidName = raidName;
        this.waves = new ArrayList<>();
        this.status = RaidStatus.IDLE;
        this.minPlayers = 1;
        this.maxPlayers = 5;
        this.cooldownSeconds = 3600;
        this.enabled = true;
        this.description = "";
    }

    public Wave getWaveByIndex(int index) {
        if (index >= 0 && index < waves.size()) return waves.get(index);
        return null;
    }

    public void addWave(Wave wave) { waves.add(wave); }
    public int getTotalWaves() { return waves.size(); }

    public boolean isConfigured() {
        return playerSpawnPoint != null && !waves.isEmpty();
    }

    public String getRaidId() { return raidId; }
    public void setRaidId(String raidId) { this.raidId = raidId; }
    public String getRaidName() { return raidName; }
    public void setRaidName(String raidName) { this.raidName = raidName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }
    public Location getPlayerSpawnPoint() { return playerSpawnPoint; }
    public void setPlayerSpawnPoint(Location playerSpawnPoint) { this.playerSpawnPoint = playerSpawnPoint; }
    public List<Wave> getWaves() { return waves; }
    public void setWaves(List<Wave> waves) { this.waves = waves != null ? waves : new ArrayList<>(); }
    public RaidStatus getStatus() { return status; }
    public void setStatus(RaidStatus status) { this.status = status; }
    public long getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(long cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }
    public int getMinPlayers() { return minPlayers; }
    public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}