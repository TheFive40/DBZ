package org.delawarex.dbz.customitems.events;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.customitems.managers.CustomArmorManager;
import org.delawarex.dbz.customitems.models.CustomArmor;

import static org.delawarex.dbz.customitems.managers.ArmorBonusManager.startRegenTask;
import static org.delawarex.dbz.customitems.managers.ArmorBonusManager.stopRegenTask;

public class EffectEvents implements Listener {
    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        String slotId = event.getSlotType().name().toLowerCase();

        ItemStack oldItem = event.getOldItem();
        CustomArmor oldArmor = CustomArmorManager.getInstance().identify(oldItem);
        if (oldArmor != null) stopRegenTask(player, slotId);

        ItemStack newItem = event.getNewItem();
        CustomArmor newArmor = CustomArmorManager.getInstance().identify(newItem);
        if (newArmor != null && !newArmor.getEffects().isEmpty()) {
            startRegenTask(player, slotId, newArmor.getEffects());
        }
    }
}
