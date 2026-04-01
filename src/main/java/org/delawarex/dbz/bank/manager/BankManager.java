package org.delawarex.dbz.bank.manager;

import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.bank.model.*;
import org.delawarex.dbz.bank.storage.BankDataManager;
import org.delawarex.service.CC;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class BankManager {

    private static BankManager instance;

    private final BankDataManager    data;
    private final LoanRangeManager rangeManager;
    private final PenaltyManager penaltyManager;

    private final Logger log = DbzMain.instance.getLogger();

    private BankManager() {
        data           = new BankDataManager();
        rangeManager   = new LoanRangeManager();
        penaltyManager = new PenaltyManager(this);
        data.loadAll();
    }

    public static BankManager getInstance() {
        if (instance == null) instance = new BankManager();
        return instance;
    }

    public static void reset() { instance = null; }

    public BankAccount getAccount(UUID uuid) {
        return data.getCached(uuid);
    }

    public BankAccount getOrCreate(Player player) {
        return data.loadOrCreate(player.getUniqueId(), player.getName());
    }

    public void save(BankAccount acc) {
        data.save(acc);
    }

    public String depositZeni(Player player, double amount) {
        if (amount <= 0) return "&cCantidad inválida.";
        BankAccount acc = getOrCreate(player);
        if (!withdrawVaultZeni(player, amount)) return "&cFondos insuficientes en tu billetera.";
        acc.setZeniBalance(acc.getZeniBalance() + amount);
        save(acc);
        return "&a✓ Depositaste &f" + String.format("%.2f", amount) + " Zenis &aal banco. Saldo banco: &f" + String.format("%.2f", acc.getZeniBalance());
    }

    public String withdrawZeni(Player player, double amount) {
        if (amount <= 0) return "&cCantidad inválida.";
        BankAccount acc = getOrCreate(player);
        if (acc.getZeniBalance() < amount) return "&cFondos insuficientes en el banco.";
        acc.setZeniBalance(acc.getZeniBalance() - amount);
        depositVaultZeni(player, amount);
        save(acc);
        return "&a✓ Retiraste &f" + String.format("%.2f", amount) + " Zenis &adel banco.";
    }

    public String depositTps(Player player, long amount) {
        if (amount <= 0) return "&cCantidad inválida.";
        long current = getPlayerTPS(player);
        if (current < amount) return "&cNo tienes suficientes TPS. Tienes: &f" + current;
        addPlayerTPS(player, -amount);
        BankAccount acc = getOrCreate(player);
        acc.setTpsBalance(acc.getTpsBalance() + amount);
        save(acc);
        return "&a✓ Depositaste &f" + amount + " TPS &aal banco. Saldo banco: &f" + acc.getTpsBalance();
    }

    public String withdrawTps(Player player, long amount) {
        if (amount <= 0) return "&cCantidad inválida.";
        BankAccount acc = getOrCreate(player);
        if (acc.getTpsBalance() < amount) return "&cFondos insuficientes de TPS en el banco.";
        acc.setTpsBalance(acc.getTpsBalance() - amount);
        addPlayerTPS(player, amount);
        save(acc);
        return "&a✓ Retiraste &f" + amount + " TPS &adel banco.";
    }

    public String requestLoan(Player player, LoanType type, double amount) {
        if (amount <= 0) return "&cCantidad inválida.";
        int level = getPlayerLevel(player);
        Optional<LoanRange> optRange = rangeManager.getRangeForLevel(level);
        if (optRange.isEmpty()) return "&cTu nivel (" + level + ") no tiene un rango de préstamo configurado.";
        LoanRange range = optRange.get();

        BankAccount acc = getOrCreate(player);
        long activeLoanCount = acc.getLoans().stream().filter(l -> l.getType() == type && !l.isFullyPaid()).count();
        if (activeLoanCount >= 3) return "&cYa tienes demasiados préstamos activos de tipo " + type.display() + ".";

        if (type == LoanType.TPS) {
            if (amount > range.getMaxTPS()) return "&cSegún tu nivel (&f" + level + "&c), puedes solicitar hasta &f" + range.getMaxTPS() + " TPS.";
            acc.setTpsBalance(acc.getTpsBalance() + (long) amount);
            addPlayerTPS(player, (long) amount);
        } else {
            if (amount > range.getMaxZenis()) return "&cSegún tu nivel (&f" + level + "&c), puedes solicitar hasta &f" + String.format("%.2f", range.getMaxZenis()) + " Zenis.";
            acc.setZeniBalance(acc.getZeniBalance() + amount);
            depositVaultZeni(player, amount);
        }

        Loan loan = new Loan(type, amount, range.getInterestRate(),
                range.getInstallmentCount(), range.getInstallmentIntervalHours(), range.getPenaltyRate());
        acc.getLoans().add(loan);
        save(acc);

        double total = loan.getTotalWithInterest();
        return "&a✓ Préstamo aprobado: &f" + String.format("%.0f", amount) + " " + type.display()
                + " &a| Total con interés: &f" + String.format("%.0f", total)
                + " &a| Cuotas: &f" + range.getInstallmentCount()
                + " &ade &f" + String.format("%.0f", loan.getInstallmentAmount())
                + " " + type.display() + " &acada " + range.getInstallmentIntervalHours() + "h.";
    }

    public String payLoan(Player player, String loanId, double amount) {
        if (amount <= 0) return "&cCantidad inválida.";
        BankAccount acc = getOrCreate(player);
        Loan loan = acc.getLoans().stream().filter(l -> l.getId().equals(loanId)).findFirst().orElse(null);
        if (loan == null) return "&cPréstamo no encontrado: &f" + loanId;
        if (loan.isFullyPaid()) return "&cEste préstamo ya está pagado.";

        if (loan.getType() == LoanType.TPS) {
            long tpsPay = (long) amount;
            if (acc.getTpsBalance() < tpsPay) return "&cNo tienes suficientes TPS en el banco para pagar.";
            acc.setTpsBalance(acc.getTpsBalance() - tpsPay);
        } else {
            if (acc.getZeniBalance() < amount) return "&cNo tienes suficientes Zenis en el banco para pagar.";
            acc.setZeniBalance(acc.getZeniBalance() - amount);
        }

        loan.setPaidAmount(loan.getPaidAmount() + amount);
        loan.setPaidInstallments(loan.getPaidInstallments() + 1);
        loan.setNextPaymentTime(System.currentTimeMillis() + loan.getIntervalMillis());
        if (loan.isOverdue()) loan.setOverdue(false);
        acc.recalcPenalties();

        if (loan.isFullyPaid()) {
            acc.getLoans().remove(loan);
            save(acc);
            return "&a✓ ¡Préstamo &f" + loanId.substring(0, 8) + "... &apagado completamente!";
        }
        save(acc);
        return "&a✓ Pago de &f" + String.format("%.0f", amount) + " " + loan.getType().display()
                + " &aregistrado. Cuotas restantes: &f" + loan.getRemainingInstallments();
    }

    public void processScheduledPayments() {
        for (BankAccount acc : data.getAllCached()) {
            boolean changed = false;
            for (Loan loan : new ArrayList<>(acc.getLoans())) {
                if (loan.isFullyPaid() || !loan.isDueNow()) continue;
                changed = true;
                if (loan.getType() == LoanType.TPS) {
                    long needed = (long) loan.getInstallmentAmount();
                    if (acc.getTpsBalance() >= needed) {
                        acc.setTpsBalance(acc.getTpsBalance() - needed);
                        loan.advancePayment(needed);
                        notifyPlayer(acc, "&a✓ Cuota automática de &f" + needed + " TPS &apagada. Restantes: &f" + loan.getRemainingInstallments());
                    } else {
                        penaltyManager.activatePenalty(acc, loan);
                        notifyPlayer(acc, "&c⚠ No pudiste pagar tu cuota de &f" + needed + " TPS&c. ¡Penalización activa!");
                    }
                } else {
                    double needed = loan.getInstallmentAmount();
                    if (acc.getZeniBalance() >= needed) {
                        acc.setZeniBalance(acc.getZeniBalance() - needed);
                        loan.advancePayment(needed);
                        notifyPlayer(acc, "&a✓ Cuota automática de &f" + String.format("%.2f", needed) + " Zenis &apagada. Restantes: &f" + loan.getRemainingInstallments());
                    } else {
                        penaltyManager.activatePenalty(acc, loan);
                        notifyPlayer(acc, "&c⚠ No pudiste pagar tu cuota de &f" + String.format("%.2f", needed) + " Zenis&c. ¡Penalización activa!");
                    }
                }
                if (loan.isFullyPaid()) acc.getLoans().remove(loan);
            }
            if (changed) save(acc);
        }
    }

    private void notifyPlayer(BankAccount acc, String msg) {
        Player p = Bukkit.getPlayer(UUID.fromString(acc.getUuid()));
        if (p != null) p.sendMessage(CC.translate(msg));
    }

    public long applyTpsPenalty(Player player, long tpsGained) {
        return penaltyManager.applyTpsPenalty(player, tpsGained);
    }

    public double applyZeniPenalty(Player player, double zeniSpent) {
        return penaltyManager.applyZeniPenalty(player, zeniSpent);
    }

    public boolean hasTpsPenalty(Player player) {
        BankAccount acc = getAccount(player.getUniqueId());
        return acc != null && acc.hasTpsPenalty();
    }

    public boolean hasZeniPenalty(Player player) {
        BankAccount acc = getAccount(player.getUniqueId());
        return acc != null && acc.hasZeniPenalty();
    }

    public int getPlayerLevel(Player player) {
        AtomicInteger lvl = new AtomicInteger(1);
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle())
                .ifPresent(stats -> lvl.set(stats.getLevel()));
        return lvl.get();
    }

    public long getPlayerTPS(Player player) {
        AtomicLong tps = new AtomicLong(0);
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle())
                .ifPresent(stats -> tps.set(stats.getResources().getTrainingPoints()));
        return tps.get();
    }

    public void addPlayerTPS(Player player, long amount) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle())
                .ifPresent(stats -> stats.getResources().addTrainingPoints((int) amount));
    }

    private void depositVaultZeni(Player player, double amount) {
        if (org.delawarex.dbz.market.storage.EconomyManager.isHooked()) {
            new org.delawarex.dbz.market.storage.EconomyManager(0).deposit(player, amount);
        }
    }

    private boolean withdrawVaultZeni(Player player, double amount) {
        if (org.delawarex.dbz.market.storage.EconomyManager.isHooked()) {
            return new org.delawarex.dbz.market.storage.EconomyManager(0).withdraw(player, amount);
        }
        return false;
    }

    public LoanRangeManager getRangeManager()   { return rangeManager; }
    public PenaltyManager   getPenaltyManager() { return penaltyManager; }
    public BankDataManager  getDataManager()    { return data; }
}
