package org.delawarex.dbz.market.engine;

import org.delawarex.dbz.market.model.MarketItem;
import org.delawarex.dbz.market.model.MarketEvent;
import org.delawarex.dbz.market.model.TrendDirection;

public class PriceEngine {

    public double getBuyPrice(MarketItem item) {
        if (item.isCacheValid()) return item.getCachedBuyPrice();
        recalculate(item);
        return item.getCachedBuyPrice();
    }

    public double getSellPrice(MarketItem item) {
        if (item.isCacheValid()) return item.getCachedSellPrice();
        recalculate(item);
        return item.getCachedSellPrice();
    }

    private void recalculate(MarketItem item) {
        double dynamic  = getDynamic(item, item.getStock());
        double buyRaw   = clamp(dynamic, item.getMinPrice(), item.getMaxPrice());
        double sellRaw  = clamp(dynamic * (1.0 - item.getSpread()), item.getMinPrice() * 0.5, item.getMaxPrice());
        double buy  = round(buyRaw  * (1.0 + item.getTaxRate()));
        double sell = round(sellRaw * (1.0 - item.getTaxRate()));
        item.updateCache(buy, sell);
    }

    public double calcOrderBuyTotal(MarketItem item, int quantity, MarketEvent event) {
        double total = 0;
        for (int i = 0; i < quantity; i++) {
            int effectiveStock = Math.max(0, item.getStock() - i);
            double unit = getDynamic(item, effectiveStock);
            unit = clamp(unit, item.getMinPrice(), item.getMaxPrice());
            unit *= getSlippage(item, i);
            if (event != null && event.isActive() && event.affects(item.getId())) {
                unit *= event.getPriceMultiplier();
            }
            total += unit;
        }
        return round(total * (1.0 + item.getTaxRate()));
    }

    public double calcOrderSellTotal(MarketItem item, int quantity, MarketEvent event) {
        double total = 0;
        for (int i = 0; i < quantity; i++) {
            int effectiveStock = item.getStock() + i;
            double unit = getDynamic(item, effectiveStock);
            unit = clamp(unit * (1.0 - item.getSpread()), item.getMinPrice() * 0.5, item.getMaxPrice());
            unit /= getSlippage(item, i);
            if (event != null && event.isActive() && event.affects(item.getId())) {
                unit *= event.getPriceMultiplier();
            }
            total += unit;
        }
        return round(total * (1.0 - item.getTaxRate()));
    }

    public void updateTrend(MarketItem item) {
        double current = getDynamic(item, item.getStock());
        double base    = item.getBasePrice();
        double thresh  = base * 0.05;
        double diff    = current - base;
        if (diff > thresh)       item.setTrend(TrendDirection.RISING);
        else if (diff < -thresh) item.setTrend(TrendDirection.FALLING);
        else                     item.setTrend(TrendDirection.STABLE);
    }

    private double getDynamic(MarketItem item, int stock) {
        return item.getBasePrice() * getStockMod(item, stock) * getDemandMod(item);
    }

    private double getStockMod(MarketItem item, int stock) {
        if (stock <= 0) return 3.0;
        double ratio = (double) item.getTargetStock() / stock;
        double mod   = 1.0 + (ratio - 1.0) * item.getStockSensitivity();
        return clamp(mod, 0.30, 3.0);
    }

    private double getDemandMod(MarketItem item) {
        double net = item.getNetDemand();
        double mod = 1.0 + (net / 100.0) * item.getDemandSensitivity();
        return clamp(mod, 0.5, 2.5);
    }

    private double getSlippage(MarketItem item, int unitIndex) {
        if (unitIndex == 0) return 1.0;
        return 1.0 + unitIndex * item.getVolumeSensitivity();
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
