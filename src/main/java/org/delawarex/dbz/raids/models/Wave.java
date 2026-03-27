package org.delawarex.dbz.raids.models;

import java.util.ArrayList;
import java.util.List;

public class Wave {

    private int waveNumber;
    private List<SpawnPoint> spawnPoints;
    private List<WaveReward> rewards;
    private WaveStatus status;
    private String description;

    public Wave(int waveNumber) {
        this.waveNumber = waveNumber;
        this.spawnPoints = new ArrayList<>();
        this.rewards = new ArrayList<>();
        this.status = WaveStatus.PENDING;
        this.description = "";
    }

    public void addSpawnPoint(SpawnPoint sp) { spawnPoints.add(sp); }
    public void addReward(WaveReward reward) { rewards.add(reward); }
    public boolean hasRewards() { return !rewards.isEmpty(); }

    public int getTotalEnemies() {
        return spawnPoints.stream().mapToInt(SpawnPoint::getQuantity).sum();
    }

    public int getWaveNumber() { return waveNumber; }
    public void setWaveNumber(int waveNumber) { this.waveNumber = waveNumber; }
    public List<SpawnPoint> getSpawnPoints() { return spawnPoints; }
    public void setSpawnPoints(List<SpawnPoint> spawnPoints) { this.spawnPoints = spawnPoints != null ? spawnPoints : new ArrayList<>(); }
    public List<WaveReward> getRewards() { return rewards; }
    public void setRewards(List<WaveReward> rewards) { this.rewards = rewards != null ? rewards : new ArrayList<>(); }
    public WaveStatus getStatus() { return status; }
    public void setStatus(WaveStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }
}