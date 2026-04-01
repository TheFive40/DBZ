package org.delawarex.dbz.tps.commands;

import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;
import org.delawarex.service.dbz.General;

import java.io.IOException;

public class TpCommand extends BaseCommand {

    @Command(name = "addtp", aliases = "addtp", inGameOnly = false, permission = "dbz.admin.addtp")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        if (command.getArgs().length < 2) return;

        String username = command.getArgs(0);
        int amount = 0;
        try {
            amount = Integer.parseInt(command.getArgs(1));
        } catch (NumberFormatException ignored) {}

        if (amount <= 0) return;

        Player target = DbzMain.instance.getServer().getPlayer(username);

        if (target != null) {
            long finalAmount = amount;
            try {
                if (BankManager.getInstance().hasTpsPenalty(target)) {
                    finalAmount = BankManager.getInstance().applyTpsPenalty(target, amount);
                }
            } catch (Exception ignored) {}
            General.addTp(username, (int) Math.max(0, finalAmount));
        } else {
            General.addTp(username, amount);
        }
    }
}
