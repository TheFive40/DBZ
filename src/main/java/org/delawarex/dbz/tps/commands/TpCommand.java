package org.delawarex.dbz.tps.commands;

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
        var ref = new Object() {
            int amount = 0;
        };
        try {
            ref.amount = Integer.parseInt(command.getArgs(1));
        } catch (NumberFormatException exception) {
        }
        General.addTp(username, ref.amount);
    }
}
