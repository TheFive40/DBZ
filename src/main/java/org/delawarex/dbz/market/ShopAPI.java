package org.delawarex.dbz.market;

import org.bukkit.entity.Player;
import org.delawarex.dbz.market.engine.TransactionEngine.TransactionResult;
import org.delawarex.dbz.market.model.MarketItem;

import java.util.Collection;

public final class ShopAPI {

    private ShopAPI() {}

    public static TransactionResult buy(Player player, String itemId, int quantity) {
        return ShopManager.getInstance().buy(player, itemId, quantity);
    }

    public static TransactionResult sell(Player player, String itemId, int quantity) {
        return ShopManager.getInstance().sell(player, itemId, quantity);
    }

    public static double getBuyPrice(String itemId) {
        MarketItem item = ShopManager.getInstance().getItem(itemId);
        if (item == null) return -1;
        return ShopManager.getInstance().getPriceEngine().getBuyPrice(item);
    }

    public static double getSellPrice(String itemId) {
        MarketItem item = ShopManager.getInstance().getItem(itemId);
        if (item == null) return -1;
        return ShopManager.getInstance().getPriceEngine().getSellPrice(item);
    }

    public static int getStock(String itemId) {
        MarketItem item = ShopManager.getInstance().getItem(itemId);
        return item != null ? item.getStock() : -1;
    }

    public static double getBalance(Player player) {
        return ShopManager.getInstance().getEconomy().getBalance(player);
    }

    public static void giveBalance(Player player, double amount) {
        ShopManager.getInstance().getEconomy().deposit(player, amount);
    }

    public static boolean takeBalance(Player player, double amount) {
        return ShopManager.getInstance().getEconomy().withdraw(player, amount);
    }

    public static Collection<MarketItem> getAllItems() {
        return ShopManager.getInstance().getAllItems();
    }

    public static MarketItem getItem(String itemId) {
        return ShopManager.getInstance().getItem(itemId);
    }

    public static void startGlobalEvent(String id, String name, double multiplier, long durationMinutes) {
        ShopManager.getInstance().startEvent(id, name, name, multiplier, durationMinutes, null);
    }

    public static void startItemEvent(String id, String name, double multiplier, long durationMinutes, String itemId) {
        ShopManager.getInstance().startEvent(id, name, name, multiplier, durationMinutes, itemId);
    }

    public static void stopEvent(String id) {
        ShopManager.getInstance().stopEvent(id);
    }
}
