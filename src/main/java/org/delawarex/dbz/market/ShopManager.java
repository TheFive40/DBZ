package org.delawarex.dbz.market;

import org.bukkit.entity.Player;
import org.delawarex.dbz.market.engine.*;
import org.delawarex.dbz.market.model.*;
import org.delawarex.dbz.market.storage.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopManager {

    private static ShopManager instance;

    private final MarketConfig    config;
    private final MarketDataManager data;
    private final EconomyManager  economy;
    private final PriceEngine     priceEngine;
    private final StockEngine     stockEngine;
    private final AntiExploitEngine exploit;
    private final TransactionEngine txEngine;

    private final ConcurrentHashMap<String, MarketItem> items = new ConcurrentHashMap<>();
    private final List<MarketEvent> activeEvents = new ArrayList<>();

    private ShopManager() {
        this.config      = MarketConfig.load();
        this.data        = new MarketDataManager(config.historyMaxSize);
        this.economy     = new EconomyManager(config.startingBalance);
        this.priceEngine = new PriceEngine();
        this.stockEngine = new StockEngine();
        this.exploit     = new AntiExploitEngine(config.cooldownSeconds, config.maxPerTransaction, config.rapidTradeWindow, config.rapidTradeLimit);
        this.txEngine    = new TransactionEngine(priceEngine, stockEngine, exploit, economy, data);

        items.putAll(data.loadAll());
    }

    public static ShopManager getInstance() {
        if (instance == null) instance = new ShopManager();
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public TransactionEngine.TransactionResult buy(Player player, String itemId, int quantity) {
        MarketItem item = items.get(itemId);
        if (item == null)    return TransactionEngine.TransactionResult.fail("Ítem no encontrado: &f" + itemId);
        if (!item.isEnabled()) return TransactionEngine.TransactionResult.fail("Este ítem no está disponible.");
        return txEngine.buy(player, item, quantity, getActiveEvent(itemId));
    }

    public TransactionEngine.TransactionResult sell(Player player, String itemId, int quantity) {
        MarketItem item = items.get(itemId);
        if (item == null)    return TransactionEngine.TransactionResult.fail("Ítem no encontrado: &f" + itemId);
        if (!item.isEnabled()) return TransactionEngine.TransactionResult.fail("Este ítem no está disponible.");
        return txEngine.sell(player, item, quantity, getActiveEvent(itemId));
    }

    public MarketItem addItem(String id, String displayName, String material, double basePrice) {
        MarketItem item = new MarketItem();
        item.setId(id);
        item.setDisplayName(displayName);
        item.setMaterial(material.toUpperCase());
        item.setBasePrice(basePrice);
        item.setMinPrice(Math.max(1, basePrice * 0.1));
        item.setMaxPrice(basePrice * 10);
        item.setTargetStock(config.defaultTargetStock);
        item.setTaxRate(config.defaultTaxRate);
        item.setSpread(config.defaultSpread);
        item.setVolumeSensitivity(config.defaultVolumeSensitivity);
        item.setDemandSensitivity(config.defaultDemandSensitivity);
        item.setStockSensitivity(config.defaultStockSensitivity);
        items.put(id, item);
        data.saveItem(item);
        return item;
    }

    public void removeItem(String id) {
        items.remove(id);
        data.deleteItem(id);
    }

    public void saveItem(MarketItem item) {
        items.put(item.getId(), item);
        data.saveItem(item);
    }

    public void startEvent(String id, String name, String description, double multiplier, long durationMinutes, String itemId) {
        activeEvents.removeIf(e -> e.getId().equals(id) || !e.isActive());
        MarketEvent event = new MarketEvent(id, name, description, multiplier, durationMinutes * 60_000L, itemId);
        activeEvents.add(event);
        items.values().forEach(MarketItem::invalidateCache);
    }

    public void stopEvent(String id) {
        activeEvents.removeIf(e -> e.getId().equals(id));
        items.values().forEach(MarketItem::invalidateCache);
    }

    public MarketEvent getActiveEvent(String itemId) {
        activeEvents.removeIf(e -> !e.isActive());
        return activeEvents.stream()
                .filter(e -> e.affects(itemId))
                .findFirst().orElse(null);
    }

    public void reload() {
        items.clear();
        data.reload();
        economy.reload();
        items.putAll(data.loadAll());
        activeEvents.clear();
        instance = null;
        instance = new ShopManager();
    }

    public MarketItem       getItem(String id)           { return items.get(id); }
    public boolean          itemExists(String id)        { return items.containsKey(id); }
    public Collection<MarketItem> getAllItems()          { return new ArrayList<>(items.values()); }
    public List<MarketItem> getEnabledItems()            {
        return items.values().stream().filter(MarketItem::isEnabled).toList();
    }
    public List<MarketEvent> getActiveEvents()          { activeEvents.removeIf(e -> !e.isActive()); return new ArrayList<>(activeEvents); }
    public EconomyManager   getEconomy()                { return economy; }
    public PriceEngine      getPriceEngine()            { return priceEngine; }
    public StockEngine      getStockEngine()            { return stockEngine; }
    public AntiExploitEngine getExploit()               { return exploit; }
    public MarketConfig     getConfig()                 { return config; }
}
