package org.delawarex.dbz.market;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delawarex.dbz.DbzMain;

import java.io.File;
import java.io.IOException;

public class MarketConfig {

    public final String currencyName;
    public final String currencySymbol;
    public final double startingBalance;
    public final double defaultTaxRate;
    public final double defaultSpread;
    public final double defaultVolumeSensitivity;
    public final double defaultDemandSensitivity;
    public final double defaultStockSensitivity;
    public final int    defaultTargetStock;
    public final int    cooldownSeconds;
    public final int    maxPerTransaction;
    public final int    rapidTradeWindow;
    public final int    rapidTradeLimit;
    public final int    historyMaxSize;

    private MarketConfig(FileConfiguration cfg) {
        this.currencyName              = cfg.getString("economy.currency_name",       "Zeni");
        this.currencySymbol            = cfg.getString("economy.currency_symbol",     "₢");
        this.startingBalance           = cfg.getDouble("economy.starting_balance",    1000.0);
        this.defaultTaxRate            = cfg.getDouble("defaults.tax_rate",           0.05);
        this.defaultSpread             = cfg.getDouble("defaults.spread",             0.20);
        this.defaultVolumeSensitivity  = cfg.getDouble("defaults.volume_sensitivity", 0.02);
        this.defaultDemandSensitivity  = cfg.getDouble("defaults.demand_sensitivity", 0.5);
        this.defaultStockSensitivity   = cfg.getDouble("defaults.stock_sensitivity",  1.0);
        this.defaultTargetStock        = cfg.getInt   ("defaults.target_stock",       100);
        this.cooldownSeconds           = cfg.getInt   ("anti_exploit.cooldown_seconds",   30);
        this.maxPerTransaction         = cfg.getInt   ("anti_exploit.max_per_transaction", 1728);
        this.rapidTradeWindow          = cfg.getInt   ("anti_exploit.rapid_trade_window",   60);
        this.rapidTradeLimit           = cfg.getInt   ("anti_exploit.rapid_trade_limit",     5);
        this.historyMaxSize            = cfg.getInt   ("market.history_max_size",     50);
    }

    public static MarketConfig load() {
        File folder = new File(DbzMain.instance.getDataFolder(), "market");
        folder.mkdirs();
        File file = new File(folder, "config.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                cfg.set("economy.currency_name",               "Zeni");
                cfg.set("economy.currency_symbol",             "₢");
                cfg.set("economy.starting_balance",            1000.0);
                cfg.set("defaults.tax_rate",                   0.05);
                cfg.set("defaults.spread",                     0.20);
                cfg.set("defaults.volume_sensitivity",         0.02);
                cfg.set("defaults.demand_sensitivity",         0.5);
                cfg.set("defaults.stock_sensitivity",          1.0);
                cfg.set("defaults.target_stock",               100);
                cfg.set("anti_exploit.cooldown_seconds",       30);
                cfg.set("anti_exploit.max_per_transaction",    1728);
                cfg.set("anti_exploit.rapid_trade_window",     60);
                cfg.set("anti_exploit.rapid_trade_limit",      5);
                cfg.set("market.history_max_size",             50);
                cfg.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        return new MarketConfig(cfg);
    }
}
