package org.delawarex.dbz.market.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.market.ShopManager;
import org.delawarex.dbz.market.model.MarketItem;
import org.delawarex.dbz.market.model.TrendDirection;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class ShopTrendMenu extends Menu {

    @Override protected String getTitle() { return "&b&lTendencias del Mercado"; }
    @Override protected int getRows()     { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        ShopManager mgr = ShopManager.getInstance();
        String sym = mgr.getConfig().currencySymbol;
        List<MarketItem> all = mgr.getEnabledItems();
        int[] slots = buildInnerSlots();
        int idx = 0;

        for (MarketItem mi : all) {
            if (idx >= slots.length) break;
            Material mat = Material.PAPER;
            try { mat = Material.valueOf(mi.getMaterial()); } catch (Exception ignored) {}

            double buy  = mgr.getPriceEngine().getBuyPrice(mi);
            double sell = mgr.getPriceEngine().getSellPrice(mi);
            TrendDirection trend = mi.getTrend();

            Material trendMat;
            if (trend == TrendDirection.RISING)       trendMat = Material.LIME_DYE;
            else if (trend == TrendDirection.FALLING) trendMat = Material.RED_DYE;
            else                                      trendMat = Material.GRAY_DYE;

            ItemStack it = new ItemStack(trendMat);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate(trend.getSymbol() + " &f" + mi.getDisplayName()));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&8" + mi.getId()));
                lore.add("");
                lore.add(CC.translate("&7Compra: &c" + String.format("%.2f", buy) + " " + sym));
                lore.add(CC.translate("&7Venta:  &a" + String.format("%.2f", sell) + " " + sym));
                lore.add(CC.translate("&7Base:   &f" + mi.getBasePrice() + " " + sym));
                lore.add("");
                lore.add(CC.translate("&7Stock: &f" + mi.getStock() + " &8/ &f" + mi.getTargetStock()));
                lore.add(CC.translate(mgr.getStockEngine().getStockBar(mi, 12)));
                lore.add("");
                lore.add(CC.translate("&7Compras (1h): &f" + mi.getWindowBuys()));
                lore.add(CC.translate("&7Ventas  (1h): &f" + mi.getWindowSells()));
                lore.add(CC.translate("&7Demanda neta: " + (mi.getNetDemand() >= 0 ? "&a+" : "&c") + (int) mi.getNetDemand()));
                meta.setLore(lore);
                it.setItemMeta(meta);
            }

            final String itemId = mi.getId();
            set(slots[idx++], it, e -> new ShopItemMenu(itemId).open(player));
        }

        set(49, back(), e -> new ShopMainMenu().open(player));
    }

    private int[] buildInnerSlots() {
        int[] slots = new int[28];
        int idx = 0;
        for (int row = 1; row <= 4; row++)
            for (int col = 1; col <= 7; col++)
                slots[idx++] = row * 9 + col;
        return slots;
    }
}
