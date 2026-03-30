package org.delawarex.dbz.market.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.market.ShopManager;
import org.delawarex.dbz.market.menus.ShopEditorMenu;
import org.delawarex.dbz.market.model.MarketEvent;
import org.delawarex.dbz.market.model.MarketItem;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;
import java.util.UUID;

public class ShopAdminCommand extends BaseCommand {

    @Command(name = "shopadmin", permission = "dbz.admin.shop")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length == 0) {
            new ShopEditorMenu(1).open(player);
            return;
        }

        switch (args.getArgs(0).toLowerCase()) {
            case "add"      -> handleAdd(player, args);
            case "remove"   -> handleRemove(player, args);
            case "edit"     -> new ShopEditorMenu(1).open(player);
            case "setstock" -> handleSetStock(player, args);
            case "addstock" -> handleAddStock(player, args);
            case "setprice" -> handleSetPrice(player, args);
            case "give"     -> handleGive(player, args);
            case "set"      -> handleSet(player, args);
            case "event"    -> handleEvent(player, args);
            case "reload"   -> handleReload(player);
            case "info"     -> handleInfo(player, args);
            default         -> sendHelp(player);
        }
    }

    private void handleAdd(Player player, CommandArgs args) {
        if (args.getArgs().length < 5) {
            player.sendMessage(CC.translate("&cUso: /shopadmin add <id> <material> <precio> <nombre...>"));
            return;
        }
        String id  = args.getArgs(1).toLowerCase();
        String mat = args.getArgs(2).toUpperCase();
        double price;
        try { price = Double.parseDouble(args.getArgs(3)); } catch (Exception e) {
            player.sendMessage(CC.translate("&cPrecio inválido.")); return;
        }
        StringBuilder name = new StringBuilder();
        for (int i = 4; i < args.getArgs().length; i++) {
            if (i > 4) name.append(" ");
            name.append(args.getArgs(i));
        }

        if (ShopManager.getInstance().itemExists(id)) {
            player.sendMessage(CC.translate("&cYa existe un ítem con ese ID.")); return;
        }
        try { org.bukkit.Material.valueOf(mat); } catch (Exception e) {
            player.sendMessage(CC.translate("&cMaterial inválido: &f" + mat)); return;
        }

        MarketItem item = ShopManager.getInstance().addItem(id, CC.translate(name.toString()), mat, price);
        player.sendMessage(CC.translate("&a✓ Ítem &f" + id + " &aañadido al mercado. Precio base: &f" + price));
    }

    private void handleRemove(Player player, CommandArgs args) {
        if (args.getArgs().length < 2) { player.sendMessage(CC.translate("&cUso: /shopadmin remove <id>")); return; }
        String id = args.getArgs(1).toLowerCase();
        if (!ShopManager.getInstance().itemExists(id)) {
            player.sendMessage(CC.translate("&cÍtem no encontrado: &f" + id)); return;
        }
        ShopManager.getInstance().removeItem(id);
        player.sendMessage(CC.translate("&a✓ Ítem &f" + id + " &aeliminado del mercado."));
    }

    private void handleSetStock(Player player, CommandArgs args) {
        if (args.getArgs().length < 3) { player.sendMessage(CC.translate("&cUso: /shopadmin setstock <id> <cantidad>")); return; }
        MarketItem item = ShopManager.getInstance().getItem(args.getArgs(1).toLowerCase());
        if (item == null) { player.sendMessage(CC.translate("&cÍtem no encontrado.")); return; }
        try {
            int qty = Integer.parseInt(args.getArgs(2));
            if (qty < 0) throw new NumberFormatException();
            item.setStock(qty);
            ShopManager.getInstance().saveItem(item);
            player.sendMessage(CC.translate("&a✓ Stock de &f" + item.getId() + " &aestablecido a &f" + qty));
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cCantidad inválida."));
        }
    }

    private void handleAddStock(Player player, CommandArgs args) {
        if (args.getArgs().length < 3) { player.sendMessage(CC.translate("&cUso: /shopadmin addstock <id> <cantidad>")); return; }
        MarketItem item = ShopManager.getInstance().getItem(args.getArgs(1).toLowerCase());
        if (item == null) { player.sendMessage(CC.translate("&cÍtem no encontrado.")); return; }
        try {
            int qty = Integer.parseInt(args.getArgs(2));
            item.setStock(Math.max(0, item.getStock() + qty));
            ShopManager.getInstance().saveItem(item);
            player.sendMessage(CC.translate("&a✓ Stock actualizado: &f" + item.getStock()));
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cCantidad inválida."));
        }
    }

    private void handleSetPrice(Player player, CommandArgs args) {
        if (args.getArgs().length < 3) { player.sendMessage(CC.translate("&cUso: /shopadmin setprice <id> <precio>")); return; }
        MarketItem item = ShopManager.getInstance().getItem(args.getArgs(1).toLowerCase());
        if (item == null) { player.sendMessage(CC.translate("&cÍtem no encontrado.")); return; }
        try {
            double price = Double.parseDouble(args.getArgs(2));
            if (price <= 0) throw new NumberFormatException();
            item.setBasePrice(price);
            ShopManager.getInstance().saveItem(item);
            player.sendMessage(CC.translate("&a✓ Precio base de &f" + item.getId() + " &aestablecido a &f" + price));
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cPrecio inválido."));
        }
    }

    private void handleGive(Player player, CommandArgs args) {
        if (args.getArgs().length < 3) { player.sendMessage(CC.translate("&cUso: /shopadmin give <jugador> <cantidad>")); return; }
        Player target = Bukkit.getPlayer(args.getArgs(1));
        if (target == null) { player.sendMessage(CC.translate("&cJugador no encontrado.")); return; }
        try {
            double amount = Double.parseDouble(args.getArgs(2));
            ShopManager.getInstance().getEconomy().deposit(target, amount);
            String sym = ShopManager.getInstance().getConfig().currencySymbol;
            player.sendMessage(CC.translate("&a✓ Dado &f" + String.format("%.2f", amount) + " " + sym + " &aa &f" + target.getName()));
            target.sendMessage(CC.translate("&a✓ Recibiste &f" + String.format("%.2f", amount) + " " + sym + " &adel staff."));
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cCantidad inválida."));
        }
    }

    private void handleSet(Player player, CommandArgs args) {
        if (args.getArgs().length < 3) { player.sendMessage(CC.translate("&cUso: /shopadmin set <jugador> <cantidad>")); return; }
        Player target = Bukkit.getPlayer(args.getArgs(1));
        if (target == null) { player.sendMessage(CC.translate("&cJugador no encontrado.")); return; }
        try {
            double amount = Double.parseDouble(args.getArgs(2));
            ShopManager.getInstance().getEconomy().setBalance(target.getUniqueId(), amount);
            String sym = ShopManager.getInstance().getConfig().currencySymbol;
            player.sendMessage(CC.translate("&a✓ Balance de &f" + target.getName() + " &aestablecido a &f" + String.format("%.2f", amount) + " " + sym));
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cCantidad inválida."));
        }
    }

    private void handleEvent(Player player, CommandArgs args) {
        if (args.getArgs().length < 2) {
            player.sendMessage(CC.translate("&cUso: /shopadmin event start <id> <multiplicador> <minutos> [itemId]"));
            player.sendMessage(CC.translate("&cUso: /shopadmin event stop <id>"));
            return;
        }
        switch (args.getArgs(1).toLowerCase()) {
            case "start" -> {
                if (args.getArgs().length < 5) {
                    player.sendMessage(CC.translate("&cUso: /shopadmin event start <id> <mult> <minutos> [itemId]")); return;
                }
                String id   = args.getArgs(2);
                double mult;
                long   mins;
                try {
                    mult = Double.parseDouble(args.getArgs(3));
                    mins = Long.parseLong(args.getArgs(4));
                } catch (NumberFormatException e) {
                    player.sendMessage(CC.translate("&cValores inválidos.")); return;
                }
                String itemId = args.getArgs().length >= 6 ? args.getArgs(5) : null;
                String name   = "Evento " + id;
                ShopManager.getInstance().startEvent(id, name, name, mult, mins, itemId);
                player.sendMessage(CC.translate("&a✓ Evento &f" + id + " &ainiciado (x" + mult + " por " + mins + " min" + (itemId != null ? " en " + itemId : " global") + ")"));
            }
            case "stop" -> {
                if (args.getArgs().length < 3) { player.sendMessage(CC.translate("&cUso: /shopadmin event stop <id>")); return; }
                ShopManager.getInstance().stopEvent(args.getArgs(2));
                player.sendMessage(CC.translate("&a✓ Evento &f" + args.getArgs(2) + " &adetenido."));
            }
            default -> player.sendMessage(CC.translate("&cSub-comando desconocido: start | stop"));
        }
    }

    private void handleInfo(Player player, CommandArgs args) {
        if (args.getArgs().length < 2) { player.sendMessage(CC.translate("&cUso: /shopadmin info <id>")); return; }
        MarketItem item = ShopManager.getInstance().getItem(args.getArgs(1).toLowerCase());
        if (item == null) { player.sendMessage(CC.translate("&cÍtem no encontrado.")); return; }
        ShopManager mgr = ShopManager.getInstance();
        String sym = mgr.getConfig().currencySymbol;
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&b&l  " + item.getDisplayName() + " &8(&7" + item.getId() + "&8)"));
        player.sendMessage(CC.translate("&7  Material: &f" + item.getMaterial() + " | Estado: " + (item.isEnabled() ? "&aActivo" : "&cDesactivado")));
        player.sendMessage(CC.translate("&7  Precio base: &f" + item.getBasePrice() + " | Min: &f" + item.getMinPrice() + " | Max: &f" + item.getMaxPrice()));
        player.sendMessage(CC.translate("&7  Compra: &c" + String.format("%.2f", mgr.getPriceEngine().getBuyPrice(item)) + " | Venta: &a" + String.format("%.2f", mgr.getPriceEngine().getSellPrice(item))));
        player.sendMessage(CC.translate("&7  Stock: &f" + item.getStock() + " &8/ &f" + item.getTargetStock()));
        player.sendMessage(CC.translate("&7  Tendencia: " + item.getTrend().getDisplay()));
        player.sendMessage(CC.translate("&7  Impuesto: &f" + (item.getTaxRate() * 100) + "% | Spread: &f" + (item.getSpread() * 100) + "%"));
        player.sendMessage(CC.translate("&7  Demanda ventana (1h): &aCompras: &f" + item.getWindowBuys() + " &7| &cVentas: &f" + item.getWindowSells()));
        player.sendMessage(CC.translate("&7  Historial reciente: &f" + item.getHistory().size() + " transacciones"));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleReload(Player player) {
        ShopManager.getInstance().reload();
        player.sendMessage(CC.translate("&a✓ Mercado recargado."));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&b&l  Admin Mercado"));
        player.sendMessage(CC.translate("&e/shopadmin add <id> <mat> <precio> <nombre>"));
        player.sendMessage(CC.translate("&e/shopadmin remove <id>"));
        player.sendMessage(CC.translate("&e/shopadmin edit &8- GUI editor"));
        player.sendMessage(CC.translate("&e/shopadmin setstock <id> <qty>"));
        player.sendMessage(CC.translate("&e/shopadmin addstock <id> <qty>"));
        player.sendMessage(CC.translate("&e/shopadmin setprice <id> <precio>"));
        player.sendMessage(CC.translate("&e/shopadmin give <jugador> <qty>"));
        player.sendMessage(CC.translate("&e/shopadmin set <jugador> <qty>"));
        player.sendMessage(CC.translate("&e/shopadmin event start <id> <mult> <mins> [item]"));
        player.sendMessage(CC.translate("&e/shopadmin event stop <id>"));
        player.sendMessage(CC.translate("&e/shopadmin info <id>"));
        player.sendMessage(CC.translate("&e/shopadmin reload"));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
}
