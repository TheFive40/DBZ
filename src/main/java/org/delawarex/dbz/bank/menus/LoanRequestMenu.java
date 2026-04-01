package org.delawarex.dbz.bank.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.dbz.bank.model.BankAccount;
import org.delawarex.dbz.bank.model.LoanRange;
import org.delawarex.dbz.bank.model.LoanType;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.service.CC;

public class LoanRequestMenu extends Menu {

    private final LoanRange  range;
    private final BankAccount acc;

    public LoanRequestMenu(LoanRange range, BankAccount acc) {
        this.range = range;
        this.acc   = acc;
    }

    @Override protected String getTitle() { return "&6&lSolicitar Préstamo"; }
    @Override protected int getRows()     { return 3; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();

        BankManager mgr    = BankManager.getInstance();
        long   availTps    = mgr.getAvailableTpsCapacity(acc, range);
        double availZeni   = mgr.getAvailableZeniCapacity(acc, range);

        set(4, item(Material.PAPER,
                "&7Rango: &fNivel " + range.getMinLevel() + " - " + range.getMaxLevel(),
                "&7Interés: &f" + (int)(range.getInterestRate()*100) + "%",
                "&7Cuotas: &f" + range.getInstallmentCount() + " cada " + range.getInstallmentIntervalHours() + "h",
                "&7Penalización por mora: &c" + (int)(range.getPenaltyRate()*100) + "%"));

        set(11, item(Material.EXPERIENCE_BOTTLE,
                        "&b&lPréstamo TPS",
                        "&7Capacidad disponible: &f" + availTps + " TPS",
                        "&7Máximo total: &8" + range.getMaxTPS() + " TPS",
                        "&7Se acreditará en tu banco y billetera.",
                        availTps <= 0 ? "&cSIN CAPACIDAD DISPONIBLE" : "",
                        "", "&a[CLICK]"),
                e -> {
                    if (availTps <= 0) {
                        player.sendMessage(CC.translate("&cNo tienes capacidad de préstamo de TPS disponible."));
                        return;
                    }
                    ChatInput.await(player, "¿Cuántos TPS solicitar? (disp. " + availTps + "):", (p, txt) -> {
                        try {
                            long v = Long.parseLong(txt);
                            p.sendMessage(CC.translate(mgr.requestLoan(p, LoanType.TPS, v)));
                        } catch (NumberFormatException ex) { p.sendMessage(CC.translate("&cCantidad inválida.")); }
                        new BankMainMenu().open(p);
                    });
                });

        set(13, item(Material.GOLD_INGOT,
                        "&6&lPréstamo Zenis",
                        "&7Capacidad disponible: &f" + String.format("%.0f", availZeni) + " Zenis",
                        "&7Máximo total: &8" + String.format("%.0f", range.getMaxZenis()) + " Zenis",
                        "&7Se acreditará en tu banco y billetera.",
                        availZeni <= 0 ? "&cSIN CAPACIDAD DISPONIBLE" : "",
                        "", "&a[CLICK]"),
                e -> {
                    if (availZeni <= 0) {
                        player.sendMessage(CC.translate("&cNo tienes capacidad de préstamo de Zenis disponible."));
                        return;
                    }
                    ChatInput.await(player, "¿Cuántos Zenis solicitar? (disp. " + String.format("%.0f", availZeni) + "):", (p, txt) -> {
                        try {
                            double v = Double.parseDouble(txt);
                            p.sendMessage(CC.translate(mgr.requestLoan(p, LoanType.ZENIS, v)));
                        } catch (NumberFormatException ex) { p.sendMessage(CC.translate("&cCantidad inválida.")); }
                        new BankMainMenu().open(p);
                    });
                });

        set(22, back(), e -> new BankMainMenu().open(player));
    }
}