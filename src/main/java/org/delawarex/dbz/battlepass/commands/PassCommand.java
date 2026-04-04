package org.delawarex.dbz.battlepass.commands;

import org.bukkit.entity.Player;
import org.delawarex.dbz.battlepass.menus.PassListMenu;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class PassCommand extends BaseCommand {

    @Command(name = "pass", aliases = {"battlepass", "bp"}, permission = "dbz.battlepass")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        new PassListMenu().open(args.getPlayer());
    }
}