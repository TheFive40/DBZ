package org.delawarex.dbz.advancedcrates.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.DbzMain;
import org.delawarex.service.CC;
import org.delawarex.dbz.advancedcrates.models.Crate;
import org.delawarex.dbz.advancedcrates.models.CrateReward;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrateOpenAnimation implements InventoryHolder {

    public static final Set<UUID> ACTIVE = new HashSet<>();

    private static final int TOTAL_FRAMES = 40;
    private static final int POOL_SIZE    = 49;
    private static final int WIN_INDEX    = 44;
    private static final int ROW_START    = 9;
    private static final int CENTER_SLOT  = 13;

    private final Crate crate;
    private final CrateReward selectedReward;
    private final Player player;
    private Inventory inventory;
    private final AtomicBoolean rewardGiven = new AtomicBoolean(false);

    public CrateOpenAnimation(Crate crate, CrateReward selectedReward, Player player) {
        this.crate          = crate;
        this.selectedReward = selectedReward;
        this.player         = player;
    }

    public void start() {
        if (selectedReward == null) return;
        ACTIVE.add(player.getUniqueId());

        String title = CC.translate("&6&l\u2B61 " + CC.strip(crate.getDisplayName()) + " \u2B61");
        inventory = Bukkit.createInventory(this, 27, title);
        buildDecor();
        player.openInventory(inventory);

        List<CrateReward> pool = buildPool();
        updateRow(pool, 0);
        schedule(pool, 0, 0);
    }

    private void schedule(List<CrateReward> pool, int frame, int pos) {
        Bukkit.getScheduler().runTaskLater(DbzMain.instance, () -> {
            if (!player.isOnline()) { giveReward(); return; }
            if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof CrateOpenAnimation)) {
                giveReward(); return;
            }

            int newPos = pos + 1;
            updateRow(pool, newPos);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK,
                    0.4f, 0.8f + Math.min(frame * 0.01f, 0.5f));

            if (frame >= TOTAL_FRAMES) {
                finalize(pool, newPos);
            } else {
                schedule(pool, frame + 1, newPos);
            }
        }, delay(frame));
    }

    private long delay(int frame) {
        if (frame < 10) return 2L;
        if (frame < 20) return 3L;
        if (frame < 28) return 5L;
        if (frame < 36) return 8L;
        return 13L;
    }

    private List<CrateReward> buildPool() {
        List<CrateReward> pool = new ArrayList<>();
        List<CrateReward> src  = crate.getRewards();
        if (src.isEmpty()) {
            for (int i = 0; i < POOL_SIZE; i++) pool.add(selectedReward);
            return pool;
        }
        for (int i = 0; i < WIN_INDEX; i++)
            pool.add(src.get((int) (Math.random() * src.size())));
        pool.add(selectedReward);
        for (int i = WIN_INDEX + 1; i < POOL_SIZE; i++)
            pool.add(src.get((int) (Math.random() * src.size())));
        return pool;
    }

    private void updateRow(List<CrateReward> pool, int offset) {
        for (int col = 0; col < 9; col++) {
            int idx = (offset + col) % POOL_SIZE;
            CrateReward r = pool.get(idx);
            ItemStack display = r.getItem() != null ? r.getItem().clone() : new ItemStack(Material.BARRIER);
            inventory.setItem(ROW_START + col, display);
        }
    }

    private void buildDecor() {
        for (int i = 0; i < 9; i++) {
            Material mat = (i == 4) ? Material.ORANGE_STAINED_GLASS_PANE : Material.BLACK_STAINED_GLASS_PANE;
            ItemStack p = new ItemStack(mat);
            ItemMeta m  = p.getItemMeta();
            m.setDisplayName(i == 4 ? ChatColor.GOLD + "\u25BC" : " ");
            p.setItemMeta(m);
            inventory.setItem(i, p);
        }
        for (int i = 18; i < 27; i++) {
            Material mat = (i == 22) ? Material.ORANGE_STAINED_GLASS_PANE : Material.BLACK_STAINED_GLASS_PANE;
            ItemStack p = new ItemStack(mat);
            ItemMeta m  = p.getItemMeta();
            m.setDisplayName(i == 22 ? ChatColor.GOLD + "\u25B2" : " ");
            p.setItemMeta(m);
            inventory.setItem(i, p);
        }
    }

    private void finalize(List<CrateReward> pool, int finalPos) {
        updateRow(pool, finalPos - (finalPos % POOL_SIZE == 0 ? 0 : 0));
        for (int col = 0; col < 9; col++) {
            if (col != 4) {
                ItemStack dim = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta m = dim.getItemMeta();
                m.setDisplayName(" ");
                dim.setItemMeta(m);
                inventory.setItem(ROW_START + col, dim);
            }
        }
        if (selectedReward.getItem() != null) {
            inventory.setItem(CENTER_SLOT, selectedReward.getItem().clone());
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);

        Bukkit.getScheduler().runTaskLater(DbzMain.instance, () -> {
            if (player.isOnline()) player.closeInventory();
            giveReward();
        }, 60L);
    }

    public void giveReward() {
        if (!rewardGiven.compareAndSet(false, true)) return;
        ACTIVE.remove(player.getUniqueId());

        if (!player.isOnline()) return;

        if (selectedReward.getItem() != null) {
            ItemStack reward = selectedReward.getItem().clone();
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), reward);
            } else {
                player.getInventory().addItem(reward);
            }
        }

        for (String cmd : selectedReward.getCommands()) {
            String finalCmd = cmd.replace("@p", player.getName()).replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        }

        String rewardName = getRewardName();
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&6&l   \u2756 CRATE ABIERTA \u2756"));
        player.sendMessage(CC.translate("&7 Crate: &f" + CC.strip(crate.getDisplayName())));
        player.sendMessage(CC.translate("&7 Recompensa: &f" + rewardName));
        player.sendMessage(CC.translate("&7 Rareza: " + crate.getRarity().getDisplay()));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private String getRewardName() {
        if (selectedReward.getItem() == null) return "Sin ítem";
        ItemStack i = selectedReward.getItem();
        if (i.hasItemMeta() && i.getItemMeta().hasDisplayName())
            return ChatColor.stripColor(i.getItemMeta().getDisplayName());
        return i.getType().name();
    }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }
}
