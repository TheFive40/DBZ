package org.delawarex.dbz.customitems.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.delawarex.dbz.customitems.managers.ArmorBonusManager;

public class PlayerBonusListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ArmorBonusManager.cleanup(event.getPlayer());
    }
}