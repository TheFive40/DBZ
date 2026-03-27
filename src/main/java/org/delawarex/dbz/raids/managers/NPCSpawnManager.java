package org.delawarex.dbz.raids.managers;

import net.minecraft.server.level.ServerLevel;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.raids.models.SpawnPoint;
import org.delawarex.dbz.raids.models.Wave;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NPCSpawnManager {

    private static final Map<Integer, String> entityToWave = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> waveAliveCount = new ConcurrentHashMap<>();

    private static class SpawnTask {
        final Location location;
        final String name;
        final int tab;

        SpawnTask(Location location, String name, int tab) {
            this.location = location;
            this.name = name;
            this.tab = tab;
        }
    }

    public static boolean spawnWaveNpcs(Wave wave, String waveId) {
        if (wave == null || wave.getSpawnPoints().isEmpty()) return false;

        List<SpawnTask> tasks = new ArrayList<>();

        for (SpawnPoint sp : wave.getSpawnPoints()) {
            sp.resetAliveCount();
            Location base = sp.getLocation();
            if (base == null || base.getWorld() == null) continue;

            for (int i = 0; i < sp.getQuantity(); i++) {
                double ox = (Math.random() - 0.5) * 2.0;
                double oz = (Math.random() - 0.5) * 2.0;
                Location loc = base.clone().add(ox, 0, oz);
                tasks.add(new SpawnTask(loc, sp.getNpcName(), sp.getNpcTab()));
            }
        }

        if (tasks.isEmpty()) return false;

        waveAliveCount.put(waveId, new AtomicInteger(tasks.size()));

        for (int i = 0; i < tasks.size(); i++) {
            final SpawnTask task = tasks.get(i);
            Bukkit.getScheduler().runTaskLater(DbzMain.instance, () -> {
                int entityId = spawnNpc(task.location, task.name, task.tab);
                if (entityId != -1) {
                    entityToWave.put(entityId, waveId);
                } else {
                    AtomicInteger count = waveAliveCount.get(waveId);
                    if (count != null) count.decrementAndGet();
                }
            }, (long) (i + 1));
        }

        return true;
    }

    private static int spawnNpc(Location loc, String name, int tab) {
        try {
            CraftWorld craftWorld = (CraftWorld) loc.getWorld();
            ServerLevel serverLevel = craftWorld.getHandle();
            IWorld world = NpcAPI.Instance().getIWorld(serverLevel);
            if (world == null) return -1;
            var npc = world.spawnClone(loc.getX(), loc.getY(), loc.getZ(), tab, name);
            return npc != null ? npc.getMCEntity().getBukkitEntity().getEntityId() : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getWaveIdForNpc(int entityId) {
        return entityToWave.get(entityId);
    }

    public static boolean markNpcDead(int entityId, String waveId) {
        String mapped = entityToWave.remove(entityId);
        if (mapped == null) return false;
        AtomicInteger count = waveAliveCount.get(mapped);
        if (count != null && count.get() > 0) {
            count.decrementAndGet();
            return true;
        }
        return false;
    }

    public static int getAliveCount(String waveId) {
        AtomicInteger count = waveAliveCount.get(waveId);
        return count != null ? Math.max(0, count.get()) : 0;
    }

    public static void clearWave(String waveId) {
        waveAliveCount.remove(waveId);
        entityToWave.entrySet().removeIf(e -> waveId.equals(e.getValue()));
    }

    public static void clearAll() {
        entityToWave.clear();
        waveAliveCount.clear();
    }
}