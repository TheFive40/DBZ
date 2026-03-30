package org.delawarex.dbz.market.commands;

import org.bukkit.entity.Player;
import org.delawarex.dbz.market.ShopManager;
import org.delawarex.dbz.market.engine.TransactionEngine.TransactionResult;
import org.delawarex.dbz.market.menus.ShopMainMenu;
import org.delawarex.dbz.market.model.MarketItem;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class ShopCommand extends BaseCommand {

    @Command(name = "shop", permission = "dbz.shop")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length == 0) {
            new ShopMainMenu().open(player);
            return;
        }

        switch (args.getArgs(0).toLowerCase()) {
            case "buy"  -> handleBuy(player, args);
            case "sell" -> handleSell(player, args);
            case "info" -> handleInfo(player, args);
            case "list" -> handleList(player);
            default     -> sendHelp(player);
        }
    }

    private void handleBuy(Player player, CommandArgs args) {
        if (args.getArgs().length < 2) { player.sendMessage(CC.translate("&cUso: /shop buy <id> [cantidad]")); return; }
        String id  = args.getArgs(1).toLowerCase();
        int    qty = 1;
        if (args.getArgs().length >= 3) {
            try { qty = Integer.parseInt(args.getArgs(2)); } catch (Exception e) {
                player.sendMessage(CC.translate("&cCantidad inválida.")); return;
            }
        }

        TransactionResult result = ShopManager.getInstance().buy(player, id, qty);
        if (!result.success()) {
            player.sendMessage(CC.translate("&8[&c✗&8] &c" + result.message()));
            return;
        }

        ShopManager mgr = ShopManager.getInstance();
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&6&l  COMPRA EXITOSA"));
        player.sendMessage(CC.translate("&7  Ítem: &f" + id + " x" + result.quantity()));
        player.sendMessage(CC.translate("&7  Precio/u: &f" + String.format("%.2f", result.perUnit()) + " " + mgr.getConfig().currencySymbol));
        player.sendMessage(CC.translate("&7  Total: &c-" + String.format("%.2f", result.total()) + " " + mgr.getConfig().currencySymbol));
        player.sendMessage(CC.translate("&7  Balance: &f" + String.format("%.2f", mgr.getEconomy().getBalance(player)) + " " + mgr.getConfig().currencySymbol));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleSell(Player player, CommandArgs args) {
        if (args.getArgs().length < 2) { player.sendMessage(CC.translate("&cUso: /shop sell <id> [cantidad]")); return; }
        String id  = args.getArgs(1).toLowerCase();
        int    qty = 1;
        if (args.getArgs().length >= 3) {
            try { qty = Integer.parseInt(args.getArgs(2)); } catch (Exception e) {
                player.sendMessage(CC.translate("&cCantidad inválida.")); return;
            }
        }

        TransactionResult result = ShopManager.getInstance().sell(player, id, qty);
        if (!result.success()) {
            player.sendMessage(CC.translate("&8[&c✗&8] &c" + result.message()));
            return;
        }

        ShopManager mgr = ShopManager.getInstance();
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&a&l  VENTA EXITOSA"));
        player.sendMessage(CC.translate("&7  Ítem: &f" + id + " x" + result.quantity()));
        player.sendMessage(CC.translate("&7  Precio/u: &f" + String.format("%.2f", result.perUnit()) + " " + mgr.getConfig().currencySymbol));
        player.sendMessage(CC.translate("&7  Total: &a+" + String.format("%.2f", result.total()) + " " + mgr.getConfig().currencySymbol));
        player.sendMessage(CC.translate("&7  Balance: &f" + String.format("%.2f", mgr.getEconomy().getBalance(player)) + " " + mgr.getConfig().currencySymbol));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleInfo(Player player, CommandArgs args) {
        if (args.getArgs().length < 2) { player.sendMessage(CC.translate("&cUso: /shop info <id>")); return; }
        MarketItem item = ShopManager.getInstance().getItem(args.getArgs(1).toLowerCase());
        if (item == null) { player.sendMessage(CC.translate("&cÍtem no encontrado.")); return; }

        ShopManager mgr = ShopManager.getInstance();
        String sym = mgr.getConfig().currencySymbol;
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&b&l  " + item.getDisplayName()));
        player.sendMessage(CC.translate("&7  ID: &f" + item.getId() + " &7| Material: &f" + item.getMaterial()));
        player.sendMessage(CC.translate("&7  Compra: &c" + String.format("%.2f", mgr.getPriceEngine().getBuyPrice(item)) + " " + sym));
        player.sendMessage(CC.translate("&7  Venta: &a" + String.format("%.2f", mgr.getPriceEngine().getSellPrice(item)) + " " + sym));
        player.sendMessage(CC.translate("&7  Stock: &f" + item.getStock() + " &8/ " + item.getTargetStock()));
        player.sendMessage(CC.translate("&7  Tendencia: " + CC.translate(item.getTrend().getDisplay())));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleList(Player player) {
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&b&l  ÍTEMS EN EL MERCADO"));
        ShopManager mgr = ShopManager.getInstance();
        String sym = mgr.getConfig().currencySymbol;
        for (MarketItem item : mgr.getEnabledItems()) {
            double buy  = mgr.getPriceEngine().getBuyPrice(item);
            double sell = mgr.getPriceEngine().getSellPrice(item);
            player.sendMessage(CC.translate("&7  &f" + item.getId()
                    + " &8| Compra: &c" + String.format("%.2f", buy) + " &8| Venta: &a" + String.format("%.2f", sell)
                    + " &8| Stock: &f" + item.getStock()
                    + " &8| " + item.getTrend().getSymbol()));
        }
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&7/shop &8- Abrir mercado GUI"));
        player.sendMessage(CC.translate("&7/shop buy <id> [qty] &8- Comprar"));
        player.sendMessage(CC.translate("&7/shop sell <id> [qty] &8- Vender"));
        player.sendMessage(CC.translate("&7/shop info <id> &8- Ver precio"));
        player.sendMessage(CC.translate("&7/shop list &8- Listar ítems"));
    }
}
