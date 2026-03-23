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

/**
 * Handles right-click use of consumable CustomItems.
 * Executes commands and grants TPs via the addtp console command.
 */
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

        // Execute commands
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

        // Grant TPs
        if (ci.getTpValue() > 0) {
            int amount = hand.getAmount();
            int consumed = ci.isTpConsumeStack() ? amount : 1;
            int totalTp  = ci.getTpValue() * consumed;

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "addtp " + player.getName() + " " + totalTp);

            int remaining = amount - consumed;
            if (remaining <= 0) hand.setAmount(0);
            else hand.setAmount(remaining);
            return;
        }

        // Plain consumable
        if (ci.isConsumable()) {
            int remaining = hand.getAmount() - 1;
            if (remaining <= 0) hand.setAmount(0);
            else hand.setAmount(remaining);
        }
    }

    /** Returns the player the user is roughly looking at within 6 blocks. */
    private Player findNearestLookedAt(Player source) {
        org.bukkit.util.Vector dir = source.getLocation().getDirection().normalize();
        Player best = null;
        double bestDot = 0.9; // cos ~26°
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