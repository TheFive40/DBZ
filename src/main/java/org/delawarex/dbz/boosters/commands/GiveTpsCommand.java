package org.delawarex.dbz.boosters.commands;

import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.boosters.events.GiveTpsCommandInterceptor;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class GiveTpsCommand extends BaseCommand {

    @Command(name = "dartps", inGameOnly = false, permission = "dbz.admin.dartps")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        if (command.length() < 2) {
            command.getSender().sendMessage(CC.translate("&cUso: /dartps <jugador> <cantidad>"));
            return;
        }

        String targetName = command.getArgs(0);
        Player target = DbzMain.instance.getServer().getPlayer(targetName);

        if (target == null) {
            command.getSender().sendMessage(CC.translate("&cJugador no encontrado: " + targetName));
            return;
        }

        try {
            int baseTPs = Integer.parseInt(command.getArgs(1));
            GiveTpsCommandInterceptor.applyBoosterAndGiveTPs(command.getSender(), target, baseTPs);
        } catch (NumberFormatException e) {
            command.getSender().sendMessage(CC.translate("&cCantidad inválida."));
        }
    }
}
