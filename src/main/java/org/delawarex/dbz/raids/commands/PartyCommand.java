package org.delawarex.dbz.raids.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.raids.managers.*;
import org.delawarex.dbz.raids.models.*;
import org.delawarex.dbz.raids.events.NPCDeathListener;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartyCommand extends BaseCommand {

    private static final Map<UUID, PendingInvite> pendingInvites = new ConcurrentHashMap<>();

    public static class PendingInvite {
        public final String partyId;
        public final UUID inviterId;
        public final long timestamp;

        public PendingInvite(String partyId, UUID inviterId) {
            this.partyId = partyId;
            this.inviterId = inviterId;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() { return System.currentTimeMillis() - timestamp > 60000; }
    }

    @Command(name = "party", permission = "dbz.party")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length == 0) { sendHelp(player); return; }

        switch (args.getArgs(0).toLowerCase()) {
            case "create" -> handleCreate(player);
            case "invite" -> {
                if (args.getArgs().length < 2) { player.sendMessage(CC.translate("&cUso: /party invite <jugador>")); return; }
                handleInvite(player, args.getArgs(1));
            }
            case "accept" -> handleAccept(player);
            case "deny", "decline", "reject" -> handleDeny(player);
            case "leave" -> handleLeave(player);
            case "start" -> {
                if (args.getArgs().length < 2) { player.sendMessage(CC.translate("&cUso: /party start <raid>")); return; }
                handleStart(player, args.getArgs(1));
            }
            case "info" -> handleInfo(player);
            case "disband" -> handleDisband(player);
            default -> sendHelp(player);
        }
    }

    private void handleCreate(Player player) {
        if (PartyManager.isInParty(player.getUniqueId())) {
            player.sendMessage(CC.translate("&c✗ Ya estás en una party."));
            return;
        }
        PartyManager.createParty(player.getUniqueId(), 5);
        player.sendMessage(CC.translate("&a✓ Party creada. Invita jugadores con &f/party invite <nombre>"));
    }

    private void handleInvite(Player player, String targetName) {
        Party party = PartyManager.getByPlayer(player.getUniqueId());
        if (party == null) { player.sendMessage(CC.translate("&c✗ No estás en una party.")); return; }
        if (!party.isLeader(player.getUniqueId())) { player.sendMessage(CC.translate("&c✗ Solo el líder puede invitar.")); return; }
        if (party.isFull()) { player.sendMessage(CC.translate("&c✗ La party está llena.")); return; }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) { player.sendMessage(CC.translate("&c✗ Jugador no encontrado.")); return; }
        if (target.getUniqueId().equals(player.getUniqueId())) { player.sendMessage(CC.translate("&c✗ No puedes invitarte a ti mismo.")); return; }
        if (PartyManager.isInParty(target.getUniqueId())) { player.sendMessage(CC.translate("&c✗ " + target.getName() + " ya está en una party.")); return; }

        PendingInvite existing = pendingInvites.get(target.getUniqueId());
        if (existing != null && !existing.isExpired()) {
            player.sendMessage(CC.translate("&c✗ " + target.getName() + " ya tiene una invitación pendiente."));
            return;
        }

        pendingInvites.put(target.getUniqueId(), new PendingInvite(party.getPartyId(), player.getUniqueId()));
        player.sendMessage(CC.translate("&a✓ Invitación enviada a &f" + target.getName()));

        target.sendMessage("");
        target.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        target.sendMessage(CC.translate("&6&l  INVITACIÓN DE PARTY"));
        target.sendMessage("");
        target.sendMessage(CC.translate("&f  " + player.getName() + " &7te invitó a su party"));
        target.sendMessage("");
        target.sendMessage(CC.translate("&a  /party accept &7para aceptar"));
        target.sendMessage(CC.translate("&c  /party deny &7para rechazar"));
        target.sendMessage(CC.translate("&7  Expira en 60 segundos"));
        target.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        target.sendMessage("");
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

        UUID targetId = target.getUniqueId();
        Bukkit.getScheduler().runTaskLater(DbzMain.instance, () -> {
            PendingInvite inv = pendingInvites.get(targetId);
            if (inv != null && inv.isExpired()) {
                pendingInvites.remove(targetId);
                Player t = Bukkit.getPlayer(targetId);
                if (t != null) t.sendMessage(CC.translate("&7La invitación de &f" + player.getName() + " &7expiró."));
            }
        }, 1200L);
    }

    private void handleAccept(Player player) {
        PendingInvite invite = pendingInvites.get(player.getUniqueId());
        if (invite == null) { player.sendMessage(CC.translate("&c✗ No tienes invitaciones pendientes.")); return; }
        if (invite.isExpired()) {
            pendingInvites.remove(player.getUniqueId());
            player.sendMessage(CC.translate("&c✗ La invitación expiró."));
            return;
        }
        if (PartyManager.isInParty(player.getUniqueId())) {
            pendingInvites.remove(player.getUniqueId());
            player.sendMessage(CC.translate("&c✗ Ya estás en una party."));
            return;
        }

        Party party = PartyManager.getById(invite.partyId);
        if (party == null) {
            pendingInvites.remove(player.getUniqueId());
            player.sendMessage(CC.translate("&c✗ La party ya no existe."));
            return;
        }

        boolean joined = PartyManager.joinParty(player.getUniqueId(), party);
        pendingInvites.remove(player.getUniqueId());

        if (joined) {
            player.sendMessage(CC.translate("&a✓ Te has unido a la party de &f" + Bukkit.getOfflinePlayer(party.getLeader()).getName()));
            Player inviter = Bukkit.getPlayer(invite.inviterId);
            if (inviter != null) inviter.sendMessage(CC.translate("&a✓ &f" + player.getName() + " &aaceptó la invitación."));
            for (UUID memberId : party.getActivePlayers()) {
                if (!memberId.equals(player.getUniqueId()) && !memberId.equals(invite.inviterId)) {
                    Player m = Bukkit.getPlayer(memberId);
                    if (m != null) m.sendMessage(CC.translate("&b→ &f" + player.getName() + " &bse unió a la party."));
                }
            }
        } else {
            player.sendMessage(CC.translate("&c✗ No se pudo unir (party llena)."));
        }
    }

    private void handleDeny(Player player) {
        PendingInvite invite = pendingInvites.remove(player.getUniqueId());
        if (invite == null) { player.sendMessage(CC.translate("&c✗ No tienes invitaciones pendientes.")); return; }
        player.sendMessage(CC.translate("&7Invitación rechazada."));
        Player inviter = Bukkit.getPlayer(invite.inviterId);
        if (inviter != null) inviter.sendMessage(CC.translate("&c✗ &f" + player.getName() + " &crechazó la invitación."));
    }

    private void handleLeave(Player player) {
        Party party = PartyManager.getByPlayer(player.getUniqueId());
        if (party == null) { player.sendMessage(CC.translate("&c✗ No estás en una party.")); return; }
        boolean wasLeader = party.isLeader(player.getUniqueId());
        for (UUID memberId : party.getActivePlayers()) {
            if (!memberId.equals(player.getUniqueId())) {
                Player m = Bukkit.getPlayer(memberId);
                if (m != null) m.sendMessage(CC.translate("&c→ &f" + player.getName() + " &cabandonó la party."));
            }
        }
        if (wasLeader) PartyManager.dissolve(party.getPartyId());
        else PartyManager.leaveParty(player.getUniqueId());
        player.sendMessage(CC.translate("&7Has abandonado la party."));
    }

    private void handleStart(Player player, String raidName) {
        Party party = PartyManager.getByPlayer(player.getUniqueId());
        if (party == null) { player.sendMessage(CC.translate("&c✗ No estás en una party.")); return; }
        if (!party.isLeader(player.getUniqueId())) { player.sendMessage(CC.translate("&c✗ Solo el líder puede iniciar la raid.")); return; }

        Raid raid = RaidManager.getInstance().getByName(raidName);
        if (raid == null) { player.sendMessage(CC.translate("&c✗ Raid no encontrada: &f" + raidName)); return; }
        if (!raid.isEnabled()) { player.sendMessage(CC.translate("&c✗ La raid está desactivada.")); return; }
        if (!raid.isConfigured()) { player.sendMessage(CC.translate("&c✗ La raid no está configurada correctamente.")); return; }
        if (party.getMemberCount() < raid.getMinPlayers()) {
            player.sendMessage(CC.translate("&c✗ Necesitas al menos &f" + raid.getMinPlayers() + " &cjugadores (actual: &f" + party.getMemberCount() + "&c).")); return;
        }

        for (UUID memberId : party.getActivePlayers()) {
            if (CooldownManager.hasCooldown(memberId, raid.getRaidId())) {
                Player member = Bukkit.getPlayer(memberId);
                String memberName = member != null ? member.getName() : memberId.toString();
                player.sendMessage(CC.translate("&c✗ &f" + memberName + " &ctiene cooldown activo: &f" + CooldownManager.getFormatted(memberId, raid.getRaidId())));
                return;
            }
        }

        if (RaidSessionManager.hasActiveSession(raid.getRaidId())) {
            player.sendMessage(CC.translate("&c✗ Ya hay una sesión activa de esta raid."));
            return;
        }

        RaidSession session = RaidSessionManager.createSession(raid, party);
        PartyManager.setStatus(party, PartyStatus.IN_RAID);

        Location spawn = raid.getPlayerSpawnPoint();
        List<Player> teleported = new ArrayList<>();

        for (UUID memberId : party.getActivePlayers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.teleport(spawn);
                teleported.add(member);
            }
        }

        Bukkit.getScheduler().runTaskLater(DbzMain.instance, () -> {
            try {
                Wave firstWave = session.getCurrentWave();

                if (firstWave == null) {
                    for (Player member : teleported) {
                        try { member.sendMessage(CC.translate("&c✗ Error: la raid no tiene oleadas configuradas.")); } catch (Exception ignored) {}
                    }
                    RaidSessionManager.failRaid(session);
                    return;
                }

                for (Player member : teleported) {
                    try {
                        member.sendTitle(
                                CC.translate("&c&l⚔ RAID INICIADA ⚔"),
                                CC.translate("&e" + raid.getRaidName()),
                                20, 80, 20
                        );
                        member.playSound(member.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.4f, 1.2f);
                        member.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                        member.sendMessage(CC.translate("&6&l  ⚔ RAID: " + raid.getRaidName().toUpperCase()));
                        member.sendMessage(CC.translate("&7  Oleadas: &f" + raid.getTotalWaves()));
                        member.sendMessage(CC.translate("&7  Enemigos esta oleada: &f" + firstWave.getTotalEnemies()));
                        member.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    } catch (Exception ignored) {}
                }

                firstWave.setStatus(WaveStatus.ACTIVE);
                String waveId = session.getSessionId() + "_wave_0";
                boolean spawned = NPCSpawnManager.spawnWaveNpcs(firstWave, waveId);

                if (!spawned) {
                    for (Player member : teleported) {
                        try { member.sendMessage(CC.translate("&c✗ Error al spawnear enemigos. Contacta un admin.")); } catch (Exception ignored) {}
                    }
                    RaidSessionManager.failRaid(session);
                }

            } catch (Exception e) {
                e.printStackTrace();
                for (Player member : teleported) {
                    try { member.sendMessage(CC.translate("&c✗ Error interno al iniciar la raid.")); } catch (Exception ignored) {}
                }
                RaidSessionManager.failRaid(session);
            }
        }, 40L);
    }

    private void handleInfo(Player player) {
        Party party = PartyManager.getByPlayer(player.getUniqueId());
        if (party == null) { player.sendMessage(CC.translate("&c✗ No estás en una party.")); return; }

        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&6&l  INFO DE PARTY"));
        player.sendMessage(CC.translate("&7  ID: &f" + party.getPartyId()));
        player.sendMessage(CC.translate("&7  Miembros: &f" + party.getMemberCount() + "/" + party.getMaxSize()));
        player.sendMessage(CC.translate("&7  Estado: &f" + party.getStatus().getDisplayName()));
        player.sendMessage(CC.translate("&7  Líder: &f" + Objects.requireNonNullElse(Bukkit.getOfflinePlayer(party.getLeader()).getName(), "?")));
        player.sendMessage(CC.translate("&7  Miembros:"));
        for (UUID memberId : party.getActivePlayers()) {
            String prefix = party.isLeader(memberId) ? "&e[LÍDER] &f" : "&7• &f";
            String name = Objects.requireNonNullElse(Bukkit.getOfflinePlayer(memberId).getName(), "?");
            player.sendMessage(CC.translate("    " + prefix + name));
        }
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private void handleDisband(Player player) {
        Party party = PartyManager.getByPlayer(player.getUniqueId());
        if (party == null) { player.sendMessage(CC.translate("&c✗ No estás en una party.")); return; }
        if (!party.isLeader(player.getUniqueId())) { player.sendMessage(CC.translate("&c✗ Solo el líder puede disolver la party.")); return; }
        for (UUID memberId : party.getActivePlayers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && !memberId.equals(player.getUniqueId())) {
                member.sendMessage(CC.translate("&c✗ La party fue disuelta por el líder."));
            }
        }
        PartyManager.dissolve(party.getPartyId());
        player.sendMessage(CC.translate("&7Party disuelta."));
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&6&l  COMANDOS DE PARTY"));
        player.sendMessage(CC.translate("&f  /party create &7- Crear party"));
        player.sendMessage(CC.translate("&f  /party invite <jugador> &7- Invitar"));
        player.sendMessage(CC.translate("&f  /party accept &7- Aceptar invitación"));
        player.sendMessage(CC.translate("&f  /party deny &7- Rechazar invitación"));
        player.sendMessage(CC.translate("&f  /party leave &7- Abandonar"));
        player.sendMessage(CC.translate("&f  /party start <raid> &7- Iniciar raid (líder)"));
        player.sendMessage(CC.translate("&f  /party info &7- Ver información"));
        player.sendMessage(CC.translate("&f  /party disband &7- Disolver (líder)"));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage("");
    }
}