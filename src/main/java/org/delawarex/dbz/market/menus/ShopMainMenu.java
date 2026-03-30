package org.delawarex.dbz.market.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.market.ShopManager;
import org.delawarex.dbz.market.model.MarketEvent;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class ShopMainMenu extends Menu {

    @Override
    protected String getTitle() { return "&6&lMercado Global"; }

    @Override
    protected int getRows() { return 4; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        ShopManager mgr = ShopManager.getInstance();
        String sym = mgr.getConfig().currencySymbol;
        double balance = mgr.getEconomy().getBalance(player);

        set(4, item(Material.GOLD_INGOT,
                "&6&lMercado Global",
                "&7El precio sube y baja con la oferta y demanda.",
                "",
                "&7Balance: &f" + String.format("%.2f", balance) + " " + sym,
                "&7Ítems activos: &f" + mgr.getEnabledItems().size()));

        set(11, item(Material.BOOK,
                        "&e&lExplorar Ítems",
                        "&7Compra y vende en el mercado.",
                        "&7Los precios son dinámicos.",
                        "", "&a[CLICK]"),
                e -> new ShopBrowseMenu(1).open(player));

        List<String> eventLines = new ArrayList<>();
        List<MarketEvent> events = mgr.getActiveEvents();
        if (events.isEmpty()) {
            eventLines.add("&7Sin eventos activos.");
        } else {
            for (MarketEvent ev : events) {
                long mins = ev.getRemainingMillis() / 60_000;
                String affectedStr = ev.getAffectedItemId() != null ? "&7Ítem: &f" + ev.getAffectedItemId() : "&7Global";
                eventLines.add("&6" + ev.getName());
                eventLines.add("&7Multiplicador: &f" + ev.getPriceMultiplier() + "x | Queda: &f" + mins + " min");
                eventLines.add(affectedStr);
            }
        }
        set(13, item(Material.BLAZE_POWDER,
                "&c&lEventos Activos", eventLines.toArray(new String[0])));

        set(15, item(Material.COMPASS,
                        "&b&lTop Transacciones",
                        "&7Tendencias del mercado.",
                        "", "&a[CLICK]"),
                e -> new ShopTrendMenu().open(player));

        set(31, item(Material.GOLD_NUGGET,
                "&7Balance: &f" + String.format("%.2f", balance) + " " + sym,
                "&7Usa /pay <jugador> <cantidad>",
                "&7para transferir dinero."));
    }
}
