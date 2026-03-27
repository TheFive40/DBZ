package org.delawarex.dbz.raids.models;

import java.util.*;

public class RaidSession {

    private final String sessionId;
    private final Raid raid;
    private final Party party;
    private int currentWaveIndex;
    private RaidStatus status;
    private final long startTime;
    private long endTime;
    private final Set<UUID> activePlayers;
    private final Set<UUID> deadPlayers;

    public RaidSession(String sessionId, Raid raid, Party party) {
        this.sessionId = sessionId;
        this.raid = raid;
        this.party = party;
        this.currentWaveIndex = 0;
        this.status = RaidStatus.IN_PROGRESS;
        this.startTime = System.currentTimeMillis();
        this.activePlayers = new HashSet<>(party.getActivePlayers());
        this.deadPlayers = new HashSet<>();
    }

    public Wave getCurrentWave() { return raid.getWaveByIndex(currentWaveIndex); }
    public boolean hasNextWave() { return currentWaveIndex + 1 < raid.getTotalWaves(); }
    public void moveToNextWave() { currentWaveIndex++; }

    public void playerDied(UUID uuid) {
        activePlayers.remove(uuid);
        deadPlayers.add(uuid);
    }

    public void playerLeft(UUID uuid) { activePlayers.remove(uuid); }
    public boolean isRaidFailed() { return activePlayers.isEmpty(); }

    public int getProgress() {
        return (currentWaveIndex * 100) / Math.max(1, raid.getTotalWaves());
    }

    public long getDurationSeconds() {
        long end = endTime > 0 ? endTime : System.currentTimeMillis();
        return (end - startTime) / 1000;
    }

    public String getSessionId() { return sessionId; }
    public Raid getRaid() { return raid; }
    public Party getParty() { return party; }
    public int getCurrentWaveIndex() { return currentWaveIndex; }
    public RaidStatus getStatus() { return status; }
    public void setStatus(RaidStatus status) { this.status = status; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public Set<UUID> getActivePlayers() { return activePlayers; }
    public Set<UUID> getDeadPlayers() { return deadPlayers; }
}