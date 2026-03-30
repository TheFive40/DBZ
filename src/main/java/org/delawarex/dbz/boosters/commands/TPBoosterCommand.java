package org.delawarex.dbz.boosters.commands;

import org.bukkit.entity.Player;
import org.delawarex.dbz.boosters.integration.BoosterTPAPI;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class TPBoosterCommand extends BaseCommand {

    @Command(name = "tpbooster", aliases = {"tpboost"}, permission = "dbz.admin.tpbooster")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        if (!command.isPlayer()) {
            command.getSender().sendMessage(CC.translate("&cDebes ser un jugador"));
            return;
        }

        Player player = command.getPlayer();

        if (command.length() < 1) {
            sendHelp(player);
            return;
        }

        switch (command.getArgs(0).toLowerCase()) {
            case "give" -> handleGive(command);
            case "test" -> handleTest(command);
            case "info" -> handleInfo(command);
            case "calculate" -> handleCalculate(command);
            default -> {
                player.sendMessage(CC.translate("&cAcción desconocida: " + command.getArgs(0)));
                sendHelp(player);
            }
        }
    }

    private void handleGive(CommandArgs command) {
        Player player = command.getPlayer();
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&cUso: /tpbooster give <cantidad>"));
            return;
        }
        try {
            int amount = Integer.parseInt(command.getArgs(1));
            if (amount <= 0) {
                player.sendMessage(CC.translate("&cLa cantidad debe ser mayor a 0"));
                return;
            }
            BoosterTPAPI.giveTPsWithBooster(player, amount);
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cCantidad inválida"));
        }
    }

    private void handleTest(CommandArgs command) {
        Player player = command.getPlayer();
        player.sendMessage(CC.translate("&6=== Test de Boosters de TPs ==="));
        player.sendMessage("");
        int[] testAmounts = {10, 50, 100, 500, 1000};
        for (int amount : testAmounts) {
            int boosted = BoosterTPAPI.calculateBoostedTPs(player, amount);
            int bonus = boosted - amount;
            double mult = BoosterTPAPI.getCombinedMultiplier(player);
            player.sendMessage(CC.translate(String.format(
                    "  &e%d TPs &7→ &a%d TPs &7(+%d) &6[x%.2f]", amount, boosted, bonus, mult)));
        }
        player.sendMessage("");
    }

    private void handleInfo(CommandArgs command) {
        Player player = command.getPlayer();
        player.sendMessage(CC.translate("&6=== Info de Boosters de TPs ==="));
        player.sendMessage("");
        double multiplier = BoosterTPAPI.getCombinedMultiplier(player);
        boolean hasBooster = BoosterTPAPI.hasActiveBooster(player);
        player.sendMessage(CC.translate("  &eEstado: " + (hasBooster ? "&aActivo" : "&cInactivo")));
        player.sendMessage(CC.translate("  &eMultiplicador: &6x" + String.format("%.2f", multiplier)));
        String percent = String.format("%.0f%%", (multiplier - 1.0) * 100);
        player.sendMessage(CC.translate("  &eBonus: &a+" + percent));
        player.sendMessage("");
    }

    private void handleCalculate(CommandArgs command) {
        Player player = command.getPlayer();
        if (command.length() < 2) {
            player.sendMessage(CC.translate("&cUso: /tpbooster calculate <cantidad>"));
            return;
        }
        try {
            int amount = Integer.parseInt(command.getArgs(1));
            if (amount <= 0) {
                player.sendMessage(CC.translate("&cLa cantidad debe ser mayor a 0"));
                return;
            }
            int boosted = BoosterTPAPI.calculateBoostedTPs(player, amount);
            int bonus = boosted - amount;
            double mult = BoosterTPAPI.getCombinedMultiplier(player);
            player.sendMessage(CC.translate("&6=== Cálculo de Boosters ==="));
            player.sendMessage("");
            player.sendMessage(CC.translate("  &eTPs Base: &a" + amount));
            player.sendMessage(CC.translate("  &eBonus: &6+" + bonus));
            player.sendMessage(CC.translate("  &eTotal: &b" + boosted + " TPs"));
            player.sendMessage(CC.translate("  &eMultiplicador: &6x" + String.format("%.2f", mult)));
            player.sendMessage("");
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cCantidad inválida"));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&6=== Boosters de TPs ==="));
        player.sendMessage("");
        player.sendMessage(CC.translate("&a/tpbooster give <cantidad> &7- Da TPs con booster"));
        player.sendMessage(CC.translate("&a/tpbooster test &7- Muestra ejemplos de boosters"));
        player.sendMessage(CC.translate("&a/tpbooster info &7- Info de boosters activos"));
        player.sendMessage(CC.translate("&a/tpbooster calculate <cantidad> &7- Calcula TPs con booster"));
        player.sendMessage("");
    }
}
