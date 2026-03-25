package org.delawarex.dbz.fragments.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.delawarex.dbz.fragments.service.FragmentBonusIntegration;

public class FragmentArmorEquipListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FragmentBonusIntegration.clearAndRemoveBonuses(player);
        FragmentBonusIntegration.clearPlayerTracking(player.getUniqueId());
    }
}