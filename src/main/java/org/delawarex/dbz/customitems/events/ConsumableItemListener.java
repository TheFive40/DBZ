// ConsumableItemListener.java
package org.delawarex.dbz.customitems.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.customitems.managers.CustomItemManager;
import org.delawarex.dbz.customitems.models.CustomItem;
import org.delawarex.service.CC;

public class ConsumableItemListener implements Listener {

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        CustomItem ci = CustomItemManager.getInstance().identify(hand);
        if (ci == null) return;

        boolean shouldConsume = ci.isConsumable() || ci.getTpValue() > 0
                || (ci.getCommands() != null && !ci.getCommands().isEmpty());
        if (!shouldConsume) return;

        event.setCancelled(true);

        // Ejecutar comandos primero (no dependen del modo de consumo)
        if (ci.getCommands() != null && !ci.getCommands().isEmpty()) {
            Player nearest = findNearestLookedAt(player);
            for (String cmd : ci.getCommands()) {
                String finalCmd = cmd
                        .replace("@dp", player.getName())
                        .replace("@p", nearest != null ? nearest.getName() : player.getName());
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                } catch (Exception ex) {
                    player.sendMessage(CC.translate("&c✗ Error ejecutando: /" + finalCmd));
                }
            }
        }

        if (ci.getTpValue() > 0) {
            boolean isSneaking = player.isSneaking();

            if (isSneaking) {
                int totalConsumed = 0;
                ItemStack[] contents = player.getInventory().getContents();
                for (ItemStack stack : contents) {
                    if (stack == null) continue;
                    CustomItem stackCi = CustomItemManager.getInstance().identify(stack);
                    if (stackCi != null && stackCi.getId().equals(ci.getId())) {
                        totalConsumed += stack.getAmount();
                        stack.setAmount(0);
                    }
                }
                if (totalConsumed == 0) return;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "addtp " + player.getName() + " " + (ci.getTpValue() * totalConsumed));

            } else {
                int amount = hand.getAmount();
                hand.setAmount(0);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "addtp " + player.getName() + " " + (ci.getTpValue() * amount));
            }
            return;
        }

        if (ci.isConsumable()) {
            if (player.isSneaking()) {
                hand.setAmount(0);
            } else {
                int remaining = hand.getAmount() - 1;
                if (remaining <= 0) hand.setAmount(0);
                else hand.setAmount(remaining);
            }
        }
    }

    private Player findNearestLookedAt(Player source) {
        org.bukkit.util.Vector dir = source.getLocation().getDirection().normalize();
        Player best = null;
        double bestDot = 0.9;
        for (Player p : source.getWorld().getPlayers()) {
            if (p.equals(source)) continue;
            double dist = source.getLocation().distance(p.getLocation());
            if (dist > 6) continue;
            org.bukkit.util.Vector toP = p.getLocation().toVector()
                    .subtract(source.getLocation().toVector()).normalize();
            double dot = dir.dot(toP);
            if (dot > bestDot) { bestDot = dot; best = p; }
        }
        return best;
    }
}