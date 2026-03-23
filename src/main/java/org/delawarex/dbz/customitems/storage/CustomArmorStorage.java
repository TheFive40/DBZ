package org.delawarex.dbz.customitems.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.customitems.models.CustomArmor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomArmorStorage {

    private final File file;
    private FileConfiguration config;

    public CustomArmorStorage() {
        File folder = new File(DbzMain.instance.getDataFolder(), "customitems");
        folder.mkdirs();
        file = new File(folder, "armors.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveArmor(CustomArmor armor) {
        String p = "armors." + armor.getId();
        config.set(p + ".material",      armor.getMaterial());   // String, e.g. "DIAMOND_CHESTPLATE"
        config.set(p + ".displayName",   armor.getDisplayName());
        config.set(p + ".lore",          armor.getLore());
        config.set(p + ".maxDurability", armor.getMaxDurability());
        config.set(p + ".unbreakable",   armor.isUnbreakable());
        config.set(p + ".bonusStat",     new HashMap<>(armor.getValueByStat()));
        config.set(p + ".operations",    new HashMap<>(armor.getOperation()));
        config.set(p + ".effects",       new HashMap<>(armor.getEffects()));
        save();
    }

    public void deleteArmor(String id) {
        config.set("armors." + id, null);
        save();
    }

    public CustomArmor loadArmor(String id) {
        String p = "armors." + id;
        if (!config.contains(p)) return null;

        CustomArmor armor = new CustomArmor();
        armor.setId(id);
        armor.setMaterial(config.getString(p + ".material", "IRON_CHESTPLATE"));
        armor.setDisplayName(config.getString(p + ".displayName", ""));
        armor.setLore(config.getStringList(p + ".lore"));
        armor.setMaxDurability(config.getInt(p + ".maxDurability", -1));
        armor.setUnbreakable(config.getBoolean(p + ".unbreakable", false));

        if (config.isConfigurationSection(p + ".bonusStat")) {
            HashMap<String, Double> stats = new HashMap<>();
            for (String k : config.getConfigurationSection(p + ".bonusStat").getKeys(false))
                stats.put(k, config.getDouble(p + ".bonusStat." + k));
            armor.setValueByStat(stats);
        }
        if (config.isConfigurationSection(p + ".operations")) {
            HashMap<String, String> ops = new HashMap<>();
            for (String k : config.getConfigurationSection(p + ".operations").getKeys(false))
                ops.put(k, config.getString(p + ".operations." + k));
            armor.setOperation(ops);
        }
        if (config.isConfigurationSection(p + ".effects")) {
            HashMap<String, Double> eff = new HashMap<>();
            for (String k : config.getConfigurationSection(p + ".effects").getKeys(false))
                eff.put(k, config.getDouble(p + ".effects." + k));
            armor.setEffects(eff);
        }
        return armor;
    }

    public Map<String, CustomArmor> loadAll() {
        Map<String, CustomArmor> map = new HashMap<>();
        if (!config.isConfigurationSection("armors")) return map;
        for (String id : config.getConfigurationSection("armors").getKeys(false)) {
            CustomArmor armor = loadArmor(id);
            if (armor != null) map.put(id, armor);
        }
        return map;
    }

    private void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}