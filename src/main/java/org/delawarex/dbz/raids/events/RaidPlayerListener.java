package org.delawarex.dbz.raids.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.delawarex.dbz.raids.managers.*;
import org.delawarex.dbz.raids.models.Party;
import org.delawarex.dbz.raids.models.RaidSession;
import org.delawarex.service.CC;

import java.util.UUID;

public class RaidPlayerListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        RaidSession session = RaidSessionManager.getByPlayer(uuid);
        if (session != null) {
            RaidSessionManager.playerLeft(uuid);
            for (UUID memberId : session.getActivePlayers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && !memberId.equals(uuid)) {
                    member.sendMessage(CC.translate("&c⚠ " + player.getName() + " se desconectó de la raid."));
                    member.sendMessage(CC.translate("&7Jugadores activos: &f" + session.getActivePlayers().size()));
                }
            }
            if (session.isRaidFailed()) {
                RaidSessionManager.failRaid(session);
                NPCSpawnManager.clearAll();
            }
        }

        Party party = PartyManager.getByPlayer(uuid);
        if (party != null) {
            boolean wasLeader = party.isLeader(uuid);
            PartyManager.leaveParty(uuid);
            if (wasLeader) {
                for (UUID memberId : party.getActivePlayers()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null) {
                        member.sendMessage(CC.translate("&c⚠ El líder se desconectó. La party fue disuelta."));
                    }
                }
                PartyManager.dissolve(party.getPartyId());
            } else {
                for (UUID memberId : party.getActivePlayers()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null) {
                        member.sendMessage(CC.translate("&c⚠ " + player.getName() + " se desconectó."));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        RaidSession session = RaidSessionManager.getByPlayer(uuid);
        if (session == null) return;

        RaidSessionManager.playerDied(uuid);

        player.sendTitle(
                CC.translate("&c&l✗ HAS SIDO DERROTADO"),
                CC.translate("&7No puedes regresar a esta raid"),
                15, 80, 20
        );

        for (UUID memberId : session.getActivePlayers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && !memberId.equals(uuid)) {
                member.sendMessage(CC.translate("&c✗ &f" + player.getName() + " &cfue derrotado."));
                member.sendMessage(CC.translate("&7Jugadores activos: &f" + session.getActivePlayers().size()));
            }
        }

        if (session.isRaidFailed()) {
            for (UUID memberId : session.getDeadPlayers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendTitle(
                            CC.translate("&c&l✗ RAID FALLIDA"),
                            CC.translate("&7Todos fueron derrotados"),
                            15, 80, 20
                    );
                }
            }
            RaidSessionManager.failRaid(session);
            NPCSpawnManager.clearAll();
        }
    }
}