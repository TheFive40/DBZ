package org.delawarex.dbz.customitems.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.customitems.managers.CustomArmorManager;
import org.delawarex.dbz.customitems.managers.CustomDurabilityManager;
import org.delawarex.dbz.customitems.managers.CustomItemManager;
import org.delawarex.dbz.customitems.models.CustomArmor;
import org.delawarex.dbz.customitems.models.CustomItem;
import org.delawarex.service.CC;

public class DurabilityListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();

        if (CustomDurabilityManager.hasCustomDurability(item)) {
            event.setCancelled(true);
            return;
        }

        int maxDur = resolveMaxDurability(item);
        if (maxDur > 0) {
            event.setCancelled(true);
            CustomDurabilityManager.setCustomMaxDurability(item, maxDur);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getDamage() <= 0) return;

        ItemStack[] armor = player.getInventory().getArmorContents();
        boolean anyBroken = false;

        for (int i = 0; i < armor.length; i++) {
            ItemStack piece = armor[i];
            if (piece == null || piece.getType() == Material.AIR) continue;

            if (!CustomDurabilityManager.hasCustomDurability(piece)) {
                int maxDur = resolveMaxDurability(piece);
                if (maxDur <= 0) continue;
                CustomDurabilityManager.setCustomMaxDurability(piece, maxDur);
            }

            boolean broken = CustomDurabilityManager.damageItem(piece, 1);
            if (broken) {
                armor[i] = new ItemStack(Material.AIR);
                anyBroken = true;
                player.sendMessage(CC.translate("&c✗ Tu equipo se ha roto."));
            }
        }

        if (anyBroken) {
            player.getInventory().setArmorContents(armor);
        }
        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) return;

        if (!CustomDurabilityManager.hasCustomDurability(hand)) {
            int maxDur = resolveMaxDurability(hand);
            if (maxDur <= 0) return;
            CustomDurabilityManager.setCustomMaxDurability(hand, maxDur);
        }

        boolean broken = CustomDurabilityManager.damageItem(hand, 1);
        if (broken) {
            hand.setAmount(0);
            player.sendMessage(CC.translate("&c✗ Tu equipo se ha roto."));
        }
        player.updateInventory();
    }

    private int resolveMaxDurability(ItemStack item) {
        CustomItem ci = CustomItemManager.getInstance().identify(item);
        if (ci != null && ci.getMaxDurability() > 0) return ci.getMaxDurability();

        CustomArmor armor = CustomArmorManager.getInstance().identify(item);
        if (armor != null && armor.getMaxDurability() > 0) return armor.getMaxDurability();

        return -1;
    }
}