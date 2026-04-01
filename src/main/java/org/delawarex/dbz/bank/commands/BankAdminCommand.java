package org.delawarex.dbz.bank.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.dbz.bank.menus.LoanAdminGUI;
import org.delawarex.dbz.bank.model.BankAccount;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class BankAdminCommand extends BaseCommand {

    @Command(name = "bankadmin", permission = "dbz.admin.bank")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length == 0) { new LoanAdminGUI(1).open(player); return; }

        switch (args.getArgs(0).toLowerCase()) {
            case "ranges"          -> new LoanAdminGUI(1).open(player);
            case "give"            -> handleGive(player, args);
            case "set"             -> handleSet(player, args);
            case "penalty"         -> handlePenalty(player, args);
            case "clearloans"      -> handleClearLoans(player, args);
            case "reload"          -> handleReload(player);
            default                -> sendHelp(player);
        }
    }

    private void handleGive(Player player, CommandArgs args) {
        if (args.getArgs().length < 4) { player.sendMessage(CC.translate("&cUso: /bankadmin give <jugador> <zenis|tps> <cantidad>")); return; }
        Player target = Bukkit.getPlayer(args.getArgs(1));
        if (target == null) { player.sendMessage(CC.translate("&cJugador no encontrado.")); return; }
        double amount; try { amount = Double.parseDouble(args.getArgs(3)); } catch (NumberFormatException e) { player.sendMessage(CC.translate("&cCantidad inválida.")); return; }
        BankAccount acc = BankManager.getInstance().getOrCreate(target);
        String type = args.getArgs(2).toLowerCase();
        if (type.equals("tps")) {
            acc.setTpsBalance(acc.getTpsBalance() + (long) amount);
            player.sendMessage(CC.translate("&a✓ Dado &f" + (long)amount + " TPS &aal banco de &f" + target.getName()));
        } else {
            acc.setZeniBalance(acc.getZeniBalance() + amount);
            player.sendMessage(CC.translate("&a✓ Dado &f" + String.format("%.2f", amount) + " Zenis &aal banco de &f" + target.getName()));
        }
        BankManager.getInstance().save(acc);
    }

    private void handleSet(Player player, CommandArgs args) {
        if (args.getArgs().length < 4) { player.sendMessage(CC.translate("&cUso: /bankadmin set <jugador> <zenis|tps> <cantidad>")); return; }
        Player target = Bukkit.getPlayer(args.getArgs(1));
        if (target == null) { player.sendMessage(CC.translate("&cJugador no encontrado.")); return; }
        double amount; try { amount = Math.max(0, Double.parseDouble(args.getArgs(3))); } catch (NumberFormatException e) { player.sendMessage(CC.translate("&cCantidad inválida.")); return; }
        BankAccount acc = BankManager.getInstance().getOrCreate(target);
        String type = args.getArgs(2).toLowerCase();
        if (type.equals("tps")) {
            acc.setTpsBalance((long) amount);
        } else {
            acc.setZeniBalance(amount);
        }
        BankManager.getInstance().save(acc);
        player.sendMessage(CC.translate("&a✓ Balance &f" + type + " &ade &f" + target.getName() + " &aestablecido a &f" + amount));
    }

    private void handlePenalty(Player player, CommandArgs args) {
        if (args.getArgs().length < 3) { player.sendMessage(CC.translate("&cUso: /bankadmin penalty <clear|info> <jugador>")); return; }
        Player target = Bukkit.getPlayer(args.getArgs(2));
        if (target == null) { player.sendMessage(CC.translate("&cJugador no encontrado.")); return; }
        BankAccount acc = BankManager.getInstance().getOrCreate(target);
        switch (args.getArgs(1).toLowerCase()) {
            case "clear" -> {
                BankManager.getInstance().getPenaltyManager().clearAllPenalties(acc);
                BankManager.getInstance().save(acc);
                player.sendMessage(CC.translate("&a✓ Penalizaciones de &f" + target.getName() + " &aclearadas."));
                target.sendMessage(CC.translate("&a✓ Un administrador ha limpiado tus penalizaciones."));
            }
            case "info" -> {
                player.sendMessage(CC.translate("&7Penalización TPS: &f" + (int)(acc.getTpsPenaltyRate()*100) + "%"));
                player.sendMessage(CC.translate("&7Penalización Zenis: &f" + (int)(acc.getZeniPenaltyRate()*100) + "%"));
            }
        }
    }

    private void handleClearLoans(Player player, CommandArgs args) {
        if (args.getArgs().length < 2) { player.sendMessage(CC.translate("&cUso: /bankadmin clearloans <jugador>")); return; }
        Player target = Bukkit.getPlayer(args.getArgs(1));
        if (target == null) { player.sendMessage(CC.translate("&cJugador no encontrado.")); return; }
        BankAccount acc = BankManager.getInstance().getOrCreate(target);
        acc.getLoans().clear();
        acc.setTpsPenaltyRate(0);
        acc.setZeniPenaltyRate(0);
        BankManager.getInstance().save(acc);
        player.sendMessage(CC.translate("&a✓ Todos los préstamos de &f" + target.getName() + " &aclearados."));
        target.sendMessage(CC.translate("&a✓ Un administrador ha saldado todos tus préstamos."));
    }

    private void handleReload(Player player) {
        BankManager.getInstance().getRangeManager().reload();
        player.sendMessage(CC.translate("&a✓ Rangos de préstamo recargados."));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&b&l  Admin Banco"));
        player.sendMessage(CC.translate("&e/bankadmin ranges &8- GUI rangos préstamo"));
        player.sendMessage(CC.translate("&e/bankadmin give <j> <zenis|tps> <qty>"));
        player.sendMessage(CC.translate("&e/bankadmin set <j> <zenis|tps> <qty>"));
        player.sendMessage(CC.translate("&e/bankadmin penalty clear <j>"));
        player.sendMessage(CC.translate("&e/bankadmin penalty info <j>"));
        player.sendMessage(CC.translate("&e/bankadmin clearloans <j>"));
        player.sendMessage(CC.translate("&e/bankadmin reload"));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
}
