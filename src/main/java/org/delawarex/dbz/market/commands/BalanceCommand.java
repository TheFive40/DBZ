package org.delawarex.dbz.market.commands;

import org.bukkit.entity.Player;
import org.delawarex.dbz.market.ShopManager;
import org.delawarex.dbz.market.storage.EconomyManager;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class BalanceCommand extends BaseCommand {

    @Command(name = "balance", permission = "dbz.shop")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (!EconomyManager.isHooked()) {
            player.sendMessage(CC.translate("&cEl sistema de economía no está disponible."));
            return;
        }

        ShopManager mgr = ShopManager.getInstance();
        double balance  = mgr.getEconomy().getBalance(player);
        String formatted = mgr.getEconomy().format(balance, "");

        player.sendMessage(CC.translate("&8[&6⚡&8] &7Tu balance: &f" + formatted));
    }
}
