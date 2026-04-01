package org.delawarex.dbz.bank.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.dbz.bank.model.BankAccount;
import org.delawarex.dbz.bank.model.Loan;
import org.delawarex.service.CC;

import java.util.List;

public class BankListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BankAccount acc = BankManager.getInstance().getOrCreate(player);
        List<Loan> overdue = acc.getActiveLoans();
        if (!overdue.isEmpty()) {
            long overdueCount = overdue.stream().filter(Loan::isOverdue).count();
            if (overdueCount > 0) {
                player.sendMessage(CC.translate("&c⚠ Tienes &f" + overdueCount + " &ccuota(s) vencida(s). Usa &f/bank balance &cpara ver detalles."));
            }
            boolean anyDueSoon = overdue.stream().anyMatch(l -> {
                long remaining = l.getNextPaymentTime() - System.currentTimeMillis();
                return remaining > 0 && remaining < 3_600_000;
            });
            if (anyDueSoon) {
                player.sendMessage(CC.translate("&e⚠ Tienes cuotas que vencen en menos de 1 hora. Asegúrate de tener saldo en el banco."));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        BankAccount acc = BankManager.getInstance().getAccount(event.getPlayer().getUniqueId());
        if (acc != null) BankManager.getInstance().save(acc);
    }
}
