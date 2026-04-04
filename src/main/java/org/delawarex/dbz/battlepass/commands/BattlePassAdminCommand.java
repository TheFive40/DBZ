package org.delawarex.dbz.battlepass.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.battlepass.manager.BattlePassManager;
import org.delawarex.dbz.battlepass.menus.BattlePassAdminMenu;
import org.delawarex.dbz.battlepass.models.BattlePass;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class BattlePassAdminCommand extends BaseCommand {

    @Command(name = "bpadmin", permission = "dbz.admin.battlepass")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length == 0) {
            new BattlePassAdminMenu(1).open(player);
            return;
        }

        BattlePassManager mgr = BattlePassManager.getInstance();

        switch (args.getArgs(0).toLowerCase()) {
            case "menu" -> new BattlePassAdminMenu(1).open(player);
            case "reload" -> {
                mgr.reload();
                player.sendMessage(CC.translate("&a✓ Pases de batalla recargados."));
            }
            case "give" -> {
                if (args.getArgs().length < 4) {
                    player.sendMessage(CC.translate("&cUso: /bpadmin give <jugador> <passId> <puntos>"));
                    return;
                }
                Player target = Bukkit.getPlayer(args.getArgs(1));
                if (target == null) { player.sendMessage(CC.translate("&cJugador no encontrado.")); return; }
                BattlePass pass = mgr.getPass(args.getArgs(2));
                if (pass == null) { player.sendMessage(CC.translate("&cPase no encontrado: &f" + args.getArgs(2))); return; }
                try {
                    int pts = Integer.parseInt(args.getArgs(3));
                    if (pts < 0) throw new NumberFormatException();
                    mgr.addPoints(target, pass.getId(), pts);
                    player.sendMessage(CC.translate("&a✓ Dados &f" + pts + " &apuntos del pase &f"
                            + CC.strip(pass.getDisplayName()) + " &aa &f" + target.getName()));
                    target.sendMessage(CC.translate("&a✓ Recibiste &f" + pts + " &apuntos en el pase &f"
                            + CC.strip(pass.getDisplayName())));
                } catch (NumberFormatException e) { player.sendMessage(CC.translate("&cCantidad inválida.")); }
            }
            case "set" -> {
                if (args.getArgs().length < 4) {
                    player.sendMessage(CC.translate("&cUso: /bpadmin set <jugador> <passId> <puntos>"));
                    return;
                }
                Player target = Bukkit.getPlayer(args.getArgs(1));
                if (target == null) { player.sendMessage(CC.translate("&cJugador no encontrado.")); return; }
                BattlePass pass = mgr.getPass(args.getArgs(2));
                if (pass == null) { player.sendMessage(CC.translate("&cPase no encontrado: &f" + args.getArgs(2))); return; }
                try {
                    int pts = Integer.parseInt(args.getArgs(3));
                    if (pts < 0) throw new NumberFormatException();
                    mgr.setPoints(target, pass.getId(), pts);
                    player.sendMessage(CC.translate("&a✓ Puntos de &f" + target.getName()
                            + " &aen &f" + CC.strip(pass.getDisplayName()) + " &afijados a &f" + pts));
                } catch (NumberFormatException e) { player.sendMessage(CC.translate("&cCantidad inválida.")); }
            }
            case "take" -> {
                if (args.getArgs().length < 4) {
                    player.sendMessage(CC.translate("&cUso: /bpadmin take <jugador> <passId> <puntos>"));
                    return;
                }
                Player target = Bukkit.getPlayer(args.getArgs(1));
                if (target == null) { player.sendMessage(CC.translate("&cJugador no encontrado.")); return; }
                BattlePass pass = mgr.getPass(args.getArgs(2));
                if (pass == null) { player.sendMessage(CC.translate("&cPase no encontrado: &f" + args.getArgs(2))); return; }
                try {
                    int pts = Integer.parseInt(args.getArgs(3));
                    if (pts < 0) throw new NumberFormatException();
                    mgr.takePoints(target, pass.getId(), pts);
                    player.sendMessage(CC.translate("&a✓ Quitados &f" + pts + " &apuntos del pase &f"
                            + CC.strip(pass.getDisplayName()) + " &aa &f" + target.getName()));
                } catch (NumberFormatException e) { player.sendMessage(CC.translate("&cCantidad inválida.")); }
            }
            case "list" -> {
                player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                player.sendMessage(CC.translate("&6&l  Pases de Batalla"));
                for (BattlePass p : mgr.getAllPasses()) {
                    player.sendMessage(CC.translate("&7● &f" + p.getId()
                            + " &8| " + p.getDisplayName()
                            + " &8| &7Niveles: &f" + p.getLevels().size()
                            + " &8| " + (p.isEnabled() ? "&aActivo" : "&cDesactivado")
                            + (p.getPermission().isEmpty() ? "" : " &8| &7Perm: &f" + p.getPermission())));
                }
                player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            }
            default -> sendHelp(player);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&6&l  Admin Pase de Batalla"));
        player.sendMessage(CC.translate("&e/bpadmin &7- Abrir menú de administración"));
        player.sendMessage(CC.translate("&e/bpadmin give <j> <passId> <pts> &7- Dar puntos"));
        player.sendMessage(CC.translate("&e/bpadmin set <j> <passId> <pts> &7- Fijar puntos"));
        player.sendMessage(CC.translate("&e/bpadmin take <j> <passId> <pts> &7- Quitar puntos"));
        player.sendMessage(CC.translate("&e/bpadmin list &7- Listar todos los pases"));
        player.sendMessage(CC.translate("&e/bpadmin reload &7- Recargar pases"));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
}