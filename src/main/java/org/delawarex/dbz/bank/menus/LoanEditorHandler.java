package org.delawarex.dbz.bank.menus;

import org.bukkit.entity.Player;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.dbz.bank.model.LoanRange;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.service.CC;

public class LoanEditorHandler {

    public static void startEdit(Player player, LoanRange range, int index) {
        player.closeInventory();
        askMinLevel(player, range, index);
    }

    private static void askMinLevel(Player player, LoanRange range, int index) {
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&b&l  Editor de Rango de Préstamo"));
        player.sendMessage(CC.translate("&7  Escribe &cCancelar &7para abortar."));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        ChatInput.await(player, "Nivel mínimo (actual: " + range.getMinLevel() + "):", (p, txt) -> {
            try {
                int v = Integer.parseInt(txt.trim());
                if (v < 1) throw new NumberFormatException();
                range.setMinLevel(v);
            } catch (NumberFormatException e) { p.sendMessage(CC.translate("&cValor inválido.")); new LoanAdminGUI(1).open(p); return; }
            askMaxLevel(p, range, index);
        });
    }

    private static void askMaxLevel(Player player, LoanRange range, int index) {
        ChatInput.await(player, "Nivel máximo (actual: " + range.getMaxLevel() + "):", (p, txt) -> {
            try {
                int v = Integer.parseInt(txt.trim());
                if (v < range.getMinLevel()) throw new NumberFormatException();
                range.setMaxLevel(v);
            } catch (NumberFormatException e) { p.sendMessage(CC.translate("&cValor inválido (debe ser >= minLevel).")); new LoanAdminGUI(1).open(p); return; }
            askMaxTPS(p, range, index);
        });
    }

    private static void askMaxTPS(Player player, LoanRange range, int index) {
        ChatInput.await(player, "Máx TPS para este rango (actual: " + range.getMaxTPS() + "):", (p, txt) -> {
            try {
                long v = Long.parseLong(txt.trim());
                if (v < 0) throw new NumberFormatException();
                range.setMaxTPS(v);
            } catch (NumberFormatException e) { p.sendMessage(CC.translate("&cValor inválido.")); new LoanAdminGUI(1).open(p); return; }
            askMaxZenis(p, range, index);
        });
    }

    private static void askMaxZenis(Player player, LoanRange range, int index) {
        ChatInput.await(player, "Máx Zenis para este rango (actual: " + String.format("%.0f", range.getMaxZenis()) + "):", (p, txt) -> {
            try {
                double v = Double.parseDouble(txt.trim());
                if (v < 0) throw new NumberFormatException();
                range.setMaxZenis(v);
            } catch (NumberFormatException e) { p.sendMessage(CC.translate("&cValor inválido.")); new LoanAdminGUI(1).open(p); return; }
            askInterest(p, range, index);
        });
    }

    private static void askInterest(Player player, LoanRange range, int index) {
        ChatInput.await(player, "Tasa de interés 0.0-1.0 (actual: " + range.getInterestRate() + "):", (p, txt) -> {
            try {
                double v = Double.parseDouble(txt.trim());
                if (v < 0 || v > 1) throw new NumberFormatException();
                range.setInterestRate(v);
            } catch (NumberFormatException e) { p.sendMessage(CC.translate("&cValor inválido (0.0-1.0).")); new LoanAdminGUI(1).open(p); return; }
            askInstallments(p, range, index);
        });
    }

    private static void askInstallments(Player player, LoanRange range, int index) {
        ChatInput.await(player, "Número de cuotas (actual: " + range.getInstallmentCount() + "):", (p, txt) -> {
            try {
                int v = Integer.parseInt(txt.trim());
                if (v < 1) throw new NumberFormatException();
                range.setInstallmentCount(v);
            } catch (NumberFormatException e) { p.sendMessage(CC.translate("&cValor inválido.")); new LoanAdminGUI(1).open(p); return; }
            askInterval(p, range, index);
        });
    }

    private static void askInterval(Player player, LoanRange range, int index) {
        ChatInput.await(player, "Horas entre cuotas (actual: " + range.getInstallmentIntervalHours() + "):", (p, txt) -> {
            try {
                int v = Integer.parseInt(txt.trim());
                if (v < 1) throw new NumberFormatException();
                range.setInstallmentIntervalHours(v);
            } catch (NumberFormatException e) { p.sendMessage(CC.translate("&cValor inválido.")); new LoanAdminGUI(1).open(p); return; }
            askPenalty(p, range, index);
        });
    }

    private static void askPenalty(Player player, LoanRange range, int index) {
        ChatInput.await(player, "Tasa de penalización por mora 0.0-0.9 (actual: " + range.getPenaltyRate() + "):", (p, txt) -> {
            try {
                double v = Double.parseDouble(txt.trim());
                if (v < 0 || v > 0.9) throw new NumberFormatException();
                range.setPenaltyRate(v);
            } catch (NumberFormatException e) { p.sendMessage(CC.translate("&cValor inválido (0.0-0.9).")); new LoanAdminGUI(1).open(p); return; }

            boolean overlaps = BankManager.getInstance().getRangeManager().overlaps(range, index);
            if (overlaps) {
                p.sendMessage(CC.translate("&c✗ Este rango se superpone con otro existente. Operación cancelada."));
                new LoanAdminGUI(1).open(p);
                return;
            }

            if (index < 0) {
                BankManager.getInstance().getRangeManager().addRange(range);
                p.sendMessage(CC.translate("&a✓ Nuevo rango creado: Nivel " + range.getMinLevel() + "-" + range.getMaxLevel()));
            } else {
                BankManager.getInstance().getRangeManager().updateRange(index, range);
                p.sendMessage(CC.translate("&a✓ Rango actualizado: Nivel " + range.getMinLevel() + "-" + range.getMaxLevel()));
            }
            new LoanAdminGUI(1).open(p);
        });
    }
}
