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

        config.set(base + ".visualItem", crate.getVisualItem() != null
                ? ItemSerializer.serialize(crate.getVisualItem()) : null);

        if (crate.getPhysicalLocation() != null) {
            Location loc = crate.getPhysicalLocation();
            config.set(base + ".location.world", loc.getWorld().getName());
            config.set(base + ".location.x", loc.getX());
            config.set(base + ".location.y", loc.getY());
            config.set(base + ".location.z", loc.getZ());
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
        if (vis != null) crate.setVisualItem(ItemSerializer.deserialize(vis));

        if (config.contains(base + ".location")) {
            String worldName = config.getString(base + ".location.world");
            World world = Bukkit.getWorld(worldName != null ? worldName : "");
            if (world != null) {
                crate.setPhysicalLocation(new Location(world,
                        config.getDouble(base + ".location.x"),
                        config.getDouble(base + ".location.y"),
                        config.getDouble(base + ".location.z")));
            }
        }

        ConfigurationSection rs = config.getConfigurationSection(base + ".rewards");
        if (rs != null) {
            List<CrateReward> rewards = new ArrayList<>();
            List<String> keys = new ArrayList<>(rs.getKeys(false));
            keys.sort(Comparator.comparingInt(k -> { try { return Integer.parseInt(k); } catch (Exception e) { return 0; } }));
            for (String k : keys) {
                String rBase = base + ".rewards." + k;
                CrateReward reward = new CrateReward();
                reward.setId(config.getString(rBase + ".id", k));
                reward.setChance(config.getDouble(rBase + ".chance", 10.0));
                String itemStr = config.getString(rBase + ".item");
                if (itemStr != null) reward.setItem(ItemSerializer.deserialize(itemStr));
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
