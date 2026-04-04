package org.delawarex.dbz.battlepass.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BattlePassPlayer {

    private String uuid;
    private String playerName;
    private Map<String, Integer> points;
    private Map<String, String> claimedRaw;

    public BattlePassPlayer() {
        this.points = new HashMap<>();
        this.claimedRaw = new HashMap<>();
    }

    public BattlePassPlayer(String uuid, String playerName) {
        this();
        this.uuid = uuid;
        this.playerName = playerName;
    }

    public int getPoints(String passId) {
        return points.getOrDefault(passId, 0);
    }

    public void addPoints(String passId, int amount) {
        points.put(passId, Math.max(0, getPoints(passId) + amount));
    }

    public void setPoints(String passId, int amount) {
        points.put(passId, Math.max(0, amount));
    }

    public Set<Integer> getClaimedLevels(String passId) {
        String raw = claimedRaw.getOrDefault(passId, "");
        if (raw.isEmpty()) return new HashSet<>();
        Set<Integer> result = new HashSet<>();
        for (String s : raw.split(",")) {
            try { result.add(Integer.parseInt(s.trim())); } catch (Exception ignored) {}
        }
        return result;
    }

    public boolean hasClaimed(String passId, int levelNumber) {
        return getClaimedLevels(passId).contains(levelNumber);
    }

    public void addClaimed(String passId, int levelNumber) {
        Set<Integer> current = getClaimedLevels(passId);
        current.add(levelNumber);
        claimedRaw.put(passId, current.stream().sorted().map(String::valueOf).collect(Collectors.joining(",")));
    }

    public String getUuid() { return uuid; }
    public void setUuid(String v) { this.uuid = v; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String v) { this.playerName = v; }
    public Map<String, Integer> getPoints() { return points; }
    public void setPoints(Map<String, Integer> v) { this.points = v != null ? v : new HashMap<>(); }
    public Map<String, String> getClaimedRaw() { return claimedRaw; }
    public void setClaimedRaw(Map<String, String> v) { this.claimedRaw = v != null ? v : new HashMap<>(); }
}