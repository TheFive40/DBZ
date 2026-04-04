package org.delawarex.dbz.battlepass.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.advancedcrates.storage.ItemSerializer;
import org.delawarex.dbz.battlepass.models.BattlePass;
import org.delawarex.dbz.battlepass.models.BattlePassLevel;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BattlePassStorage {

    private final File file;
    private FileConfiguration config;

    public BattlePassStorage() {
        File folder = new File(DbzMain.instance.getDataFolder(), "battlepass");
        folder.mkdirs();
        file = new File(folder, "passes.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void savePass(BattlePass pass) {
        String base = "passes." + pass.getId();
        config.set(base + ".displayName", pass.getDisplayName());
        config.set(base + ".description", pass.getDescription());
        config.set(base + ".permission", pass.getPermission());
        config.set(base + ".enabled", pass.isEnabled());
        config.set(base + ".material", pass.getMaterial());
        config.set(base + ".levels", null);
        for (int i = 0; i < pass.getLevels().size(); i++) {
            BattlePassLevel level = pass.getLevels().get(i);
            String lBase = base + ".levels." + i;
            config.set(lBase + ".levelNumber", level.getLevelNumber());
            config.set(lBase + ".requiredPoints", level.getRequiredPoints());
            config.set(lBase + ".displayName", level.getDisplayName());
            config.set(lBase + ".commands", level.getCommands());
            config.set(lBase + ".items", null);
            for (int j = 0; j < level.getItems().size(); j++) {
                ItemStack item = level.getItems().get(j);
                if (item == null) continue;
                try { config.set(lBase + ".items." + j, ItemSerializer.serialize(item)); }
                catch (Exception e) { e.printStackTrace(); }
            }
        }
        save();
    }

    public void deletePass(String id) {
        config.set("passes." + id, null);
        save();
    }

    public Map<String, BattlePass> loadAll() {
        Map<String, BattlePass> result = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("passes");
        if (section == null) return result;
        for (String id : section.getKeys(false)) {
            BattlePass pass = loadPass(id);
            if (pass != null) result.put(id, pass);
        }
        return result;
    }

    private BattlePass loadPass(String id) {
        String base = "passes." + id;
        if (!config.contains(base)) return null;
        BattlePass pass = new BattlePass(id);
        pass.setDisplayName(config.getString(base + ".displayName", "&e" + id));
        pass.setDescription(config.getString(base + ".description", ""));
        pass.setPermission(config.getString(base + ".permission", ""));
        pass.setEnabled(config.getBoolean(base + ".enabled", true));
        pass.setMaterial(config.getString(base + ".material", "BOOK"));
        ConfigurationSection levelsSection = config.getConfigurationSection(base + ".levels");
        List<BattlePassLevel> levels = new ArrayList<>();
        if (levelsSection != null) {
            List<String> keys = new ArrayList<>(levelsSection.getKeys(false));
            keys.sort(Comparator.comparingInt(k -> { try { return Integer.parseInt(k); } catch (Exception e) { return 0; } }));
            for (String k : keys) {
                String lBase = base + ".levels." + k;
                BattlePassLevel level = new BattlePassLevel();
                level.setLevelNumber(config.getInt(lBase + ".levelNumber", 1));
                level.setRequiredPoints(config.getInt(lBase + ".requiredPoints", 0));
                level.setDisplayName(config.getString(lBase + ".displayName", "Nivel " + level.getLevelNumber()));
                level.setCommands(config.getStringList(lBase + ".commands"));
                List<ItemStack> items = new ArrayList<>();
                ConfigurationSection itemsSec = config.getConfigurationSection(lBase + ".items");
                if (itemsSec != null) {
                    List<String> iKeys = new ArrayList<>(itemsSec.getKeys(false));
                    iKeys.sort(Comparator.comparingInt(k2 -> { try { return Integer.parseInt(k2); } catch (Exception e) { return 0; } }));
                    for (String ik : iKeys) {
                        String itemStr = config.getString(lBase + ".items." + ik);
                        if (itemStr != null && !itemStr.isEmpty()) {
                            try {
                                ItemStack item = ItemSerializer.deserialize(itemStr);
                                if (item != null) items.add(item);
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                    }
                }
                level.setItems(items);
                levels.add(level);
            }
        }
        pass.setLevels(levels);
        return pass;
    }

    private void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}