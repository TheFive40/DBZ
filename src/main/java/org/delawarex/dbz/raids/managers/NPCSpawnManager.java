package org.delawarex.dbz.raids.managers;

import net.minecraft.server.level.ServerLevel;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.delawarex.dbz.raids.models.SpawnPoint;
import org.delawarex.dbz.raids.models.Wave;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NPCSpawnManager {

    private static final Map<String, Set<Integer>> waveNpcIds = new ConcurrentHashMap<>();

    public static boolean spawnWaveNpcs(Wave wave, String waveId) {
        if (wave == null || wave.getSpawnPoints().isEmpty()) return false;
        Set<Integer> ids = ConcurrentHashMap.newKeySet();
        int spawned = 0;
        for (SpawnPoint sp : wave.getSpawnPoints()) {
            sp.resetAliveCount();
            Location loc = sp.getLocation();
            if (loc == null || loc.getWorld() == null) continue;
            for (int i = 0; i < sp.getQuantity(); i++) {
                int entityId = spawnNpc(loc, sp.getNpcName(), sp.getNpcTab());
                if (entityId != -1) {
                    ids.add(entityId);
                    spawned++;
                }
            }
        }
        waveNpcIds.put(waveId, ids);
        return spawned > 0;
    }

    private static int spawnNpc(Location loc, String name, int tab) {
        try {
            CraftWorld craftWorld = (CraftWorld) loc.getWorld();
            ServerLevel serverLevel = craftWorld.getHandle();
            IWorld world = NpcAPI.Instance().getIWorld(serverLevel);
            var npc = world.spawnClone(loc.getX(), loc.getY(), loc.getZ(), tab, name);
            return npc != null ? npc.getMCEntity().getId() : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getWaveIdForNpc(int entityId) {
        for (Map.Entry<String, Set<Integer>> entry : waveNpcIds.entrySet()) {
            if (entry.getValue().contains(entityId)) return entry.getKey();
        }
        return null;
    }

    public static boolean markNpcDead(int entityId, String waveId) {
        Set<Integer> ids = waveNpcIds.get(waveId);
        return ids != null && ids.remove(entityId);
    }

    public static int getAliveCount(String waveId) {
        Set<Integer> ids = waveNpcIds.get(waveId);
        return ids == null ? 0 : ids.size();
    }

    public static void clearWave(String waveId) {
        waveNpcIds.remove(waveId);
    }

    public static void clearAll() {
        waveNpcIds.clear();
    }
}