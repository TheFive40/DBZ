package org.delawarex.dbz.raids.events;

import noppes.npcs.api.event.NpcEvent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.raids.managers.NPCSpawnManager;
import org.delawarex.dbz.raids.managers.PartyManager;
import org.delawarex.dbz.raids.managers.RaidSessionManager;
import org.delawarex.dbz.raids.models.RaidSession;
import org.delawarex.dbz.raids.models.Wave;
import org.delawarex.dbz.raids.models.WaveReward;
import org.delawarex.dbz.raids.models.WaveStatus;
import org.delawarex.service.CC;

import java.util.*;

public class NPCDeathListener implements Listener {

    private static final Set<String> processedWaves = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, BukkitTask> countdownTasks = new HashMap<>();

    public void onNpcDie(NpcEvent.DiedEvent event) {
        try {
            int entityId = event.npc.getMCEntity().getBukkitEntity().getEntityId();
            String waveId = NPCSpawnManager.getWaveIdForNpc(entityId);
            if (waveId == null) return;

            String sessionId = waveId.substring(0, waveId.lastIndexOf("_wave_"));
            RaidSession session = RaidSessionManager.getById(sessionId);
            if (session == null) return;

            boolean tracked = NPCSpawnManager.markNpcDead(entityId, waveId);
            if (!tracked) return;

            Bukkit.getScheduler().runTask(DbzMain.instance, () -> {
                int remaining = NPCSpawnManager.getAliveCount(waveId);
                if (remaining > 0) {
                    updateProgress(session, remaining);
                } else {
                    completeWave(session, waveId);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProgress(RaidSession session, int remaining) {
        Wave wave = session.getCurrentWave();
        if (wave == null) return;
        int total = wave.getTotalEnemies();
        int killed = total - remaining;
        int percent = total > 0 ? (killed * 100) / total : 0;

        for (UUID uuid : session.getActivePlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            if (remaining <= 5) {
                String color = remaining <= 2 ? "&c" : "&e";
                p.sendTitle(
                        CC.translate(color + "⚔ " + remaining + " enemigo" + (remaining == 1 ? "" : "s")),
                        CC.translate("&7Oleada " + (session.getCurrentWaveIndex() + 1) + "/" + session.getRaid().getTotalWaves()),
                        3, 30, 8
                );
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, remaining <= 2 ? 0.6f : 1.2f);
            } else if (remaining % 5 == 0) {
                p.sendActionBar(CC.translate("&e⚔ " + killed + "/" + total + " &7(" + percent + "%)"));
            }
        }
    }

    private void completeWave(RaidSession session, String waveId) {
        if (!processedWaves.add(waveId)) return;

        executeWaveRewards(session);
        NPCSpawnManager.clearWave(waveId);

        int waveNum = session.getCurrentWaveIndex() + 1;
        int totalWaves = session.getRaid().getTotalWaves();
        List<Player> players = getActivePlayers(session);

        for (Player p : players) {
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.1f);
            p.sendTitle(
                    CC.translate("&a&l✓ OLEADA " + waveNum + "/" + totalWaves),
                    CC.translate("&7Todos los enemigos han sido derrotados"),
                    10, 70, 20
            );
            p.spawnParticle(Particle.FIREWORKS_SPARK, p.getLocation().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
        }

        if (!session.hasNextWave()) {
            Bukkit.getScheduler().runTaskLater(DbzMain.instance, () -> {
                completeRaid(session);
                processedWaves.remove(waveId);
            }, 80L);
        } else {
            startCountdown(session, waveId);
        }
    }

    private void startCountdown(RaidSession session, String previousWaveId) {
        String key = session.getSessionId() + "_cd";
        if (countdownTasks.containsKey(key)) return;

        int nextWaveNum = session.getCurrentWaveIndex() + 2;
        int totalWaves = session.getRaid().getTotalWaves();
        final int[] seconds = {10};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(DbzMain.instance, () -> {
            List<Player> players = getActivePlayers(session);

            if (seconds[0] > 0) {
                String titleColor = seconds[0] <= 3 ? "&c" : seconds[0] <= 6 ? "&e" : "&a";
                for (Player p : players) {
                    p.sendTitle(
                            CC.translate(titleColor + "&l" + seconds[0]),
                            CC.translate("&7Próxima oleada: &6" + nextWaveNum + " &7de &6" + totalWaves),
                            0, 22, 5
                    );
                    if (seconds[0] <= 5) {
                        float pitch = 0.8f + (0.04f * (6 - seconds[0]));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, pitch);
                    }
                }
                seconds[0]--;
            } else {
                BukkitTask t = countdownTasks.remove(key);
                if (t != null) t.cancel();

                processedWaves.remove(previousWaveId);
                session.moveToNextWave();
                Wave nextWave = session.getCurrentWave();

                if (nextWave != null) {
                    nextWave.setStatus(WaveStatus.ACTIVE);
                    String newWaveId = session.getSessionId() + "_wave_" + session.getCurrentWaveIndex();
                    NPCSpawnManager.spawnWaveNpcs(nextWave, newWaveId);

                    for (Player p : players) {
                        p.sendTitle(
                                CC.translate("&c&l⚔ OLEADA " + nextWaveNum + "/" + totalWaves),
                                CC.translate("&7¡Los enemigos avanzan!"),
                                10, 60, 20
                        );
                        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.4f, 1.3f);
                        p.spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
                        p.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                        p.sendMessage(CC.translate("&c&l  ⚔ OLEADA " + nextWaveNum + "/" + totalWaves + " INICIADA ⚔"));
                        p.sendMessage(CC.translate("&7  Enemigos: &f" + nextWave.getTotalEnemies()));
                        p.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    }
                }
            }
        }, 20L, 20L);

        countdownTasks.put(key, task);
    }

    private void completeRaid(RaidSession session) {
        List<Player> players = getActivePlayers(session);
        long duration = session.getDurationSeconds();
        long m = duration / 60, s = duration % 60;

        for (Player p : players) {
            p.sendTitle(
                    CC.translate("&6&l🏆 RAID COMPLETADA"),
                    CC.translate("&e" + session.getRaid().getRaidName()),
                    20, 120, 40
            );
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
            Bukkit.getScheduler().runTaskLater(DbzMain.instance, () ->
                            p.spawnParticle(Particle.FIREWORKS_SPARK, p.getLocation().add(0, 2, 0), 80, 1, 1, 1, 0.15f),
                    10L);
            p.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            p.sendMessage(CC.translate("&6&l        🏆 RAID COMPLETADA 🏆"));
            p.sendMessage(CC.translate("&eRaid: &f" + session.getRaid().getRaidName()));
            p.sendMessage(CC.translate("&eOleadas: &a" + session.getRaid().getTotalWaves() + "/" + session.getRaid().getTotalWaves()));
            p.sendMessage(CC.translate("&eTiempo total: &f" + m + "m " + s + "s"));
            p.sendMessage(CC.translate("&eSobrevivientes: &f" + players.size()));
            p.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        }

        RaidSessionManager.completeRaid(session);
        PartyManager.setStatus(session.getParty(), org.delawarex.dbz.raids.models.PartyStatus.WAITING);
    }

    private void executeWaveRewards(RaidSession session) {
        Wave wave = session.getCurrentWave();
        if (wave == null || !wave.hasRewards()) return;
        for (WaveReward reward : wave.getRewards()) {
            if (!reward.shouldExecute()) continue;
            for (UUID uuid : session.getActivePlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;
                String cmd = reward.getCommand()
                        .replace("@p", p.getName())
                        .replace("{player}", p.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                p.sendTitle(CC.translate("&6&l✦ RECOMPENSA"), CC.translate("&f¡Has recibido una recompensa!"), 10, 50, 15);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
            }
        }
    }

    private List<Player> getActivePlayers(RaidSession session) {
        List<Player> list = new ArrayList<>();
        for (UUID uuid : session.getActivePlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) list.add(p);
        }
        return list;
    }

    public static void clearSessionTracking(String sessionId) {
        processedWaves.removeIf(id -> id.startsWith(sessionId));
        List<String> toRemove = new ArrayList<>();
        countdownTasks.forEach((key, task) -> {
            if (key.startsWith(sessionId)) {
                task.cancel();
                toRemove.add(key);
            }
        });
        toRemove.forEach(countdownTasks::remove);
    }
}