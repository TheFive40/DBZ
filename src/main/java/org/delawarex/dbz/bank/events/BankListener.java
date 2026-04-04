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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BankListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BankAccount acc = BankManager.getInstance().getOrCreate(player);
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM HH:mm");
        List<Loan> activeLoans = acc.getActiveLoans();

        if (activeLoans.isEmpty()) return;

        long overdueCount = activeLoans.stream().filter(Loan::isOverdue).count();
        long graceCount = activeLoans.stream()
                .filter(l -> l.getGraceDeadline() > 0 && System.currentTimeMillis() < l.getGraceDeadline())
                .count();

        if (overdueCount > 0) {
            player.sendMessage(CC.translate("&c⚠ Tienes &f" + overdueCount + " &ccuota(s) con penalización activa. Usa &f/bank &cpara pagar."));
        } else if (graceCount > 0) {
            activeLoans.stream()
                    .filter(l -> l.getGraceDeadline() > 0 && System.currentTimeMillis() < l.getGraceDeadline())
                    .findFirst()
                    .ifPresent(l -> {
                        String deadline = fmt.format(new Date(l.getGraceDeadline()));
                        player.sendMessage(CC.translate("&e⚠ Tienes cuota(s) pendiente(s) de pago. Vence: &f" + deadline + "&e. Usa &f/bank &epara pagar."));
                    });
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        BankAccount acc = BankManager.getInstance().getAccount(event.getPlayer().getUniqueId());
        if (acc != null) BankManager.getInstance().save(acc);
    }
}