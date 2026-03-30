package org.delawarex.dbz.market.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.market.model.MarketItem;
import org.delawarex.dbz.market.model.Transaction;
import org.delawarex.dbz.market.model.TrendDirection;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MarketDataManager {

    private final File              itemFile;
    private       FileConfiguration itemConfig;
    private final int               historyMax;

    public MarketDataManager(int historyMax) {
        this.historyMax = historyMax;
        File folder = new File(DbzMain.instance.getDataFolder(), "market");
        folder.mkdirs();
        this.itemFile = new File(folder, "items.yml");
        reload();
    }

    public void reload() {
        if (!itemFile.exists()) {
            try { itemFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        itemConfig = YamlConfiguration.loadConfiguration(itemFile);
    }

    public void saveItem(MarketItem item) {
        String p = "items." + item.getId();
        itemConfig.set(p + ".displayName",        item.getDisplayName());
        itemConfig.set(p + ".material",           item.getMaterial());
        itemConfig.set(p + ".basePrice",          item.getBasePrice());
        itemConfig.set(p + ".minPrice",           item.getMinPrice());
        itemConfig.set(p + ".maxPrice",           item.getMaxPrice());
        itemConfig.set(p + ".stock",              item.getStock());
        itemConfig.set(p + ".targetStock",        item.getTargetStock());
        itemConfig.set(p + ".taxRate",            item.getTaxRate());
        itemConfig.set(p + ".spread",             item.getSpread());
        itemConfig.set(p + ".volumeSensitivity",  item.getVolumeSensitivity());
        itemConfig.set(p + ".demandSensitivity",  item.getDemandSensitivity());
        itemConfig.set(p + ".stockSensitivity",   item.getStockSensitivity());
        itemConfig.set(p + ".enabled",            item.isEnabled());
        itemConfig.set(p + ".lastUpdated",        item.getLastUpdated());
        itemConfig.set(p + ".windowBuys",         item.getWindowBuys());
        itemConfig.set(p + ".windowSells",        item.getWindowSells());
        itemConfig.set(p + ".windowStart",        item.getWindowStart());
        itemConfig.set(p + ".trend",              item.getTrend().name());

        List<Map<String, Object>> histList = new ArrayList<>();
        for (Transaction tx : item.getHistory()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("player",   tx.getPlayerName());
            map.put("uuid",     tx.getPlayerId().toString());
            map.put("type",     tx.getType().name());
            map.put("qty",      tx.getQuantity());
            map.put("perUnit",  tx.getPricePerUnit());
            map.put("total",    tx.getTotal());
            map.put("time",     tx.getTimestamp());
            histList.add(map);
        }
        itemConfig.set(p + ".history", histList);
        save();
    }

    public void deleteItem(String id) {
        itemConfig.set("items." + id, null);
        save();
    }

    public MarketItem loadItem(String id) {
        String p = "items." + id;
        if (!itemConfig.contains(p)) return null;

        MarketItem item = new MarketItem();
        item.setId(id);
        item.setDisplayName(itemConfig.getString(p + ".displayName", id));
        item.setMaterial(itemConfig.getString(p + ".material", "STONE"));
        item.setBasePrice(itemConfig.getDouble(p + ".basePrice", 100));
        item.setMinPrice(itemConfig.getDouble(p + ".minPrice", 10));
        item.setMaxPrice(itemConfig.getDouble(p + ".maxPrice", 10000));
        item.setStock(itemConfig.getInt(p + ".stock", 0));
        item.setTargetStock(itemConfig.getInt(p + ".targetStock", 100));
        item.setTaxRate(itemConfig.getDouble(p + ".taxRate", 0.05));
        item.setSpread(itemConfig.getDouble(p + ".spread", 0.20));
        item.setVolumeSensitivity(itemConfig.getDouble(p + ".volumeSensitivity", 0.02));
        item.setDemandSensitivity(itemConfig.getDouble(p + ".demandSensitivity", 0.5));
        item.setStockSensitivity(itemConfig.getDouble(p + ".stockSensitivity", 1.0));
        item.setEnabled(itemConfig.getBoolean(p + ".enabled", true));
        item.setLastUpdated(itemConfig.getLong(p + ".lastUpdated", 0));
        item.setWindowBuys(itemConfig.getInt(p + ".windowBuys", 0));
        item.setWindowSells(itemConfig.getInt(p + ".windowSells", 0));
        item.setWindowStart(itemConfig.getLong(p + ".windowStart", System.currentTimeMillis()));

        String trendStr = itemConfig.getString(p + ".trend", "STABLE");
        try { item.setTrend(TrendDirection.valueOf(trendStr)); } catch (Exception ignored) {}

        List<?> histRaw = itemConfig.getList(p + ".history");
        if (histRaw != null) {
            List<Transaction> txList = new ArrayList<>();
            for (Object o : histRaw) {
                if (!(o instanceof Map<?,?> map)) continue;
                try {
                    UUID   uuid   = UUID.fromString((String) map.get("uuid"));
                    String pName  = (String) map.get("player");
                    Transaction.Type type = Transaction.Type.valueOf((String) map.get("type"));
                    int    qty    = ((Number) map.get("qty")).intValue();
                    double per    = ((Number) map.get("perUnit")).doubleValue();
                    double total  = ((Number) map.get("total")).doubleValue();
                    txList.add(new Transaction(uuid, pName, id, type, qty, per, total));
                } catch (Exception ignored) {}
            }
            item.setHistory(txList);
        }
        return item;
    }

    public Map<String, MarketItem> loadAll() {
        Map<String, MarketItem> result = new LinkedHashMap<>();
        if (!itemConfig.isConfigurationSection("items")) return result;
        for (String id : itemConfig.getConfigurationSection("items").getKeys(false)) {
            MarketItem item = loadItem(id);
            if (item != null) result.put(id, item);
        }
        return result;
    }

    private void save() {
        try { itemConfig.save(itemFile); } catch (IOException e) { e.printStackTrace(); }
    }
}
