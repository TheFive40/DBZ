package org.delawarex.dbz.advancedcrates.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.delawarex.dbz.advancedcrates.managers.CrateManager;
import org.delawarex.dbz.advancedcrates.models.Crate;

public class WorldLoad implements Listener {

    private final CrateManager crateManager;

    public WorldLoad(CrateManager crateManager) {
        this.crateManager = crateManager;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        String worldName = event.getWorld().getName();

        for (Crate crate : crateManager.getAll()) {
            if (crate.hasPendingLocation(worldName)) {
                crate.applyPendingLocation(event.getWorld());

                Bukkit.getLogger().info(
                        "[Crates] Location aplicada para crate en mundo: " + worldName
                );
            }
        }
    }
}
