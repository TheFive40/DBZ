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
    private final LoanRangeManager   rangeManager;
    private final PenaltyManager     penaltyManager;

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

        double depositedToBank = amount;
        if (acc.hasZeniPenalty()) {
            double penaltyAmount = amount * acc.getZeniPenaltyRate();
            penaltyAmount = Math.min(penaltyAmount, amount);
            depositedToBank = amount - penaltyAmount;
            boolean anyPaid = penaltyManager.applyZeniDebtPayment(acc, penaltyAmount);
            if (penaltyAmount > 0) {
                player.sendMessage(CC.translate("&c⚠ Penalización: &f" + String.format("%.2f", penaltyAmount) + " Zenis de tu depósito aplicados a tu deuda."));
            }
            if (anyPaid) notifyLoanPaidOff(acc, LoanType.ZENIS);
        }

        acc.setZeniBalance(acc.getZeniBalance() + depositedToBank);
        save(acc);
        return "&a✓ Depositaste &f" + String.format("%.2f", depositedToBank) + " Zenis &aal banco. Saldo banco: &f" + String.format("%.2f", acc.getZeniBalance());
    }

    public String withdrawZeni(Player player, double amount) {
        if (amount <= 0) return "&cCantidad inválida.";
        BankAccount acc = getOrCreate(player);
        if (acc.getZeniBalance() < amount) return "&cFondos insuficientes en el banco.";

        double penaltyAmount = 0.0;
        if (acc.hasZeniPenalty()) {
            penaltyAmount = amount * acc.getZeniPenaltyRate();
            penaltyAmount = Math.min(penaltyAmount, acc.getZeniBalance() - amount);
            penaltyAmount = Math.max(0, penaltyAmount);
        }

        acc.setZeniBalance(acc.getZeniBalance() - amount - penaltyAmount);

        if (penaltyAmount > 0) {
            boolean anyPaid = penaltyManager.applyZeniDebtPayment(acc, penaltyAmount);
            player.sendMessage(CC.translate("&c⚠ Penalización: &f" + String.format("%.2f", penaltyAmount) + " Zenis descontados para cubrir tu deuda."));
            if (anyPaid) notifyLoanPaidOff(acc, LoanType.ZENIS);
        }

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
        checkCapacityReset(acc);

        long activeLoanCount = acc.getLoans().stream().filter(l -> l.getType() == type && !l.isFullyPaid()).count();
        if (activeLoanCount >= 3) return "&cYa tienes demasiados préstamos activos de tipo " + type.display() + ".";

        if (type == LoanType.TPS) {
            long maxCapacity  = range.getMaxTPS();
            long usedCapacity = acc.getUsedTpsCapacity();
            long available    = maxCapacity - usedCapacity;

            if (available <= 0) {
                return "&cHas agotado tu capacidad de préstamo de TPS. &7" + getResetTimeString(acc);
            }
            if (amount > available) {
                return "&cCapacidad insuficiente. Disponible: &f" + available + " TPS. &7" + getResetTimeString(acc);
            }

            acc.setTpsBalance(acc.getTpsBalance() + (long) amount);
            addPlayerTPS(player, (long) amount);
            acc.setUsedTpsCapacity(usedCapacity + (long) amount);

        } else {
            double maxCapacity  = range.getMaxZenis();
            double usedCapacity = acc.getUsedZenisCapacity();
            double available    = maxCapacity - usedCapacity;

            if (available <= 0) {
                return "&cHas agotado tu capacidad de préstamo de Zenis. &7" + getResetTimeString(acc);
            }
            if (amount > available) {
                return "&cCapacidad insuficiente. Disponible: &f" + String.format("%.2f", available) + " Zenis. &7" + getResetTimeString(acc);
            }

            acc.setZeniBalance(acc.getZeniBalance() + amount);
            depositVaultZeni(player, amount);
            acc.setUsedZenisCapacity(usedCapacity + amount);
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

    public void checkCapacityReset(BankAccount acc) {
        long resetDays = BankConfigManager.getInstance().getCapacityResetDays();

        if (acc.getLastCapacityReset() == 0) {
            acc.setLastCapacityReset(System.currentTimeMillis());
            save(acc);
            return;
        }

        long elapsedDays = (System.currentTimeMillis() - acc.getLastCapacityReset()) / 86400000L;
        if (elapsedDays >= resetDays) {
            acc.setUsedTpsCapacity(0);
            acc.setUsedZenisCapacity(0.0);
            acc.setLastCapacityReset(System.currentTimeMillis());
            save(acc);
        }
    }

    private String getResetTimeString(BankAccount acc) {
        long resetDays = BankConfigManager.getInstance().getCapacityResetDays();
        long resetAt   = acc.getLastCapacityReset() + (resetDays * 86400000L);
        long remaining = Math.max(0, resetAt - System.currentTimeMillis());
        long days  = remaining / 86400000L;
        long hours = (remaining % 86400000L) / 3600000L;
        long mins  = (remaining % 3600000L) / 60000L;
        if (days > 0) return "Reset en: &f" + days + "d " + hours + "h " + mins + "m";
        if (hours > 0) return "Reset en: &f" + hours + "h " + mins + "m";
        return "Reset en: &f" + mins + "m";
    }

    public long getAvailableTpsCapacity(BankAccount acc, LoanRange range) {
        checkCapacityReset(acc);
        return Math.max(0, range.getMaxTPS() - acc.getUsedTpsCapacity());
    }

    public double getAvailableZeniCapacity(BankAccount acc, LoanRange range) {
        checkCapacityReset(acc);
        return Math.max(0, range.getMaxZenis() - acc.getUsedZenisCapacity());
    }

    public void resetCapacity(BankAccount acc) {
        acc.setUsedTpsCapacity(0);
        acc.setUsedZenisCapacity(0.0);
        acc.setLastCapacityReset(System.currentTimeMillis());
        save(acc);
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
        loan.setOverdueInterest(0.0);
        loan.setNotifiedOverdue(false);
        if (loan.isOverdue()) loan.setOverdue(false);
        acc.recalcPenalties();

        if (loan.isFullyPaid()) {
            acc.getLoans().remove(loan);
            save(acc);
            String msg = "&a✓ ¡Préstamo &f" + loanId.substring(0, 8) + "... &asaldado completamente!";
            sendMail(acc, "Tu prestamo ID:" + loanId.substring(0, 8) + " ha sido saldado completamente. Gracias.");
            return msg;
        }

        save(acc);
        return "&a✓ Pago de &f" + String.format("%.0f", amount) + " " + loan.getType().display()
                + " &aregistrado. Cuotas restantes: &f" + loan.getRemainingInstallments();
    }

    public void processScheduledPayments() {
        for (BankAccount acc : data.getAllCached()) {
            boolean changed = false;

            List<Loan> dueTps  = new ArrayList<>();
            List<Loan> dueZeni = new ArrayList<>();

            for (Loan loan : acc.getLoans()) {
                if (!loan.isFullyPaid() && loan.isDueNow()) {
                    loan.setNotifiedOverdue(false);
                    if (loan.getType() == LoanType.TPS) dueTps.add(loan);
                    else dueZeni.add(loan);
                }
            }

            if (!dueTps.isEmpty()) {
                long totalNeeded = dueTps.stream()
                        .mapToLong(l -> Math.round(l.getInstallmentAmount() + l.getOverdueInterest()))
                        .sum();

                if (acc.getTpsBalance() >= totalNeeded) {
                    acc.setTpsBalance(acc.getTpsBalance() - totalNeeded);
                    for (Loan loan : dueTps) {
                        loan.advancePayment(loan.getInstallmentAmount());
                        changed = true;
                        if (loan.isFullyPaid()) {
                            acc.getLoans().remove(loan);
                            notifyPlayer(acc, "&a✓ ¡Préstamo de TPS &f" + loan.getId().substring(0, 8) + "... &asaldado completamente!");
                            sendMail(acc, "Tu prestamo de TPS ha sido saldado completamente de forma automatica.");
                        }
                    }
                    acc.recalcPenalties();
                    notifyPlayer(acc, "&a✓ Cuota(s) TPS pagada(s) automáticamente: &f-" + totalNeeded + " TPS");
                } else {
                    for (Loan loan : dueTps) {
                        double interest = loan.getInstallmentAmount() * 0.05;
                        loan.setOverdueInterest(loan.getOverdueInterest() + interest);
                        loan.setNextPaymentTime(loan.getNextPaymentTime() + loan.getIntervalMillis());
                        penaltyManager.activatePenalty(acc, loan);
                        loan.setNotifiedOverdue(true);
                        changed = true;
                    }
                    acc.recalcPenalties();
                    long totalDue = dueTps.stream().mapToLong(l -> Math.round(l.getInstallmentAmount())).sum();
                    notifyPlayer(acc, "&c⚠ No se pudo cobrar tu cuota de &f" + totalDue + " TPS&c. Penalización activa. Usa &f/bank");
                    sendMail(acc, "El banco intento cobrar " + totalDue + " TPS pero no tenias saldo suficiente. Se ha activado una penalizacion sobre tus ingresos de TPS.");
                }
            }

            if (!dueZeni.isEmpty()) {
                double totalNeeded = dueZeni.stream()
                        .mapToDouble(l -> l.getInstallmentAmount() + l.getOverdueInterest())
                        .sum();

                if (acc.getZeniBalance() >= totalNeeded) {
                    acc.setZeniBalance(acc.getZeniBalance() - totalNeeded);
                    for (Loan loan : dueZeni) {
                        loan.advancePayment(loan.getInstallmentAmount());
                        changed = true;
                        if (loan.isFullyPaid()) {
                            acc.getLoans().remove(loan);
                            notifyPlayer(acc, "&a✓ ¡Préstamo de Zenis &f" + loan.getId().substring(0, 8) + "... &asaldado completamente!");
                            sendMail(acc, "Tu prestamo de Zenis ha sido saldado completamente de forma automatica.");
                        }
                    }
                    acc.recalcPenalties();
                    notifyPlayer(acc, "&a✓ Cuota(s) Zenis pagada(s) automáticamente: &f-" + String.format("%.2f", totalNeeded) + " Zenis");
                } else {
                    for (Loan loan : dueZeni) {
                        double interest = loan.getInstallmentAmount() * 0.05;
                        loan.setOverdueInterest(loan.getOverdueInterest() + interest);
                        loan.setNextPaymentTime(loan.getNextPaymentTime() + loan.getIntervalMillis());
                        penaltyManager.activatePenalty(acc, loan);
                        loan.setNotifiedOverdue(true);
                        changed = true;
                    }
                    acc.recalcPenalties();
                    double totalDue = dueZeni.stream().mapToDouble(Loan::getInstallmentAmount).sum();
                    notifyPlayer(acc, "&c⚠ No se pudo cobrar tu cuota de &f" + String.format("%.2f", totalDue) + " Zenis&c. Penalización activa. Usa &f/bank");
                    sendMail(acc, "El banco intento cobrar " + String.format("%.2f", totalDue) + " Zenis pero no tenias saldo suficiente. Se ha activado una penalizacion sobre tus movimientos de Zenis.");
                }
            }

            if (changed) save(acc);
        }
    }

    public void notifyLoanPaidOff(BankAccount acc, LoanType type) {
        Player online = Bukkit.getPlayer(UUID.fromString(acc.getUuid()));
        if (online != null) {
            online.sendMessage(CC.translate("&a✓ ¡Tu deuda de " + type.display() + " ha sido saldada completamente mediante los pagos de penalización!"));
        }
        sendMail(acc, "Tu deuda de " + type.display() + " ha sido saldada completamente gracias a los descuentos de penalizacion.");
    }

    public void notifyPlayer(BankAccount acc, String msg) {
        Player p = Bukkit.getPlayer(UUID.fromString(acc.getUuid()));
        if (p != null) p.sendMessage(CC.translate(msg));
    }

    public void sendMail(BankAccount acc, String message) {
        if (acc.getPlayerName() == null || acc.getPlayerName().isEmpty()) return;
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "mail send " + acc.getPlayerName() + " [Banco] " + message);
        } catch (Exception ignored) {}
    }

    public long applyTpsPenalty(Player player, long tpsGained) {
        return penaltyManager.applyTpsPenalty(player, tpsGained);
    }

    public double applyZeniPenaltyOnMovement(Player player, double amount) {
        BankAccount acc = getAccount(player.getUniqueId());
        if (acc == null || !acc.hasZeniPenalty()) return 0.0;
        double penalty = penaltyManager.applyZeniPenalty(player, amount);
        save(acc);
        return penalty;
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
