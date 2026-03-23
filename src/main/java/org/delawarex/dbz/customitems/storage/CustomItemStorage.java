package org.delawarex.dbz.customitems.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.customitems.models.CustomItem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomItemStorage {

    private final File file;
    private FileConfiguration config;

    public CustomItemStorage() {
        File folder = new File(DbzMain.instance.getDataFolder(), "customitems");
        folder.mkdirs();
        file = new File(folder, "items.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveItem(CustomItem item) {
        String p = "items." + item.getId();
        config.set(p + ".material",       item.getMaterial());   // String, e.g. "DIAMOND_SWORD"
        config.set(p + ".displayName",    item.getDisplayName());
        config.set(p + ".lore",           item.getLore());
        config.set(p + ".active",         item.isActive());
        config.set(p + ".maxDurability",  item.getMaxDurability());
        config.set(p + ".unbreakable",    item.isUnbreakable());
        config.set(p + ".consumable",     item.isConsumable());
        config.set(p + ".commands",       item.getCommands());
        config.set(p + ".tpValue",        item.getTpValue());
        config.set(p + ".tpConsumeStack", item.isTpConsumeStack());
        config.set(p + ".bonusStat",      new HashMap<>(item.getValueByStat()));
        config.set(p + ".operations",     new HashMap<>(item.getOperation()));
        config.set(p + ".effects",        new HashMap<>(item.getEffects()));
        save();
    }

    public void deleteItem(String id) {
        config.set("items." + id, null);
        save();
    }

    public CustomItem loadItem(String id) {
        String p = "items." + id;
        if (!config.contains(p)) return null;

        CustomItem item = new CustomItem();
        item.setId(id);
        item.setMaterial(config.getString(p + ".material", "STONE"));
        item.setDisplayName(config.getString(p + ".displayName", ""));
        item.setLore(config.getStringList(p + ".lore"));
        item.setActive(config.getBoolean(p + ".active", true));
        item.setMaxDurability(config.getInt(p + ".maxDurability", -1));
        item.setUnbreakable(config.getBoolean(p + ".unbreakable", false));
        item.setConsumable(config.getBoolean(p + ".consumable", false));
        item.setCommands(config.getStringList(p + ".commands"));
        item.setTpValue(config.getInt(p + ".tpValue", 0));
        item.setTpConsumeStack(config.getBoolean(p + ".tpConsumeStack", false));

        if (config.isConfigurationSection(p + ".bonusStat")) {
            HashMap<String, Double> stats = new HashMap<>();
            for (String k : config.getConfigurationSection(p + ".bonusStat").getKeys(false))
                stats.put(k, config.getDouble(p + ".bonusStat." + k));
            item.setValueByStat(stats);
        }
        if (config.isConfigurationSection(p + ".operations")) {
            HashMap<String, String> ops = new HashMap<>();
            for (String k : config.getConfigurationSection(p + ".operations").getKeys(false))
                ops.put(k, config.getString(p + ".operations." + k));
            item.setOperation(ops);
        }
        if (config.isConfigurationSection(p + ".effects")) {
            HashMap<String, Double> eff = new HashMap<>();
            for (String k : config.getConfigurationSection(p + ".effects").getKeys(false))
                eff.put(k, config.getDouble(p + ".effects." + k));
            item.setEffects(eff);
        }
        return item;
    }

    public Map<String, CustomItem> loadAll() {
        Map<String, CustomItem> map = new HashMap<>();
        if (!config.isConfigurationSection("items")) return map;
        for (String id : config.getConfigurationSection("items").getKeys(false)) {
            CustomItem item = loadItem(id);
            if (item != null) map.put(id, item);
        }
        return map;
    }

    private void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}