package org.delawarex.dbz.market.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.market.ShopManager;
import org.delawarex.dbz.market.engine.TransactionEngine.TransactionResult;
import org.delawarex.dbz.market.model.MarketItem;
import org.delawarex.dbz.market.model.Transaction;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class ShopItemMenu extends Menu {

    private final String itemId;

    public ShopItemMenu(String itemId) { this.itemId = itemId; }

    @Override protected String getTitle() { return "&6Mercado — "; }
    @Override protected int getRows()     { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        ShopManager mgr  = ShopManager.getInstance();
        MarketItem  mi   = mgr.getItem(itemId);
        if (mi == null) { player.closeInventory(); return; }

        String sym   = mgr.getConfig().currencySymbol;
        double buy   = mgr.getPriceEngine().getBuyPrice(mi);
        double sell  = mgr.getPriceEngine().getSellPrice(mi);
        String stockBar = mgr.getStockEngine().getStockBar(mi, 14);

        Material mat;
        try { mat = Material.valueOf(mi.getMaterial()); } catch (Exception e) { mat = Material.STONE; }

        ItemStack preview = new ItemStack(mat);
        ItemMeta pmeta = preview.getItemMeta();
        if (pmeta != null) {
            pmeta.setDisplayName(CC.translate(mi.getDisplayName()));
            List<String> pl = new ArrayList<>();
            pl.add(CC.translate("&8ID: &7" + mi.getId()));
            pl.add(CC.translate("&7Material: &f" + mi.getMaterial()));
            pl.add("");
            pl.add(CC.translate("&7Compra: &c" + String.format("%.2f", buy) + " " + sym));
            pl.add(CC.translate("&7Venta:  &a" + String.format("%.2f", sell) + " " + sym));
            pl.add(CC.translate("&7Spread: &f" + String.format("%.0f", mi.getSpread() * 100) + "% | Impuesto: &f" + String.format("%.0f", mi.getTaxRate() * 100) + "%"));
            pl.add("");
            pl.add(CC.translate("&7Stock: &f" + mi.getStock() + " &8/ &f" + mi.getTargetStock()));
            pl.add(CC.translate(stockBar));
            pl.add(CC.translate("&7Tendencia: " + mi.getTrend().getDisplay()));
            pl.add("");
            pl.add(CC.translate("&7Demanda (1h): &aCompras: &f" + mi.getWindowBuys() + " &7| &cVentas: &f" + mi.getWindowSells()));
            pl.add("");
            long cd = mgr.getExploit().getCooldownRemaining(player, mi);
            if (cd > 0) pl.add(CC.translate("&cCooldown: &f" + cd + "s"));
            pmeta.setLore(pl);
            preview.setItemMeta(pmeta);
        }
        set(4, preview);

        int[] buyQtys  = {1, 8, 16, 32, 64};
        int[] sellQtys = {1, 8, 16, 32, 64};
        int[] buySlots  = {10, 11, 12, 13, 14};
        int[] sellSlots = {19, 20, 21, 22, 23};

        for (int i = 0; i < buyQtys.length; i++) {
            final int qty = buyQtys[i];
            double est = mgr.getPriceEngine().calcOrderBuyTotal(mi, qty, mgr.getActiveEvent(itemId));
            set(buySlots[i], item(Material.LIME_STAINED_GLASS_PANE,
                            "&a&lCOMPRAR x" + qty,
                            "&7Precio estimado:",
                            "&c-" + String.format("%.2f", est) + " " + sym,
                            "",
                            "&7Balance actual: &f" + String.format("%.2f", mgr.getEconomy().getBalance(player)) + " " + sym,
                            "", "&a[CLICK para comprar]"),
                    e -> {
                        TransactionResult res = mgr.buy(player, itemId, qty);
                        if (!res.success()) player.sendMessage(CC.translate("&8[&c✗&8] &c" + res.message()));
                        else {
                            player.sendMessage(CC.translate("&a✓ Compra: &fx" + res.quantity() + " por &c" + String.format("%.2f", res.total()) + " " + mgr.getConfig().currencySymbol));
                            new ShopItemMenu(itemId).open(player);
                        }
                    });
        }

        for (int i = 0; i < sellQtys.length; i++) {
            final int qty = sellQtys[i];
            double est = mgr.getPriceEngine().calcOrderSellTotal(mi, qty, mgr.getActiveEvent(itemId));
            set(sellSlots[i], item(Material.RED_STAINED_GLASS_PANE,
                            "&c&lVENDER x" + qty,
                            "&7Precio estimado:",
                            "&a+" + String.format("%.2f", est) + " " + sym,
                            "",
                            "&7En inventario: &f" + countInventory(player, mat),
                            "", "&a[CLICK para vender]"),
                    e -> {
                        TransactionResult res = mgr.sell(player, itemId, qty);
                        if (!res.success()) player.sendMessage(CC.translate("&8[&c✗&8] &c" + res.message()));
                        else {
                            player.sendMessage(CC.translate("&a✓ Venta: &fx" + res.quantity() + " por &a+" + String.format("%.2f", res.total()) + " " + mgr.getConfig().currencySymbol));
                            new ShopItemMenu(itemId).open(player);
                        }
                    });
        }

        List<String> histLines = new ArrayList<>();
        List<Transaction> history = mi.getHistory();
        int start = Math.max(0, history.size() - 5);
        for (int i = history.size() - 1; i >= start; i--) {
            Transaction tx = history.get(i);
            String col = tx.getType() == Transaction.Type.BUY ? "&c" : "&a";
            String sign = tx.getType() == Transaction.Type.BUY ? "-" : "+";
            histLines.add(CC.translate(col + (tx.getType() == Transaction.Type.BUY ? "C" : "V")
                    + " &f" + tx.getPlayerName() + " x" + tx.getQuantity()
                    + " " + sign + String.format("%.2f", tx.getTotal()) + " " + sym));
        }
        if (histLines.isEmpty()) histLines.add("&7Sin historial.");
        histLines.add(0, "&7Últimas transacciones:");
        set(31, item(Material.BOOK, "&b&lHistorial", histLines.toArray(new String[0])));

        set(36, back(), e -> new ShopBrowseMenu(1).open(player));
    }

    private int countInventory(Player player, Material mat) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == mat) count += stack.getAmount();
        }
        return count;
    }
}
