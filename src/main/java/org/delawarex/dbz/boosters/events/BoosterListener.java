package org.delawarex.dbz.boosters.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.delawarex.dbz.boosters.managers.PersonalBoosterManager;
import org.delawarex.dbz.boosters.models.PersonalBooster;
import org.delawarex.dbz.boosters.storage.BoosterStorage;

import java.util.List;

public class BoosterListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            List<PersonalBooster> boosters = BoosterStorage.loadPersonalBoosters(player.getUniqueId());
            for (PersonalBooster booster : boosters) PersonalBoosterManager.addBooster(booster);
            if (!boosters.isEmpty())
                player.sendMessage("§7[Boosters] §aCargados " + boosters.size() + " booster(s) personal(es)");
        } catch (Exception e) {
            player.sendMessage("§c[Boosters] Error al cargar tus boosters personales");
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        try {
            List<PersonalBooster> boosters = PersonalBoosterManager.getPlayerBoosters(player.getUniqueId());
            if (!boosters.isEmpty()) BoosterStorage.savePersonalBoosters(player.getUniqueId(), boosters);
            PersonalBoosterManager.removeExpiredBoosters(player.getUniqueId());
        } catch (Exception e) {
            System.err.println("[Boosters] Error al guardar boosters de " + player.getName());
            e.printStackTrace();
        }
    }
}
