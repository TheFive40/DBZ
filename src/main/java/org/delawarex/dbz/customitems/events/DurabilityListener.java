package org.delawarex.dbz.customitems.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.customitems.managers.CustomDurabilityManager;
import org.delawarex.service.CC;

public class DurabilityListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();

        if (!CustomDurabilityManager.hasCustomDurability(item)) return;
        event.setCancelled(true);

        boolean broken = CustomDurabilityManager.damageItem(item, event.getDamage());

        if (broken) {
            Player player = event.getPlayer();
            item.setAmount(0);
            player.sendMessage(CC.translate("&c✗ Tu equipo se ha roto."));
        }
    }
}