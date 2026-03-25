package org.delawarex.dbz.fragments.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.fragments.model.CustomizedArmor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FragmentArmorStorage {

    private File dataFolder;
    private File armorFile;
    private FileConfiguration armorConfig;
    private Set<String> registeredHashes;

    public FragmentArmorStorage() {
        this.registeredHashes = new HashSet<>();
        loadStorage();
    }

    private void loadStorage() {
        dataFolder = new File(DbzMain.instance.getDataFolder(), "fragments");
        if (!dataFolder.exists()) dataFolder.mkdirs();
        armorFile = new File(dataFolder, "customized_armors.yml");
        if (!armorFile.exists()) {
            try { armorFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        armorConfig = YamlConfiguration.loadConfiguration(armorFile);
        loadHashes();
    }

    private void loadHashes() {
        if (!armorConfig.contains("armors")) return;
        for (String hash : armorConfig.getConfigurationSection("armors").getKeys(false)) {
            registeredHashes.add(hash);
        }
    }

    public void saveArmor(CustomizedArmor armor) {
        String path = "armors." + armor.getHash();
        armorConfig.set(path + ".hash", armor.getHash());
        armorConfig.set(path + ".tier", armor.getTier());
        armorConfig.set(path + ".materialType", armor.getMaterialType());
        armorConfig.set(path + ".armorSlot", armor.getArmorSlot());
        armorConfig.set(path + ".displayName", armor.getDisplayName());
        for (Map.Entry<String, Integer> entry : armor.getAttributes().entrySet()) {
            armorConfig.set(path + ".attributes." + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : armor.getOperations().entrySet()) {
            armorConfig.set(path + ".operations." + entry.getKey(), entry.getValue());
        }
        armorConfig.set(path + ".timestamp", System.currentTimeMillis());
        try {
            armorConfig.save(armorFile);
            registeredHashes.add(armor.getHash());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public CustomizedArmor loadArmor(String hash) {
        String path = "armors." + hash;
        if (!armorConfig.contains(path)) return null;
        CustomizedArmor armor = new CustomizedArmor(
                armorConfig.getString(path + ".hash"),
                armorConfig.getString(path + ".tier"));
        armor.setMaterialType(armorConfig.getString(path + ".materialType"));
        armor.setArmorSlot(armorConfig.getString(path + ".armorSlot"));
        armor.setDisplayName(armorConfig.getString(path + ".displayName"));
        if (armorConfig.contains(path + ".attributes")) {
            for (String attr : armorConfig.getConfigurationSection(path + ".attributes").getKeys(false)) {
                armor.getAttributes().put(attr, armorConfig.getInt(path + ".attributes." + attr));
            }
        }
        if (armorConfig.contains(path + ".operations")) {
            for (String attr : armorConfig.getConfigurationSection(path + ".operations").getKeys(false)) {
                armor.getOperations().put(attr, armorConfig.getString(path + ".operations." + attr));
            }
        }
        return armor;
    }

    public boolean hashExists(String hash) { return registeredHashes.contains(hash); }

    public Set<String> getRegisteredHashes() { return new HashSet<>(registeredHashes); }

    public void deleteArmor(String hash) {
        armorConfig.set("armors." + hash, null);
        try { armorConfig.save(armorFile); registeredHashes.remove(hash); } catch (IOException e) { e.printStackTrace(); }
    }

    public Map<String, CustomizedArmor> loadAllArmors() {
        Map<String, CustomizedArmor> armors = new HashMap<>();
        if (!armorConfig.contains("armors")) return armors;
        for (String hash : armorConfig.getConfigurationSection("armors").getKeys(false)) {
            CustomizedArmor armor = loadArmor(hash);
            if (armor != null) armors.put(hash, armor);
        }
        return armors;
    }

    public void reload() {
        armorConfig = YamlConfiguration.loadConfiguration(armorFile);
        registeredHashes.clear();
        loadHashes();
    }

    public Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total_armors", registeredHashes.size());
        if (!armorConfig.contains("armors")) return stats;
        Map<String, Integer> tierCount = new HashMap<>();
        for (String hash : armorConfig.getConfigurationSection("armors").getKeys(false)) {
            String tier = armorConfig.getString("armors." + hash + ".tier");
            if (tier != null) tierCount.put(tier, tierCount.getOrDefault(tier, 0) + 1);
        }
        stats.putAll(tierCount);
        return stats;
    }
}