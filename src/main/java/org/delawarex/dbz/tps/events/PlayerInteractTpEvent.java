package org.delawarex.dbz.tps.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.tps.managers.TpManager;

public class PlayerInteractTpEvent implements Listener {

    @EventHandler
    public void onTpConsume(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

        TpManager tpManager = new TpManager();
        ItemStack itemInHand = event.getPlayer().getItemInHand();

        if (!tpManager.isTp(itemInHand)) return;

        var player = event.getPlayer();
        int value = tpManager.getValue(itemInHand);
        int totalTps;

        if (player.isSneaking()) {
            int totalAmount = 0;

            for (ItemStack stack : player.getInventory().getContents()) {
                if (stack == null) continue;
                if (tpManager.isTp(stack) && tpManager.getValue(stack) == value) {
                    totalAmount += stack.getAmount();
                    stack.setAmount(0);
                }
            }

            if (totalAmount == 0) return;

            totalTps = value * totalAmount;

        } else {
            int amount = itemInHand.getAmount();
            totalTps = value * amount;
            itemInHand.setAmount(0);
        }

        event.setCancelled(true);
        DbzMain.instance.getServer().dispatchCommand(
                DbzMain.instance.getServer().getConsoleSender(),
                "addtp " + player.getName() + " " + totalTps
        );
    }
}