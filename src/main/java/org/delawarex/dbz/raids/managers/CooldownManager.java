package org.delawarex.dbz.raids.managers;

import org.delawarex.dbz.raids.storage.CooldownStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private static final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private static CooldownStorage storage;

    public static void init() {
        storage = new CooldownStorage();
        load();
    }

    public static void setCooldown(UUID uuid, String raidId, long seconds) {
        long end = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(raidId, end);
        if (storage != null) storage.saveCooldown(uuid, raidId, end);
    }

    public static boolean hasCooldown(UUID uuid, String raidId) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return false;
        Long end = map.get(raidId);
        if (end == null) return false;
        if (System.currentTimeMillis() >= end) {
            map.remove(raidId);
            if (storage != null) storage.removeCooldown(uuid, raidId);
            return false;
        }
        return true;
    }

    public static long getRemaining(UUID uuid, String raidId) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map == null) return 0;
        Long end = map.get(raidId);
        if (end == null) return 0;
        return Math.max(0, (end - System.currentTimeMillis()) / 1000);
    }

    public static String getFormatted(UUID uuid, String raidId) {
        long secs = getRemaining(uuid, raidId);
        long h = secs / 3600, m = (secs % 3600) / 60, s = secs % 60;
        if (h > 0) return h + "h " + m + "m " + s + "s";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    public static void clearCooldown(UUID uuid, String raidId) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map != null) {
            map.remove(raidId);
            if (storage != null) storage.removeCooldown(uuid, raidId);
        }
    }

    private static void load() {
        if (storage == null) return;
        long now = System.currentTimeMillis();
        storage.loadAll().forEach((uuid, map) -> {
            Map<String, Long> active = new ConcurrentHashMap<>();
            map.forEach((raidId, end) -> {
                if (end > now) active.put(raidId, end);
            });
            if (!active.isEmpty()) cooldowns.put(uuid, active);
        });
    }
}