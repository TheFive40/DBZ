package org.delawarex.dbz.boosters.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.delawarex.dbz.boosters.managers.GlobalBoosterManager;
import org.delawarex.dbz.boosters.storage.BoosterStorage;

public class BoosterTaskScheduler {

    private static BukkitTask expirationCheckTask;
    private static BukkitTask autoSaveTask;
    private static JavaPlugin plugin;

    public static void initialize(JavaPlugin instance) {
        plugin = instance;
        scheduleExpirationCheck();
        scheduleAutoSave();
    }

    private static void scheduleExpirationCheck() {
        expirationCheckTask = Bukkit.getScheduler().runTaskTimer(plugin,
                GlobalBoosterManager::checkExpiration, 0L, 600L);
    }

    private static void scheduleAutoSave() {
        autoSaveTask = Bukkit.getScheduler().runTaskTimer(plugin,
                BoosterStorage::saveAllData, 0L, 6000L);
    }

    public static void cancelTasks() {
        if (expirationCheckTask != null) expirationCheckTask.cancel();
        if (autoSaveTask != null) autoSaveTask.cancel();
    }

    public static void setExpirationCheckInterval(long ticks) {
        if (expirationCheckTask != null) expirationCheckTask.cancel();
        expirationCheckTask = Bukkit.getScheduler().runTaskTimer(plugin,
                GlobalBoosterManager::checkExpiration, 0L, ticks);
    }

    public static void setAutoSaveInterval(long ticks) {
        if (autoSaveTask != null) autoSaveTask.cancel();
        autoSaveTask = Bukkit.getScheduler().runTaskTimer(plugin,
                BoosterStorage::saveAllData, 0L, ticks);
    }
}
