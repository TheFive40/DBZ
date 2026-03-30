package org.delawarex.dbz.market.model;

import java.util.ArrayList;
import java.util.List;

public class MarketItem {

    private static final long WINDOW_DURATION = 60 * 60 * 1000L;
    private static final long CACHE_TTL       = 5_000L;

    private String        id;
    private String        displayName;
    private String        material;
    private double        basePrice;
    private double        minPrice;
    private double        maxPrice;
    private int           stock;
    private int           targetStock;
    private double        taxRate;
    private double        spread;
    private double        volumeSensitivity;
    private double        demandSensitivity;
    private double        stockSensitivity;
    private boolean       enabled;
    private long          lastUpdated;
    private int           windowBuys;
    private int           windowSells;
    private long          windowStart;
    private TrendDirection trend;
    private List<Transaction> history;
    private double        cachedBuyPrice;
    private double        cachedSellPrice;
    private long          cacheTime;

    public MarketItem() {
        this.history           = new ArrayList<>();
        this.enabled           = true;
        this.taxRate           = 0.05;
        this.spread            = 0.20;
        this.volumeSensitivity = 0.02;
        this.demandSensitivity = 0.5;
        this.stockSensitivity  = 1.0;
        this.targetStock       = 100;
        this.trend             = TrendDirection.STABLE;
        this.windowStart       = System.currentTimeMillis();
        this.cacheTime         = 0;
    }

    public void recordBuy(int qty) {
        refreshWindow();
        windowBuys += qty;
        invalidateCache();
    }

    public void recordSell(int qty) {
        refreshWindow();
        windowSells += qty;
        invalidateCache();
    }

    private void refreshWindow() {
        long now = System.currentTimeMillis();
        if (now - windowStart > WINDOW_DURATION) {
            windowBuys  = windowBuys  / 2;
            windowSells = windowSells / 2;
            windowStart = now;
        }
    }

    public double getNetDemand() {
        refreshWindow();
        return windowBuys - windowSells;
    }

    public void invalidateCache() {
        cacheTime = 0;
    }

    public boolean isCacheValid() {
        return System.currentTimeMillis() - cacheTime < CACHE_TTL;
    }

    public void updateCache(double buy, double sell) {
        this.cachedBuyPrice  = buy;
        this.cachedSellPrice = sell;
        this.cacheTime       = System.currentTimeMillis();
    }

    public void addToHistory(Transaction t) {
        history.add(t);
        if (history.size() > 50) history.remove(0);
        lastUpdated = System.currentTimeMillis();
    }

    public String         getId()                { return id; }
    public void           setId(String id)        { this.id = id; }
    public String         getDisplayName()        { return displayName; }
    public void           setDisplayName(String d){ this.displayName = d; }
    public String         getMaterial()           { return material; }
    public void           setMaterial(String m)   { this.material = m; }
    public double         getBasePrice()          { return basePrice; }
    public void           setBasePrice(double v)  { this.basePrice = v; invalidateCache(); }
    public double         getMinPrice()           { return minPrice; }
    public void           setMinPrice(double v)   { this.minPrice = v; invalidateCache(); }
    public double         getMaxPrice()           { return maxPrice; }
    public void           setMaxPrice(double v)   { this.maxPrice = v; invalidateCache(); }
    public int            getStock()              { return stock; }
    public void           setStock(int s)         { this.stock = s; invalidateCache(); }
    public int            getTargetStock()        { return targetStock; }
    public void           setTargetStock(int t)   { this.targetStock = t; }
    public double         getTaxRate()            { return taxRate; }
    public void           setTaxRate(double v)    { this.taxRate = v; }
    public double         getSpread()             { return spread; }
    public void           setSpread(double v)     { this.spread = v; }
    public double         getVolumeSensitivity()  { return volumeSensitivity; }
    public void           setVolumeSensitivity(double v) { this.volumeSensitivity = v; }
    public double         getDemandSensitivity()  { return demandSensitivity; }
    public void           setDemandSensitivity(double v) { this.demandSensitivity = v; }
    public double         getStockSensitivity()   { return stockSensitivity; }
    public void           setStockSensitivity(double v)  { this.stockSensitivity = v; }
    public boolean        isEnabled()             { return enabled; }
    public void           setEnabled(boolean e)   { this.enabled = e; }
    public long           getLastUpdated()        { return lastUpdated; }
    public void           setLastUpdated(long l)  { this.lastUpdated = l; }
    public int            getWindowBuys()         { return windowBuys; }
    public void           setWindowBuys(int v)    { this.windowBuys = v; }
    public int            getWindowSells()        { return windowSells; }
    public void           setWindowSells(int v)   { this.windowSells = v; }
    public long           getWindowStart()        { return windowStart; }
    public void           setWindowStart(long v)  { this.windowStart = v; }
    public TrendDirection getTrend()              { return trend; }
    public void           setTrend(TrendDirection t) { this.trend = t; }
    public List<Transaction> getHistory()         { return history; }
    public void           setHistory(List<Transaction> h) { this.history = h; }
    public double         getCachedBuyPrice()     { return cachedBuyPrice; }
    public double         getCachedSellPrice()    { return cachedSellPrice; }
}
