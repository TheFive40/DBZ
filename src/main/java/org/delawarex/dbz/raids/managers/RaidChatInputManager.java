package org.delawarex.dbz.raids.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.raids.managers.RaidManager;
import org.delawarex.dbz.raids.menus.RaidConfigMenu;
import org.delawarex.dbz.raids.menus.RaidMainMenu;
import org.delawarex.dbz.raids.menus.RaidWaveConfigMenu;
import org.delawarex.dbz.raids.models.Raid;
import org.delawarex.dbz.raids.models.SpawnPoint;
import org.delawarex.dbz.raids.models.Wave;
import org.delawarex.dbz.raids.models.WaveReward;
import org.delawarex.service.CC;

public class RaidChatInputManager {

    public static void startCreateRaidInput(Player player) {
        ChatInput.await(player, "Nombre de la raid (sin espacios largos):", (p, name) -> {
            if (name.trim().isEmpty() || name.length() > 30) {
                p.sendMessage(CC.translate("&c✗ Nombre inválido (1-30 caracteres)."));
                new RaidMainMenu().open(p);
                return;
            }
            ChatInput.await(p, "Descripción de la raid:", (p2, desc) -> {
                ChatInput.await(p2, "Cooldown en minutos (ej: 60):", (p3, cooldownStr) -> {
                    try {
                        int minutes = Integer.parseInt(cooldownStr.trim());
                        if (minutes < 1 || minutes > 10080) throw new NumberFormatException();
                        Raid raid = RaidManager.getInstance().createRaid(name.trim());
                        raid.setDescription(desc.trim());
                        raid.setCooldownSeconds(minutes * 60L);
                        RaidManager.getInstance().saveRaid(raid);
                        p3.sendMessage(CC.translate("&a✓ Raid &f" + name + " &acreada."));
                        Bukkit.getScheduler().runTaskLater(DbzMain.instance,
                                () -> new RaidConfigMenu(raid.getRaidId()).open(p3), 2L);
                    } catch (NumberFormatException e) {
                        p3.sendMessage(CC.translate("&c✗ Cooldown inválido (1-10080 minutos)."));
                        new RaidMainMenu().open(p3);
                    }
                });
            });
        });
    }

    public static void startRenameInput(Player player, String raidId) {
        ChatInput.await(player, "Nuevo nombre para la raid:", (p, name) -> {
            if (name.trim().isEmpty() || name.length() > 30) {
                p.sendMessage(CC.translate("&c✗ Nombre inválido."));
            } else {
                Raid raid = RaidManager.getInstance().getById(raidId);
                if (raid != null) {
                    raid.setRaidName(name.trim());
                    RaidManager.getInstance().saveRaid(raid);
                    p.sendMessage(CC.translate("&a✓ Nombre actualizado: &f" + name.trim()));
                }
            }
            Bukkit.getScheduler().runTaskLater(DbzMain.instance,
                    () -> new RaidConfigMenu(raidId).open(p), 2L);
        });
    }

    public static void startDescriptionInput(Player player, String raidId) {
        ChatInput.await(player, "Nueva descripción para la raid:", (p, desc) -> {
            Raid raid = RaidManager.getInstance().getById(raidId);
            if (raid != null) {
                raid.setDescription(desc.trim());
                RaidManager.getInstance().saveRaid(raid);
                p.sendMessage(CC.translate("&a✓ Descripción actualizada."));
            }
            Bukkit.getScheduler().runTaskLater(DbzMain.instance,
                    () -> new RaidConfigMenu(raidId).open(p), 2L);
        });
    }

    public static void startCooldownInput(Player player, String raidId) {
        ChatInput.await(player, "Cooldown en minutos (ej: 60):", (p, str) -> {
            try {
                int minutes = Integer.parseInt(str.trim());
                if (minutes < 1 || minutes > 10080) throw new NumberFormatException();
                Raid raid = RaidManager.getInstance().getById(raidId);
                if (raid != null) {
                    raid.setCooldownSeconds(minutes * 60L);
                    RaidManager.getInstance().saveRaid(raid);
                    p.sendMessage(CC.translate("&a✓ Cooldown: &f" + minutes + " minutos."));
                }
            } catch (NumberFormatException e) {
                p.sendMessage(CC.translate("&c✗ Número inválido (1-10080)."));
            }
            Bukkit.getScheduler().runTaskLater(DbzMain.instance,
                    () -> new RaidConfigMenu(raidId).open(p), 2L);
        });
    }

    public static void startPlayersInput(Player player, String raidId) {
        ChatInput.await(player, "Jugadores mínimo y máximo (ej: 1 5):", (p, str) -> {
            try {
                String[] parts = str.trim().split("\\s+");
                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
                if (min < 1 || max < min || max > 20) throw new NumberFormatException();
                Raid raid = RaidManager.getInstance().getById(raidId);
                if (raid != null) {
                    raid.setMinPlayers(min);
                    raid.setMaxPlayers(max);
                    RaidManager.getInstance().saveRaid(raid);
                    p.sendMessage(CC.translate("&a✓ Jugadores: &f" + min + "-" + max));
                }
            } catch (Exception e) {
                p.sendMessage(CC.translate("&c✗ Formato inválido. Usa: 1 5"));
            }
            Bukkit.getScheduler().runTaskLater(DbzMain.instance,
                    () -> new RaidConfigMenu(raidId).open(p), 2L);
        });
    }

    public static void startSpawnPointInput(Player player, String raidId, int waveIndex) {
        org.bukkit.Location savedLoc = player.getLocation().clone();
        ChatInput.await(player, "Nombre del NPC a spawnear:", (p, npcName) -> {
            if (npcName.trim().isEmpty()) {
                p.sendMessage(CC.translate("&c✗ Nombre inválido."));
                Bukkit.getScheduler().runTaskLater(DbzMain.instance,
                        () -> new RaidWaveConfigMenu(raidId, waveIndex).open(p), 2L);
                return;
            }
            ChatInput.await(p, "Tab del NPC (número, ej: 10):", (p2, tabStr) -> {
                try {
                    int tab = Integer.parseInt(tabStr.trim());
                    ChatInput.await(p2, "Cantidad de NPCs a spawnear:", (p3, qtyStr) -> {
                        try {
                            int qty = Integer.parseInt(qtyStr.trim());
                            if (qty < 1 || qty > 100) throw new NumberFormatException();
                            Raid raid = RaidManager.getInstance().getById(raidId);
                            if (raid != null) {
                                Wave wave = raid.getWaveByIndex(waveIndex);
                                if (wave != null) {
                                    wave.addSpawnPoint(new SpawnPoint(savedLoc, npcName.trim(), tab, qty));
                                    RaidManager.getInstance().saveRaid(raid);
                                    p3.sendMessage(CC.translate("&a✓ Spawn creado: &f" + npcName.trim() + " x" + qty));
                                }
                            }
                        } catch (NumberFormatException e) {
                            p3.sendMessage(CC.translate("&c✗ Cantidad inválida (1-100)."));
                        }
                        Bukkit.getScheduler().runTaskLater(DbzMain.instance,
                                () -> new RaidWaveConfigMenu(raidId, waveIndex).open(p3), 2L);
                    });
                } catch (NumberFormatException e) {
                    p2.sendMessage(CC.translate("&c✗ Tab inválido."));
                    Bukkit.getScheduler().runTaskLater(DbzMain.instance,
                            () -> new RaidWaveConfigMenu(raidId, waveIndex).open(p2), 2L);
                }
            });
        });
    }

    public static void startRewardInput(Player player, String raidId, int waveIndex) {
        ChatInput.await(player, "Comando de recompensa (sin /) — @p = jugador:", (p, cmd) -> {
            String cleanCmd = cmd.startsWith("/") ? cmd.substring(1) : cmd;
            if (cleanCmd.trim().isEmpty()) {
                p.sendMessage(CC.translate("&c✗ Comando inválido."));
                Bukkit.getScheduler().runTaskLater(DbzMain.instance,
                        () -> new RaidWaveConfigMenu(raidId, waveIndex).open(p), 2L);
                return;
            }
            ChatInput.await(p, "Probabilidad 0-100 (100 = siempre):", (p2, probStr) -> {
                try {
                    int prob = Integer.parseInt(probStr.trim());
                    if (prob < 0 || prob > 100) throw new NumberFormatException();
                    Raid raid = RaidManager.getInstance().getById(raidId);
                    if (raid != null) {
                        Wave wave = raid.getWaveByIndex(waveIndex);
                        if (wave != null) {
                            wave.addReward(new WaveReward(cleanCmd.trim(), prob));
                            RaidManager.getInstance().saveRaid(raid);
                            p2.sendMessage(CC.translate("&a✓ Recompensa: &f/" + cleanCmd + " &7(" + prob + "%)"));
                        }
                    }
                } catch (NumberFormatException e) {
                    p2.sendMessage(CC.translate("&c✗ Probabilidad inválida (0-100)."));
                }
                Bukkit.getScheduler().runTaskLater(DbzMain.instance,
                        () -> new RaidWaveConfigMenu(raidId, waveIndex).open(p2), 2L);
            });
        });
    }
}