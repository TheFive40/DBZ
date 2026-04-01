package org.delawarex.dbz.bank.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delawarex.dbz.DbzMain;

import java.io.File;
import java.io.IOException;

public class BankConfigManager {

    private static BankConfigManager instance;
    private FileConfiguration config;
    private final File file;

    private BankConfigManager() {
        File folder = new File(DbzMain.instance.getDataFolder(), "bank");
        folder.mkdirs();
        file = new File(folder, "bank_config.yml");
        load();
    }

    public static BankConfigManager getInstance() {
        if (instance == null) instance = new BankConfigManager();
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    private void load() {
        if (!file.exists()) {
            createDefault();
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void createDefault() {
        try {
            file.createNewFile();
            YamlConfiguration def = new YamlConfiguration();
            def.set("loan.capacity_reset_days", 7);
            def.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getCapacityResetDays() {
        return Math.max(1, config.getLong("loan.capacity_reset_days", 7));
    }

    public void setCapacityResetDays(long days) {
        config.set("loan.capacity_reset_days", Math.max(1, days));
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}