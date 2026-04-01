package org.delawarex.dbz.market.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.dbz.bank.model.BankAccount;
import org.delawarex.dbz.market.ShopManager;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;

public class PayCommand extends BaseCommand {

    @Command(name = "pay", permission = "dbz.shop")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length < 2) {
            player.sendMessage(CC.translate("&cUso: /pay <jugador> <cantidad>"));
            return;
        }

        Player target = Bukkit.getPlayer(args.getArgs(0));
        if (target == null) {
            player.sendMessage(CC.translate("&cJugador no encontrado."));
            return;
        }
        if (target.equals(player)) {
            player.sendMessage(CC.translate("&cNo puedes pagarte a ti mismo."));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args.getArgs(1));
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cCantidad inválida."));
            return;
        }

        ShopManager mgr = ShopManager.getInstance();
        String sym = mgr.getConfig().currencySymbol;

        double penaltyAmount = 0.0;
        BankAccount senderAcc = null;
        try {
            senderAcc = BankManager.getInstance().getAccount(player.getUniqueId());
            if (senderAcc != null && senderAcc.hasZeniPenalty()) {
                penaltyAmount = amount * senderAcc.getZeniPenaltyRate();
            }
        } catch (Exception ignored) {}

        double totalCharge = amount + penaltyAmount;

        if (mgr.getEconomy().getBalance(player) < totalCharge) {
            player.sendMessage(CC.translate("&cFondos insuficientes."));
            return;
        }

        if (!mgr.getEconomy().transfer(player, target, amount)) {
            player.sendMessage(CC.translate("&cFondos insuficientes."));
            return;
        }

        if (penaltyAmount > 0 && senderAcc != null) {
            try {
                mgr.getEconomy().withdraw(player, penaltyAmount);
                boolean anyPaid = BankManager.getInstance().getPenaltyManager().applyZeniDebtPayment(senderAcc, penaltyAmount);
                BankManager.getInstance().save(senderAcc);
                player.sendMessage(CC.translate("&c⚠ Penalización: &f" + String.format("%.2f", penaltyAmount) + " " + sym + " &cdescontados para tu deuda bancaria."));
                if (anyPaid) BankManager.getInstance().notifyLoanPaidOff(senderAcc, org.delawarex.dbz.bank.model.LoanType.ZENIS);
            } catch (Exception ignored) {}
        }

        player.sendMessage(CC.translate("&a✓ Enviaste &f" + String.format("%.2f", amount) + " " + sym + " &aa &f" + target.getName()));
        target.sendMessage(CC.translate("&a✓ Recibiste &f" + String.format("%.2f", amount) + " " + sym + " &ade &f" + player.getName()));
    }
}
