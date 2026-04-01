package org.delawarex.dbz.advancedcrates.commands;

import org.delawarex.dbz.advancedcrates.menus.CrateListMenu;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class CratesCommand extends BaseCommand {

    @Command(name = "crates", aliases = {"crates", "crate"}, permission = "advancedcrates.use")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        new CrateListMenu(1).open(command.getPlayer());

    }
}
