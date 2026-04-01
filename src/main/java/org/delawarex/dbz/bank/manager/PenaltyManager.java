package org.delawarex.dbz.bank.manager;

import org.bukkit.entity.Player;
import org.delawarex.dbz.bank.model.BankAccount;
import org.delawarex.dbz.bank.model.Loan;
import org.delawarex.dbz.bank.model.LoanType;
import org.delawarex.service.CC;

public class PenaltyManager {

    private final BankManager manager;

    public PenaltyManager(BankManager manager) {
        this.manager = manager;
    }

    public long applyTpsPenalty(Player player, long tpsGained) {
        BankAccount acc = manager.getAccount(player.getUniqueId());
        if (acc == null || !acc.hasTpsPenalty()) return tpsGained;

        double rate     = acc.getTpsPenaltyRate();
        long   penalty  = (long) Math.ceil(tpsGained * rate);
        long   received = tpsGained - penalty;

        applyTpsDebtPayment(acc, penalty);
        manager.save(acc);

        player.sendMessage(CC.translate("&c⚠ Penalización activa: &f-" + penalty + " TPS &8(" + (int)(rate*100) + "%) &capplicados a tu deuda."));
        return Math.max(0, received);
    }

    public double applyZeniPenalty(Player player, double zenisSpent) {
        BankAccount acc = manager.getAccount(player.getUniqueId());
        if (acc == null || !acc.hasZeniPenalty()) return zenisSpent;

        double rate    = acc.getZeniPenaltyRate();
        double extra   = zenisSpent * rate;
        double total   = zenisSpent + extra;

        applyZeniDebtPayment(acc, extra);
        manager.save(acc);

        player.sendMessage(CC.translate("&c⚠ Penalización activa: &f+" + String.format("%.2f", extra) + " Zenis &8(" + (int)(rate*100) + "%) &caplicados a tu deuda."));
        return total;
    }

    private void applyTpsDebtPayment(BankAccount acc, long amount) {
        long remaining = amount;
        for (Loan loan : acc.getLoans()) {
            if (loan.getType() != LoanType.TPS || !loan.isOverdue()) continue;
            double needed = loan.getInstallmentAmount() - (loan.getPaidAmount() % loan.getInstallmentAmount());
            if (needed <= 0) needed = loan.getInstallmentAmount();
            if (remaining >= needed) {
                loan.advancePayment(needed);
                remaining -= (long) needed;
                if (loan.isFullyPaid()) {
                    acc.getLoans().remove(loan);
                    break;
                }
            } else {
                loan.setPaidAmount(loan.getPaidAmount() + remaining);
                remaining = 0;
            }
            if (remaining <= 0) break;
        }
        acc.recalcPenalties();
    }

    private void applyZeniDebtPayment(BankAccount acc, double amount) {
        double remaining = amount;
        for (Loan loan : acc.getLoans()) {
            if (loan.getType() != LoanType.ZENIS || !loan.isOverdue()) continue;
            double needed = loan.getInstallmentAmount() - (loan.getPaidAmount() % loan.getInstallmentAmount());
            if (needed <= 0) needed = loan.getInstallmentAmount();
            if (remaining >= needed) {
                loan.advancePayment(needed);
                remaining -= needed;
                if (loan.isFullyPaid()) {
                    acc.getLoans().remove(loan);
                    break;
                }
            } else {
                loan.setPaidAmount(loan.getPaidAmount() + remaining);
                remaining = 0;
            }
            if (remaining <= 0) break;
        }
        acc.recalcPenalties();
    }

    public void activatePenalty(BankAccount acc, Loan loan) {
        loan.setOverdue(true);
        acc.recalcPenalties();
    }

    public void clearAllPenalties(BankAccount acc) {
        acc.getLoans().forEach(l -> l.setOverdue(false));
        acc.setTpsPenaltyRate(0);
        acc.setZeniPenaltyRate(0);
    }
}
