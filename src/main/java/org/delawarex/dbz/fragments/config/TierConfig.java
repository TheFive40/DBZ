package org.delawarex.dbz.fragments.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delawarex.dbz.DbzMain;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TierConfig {

    private File configFile;
    private FileConfiguration config;
    private Map<String, Map<String, Integer>> tierLimits;
    private Map<String, List<String>> tierAllowedOperations;

    public TierConfig() {
        this.tierLimits = new HashMap<>();
        this.tierAllowedOperations = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        File dataFolder = new File(DbzMain.instance.getDataFolder(), "fragments");
        if (!dataFolder.exists()) dataFolder.mkdirs();
        configFile = new File(dataFolder, "tier_config.yml");
        if (!configFile.exists()) createDefaultConfig();
        config = YamlConfiguration.loadConfiguration(configFile);
        loadTierLimits();
        loadTierOperations();
    }

    private void createDefaultConfig() {
        try {
            configFile.createNewFile();
            config = YamlConfiguration.loadConfiguration(configFile);
            String[] stats = {"STR", "SKP", "RES", "VIT", "PWR", "ENE"};
            int[] limits = {10, 20, 30, 50};
            String[] tiers = {"TIER_1", "TIER_2", "TIER_3", "VIP"};
            for (int t = 0; t < tiers.length; t++) {
                for (String stat : stats) {
                    config.set("tiers." + tiers[t] + "." + stat, limits[t]);
                }
                config.set("tiers." + tiers[t] + ".allowed_operations", Arrays.asList("+", "-", "*"));
            }
            config.set("default_tier", "TIER_1");
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTierLimits() {
        if (!config.contains("tiers")) return;
        for (String tier : config.getConfigurationSection("tiers").getKeys(false)) {
            Map<String, Integer> limits = new HashMap<>();
            for (String attr : config.getConfigurationSection("tiers." + tier).getKeys(false)) {
                if (attr.equals("allowed_operations")) continue;
                limits.put(attr, config.getInt("tiers." + tier + "." + attr));
            }
            tierLimits.put(tier, limits);
        }
    }

    private void loadTierOperations() {
        if (!config.contains("tiers")) return;
        for (String tier : config.getConfigurationSection("tiers").getKeys(false)) {
            List<String> operations = config.getStringList("tiers." + tier + ".allowed_operations");
            if (operations.isEmpty()) operations = Arrays.asList("+", "-", "*");
            tierAllowedOperations.put(tier, operations);
        }
    }

    public int getLimit(String tier, String attribute) {
        if (!tierLimits.containsKey(tier)) return 0;
        return tierLimits.get(tier).getOrDefault(attribute, 0);
    }

    public boolean canApply(String tier, String attribute, int currentValue, int valueToAdd, String operation) {
        int limit = getLimit(tier, attribute);
        if (operation == null || !operation.equals("*")) {
            return (currentValue + valueToAdd) <= limit;
        } else {
            int totalScaled = currentValue + valueToAdd;
            double percentageReal = (totalScaled / 100.0 - 1.0) * 100.0;
            return Math.abs(percentageReal) <= limit;
        }
    }

    public boolean exceedsLimit(String tier, String attribute, int currentValue, String operation) {
        int limit = getLimit(tier, attribute);
        if (operation == null || !operation.equals("*")) {
            return currentValue > limit;
        } else {
            double percentageReal = Math.abs((currentValue / 100.0 - 1.0) * 100.0);
            return percentageReal > limit;
        }
    }

    public boolean isOperationAllowed(String tier, String operation) {
        List<String> allowed = tierAllowedOperations.getOrDefault(tier, Arrays.asList("+", "-", "*"));
        return allowed.contains(operation);
    }

    public List<String> getAllowedOperations(String tier) {
        return new ArrayList<>(tierAllowedOperations.getOrDefault(tier, Arrays.asList("+", "-", "*")));
    }

    public boolean setAllowedOperations(String tier, List<String> operations) {
        if (!tierLimits.containsKey(tier)) return false;
        for (String op : operations) {
            if (!op.equals("+") && !op.equals("-") && !op.equals("*")) return false;
        }
        tierAllowedOperations.put(tier, new ArrayList<>(operations));
        config.set("tiers." + tier + ".allowed_operations", operations);
        try { config.save(configFile); return true; } catch (IOException e) { e.printStackTrace(); return false; }
    }

    public String getDefaultTier() {
        return config.getString("default_tier", "TIER_1");
    }

    public boolean setDefaultTier(String tier) {
        if (!tierLimits.containsKey(tier)) return false;
        config.set("default_tier", tier);
        try { config.save(configFile); return true; } catch (IOException e) { e.printStackTrace(); return false; }
    }

    public Map<String, Map<String, Integer>> getAllTiers() {
        return new HashMap<>(tierLimits);
    }

    public boolean setLimit(String tier, String attribute, int limit) {
        if (!tierLimits.containsKey(tier)) {
            tierLimits.put(tier, new HashMap<>());
            tierAllowedOperations.put(tier, Arrays.asList("+", "-", "*"));
            config.set("tiers." + tier + ".allowed_operations", Arrays.asList("+", "-", "*"));
        }
        tierLimits.get(tier).put(attribute, limit);
        config.set("tiers." + tier + "." + attribute, limit);
        try { config.save(configFile); return true; } catch (IOException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteTier(String tier) {
        if (!tierLimits.containsKey(tier)) return false;
        if (tier.equals(getDefaultTier())) return false;
        tierLimits.remove(tier);
        tierAllowedOperations.remove(tier);
        config.set("tiers." + tier, null);
        try { config.save(configFile); return true; } catch (IOException e) { e.printStackTrace(); return false; }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        tierLimits.clear();
        tierAllowedOperations.clear();
        loadTierLimits();
        loadTierOperations();
    }
}