package org.delawarex.dbz.market.menus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.market.ShopManager;
import org.delawarex.dbz.market.model.MarketItem;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class ShopBrowseMenu extends Menu {

    private static final int PAGE_SIZE = 28;
    private final int page;

    public ShopBrowseMenu(int page) { this.page = page; }

    @Override protected String getTitle() { return "&6&lMercado — Pág. " + page; }
    @Override protected int getRows()     { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();

        ShopManager mgr  = ShopManager.getInstance();
        String      sym  = mgr.getConfig().currencySymbol;
        List<MarketItem> all   = mgr.getEnabledItems();
        int total  = all.size();
        int pages  = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start  = (page - 1) * PAGE_SIZE;
        int end    = Math.min(start + PAGE_SIZE, total);
        int[] slots = buildInnerSlots();

        for (int i = start; i < end; i++) {
            MarketItem mi = all.get(i);
            Material mat;
            try { mat = Material.valueOf(mi.getMaterial()); }
            catch (Exception e) { mat = Material.STONE; }

            double buy  = mgr.getPriceEngine().getBuyPrice(mi);
            double sell = mgr.getPriceEngine().getSellPrice(mi);
            double pct  = mgr.getStockEngine().getStockPercent(mi);
            String stockBar = mgr.getStockEngine().getStockBar(mi, 10);
            String stockColor = mgr.getStockEngine().getStockColor(mi);

            ItemStack it = new ItemStack(mat);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate(mi.getDisplayName()));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&8ID: &7" + mi.getId()));
                lore.add("");
                lore.add(CC.translate("&7Compra: &c" + String.format("%.2f", buy) + " " + sym));
                lore.add(CC.translate("&7Venta:  &a" + String.format("%.2f", sell) + " " + sym));
                lore.add("");
                lore.add(CC.translate("&7Stock: " + stockColor + mi.getStock() + " &8/ &7" + mi.getTargetStock()));
                lore.add(CC.translate(stockBar + " &8(" + String.format("%.0f", pct) + "%)"));
                lore.add("");
                lore.add(CC.translate("&7Tendencia: " + mi.getTrend().getDisplay()));
                lore.add("");
                lore.add(ChatColor.GREEN + "[CLICK] Ver detalles");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }

            final String itemId = mi.getId();
            set(slots[i - start], it, e -> new ShopItemMenu(itemId).open(player));
        }

        if (page > 1)   set(45, navButton("◀ Anterior", true), e -> new ShopBrowseMenu(page - 1).open(player));
        set(49, item(Material.BOOK, "&fPágina &e" + page + "&7/&e" + pages));
        if (page < pages) set(53, navButton("Siguiente ▶", true), e -> new ShopBrowseMenu(page + 1).open(player));
        set(48, back(), e -> new ShopMainMenu().open(player));
    }

    private int[] buildInnerSlots() {
        int[] slots = new int[PAGE_SIZE];
        int idx = 0;
        for (int row = 1; row <= 4; row++)
            for (int col = 1; col <= 7; col++)
                slots[idx++] = row * 9 + col;
        return slots;
    }
}
