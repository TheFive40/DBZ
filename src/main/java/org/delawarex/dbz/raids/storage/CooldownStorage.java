package org.delawarex.dbz.raids.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delawarex.dbz.DbzMain;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CooldownStorage {

    private final File file;
    private FileConfiguration config;

    public CooldownStorage() {
        File folder = new File(DbzMain.instance.getDataFolder(), "raids");
        folder.mkdirs();
        file = new File(folder, "cooldowns.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveCooldown(UUID playerId, String raidId, long endTime) {
        config.set("cooldowns." + playerId + "." + raidId, endTime);
        save();
    }

    public void removeCooldown(UUID playerId, String raidId) {
        config.set("cooldowns." + playerId + "." + raidId, null);
        save();
    }

    public Map<UUID, Map<String, Long>> loadAll() {
        Map<UUID, Map<String, Long>> result = new HashMap<>();
        ConfigurationSection root = config.getConfigurationSection("cooldowns");
        if (root == null) return result;
        for (String uuidStr : root.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Long> raidMap = new HashMap<>();
                ConfigurationSection playerSection = root.getConfigurationSection(uuidStr);
                if (playerSection != null) {
                    for (String raidId : playerSection.getKeys(false)) {
                        raidMap.put(raidId, playerSection.getLong(raidId));
                    }
                }
                if (!raidMap.isEmpty()) result.put(uuid, raidMap);
            } catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    private void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}