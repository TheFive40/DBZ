package org.delawarex.dbz.market.engine;

import org.delawarex.dbz.market.model.MarketItem;

public class StockEngine {

    public boolean canBuy(MarketItem item, int qty) {
        return item.getStock() >= qty;
    }

    public void consumeStock(MarketItem item, int qty) {
        item.setStock(Math.max(0, item.getStock() - qty));
    }

    public void addStock(MarketItem item, int qty) {
        item.setStock(item.getStock() + qty);
    }

    public double getStockPercent(MarketItem item) {
        if (item.getTargetStock() <= 0) return 100.0;
        return Math.min(100.0, (item.getStock() * 100.0) / item.getTargetStock());
    }

    public String getStockBar(MarketItem item, int length) {
        int filled = (int) (getStockPercent(item) / 100.0 * length);
        StringBuilder bar = new StringBuilder();
        String color = getStockColor(item);
        bar.append(color);
        for (int i = 0; i < length; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        return bar.toString();
    }

    public String getStockColor(MarketItem item) {
        double pct = getStockPercent(item);
        if (pct > 60) return "&a";
        if (pct > 25) return "&e";
        return "&c";
    }
}
