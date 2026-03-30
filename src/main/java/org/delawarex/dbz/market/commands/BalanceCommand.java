package org.delawarex.dbz.market.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.market.ShopManager;
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
        ShopManager mgr = ShopManager.getInstance();
        String sym = mgr.getConfig().currencySymbol;
        String name = mgr.getConfig().currencyName;
        player.sendMessage(CC.translate("&8[&6⚡&8] &7Tu balance: &f"
                + String.format("%.2f", mgr.getEconomy().getBalance(player))
                + " &e" + sym + " &8(" + name + ")"));
    }
}
