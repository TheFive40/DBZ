package org.delawarex.dbz.fragments.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.fragments.model.ArmorFragment;
import org.delawarex.dbz.fragments.manager.FragmentManager;
import org.delawarex.service.CC;

public class FragmentApplyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFragmentUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!ArmorFragment.isFragment(itemInHand)) return;

        event.setCancelled(true);

        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack targetArmor = null;
        int targetSlot = -1;

        for (int i = 3; i >= 0; i--) {
            if (armorContents[i] != null && armorContents[i].getType() != Material.AIR) {
                targetArmor = armorContents[i];
                targetSlot = i;
                break;
            }
        }

        if (targetArmor == null) {
            player.sendMessage(CC.translate("&c\u2717 No tienes ninguna armadura equipada"));
            player.sendMessage(CC.translate("&7Equipa al menos una pieza de armadura"));
            return;
        }

        boolean applied = FragmentManager.getInstance().applyFragment(player, itemInHand, targetArmor);

        if (applied) {
            armorContents[targetSlot] = targetArmor;
            player.getInventory().setArmorContents(armorContents);
            player.updateInventory();
        }
    }
}