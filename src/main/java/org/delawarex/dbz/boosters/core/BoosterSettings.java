package org.delawarex.dbz.boosters.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BoosterSettings {

    private static final Map<String, Double> globalMultipliers = new ConcurrentHashMap<>();
    private static final Map<Integer, Double> personalBoosterLevels = new ConcurrentHashMap<>();
    private static final Map<String, Double> rankMultipliers = new ConcurrentHashMap<>();

    private static long globalBoosterDuration = 3600;
    private static long personalBoosterDuration = 900;
    private static long personalBoosterStorageDays = 30;

    static {
        initializeDefaults();
    }

    private static void initializeDefaults() {
        personalBoosterLevels.put(1, 0.10);
        personalBoosterLevels.put(2, 0.25);
        personalBoosterLevels.put(3, 0.50);
        personalBoosterLevels.put(4, 1.00);
        personalBoosterLevels.put(5, 2.00);

        rankMultipliers.put("admin", 2.0);
        rankMultipliers.put("moderator", 1.8);
        rankMultipliers.put("vip+", 1.4);
        rankMultipliers.put("vip", 1.3);
    }

    public static void setGlobalMultiplier(double multiplier) {
        globalMultipliers.put("current", multiplier);
    }

    public static double getGlobalMultiplier() {
        return globalMultipliers.getOrDefault("current", 1.0);
    }

    public static void setPersonalBoosterMultiplier(int level, double multiplier) {
        if (level >= 1 && level <= 5) personalBoosterLevels.put(level, multiplier);
    }

    public static double getPersonalBoosterMultiplier(int level) {
        return personalBoosterLevels.getOrDefault(level, 0.0);
    }

    public static void setRankMultiplier(String rank, double multiplier) {
        rankMultipliers.put(rank.toLowerCase(), multiplier);
    }

    public static double getRankMultiplier(String rank) {
        return rankMultipliers.getOrDefault(rank.toLowerCase(), 1.0);
    }

    public static Map<Integer, Double> getAllPersonalBoosterLevels() {
        return new HashMap<>(personalBoosterLevels);
    }

    public static Map<String, Double> getAllRankMultipliers() {
        return new HashMap<>(rankMultipliers);
    }

    public static void setGlobalBoosterDuration(long seconds) { globalBoosterDuration = seconds; }
    public static long getGlobalBoosterDuration() { return globalBoosterDuration; }
    public static void setPersonalBoosterDuration(long seconds) { personalBoosterDuration = seconds; }
    public static long getPersonalBoosterDuration() { return personalBoosterDuration; }
    public static void setPersonalBoosterStorageDays(long days) { personalBoosterStorageDays = days; }
    public static long getPersonalBoosterStorageDays() { return personalBoosterStorageDays; }
}
