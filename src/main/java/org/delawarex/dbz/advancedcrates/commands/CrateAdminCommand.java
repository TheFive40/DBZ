package org.delawarex.dbz.advancedcrates.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.advancedcrates.managers.CrateManager;
import org.delawarex.dbz.advancedcrates.menus.CrateAdminMenu;
import org.delawarex.dbz.advancedcrates.models.Crate;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class CrateAdminCommand extends BaseCommand {


    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&c&l  AdvancedCrates — Admin"));
        player.sendMessage(CC.translate("&e/crateadmin &7- Abrir menú de administración"));
        player.sendMessage(CC.translate("&e/crateadmin givekey <jugador> <crateId> <cantidad>"));
        player.sendMessage(CC.translate("&e/crateadmin reload &7- Recargar crates"));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    @Command(name = "crateadmin", aliases = {"crateadmin", "cadm"}, permission = "advancedcrates.admin")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        String[] args = command.getArgs();
        Player player = command.getPlayer();
        if (args.length == 0) {
            new CrateAdminMenu(1).open(player);
        }

        CrateManager mgr = DbzMain.get().getCrateManager();

        switch (args[0].toLowerCase()) {
            case "givekey" -> {
                if (!player.hasPermission("advancedcrates.givekey")) {
                    player.sendMessage(CC.translate("&c\u2717 Sin permiso para dar llaves."));
                }
                if (args.length < 4) {
                    player.sendMessage(CC.translate("&cUso: /crateadmin givekey <jugador> <crateId> <cantidad>"));
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(CC.translate("&c\u2717 Jugador no encontrado: &f" + args[1]));
                }
                Crate crate = mgr.getCrate(args[2]);
                if (crate == null) {
                    player.sendMessage(CC.translate("&c\u2717 Crate no encontrada: &f" + args[2]));
                }
                int amount = 0;
                try {
                    amount = Integer.parseInt(args[3]);
                    if (amount < 1) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    player.sendMessage(CC.translate("&c\u2717 Cantidad inválida."));
                }
                for (int i = 0; i < amount; i++) {
                    if (target.getInventory().firstEmpty() == -1)
                        target.getWorld().dropItem(target.getLocation(), mgr.buildKeyItem(crate));
                    else
                        target.getInventory().addItem(mgr.buildKeyItem(crate));
                }
                player.sendMessage(CC.translate("&a\u2713 Dadas &f" + amount + " &allave(s) de &f"
                        + CC.strip(crate.getDisplayName()) + " &aa &f" + target.getName()));
                target.sendMessage(CC.translate("&a\u2713 Recibiste &f" + amount + " &allave(s) de &f"
                        + CC.strip(crate.getDisplayName())));
            }

            case "reload" -> {
                mgr.reload();
                player.sendMessage(CC.translate("&a\u2713 Plugin recargado."));
            }

            case "help" -> sendHelp(player);

            default -> {
                player.sendMessage(CC.translate("&c\u2717 Subcomando desconocido: &f" + args[0]));
                sendHelp(player);
            }
        }
    }
}
