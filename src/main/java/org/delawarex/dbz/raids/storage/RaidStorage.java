package org.delawarex.dbz.raids.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.raids.models.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RaidStorage {

    private final File file;
    private FileConfiguration config;

    public RaidStorage() {
        File folder = new File(DbzMain.instance.getDataFolder(), "raids");
        folder.mkdirs();
        file = new File(folder, "raids.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveRaid(Raid raid) {
        String base = "raids." + raid.getRaidId();
        config.set(base + ".name", raid.getRaidName());
        config.set(base + ".description", raid.getDescription());
        config.set(base + ".enabled", raid.isEnabled());
        config.set(base + ".cooldown", raid.getCooldownSeconds());
        config.set(base + ".minPlayers", raid.getMinPlayers());
        config.set(base + ".maxPlayers", raid.getMaxPlayers());

        if (raid.getPlayerSpawnPoint() != null) {
            saveLocation(base + ".playerSpawn", raid.getPlayerSpawnPoint());
        } else {
            config.set(base + ".playerSpawn", null);
        }

        config.set(base + ".waves", null);
        for (int i = 0; i < raid.getWaves().size(); i++) {
            Wave wave = raid.getWaves().get(i);
            String wBase = base + ".waves." + i;
            config.set(wBase + ".number", wave.getWaveNumber());
            config.set(wBase + ".description", wave.getDescription());

            for (int j = 0; j < wave.getSpawnPoints().size(); j++) {
                SpawnPoint sp = wave.getSpawnPoints().get(j);
                String sBase = wBase + ".spawns." + j;
                config.set(sBase + ".npcName", sp.getNpcName());
                config.set(sBase + ".npcTab", sp.getNpcTab());
                config.set(sBase + ".quantity", sp.getQuantity());
                if (sp.getLocation() != null) saveLocation(sBase + ".location", sp.getLocation());
            }

            for (int j = 0; j < wave.getRewards().size(); j++) {
                WaveReward reward = wave.getRewards().get(j);
                String rBase = wBase + ".rewards." + j;
                config.set(rBase + ".command", reward.getCommand());
                config.set(rBase + ".probability", reward.getProbability());
            }
        }

        save();
    }

    public void deleteRaid(String raidId) {
        config.set("raids." + raidId, null);
        save();
    }

    public Map<String, Raid> loadAll() {
        Map<String, Raid> raids = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("raids");
        if (section == null) return raids;
        for (String id : section.getKeys(false)) {
            Raid raid = loadRaid(id);
            if (raid != null) raids.put(id, raid);
        }
        return raids;
    }

    private Raid loadRaid(String id) {
        String base = "raids." + id;
        if (!config.contains(base)) return null;

        Raid raid = new Raid(id, config.getString(base + ".name", "Sin nombre"));
        raid.setDescription(config.getString(base + ".description", ""));
        raid.setEnabled(config.getBoolean(base + ".enabled", true));
        raid.setCooldownSeconds(config.getLong(base + ".cooldown", 3600));
        raid.setMinPlayers(config.getInt(base + ".minPlayers", 1));
        raid.setMaxPlayers(config.getInt(base + ".maxPlayers", 5));

        if (config.contains(base + ".playerSpawn")) {
            raid.setPlayerSpawnPoint(loadLocation(base + ".playerSpawn"));
        }

        ConfigurationSection wavesSection = config.getConfigurationSection(base + ".waves");
        if (wavesSection != null) {
            List<String> waveKeys = new ArrayList<>(wavesSection.getKeys(false));
            waveKeys.sort(Comparator.comparingInt(k -> {
                try { return Integer.parseInt(k); } catch (Exception e) { return 0; }
            }));

            for (String wi : waveKeys) {
                String wBase = base + ".waves." + wi;
                Wave wave = new Wave(config.getInt(wBase + ".number", 1));
                wave.setDescription(config.getString(wBase + ".description", ""));

                ConfigurationSection spawnsSection = config.getConfigurationSection(wBase + ".spawns");
                if (spawnsSection != null) {
                    List<String> spawnKeys = new ArrayList<>(spawnsSection.getKeys(false));
                    spawnKeys.sort(Comparator.comparingInt(k -> {
                        try { return Integer.parseInt(k); } catch (Exception e) { return 0; }
                    }));
                    for (String si : spawnKeys) {
                        String sBase = wBase + ".spawns." + si;
                        Location loc = loadLocation(sBase + ".location");
                        if (loc != null) {
                            wave.addSpawnPoint(new SpawnPoint(
                                    loc,
                                    config.getString(sBase + ".npcName", ""),
                                    config.getInt(sBase + ".npcTab", 1),
                                    config.getInt(sBase + ".quantity", 1)
                            ));
                        }
                    }
                }

                ConfigurationSection rewardsSection = config.getConfigurationSection(wBase + ".rewards");
                if (rewardsSection != null) {
                    List<String> rewardKeys = new ArrayList<>(rewardsSection.getKeys(false));
                    rewardKeys.sort(Comparator.comparingInt(k -> {
                        try { return Integer.parseInt(k); } catch (Exception e) { return 0; }
                    }));
                    for (String ri : rewardKeys) {
                        String rBase = wBase + ".rewards." + ri;
                        wave.addReward(new WaveReward(
                                config.getString(rBase + ".command", ""),
                                config.getInt(rBase + ".probability", 100)
                        ));
                    }
                }

                raid.addWave(wave);
            }
        }

        return raid;
    }

    private void saveLocation(String path, Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", (double) loc.getYaw());
        config.set(path + ".pitch", (double) loc.getPitch());
    }

    private Location loadLocation(String path) {
        if (!config.contains(path)) return null;
        String worldName = config.getString(path + ".world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(
                world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
        );
    }

    private void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}