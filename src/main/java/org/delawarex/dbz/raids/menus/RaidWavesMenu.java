package org.delawarex.dbz.raids.menus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.raids.managers.RaidManager;
import org.delawarex.dbz.raids.models.Raid;
import org.delawarex.dbz.raids.models.Wave;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class RaidWavesMenu extends Menu {

    private final String raidId;

    public RaidWavesMenu(String raidId) { this.raidId = raidId; }

    @Override
    protected String getTitle() { return "&6&lOleadas"; }

    @Override
    protected int getRows() { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        Raid raid = RaidManager.getInstance().getById(raidId);
        if (raid == null) { player.closeInventory(); return; }

        int[] slots = buildInnerSlots();
        int slotIdx = 0;
        for (int i = 0; i < raid.getTotalWaves() && slotIdx < slots.length; i++) {
            Wave wave = raid.getWaveByIndex(i);
            if (wave == null) continue;
            final int waveIndex = i;

            ItemStack it = new ItemStack(Material.BLAZE_POWDER);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate("&6&lOleada " + wave.getWaveNumber()));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7Enemigos totales: &f" + wave.getTotalEnemies()));
                lore.add(CC.translate("&7Spawns: &f" + wave.getSpawnPoints().size()));
                lore.add(CC.translate("&7Recompensas: &f" + wave.getRewards().size()));
                lore.add("");
                lore.add(ChatColor.GREEN + "[CLICK] Editar");
                lore.add(ChatColor.RED + "[SHIFT+CLICK] Eliminar");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }

            set(slots[slotIdx], it, e -> {
                if (e.isShiftClick()) {
                    raid.getWaves().remove(waveIndex);
                    for (int j = 0; j < raid.getWaves().size(); j++) {
                        raid.getWaves().get(j).setWaveNumber(j + 1);
                    }
                    RaidManager.getInstance().saveRaid(raid);
                    player.sendMessage(CC.translate("&c✗ Oleada eliminada."));
                    new RaidWavesMenu(raidId).open(player);
                } else {
                    new RaidWaveConfigMenu(raidId, waveIndex).open(player);
                }
            });
            slotIdx++;
        }

        set(49, item(Material.EMERALD_BLOCK,
                        "&a&lAgregar Oleada",
                        "&7Oleada #" + (raid.getTotalWaves() + 1),
                        "", "&a[CLICK]"),
                e -> {
                    Wave newWave = new Wave(raid.getTotalWaves() + 1);
                    raid.addWave(newWave);
                    RaidManager.getInstance().saveRaid(raid);
                    player.sendMessage(CC.translate("&a✓ Oleada " + newWave.getWaveNumber() + " creada."));
                    new RaidWavesMenu(raidId).open(player);
                });

        set(45, back(), e -> new RaidConfigMenu(raidId).open(player));
    }

    private int[] buildInnerSlots() {
        int[] slots = new int[28];
        int idx = 0;
        for (int row = 1; row <= 4; row++)
            for (int col = 1; col <= 7; col++)
                slots[idx++] = row * 9 + col;
        return slots;
    }
}