package org.delawarex.dbz.bank;

import org.bukkit.scheduler.BukkitRunnable;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.bank.commands.BankAdminCommand;
import org.delawarex.dbz.bank.commands.BankCommand;
import org.delawarex.dbz.bank.events.BankListener;
import org.delawarex.dbz.bank.manager.BankManager;

public class BankModule {

    private static BukkitRunnable scheduler;

    public static void enable() {
        BankManager.getInstance();

        DbzMain.instance.getServer().getPluginManager()
                .registerEvents(new BankListener(), DbzMain.instance);

        new BankCommand();
        new BankAdminCommand();

        scheduler = new BukkitRunnable() {
            @Override
            public void run() {
                BankManager.getInstance().processScheduledPayments();
            }
        };
        scheduler.runTaskTimer(DbzMain.instance, 20L * 60, 20L * 60);

        DbzMain.instance.getLogger().info("[Bank] Módulo bancario cargado.");
    }

    public static void disable() {
        if (scheduler != null) scheduler.cancel();
        BankManager mgr = BankManager.getInstance();
        mgr.getDataManager().getAllCached().forEach(acc -> mgr.save(acc));
        BankManager.reset();
        DbzMain.instance.getLogger().info("[Bank] Módulo bancario detenido.");
    }
}
