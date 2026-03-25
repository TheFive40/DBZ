package org.delawarex.dbz.fragments;

import org.bukkit.scheduler.BukkitRunnable;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.fragments.commands.FragmentAdminCommand;
import org.delawarex.dbz.fragments.commands.FragmentCommand;
import org.delawarex.dbz.fragments.commands.FragmentTierCommand;
import org.delawarex.dbz.fragments.events.FragmentApplyListener;
import org.delawarex.dbz.fragments.events.FragmentArmorEquipListener;
import org.delawarex.dbz.fragments.events.TierFragmentApplyListener;
import org.delawarex.dbz.fragments.manager.FragmentManager;
import org.delawarex.dbz.fragments.service.FragmentBonusIntegration;

public class FragmentsModule {

    private static BukkitRunnable bonusTask;

    public static void enable() {
        DbzMain plugin = DbzMain.instance;

        FragmentManager.getInstance();

        plugin.getServer().getPluginManager().registerEvents(new FragmentApplyListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new TierFragmentApplyListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new FragmentArmorEquipListener(), plugin);

        new FragmentCommand();
        new FragmentAdminCommand();
        new FragmentTierCommand();

        bonusTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                    try {
                        FragmentBonusIntegration.applyFragmentBonuses(player);
                    } catch (Exception ignored) {}
                }
            }
        };
        bonusTask.runTaskTimer(plugin, 20L, 20L);

        plugin.getLogger().info("[Fragments] M\u00f3dulo cargado correctamente.");
    }

    public static void disable() {
        if (bonusTask != null) bonusTask.cancel();
        for (org.bukkit.entity.Player player : DbzMain.instance.getServer().getOnlinePlayers()) {
            try {
                FragmentBonusIntegration.clearAndRemoveBonuses(player);
            } catch (Exception ignored) {}
        }
        DbzMain.instance.getLogger().info("[Fragments] M\u00f3dulo detenido.");
    }
}