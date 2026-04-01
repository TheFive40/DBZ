package org.delawarex.dbz.bank.commands;

import org.bukkit.entity.Player;
import org.delawarex.dbz.bank.manager.BankConfigManager;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.dbz.bank.menus.BankMainMenu;
import org.delawarex.dbz.bank.model.BankAccount;
import org.delawarex.dbz.bank.model.Loan;
import org.delawarex.dbz.bank.model.LoanRange;
import org.delawarex.dbz.bank.model.LoanType;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BankCommand extends BaseCommand {

    @Command(name = "bank", permission = "dbz.bank")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length == 0) {
            new BankMainMenu().open(player);
            return;
        }

        switch (args.getArgs(0).toLowerCase()) {
            case "deposit"  -> handleDeposit(player, args);
            case "withdraw" -> handleWithdraw(player, args);
            case "balance"  -> handleBalance(player);
            case "loan"     -> handleLoan(player, args);
            default         -> sendHelp(player);
        }
    }

    private void handleDeposit(Player player, CommandArgs args) {
        if (args.getArgs().length < 3) { player.sendMessage(CC.translate("&cUso: /bank deposit <zenis|tps> <cantidad>")); return; }
        String type = args.getArgs(1).toLowerCase();
        double amount;
        try { amount = Double.parseDouble(args.getArgs(2)); if (amount <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { player.sendMessage(CC.translate("&cCantidad inválida.")); return; }

        String result;
        if (type.equals("tps")) {
            result = BankManager.getInstance().depositTps(player, (long) amount);
        } else if (type.equals("zenis") || type.equals("zeni")) {
            result = BankManager.getInstance().depositZeni(player, amount);
        } else {
            result = "&cTipo inválido. Usa: zenis | tps";
        }
        player.sendMessage(CC.translate(result));
    }

    private void handleWithdraw(Player player, CommandArgs args) {
        if (args.getArgs().length < 3) { player.sendMessage(CC.translate("&cUso: /bank withdraw <zenis|tps> <cantidad>")); return; }
        String type = args.getArgs(1).toLowerCase();
        double amount;
        try { amount = Double.parseDouble(args.getArgs(2)); if (amount <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { player.sendMessage(CC.translate("&cCantidad inválida.")); return; }

        String result;
        if (type.equals("tps")) {
            result = BankManager.getInstance().withdrawTps(player, (long) amount);
        } else if (type.equals("zenis") || type.equals("zeni")) {
            result = BankManager.getInstance().withdrawZeni(player, amount);
        } else {
            result = "&cTipo inválido. Usa: zenis | tps";
        }
        player.sendMessage(CC.translate(result));
    }

    private void handleBalance(Player player) {
        BankManager     mgr  = BankManager.getInstance();
        BankAccount     acc  = mgr.getOrCreate(player);
        int             lvl  = mgr.getPlayerLevel(player);
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm");

        mgr.checkCapacityReset(acc);

        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&6&l  BANCO — " + player.getName()));
        player.sendMessage(CC.translate("&7  Nivel: &f" + lvl));
        player.sendMessage(CC.translate("&7  Zenis en banco: &f" + String.format("%.2f", acc.getZeniBalance())));
        player.sendMessage(CC.translate("&7  TPS en banco:   &f" + acc.getTpsBalance()));

        if (acc.hasTpsPenalty())  player.sendMessage(CC.translate("&c  ⚠ Penalización TPS activa: &f" + (int)(acc.getTpsPenaltyRate()*100) + "%"));
        if (acc.hasZeniPenalty()) player.sendMessage(CC.translate("&c  ⚠ Penalización Zenis activa: &f" + (int)(acc.getZeniPenaltyRate()*100) + "%"));

        Optional<LoanRange> range = mgr.getRangeManager().getRangeForLevel(lvl);
        range.ifPresent(r -> {
            long availTps   = mgr.getAvailableTpsCapacity(acc, r);
            double availZeni = mgr.getAvailableZeniCapacity(acc, r);
            long resetDays  = BankConfigManager.getInstance().getCapacityResetDays();
            player.sendMessage(CC.translate("&7  Rango nivel &f" + r.getMinLevel() + "-" + r.getMaxLevel() + "&7:"));
            player.sendMessage(CC.translate("&7    Cap. TPS disp.: &f" + availTps + "&7/&f" + r.getMaxTPS()));
            player.sendMessage(CC.translate("&7    Cap. Zenis disp.: &f" + String.format("%.0f", availZeni) + "&7/&f" + String.format("%.0f", r.getMaxZenis())));
            if (acc.getLastCapacityReset() > 0) {
                long resetAt = acc.getLastCapacityReset() + (resetDays * 86400000L);
                player.sendMessage(CC.translate("&7    Próx. reset capacidad: &f" + fmt.format(new Date(resetAt))));
            }
        });

        List<Loan> loans = acc.getActiveLoans();
        if (!loans.isEmpty()) {
            player.sendMessage(CC.translate("&7  Préstamos activos: &f" + loans.size()));
            for (Loan l : loans) {
                String overdueStr = l.isOverdue() ? " &c[VENCIDO]" : "";
                String nextPay = fmt.format(new Date(l.getNextPaymentTime()));
                player.sendMessage(CC.translate("  &8● &f" + l.getType().display()
                        + " &7Restante: &f" + String.format("%.0f", l.getRemainingAmount())
                        + " &7| Cuotas: &f" + l.getPaidInstallments() + "/" + l.getTotalInstallments()
                        + " &7| Próxima: &f" + nextPay + overdueStr));
                player.sendMessage(CC.translate("    &8ID: &7" + l.getId().substring(0, 8) + "..."));
            }
        }
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleLoan(Player player, CommandArgs args) {
        if (args.getArgs().length < 2) { sendLoanHelp(player); return; }
        switch (args.getArgs(1).toLowerCase()) {
            case "request" -> {
                if (args.getArgs().length < 4) { player.sendMessage(CC.translate("&cUso: /bank loan request <tps|zenis> <cantidad>")); return; }
                LoanType type = parseLoanType(args.getArgs(2));
                if (type == null) { player.sendMessage(CC.translate("&cTipo inválido. Usa: tps | zenis")); return; }
                double amount;
                try { amount = Double.parseDouble(args.getArgs(3)); if (amount <= 0) throw new NumberFormatException(); }
                catch (NumberFormatException e) { player.sendMessage(CC.translate("&cCantidad inválida.")); return; }
                player.sendMessage(CC.translate(BankManager.getInstance().requestLoan(player, type, amount)));
            }
            case "pay" -> {
                if (args.getArgs().length < 4) { player.sendMessage(CC.translate("&cUso: /bank loan pay <id> <cantidad>")); return; }
                double amount;
                try { amount = Double.parseDouble(args.getArgs(3)); if (amount <= 0) throw new NumberFormatException(); }
                catch (NumberFormatException e) { player.sendMessage(CC.translate("&cCantidad inválida.")); return; }
                player.sendMessage(CC.translate(BankManager.getInstance().payLoan(player, args.getArgs(2), amount)));
            }
            case "list" -> handleBalance(player);
            default -> sendLoanHelp(player);
        }
    }

    private LoanType parseLoanType(String s) {
        if (s.equalsIgnoreCase("tps"))               return LoanType.TPS;
        if (s.equalsIgnoreCase("zenis") || s.equalsIgnoreCase("zeni")) return LoanType.ZENIS;
        return null;
    }

    private void sendLoanHelp(Player player) {
        player.sendMessage(CC.translate("&7/bank loan request <tps|zenis> <cantidad>"));
        player.sendMessage(CC.translate("&7/bank loan pay <id> <cantidad>"));
        player.sendMessage(CC.translate("&7/bank loan list"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&6&l  BANCO — Comandos"));
        player.sendMessage(CC.translate("&f  /bank &8- GUI principal"));
        player.sendMessage(CC.translate("&f  /bank balance &8- Ver saldo y préstamos"));
        player.sendMessage(CC.translate("&f  /bank deposit <zenis|tps> <cantidad>"));
        player.sendMessage(CC.translate("&f  /bank withdraw <zenis|tps> <cantidad>"));
        player.sendMessage(CC.translate("&f  /bank loan request <tps|zenis> <cantidad>"));
        player.sendMessage(CC.translate("&f  /bank loan pay <id> <cantidad>"));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }
}