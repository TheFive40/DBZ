package org.delawarex.dbz.market.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyManager {

    private final File                              file;
    private       FileConfiguration                config;
    private final ConcurrentHashMap<UUID, Double>  cache = new ConcurrentHashMap<>();
    private final double                           startingBalance;

    public EconomyManager(double startingBalance) {
        this.startingBalance = startingBalance;
        File folder = new File(DbzMain.instance.getDataFolder(), "market");
        folder.mkdirs();
        this.file = new File(folder, "balances.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        config = YamlConfiguration.loadConfiguration(file);
        cache.clear();
        if (config.isConfigurationSection("balances")) {
            for (String key : config.getConfigurationSection("balances").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    cache.put(uuid, config.getDouble("balances." + key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public double getBalance(Player player) {
        return cache.getOrDefault(player.getUniqueId(), startingBalance);
    }

    public double getBalance(UUID uuid) {
        return cache.getOrDefault(uuid, startingBalance);
    }

    public boolean withdraw(Player player, double amount) {
        double current = getBalance(player);
        if (current < amount) return false;
        set(player.getUniqueId(), current - amount);
        return true;
    }

    public void deposit(Player player, double amount) {
        set(player.getUniqueId(), getBalance(player) + amount);
    }

    public boolean transfer(Player from, Player to, double amount) {
        if (getBalance(from) < amount) return false;
        withdraw(from, amount);
        deposit(to, amount);
        return true;
    }

    public void setBalance(UUID uuid, double amount) {
        set(uuid, Math.max(0, amount));
    }

    public void giveBalance(UUID uuid, double amount) {
        set(uuid, getBalance(uuid) + amount);
    }

    public String format(double amount, String symbol) {
        return String.format("%.2f %s", amount, symbol);
    }

    private void set(UUID uuid, double amount) {
        amount = Math.round(amount * 100.0) / 100.0;
        cache.put(uuid, amount);
        config.set("balances." + uuid.toString(), amount);
        save();
    }

    private void save() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
