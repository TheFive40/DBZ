package org.delawarex.dbz.bank.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.bank.model.BankAccount;
import org.delawarex.dbz.bank.model.Loan;
import org.delawarex.dbz.bank.model.LoanType;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.UUID;

public class PenaltyManager {

    private final BankManager manager;

    public PenaltyManager(BankManager manager) {
        this.manager = manager;
    }

    public long applyTpsPenalty(Player player, long tpsGained) {
        BankAccount acc = manager.getAccount(player.getUniqueId());
        if (acc == null || !acc.hasTpsPenalty()) return tpsGained;

        double rate    = acc.getTpsPenaltyRate();
        long   penalty = (long) Math.ceil(tpsGained * rate);
        long   received = Math.max(0, tpsGained - penalty);

        boolean anyPaid = applyTpsDebtPayment(acc, penalty);
        manager.save(acc);

        player.sendMessage(CC.translate("&c⚠ Penalización activa: &f-" + penalty + " TPS &8(" + (int)(rate*100) + "%) &caplicados a tu deuda."));

        if (anyPaid) {
            manager.notifyLoanPaidOff(acc, LoanType.TPS);
        }

        return received;
    }

    public double applyZeniPenalty(Player player, double amount) {
        BankAccount acc = manager.getAccount(player.getUniqueId());
        if (acc == null || !acc.hasZeniPenalty()) return 0.0;

        double rate    = acc.getZeniPenaltyRate();
        double penalty = amount * rate;

        boolean anyPaid = applyZeniDebtPayment(acc, penalty);
        manager.save(acc);

        player.sendMessage(CC.translate("&c⚠ Penalización activa: &f" + String.format("%.2f", penalty) + " Zenis &8(" + (int)(rate*100) + "%) &caplicados a tu deuda."));

        if (anyPaid) {
            manager.notifyLoanPaidOff(acc, LoanType.ZENIS);
        }

        return penalty;
    }

    public boolean applyTpsDebtPayment(BankAccount acc, long amount) {
        long remaining = amount;
        boolean anyFullyPaid = false;

        for (Loan loan : new ArrayList<>(acc.getLoans())) {
            if (loan.getType() != LoanType.TPS || !loan.isOverdue()) continue;
            double needed = loan.getTotalDueInstallment();
            if (needed <= 0) needed = loan.getInstallmentAmount();

            if (remaining >= (long) Math.ceil(needed)) {
                loan.advancePayment(loan.getInstallmentAmount());
                remaining -= (long) Math.ceil(needed);
                if (loan.isFullyPaid()) {
                    acc.getLoans().remove(loan);
                    anyFullyPaid = true;
                }
            } else {
                loan.setPaidAmount(loan.getPaidAmount() + remaining);
                remaining = 0;
            }
            if (remaining <= 0) break;
        }

        acc.recalcPenalties();
        return anyFullyPaid;
    }

    public boolean applyZeniDebtPayment(BankAccount acc, double amount) {
        double remaining = amount;
        boolean anyFullyPaid = false;

        for (Loan loan : new ArrayList<>(acc.getLoans())) {
            if (loan.getType() != LoanType.ZENIS || !loan.isOverdue()) continue;
            double needed = loan.getTotalDueInstallment();
            if (needed <= 0) needed = loan.getInstallmentAmount();

            if (remaining >= needed) {
                loan.advancePayment(loan.getInstallmentAmount());
                remaining -= needed;
                if (loan.isFullyPaid()) {
                    acc.getLoans().remove(loan);
                    anyFullyPaid = true;
                }
            } else {
                loan.setPaidAmount(loan.getPaidAmount() + remaining);
                remaining = 0;
            }
            if (remaining <= 0) break;
        }

        acc.recalcPenalties();
        return anyFullyPaid;
    }

    public void activatePenalty(BankAccount acc, Loan loan) {
        loan.setOverdue(true);
        acc.recalcPenalties();
    }

    public void clearAllPenalties(BankAccount acc) {
        acc.getLoans().forEach(l -> {
            l.setOverdue(false);
            l.setNotifiedOverdue(false);
            l.setOverdueInterest(0.0);
        });
        acc.setTpsPenaltyRate(0);
        acc.setZeniPenaltyRate(0);
    }
}
