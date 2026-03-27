package org.delawarex.dbz.raids.commands;

import org.bukkit.entity.Player;
import org.delawarex.dbz.raids.managers.RaidManager;
import org.delawarex.dbz.raids.menus.RaidMainMenu;
import org.delawarex.dbz.raids.menus.RaidListMenu;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class RaidCommand extends BaseCommand {

    @Command(name = "raid", permission = "dbz.admin.raid")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length == 0) {
            new RaidMainMenu().open(player);
            return;
        }

        switch (args.getArgs(0).toLowerCase()) {
            case "menu" -> new RaidMainMenu().open(player);
            case "list" -> new RaidListMenu(1).open(player);
            case "reload" -> {
                RaidManager.getInstance().reload();
                player.sendMessage(CC.translate("&a✓ Sistema de raids recargado."));
            }
            default -> {
                player.sendMessage(CC.translate("&8[&e&l?&8] &e/raid: menu | list | reload"));
            }
        }
    }
}