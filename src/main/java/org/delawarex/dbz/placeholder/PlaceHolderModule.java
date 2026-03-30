package org.delawarex.dbz.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PlaceHolderModule {
    private static DbzExpansion placeholderExpansion;

    public static void initialize(JavaPlugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new DbzExpansion();

            if (placeholderExpansion.register()) {
                plugin.getLogger().info("PlaceholderAPI registrado exitosamente");
            } else {
                plugin.getLogger().warning("No se pudo registrar PlaceholderAPI");
            }
        } else {
            plugin.getLogger().info("PlaceholderAPI no encontrado - Placeholders no disponibles");
        }
    }

    public static void shutdown() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
    }
}
