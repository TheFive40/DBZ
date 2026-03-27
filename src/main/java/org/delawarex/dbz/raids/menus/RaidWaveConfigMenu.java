package org.delawarex.dbz.raids.menus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.raids.managers.RaidChatInputManager;
import org.delawarex.dbz.raids.managers.RaidManager;
import org.delawarex.dbz.raids.models.Raid;
import org.delawarex.dbz.raids.models.SpawnPoint;
import org.delawarex.dbz.raids.models.Wave;
import org.delawarex.dbz.raids.models.WaveReward;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class RaidWaveConfigMenu extends Menu {

    private final String raidId;
    private final int waveIndex;

    public RaidWaveConfigMenu(String raidId, int waveIndex) {
        this.raidId = raidId;
        this.waveIndex = waveIndex;
    }

    @Override
    protected String getTitle() { return "&6&lOleada " + (waveIndex + 1); }

    @Override
    protected int getRows() { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        Raid raid = RaidManager.getInstance().getById(raidId);
        if (raid == null) { player.closeInventory(); return; }
        Wave wave = raid.getWaveByIndex(waveIndex);
        if (wave == null) { player.closeInventory(); return; }

        set(4, item(Material.BLAZE_POWDER,
                "&6&lOleada " + wave.getWaveNumber(),
                "&7Enemigos: &f" + wave.getTotalEnemies(),
                "&7Spawns: &f" + wave.getSpawnPoints().size(),
                "&7Recompensas: &f" + wave.getRewards().size()));

        set(10, item(Material.SPAWNER,
                        "&c&lPuntos de Spawn",
                        "&7Cantidad: &f" + wave.getSpawnPoints().size(),
                        "&7Tu posición se guarda al crear",
                        "", "&a[CLICK para agregar]"),
                e -> {
                    player.closeInventory();
                    RaidChatInputManager.startSpawnPointInput(player, raidId, waveIndex);
                });

        int spawnCol = 11;
        for (int i = 0; i < Math.min(wave.getSpawnPoints().size(), 5); i++) {
            SpawnPoint sp = wave.getSpawnPoints().get(i);
            final int spIdx = i;
            ItemStack it = new ItemStack(Material.SKELETON_SKULL);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate("&e" + sp.getNpcName()));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7Tab: &f" + sp.getNpcTab()));
                lore.add(CC.translate("&7Cantidad: &f" + sp.getQuantity()));
                if (sp.getLocation() != null) {
                    lore.add(CC.translate("&7Pos: &f" + sp.getLocation().getBlockX()
                            + ", " + sp.getLocation().getBlockY() + ", " + sp.getLocation().getBlockZ()));
                }
                lore.add("");
                lore.add(ChatColor.RED + "[CLICK] Eliminar");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }
            set(spawnCol, it, e -> {
                wave.getSpawnPoints().remove(spIdx);
                RaidManager.getInstance().saveRaid(raid);
                player.sendMessage(CC.translate("&c✗ Spawn eliminado."));
                new RaidWaveConfigMenu(raidId, waveIndex).open(player);
            });
            spawnCol++;
        }

        set(19, item(Material.DIAMOND,
                        "&b&lRecompensas",
                        "&7Cantidad: &f" + wave.getRewards().size(),
                        "", "&a[CLICK para agregar]"),
                e -> {
                    player.closeInventory();
                    RaidChatInputManager.startRewardInput(player, raidId, waveIndex);
                });

        int rewardCol = 20;
        for (int i = 0; i < Math.min(wave.getRewards().size(), 5); i++) {
            WaveReward reward = wave.getRewards().get(i);
            final int rwIdx = i;
            String cmdDisplay = reward.getCommand().length() > 20
                    ? reward.getCommand().substring(0, 20) + "..." : reward.getCommand();
            ItemStack it = new ItemStack(Material.GOLD_INGOT);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate("&e/" + cmdDisplay));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7Cmd: &f/" + reward.getCommand()));
                lore.add(CC.translate("&7Probabilidad: &f" + reward.getProbability() + "%"));
                lore.add("");
                lore.add(ChatColor.RED + "[CLICK] Eliminar");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }
            set(rewardCol, it, e -> {
                wave.getRewards().remove(rwIdx);
                RaidManager.getInstance().saveRaid(raid);
                player.sendMessage(CC.translate("&c✗ Recompensa eliminada."));
                new RaidWaveConfigMenu(raidId, waveIndex).open(player);
            });
            rewardCol++;
        }

        set(36, back(), e -> new RaidWavesMenu(raidId).open(player));
    }
}