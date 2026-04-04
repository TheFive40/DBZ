package org.delawarex.dbz.battlepass.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.delawarex.dbz.battlepass.manager.BattlePassManager;
import org.delawarex.dbz.battlepass.models.BattlePassPlayer;

public class BattlePassListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BattlePassManager.getInstance().getOrCreatePlayer(player.getUniqueId(), player.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BattlePassPlayer bp = BattlePassManager.getInstance().getPlayer(event.getPlayer().getUniqueId());
        if (bp != null) BattlePassManager.getInstance().savePlayer(bp);
    }
}