package org.delawarex.dbz.boosters.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.delawarex.dbz.boosters.storage.BoosterStorage;

public class BoosterModule {

    private static JavaPlugin plugin;

    public static void initialize(JavaPlugin instance) {
        plugin = instance;
        BoosterStorage.loadAllData();
        BoosterTaskScheduler.initialize(plugin);
        plugin.getLogger().info("[Boosters] Sistema de boosters inicializado.");
    }

    public static void shutdown() {
        BoosterStorage.saveAllData();
        BoosterTaskScheduler.cancelTasks();
        plugin.getLogger().info("[Boosters] Sistema de boosters guardado y apagado.");
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }
}
