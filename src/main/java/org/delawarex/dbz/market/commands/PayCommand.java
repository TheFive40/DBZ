package org.delawarex.dbz.market.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.market.ShopManager;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class PayCommand extends BaseCommand {

    @Command(name = "pay", permission = "dbz.shop")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length < 2) {
            player.sendMessage(CC.translate("&cUso: /pay <jugador> <cantidad>"));
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(0));
        if (target == null) {
            player.sendMessage(CC.translate("&cJugador no encontrado."));
            return;
        }
        if (target.equals(player)) {
            player.sendMessage(CC.translate("&cNo puedes pagarte a ti mismo."));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args.getArgs(1));
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cCantidad inválida."));
            return;
        }

        ShopManager mgr = ShopManager.getInstance();
        String sym = mgr.getConfig().currencySymbol;

        if (!mgr.getEconomy().transfer(player, target, amount)) {
            player.sendMessage(CC.translate("&cFondos insuficientes."));
            return;
        }

        player.sendMessage(CC.translate("&a✓ Enviaste &f" + String.format("%.2f", amount) + " " + sym + " &aa &f" + target.getName()));
        target.sendMessage(CC.translate("&a✓ Recibiste &f" + String.format("%.2f", amount) + " " + sym + " &ade &f" + player.getName()));
    }
}
