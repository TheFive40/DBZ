package org.delawarex.dbz.bank.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.dbz.bank.model.BankAccount;
import org.delawarex.dbz.bank.model.Loan;
import org.delawarex.dbz.bank.model.LoanRange;
import org.delawarex.dbz.bank.model.LoanType;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.service.CC;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BankMainMenu extends Menu {

    @Override protected String getTitle() { return "&6&lBanco"; }
    @Override protected int getRows()     { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        BankManager  mgr = BankManager.getInstance();
        BankAccount  acc = mgr.getOrCreate(player);
        int          lvl = mgr.getPlayerLevel(player);
        Optional<LoanRange> range = mgr.getRangeManager().getRangeForLevel(lvl);
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm");

        set(4, item(Material.GOLD_INGOT,
                "&6&lTu Cuenta Bancaria",
                "&7Nivel: &f" + lvl,
                "&7Zenis banco: &f" + String.format("%.2f", acc.getZeniBalance()),
                "&7TPS banco:   &f" + acc.getTpsBalance(),
                acc.hasTpsPenalty()  ? "&c⚠ Penalización TPS: &f"   + (int)(acc.getTpsPenaltyRate() *100)+"%" : "",
                acc.hasZeniPenalty() ? "&c⚠ Penalización Zenis: &f" + (int)(acc.getZeniPenaltyRate()*100)+"%" : ""));

        set(10, item(Material.EMERALD,
                        "&aDepositar Zenis",
                        "&7Transfiere Zenis de tu billetera al banco.",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "¿Cuántos Zenis depositar?", (p, txt) -> {
                    try { double v = Double.parseDouble(txt); p.sendMessage(CC.translate(mgr.depositZeni(p, v))); }
                    catch (NumberFormatException ex) { p.sendMessage(CC.translate("&cCantidad inválida.")); }
                    new BankMainMenu().open(p);
                }));

        set(11, item(Material.REDSTONE,
                        "&cRetirar Zenis",
                        "&7Transfiere Zenis del banco a tu billetera.",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "¿Cuántos Zenis retirar?", (p, txt) -> {
                    try { double v = Double.parseDouble(txt); p.sendMessage(CC.translate(mgr.withdrawZeni(p, v))); }
                    catch (NumberFormatException ex) { p.sendMessage(CC.translate("&cCantidad inválida.")); }
                    new BankMainMenu().open(p);
                }));

        set(12, item(Material.EXPERIENCE_BOTTLE,
                        "&aDepositar TPS",
                        "&7Guarda TPS en el banco.",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "¿Cuántos TPS depositar?", (p, txt) -> {
                    try { long v = Long.parseLong(txt); p.sendMessage(CC.translate(mgr.depositTps(p, v))); }
                    catch (NumberFormatException ex) { p.sendMessage(CC.translate("&cCantidad inválida.")); }
                    new BankMainMenu().open(p);
                }));

        set(13, item(Material.NETHER_STAR,
                        "&cRetirar TPS",
                        "&7Retira TPS del banco.",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "¿Cuántos TPS retirar?", (p, txt) -> {
                    try { long v = Long.parseLong(txt); p.sendMessage(CC.translate(mgr.withdrawTps(p, v))); }
                    catch (NumberFormatException ex) { p.sendMessage(CC.translate("&cCantidad inválida.")); }
                    new BankMainMenu().open(p);
                }));

        List<String> rangeLines = new ArrayList<>();
        range.ifPresentOrElse(r -> {
            rangeLines.add("&7Rango: &fNivel " + r.getMinLevel() + " - " + r.getMaxLevel());
            rangeLines.add("&7Máx TPS: &f" + r.getMaxTPS());
            rangeLines.add("&7Máx Zenis: &f" + String.format("%.0f", r.getMaxZenis()));
            rangeLines.add("&7Interés: &f" + (int)(r.getInterestRate()*100) + "%");
            rangeLines.add("&7Cuotas: &f" + r.getInstallmentCount() + " c/u " + r.getInstallmentIntervalHours() + "h");
            rangeLines.add("");
            rangeLines.add("&e[CLICK para solicitar préstamo]");
        }, () -> rangeLines.add("&cTu nivel no tiene rango configurado."));

        set(14, item(Material.PAPER, "&b&lSolicitar Préstamo", rangeLines.toArray(new String[0])),
                e -> {
                    if (range.isEmpty()) { player.sendMessage(CC.translate("&cNo tienes rango de préstamo configurado.")); return; }
                    new LoanRequestMenu(range.get()).open(player);
                });

        List<Loan> loans = acc.getActiveLoans();
        int slot = 28;
        for (Loan l : loans) {
            if (slot > 34) break;
            boolean overdue = l.isOverdue();
            String title = (overdue ? "&c⚠ " : "&6") + l.getType().display() + " &8[" + l.getId().substring(0, 6) + "...]";
            set(slot, item(overdue ? Material.RED_DYE : Material.ORANGE_DYE,
                            title,
                            "&7Original: &f" + String.format("%.0f", l.getOriginalAmount()),
                            "&7Restante: &f" + String.format("%.0f", l.getRemainingAmount()),
                            "&7Cuotas: &f" + l.getPaidInstallments() + "/" + l.getTotalInstallments(),
                            "&7Cuota: &f" + String.format("%.0f", l.getInstallmentAmount()),
                            "&7Próxima: &f" + fmt.format(new Date(l.getNextPaymentTime())),
                            overdue ? "&c⚠ VENCIDA - Penalización activa" : "",
                            "", "&a[CLICK para pagar cuota]"),
                    ev -> {
                        if (l.getType() == LoanType.TPS) {
                            player.sendMessage(CC.translate(mgr.payLoan(player, l.getId(), l.getInstallmentAmount())));
                        } else {
                            player.sendMessage(CC.translate(mgr.payLoan(player, l.getId(), l.getInstallmentAmount())));
                        }
                        new BankMainMenu().open(player);
                    });
            slot++;
        }

        if (loans.isEmpty()) {
            set(31, item(Material.LIME_DYE, "&a&lSin préstamos activos", "&7Tu historial crediticio está limpio."));
        }
    }
}
