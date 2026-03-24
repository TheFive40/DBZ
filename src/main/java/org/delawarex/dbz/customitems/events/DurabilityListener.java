package org.delawarex.dbz.customitems.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
        Player player  = event.getPlayer();

        if (CustomDurabilityManager.hasCustomDurability(item)) {
            event.setCancelled(true);
            applyDamage(player, item, event.getDamage());
            return;
        }

        int maxDur = resolveMaxDurability(item);
        if (maxDur > 0) {
            event.setCancelled(true);
            CustomDurabilityManager.setCustomMaxDurability(item, maxDur);
            applyDamage(player, item, event.getDamage());
        }
    }

    private void applyDamage(Player player, ItemStack item, int damage) {
        boolean broken = CustomDurabilityManager.damageItem(item, damage);
        if (broken) {
            item.setAmount(0);
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