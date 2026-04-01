package org.delawarex.dbz.advancedcrates.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.advancedcrates.models.Crate;
import org.delawarex.dbz.advancedcrates.models.CrateReward;
import org.delawarex.dbz.advancedcrates.models.Rarity;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CrateStorage {

    private final File file;
    private FileConfiguration config;

    public CrateStorage() {
        File folder = DbzMain.instance.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        file = new File(folder, "crates.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveCrate(Crate crate) {
        String base = "crates." + crate.getId();
        config.set(base + ".displayName", crate.getDisplayName());
        config.set(base + ".lore", crate.getLore());
        config.set(base + ".material", crate.getMaterial());
        config.set(base + ".rarity", crate.getRarity().name());
        config.set(base + ".keyId", crate.getKeyId());
        config.set(base + ".keyDisplayName", crate.getKeyDisplayName());
        config.set(base + ".keyMaterial", crate.getKeyMaterial());
        config.set(base + ".enabled", crate.isEnabled());

        try {
            config.set(base + ".visualItem", crate.getVisualItem() != null
                    ? ItemSerializer.serialize(crate.getVisualItem()) : null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Location loc = crate.getPhysicalLocation();
        if (loc != null && loc.getWorld() != null) {
            config.set(base + ".location.world", loc.getWorld().getName());
            config.set(base + ".location.x", loc.getX());
            config.set(base + ".location.y", loc.getY());
            config.set(base + ".location.z", loc.getZ());
            config.set(base + ".location.yaw", (double) loc.getYaw());
            config.set(base + ".location.pitch", (double) loc.getPitch());
        } else if (crate.hasPendingLocation()) {
        } else {
            config.set(base + ".location", null);
        }

        config.set(base + ".rewards", null);
        for (int i = 0; i < crate.getRewards().size(); i++) {
            CrateReward r = crate.getRewards().get(i);
            String rBase = base + ".rewards." + i;
            config.set(rBase + ".id", r.getId());
            config.set(rBase + ".chance", r.getChance());
            config.set(rBase + ".item", r.getItem() != null ? ItemSerializer.serialize(r.getItem()) : null);
            config.set(rBase + ".commands", r.getCommands());
        }
        save();
    }

    public void deleteCrate(String id) {
        config.set("crates." + id, null);
        save();
    }

    public Map<String, Crate> loadAll() {
        Map<String, Crate> map = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("crates");
        if (section == null) return map;
        for (String id : section.getKeys(false)) {
            Crate crate = loadCrate(id);
            if (crate != null) map.put(id, crate);
        }
        return map;
    }

    private Crate loadCrate(String id) {
        String base = "crates." + id;
        if (!config.contains(base)) return null;
        Crate crate = new Crate();
        crate.setId(id);
        crate.setDisplayName(config.getString(base + ".displayName", "&6" + id));
        crate.setLore(config.getStringList(base + ".lore"));
        crate.setMaterial(config.getString(base + ".material", "CHEST"));
        crate.setRarity(Rarity.fromString(config.getString(base + ".rarity", "COMMON")));
        crate.setKeyId(config.getString(base + ".keyId", id + "_key"));
        crate.setKeyDisplayName(config.getString(base + ".keyDisplayName", "&eLlave de " + id));
        crate.setKeyMaterial(config.getString(base + ".keyMaterial", "TRIPWIRE_HOOK"));
        crate.setEnabled(config.getBoolean(base + ".enabled", true));

        String vis = config.getString(base + ".visualItem");
        if (vis != null && !vis.isEmpty()) {
            try { crate.setVisualItem(ItemSerializer.deserialize(vis)); }
            catch (Exception e) { e.printStackTrace(); }
        }

        if (config.contains(base + ".location.world")) {
            try {
                String worldName = config.getString(base + ".location.world", "");
                if (!worldName.isEmpty()) {
                    double x = config.getDouble(base + ".location.x");
                    double y = config.getDouble(base + ".location.y");
                    double z = config.getDouble(base + ".location.z");
                    float yaw = (float) config.getDouble(base + ".location.yaw", 0.0);
                    float pitch = (float) config.getDouble(base + ".location.pitch", 0.0);

                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        crate.setPhysicalLocation(new Location(world, x, y, z, yaw, pitch));
                    } else {
                        crate.setPendingLocation(worldName, x, y, z, yaw, pitch);
                        DbzMain.instance.getLogger().info(
                                "[Crates] Mundo '" + worldName + "' no cargado aun para crate '" + id + "'. Se resolverá automáticamente cuando se cargue.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ConfigurationSection rs = config.getConfigurationSection(base + ".rewards");
        if (rs != null) {
            List<CrateReward> rewards = new ArrayList<>();
            List<String> keys = new ArrayList<>(rs.getKeys(false));
            keys.sort(Comparator.comparingInt(k -> {
                try { return Integer.parseInt(k); } catch (Exception e) { return 0; }
            }));
            for (String k : keys) {
                String rBase = base + ".rewards." + k;
                CrateReward reward = new CrateReward();
                reward.setId(config.getString(rBase + ".id", k));
                reward.setChance(config.getDouble(rBase + ".chance", 10.0));
                String itemStr = config.getString(rBase + ".item");
                if (itemStr != null && !itemStr.isEmpty()) {
                    try { reward.setItem(ItemSerializer.deserialize(itemStr)); }
                    catch (Exception e) { e.printStackTrace(); }
                }
                reward.setCommands(config.getStringList(rBase + ".commands"));
                rewards.add(reward);
            }
            crate.setRewards(rewards);
        }
        return crate;
    }

    private void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
