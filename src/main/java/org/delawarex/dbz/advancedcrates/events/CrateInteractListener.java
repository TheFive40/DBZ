package org.delawarex.dbz.advancedcrates.events;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.delawarex.dbz.DbzMain;
import org.delawarex.service.CC;
import org.delawarex.dbz.advancedcrates.managers.CrateManager;
import org.delawarex.dbz.advancedcrates.menus.CrateOpenAnimation;
import org.delawarex.dbz.advancedcrates.models.Crate;
import org.delawarex.dbz.advancedcrates.models.CrateReward;

public class CrateInteractListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();

        if (CrateOpenAnimation.ACTIVE.contains(player.getUniqueId())) return;

        CrateManager mgr = DbzMain.get().getCrateManager();
        Location clickedLoc = event.getClickedBlock().getLocation();

        Crate crate = findCrateAtLocation(mgr, clickedLoc);
        if (crate == null) return;

        event.setCancelled(true);

        if (!crate.isEnabled()) {
            player.sendMessage(CC.translate("&c\u2717 Esta crate no está disponible."));
            return;
        }

        if (!mgr.hasKey(player, crate)) {
            player.sendMessage(CC.translate("&c\u2717 Necesitas la llave: &f"
                    + CC.strip(crate.getKeyDisplayName())));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (!mgr.consumeKey(player, crate)) return;

        CrateReward reward = crate.selectReward();
        if (reward == null) {
            player.sendMessage(CC.translate("&c\u2717 Esta crate no tiene recompensas configuradas."));
            return;
        }

        new CrateOpenAnimation(crate, reward, player).start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        CrateManager mgr = DbzMain.get().getCrateManager();
        Location loc = event.getBlock().getLocation();
        Crate crate = findCrateAtLocation(mgr, loc);
        if (crate == null) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(CC.translate("&c\u2717 No puedes romper un bloque de crate."));
    }

    private Crate findCrateAtLocation(CrateManager mgr, Location loc) {
        for (Crate crate : mgr.getAll()) {
            Location cl = crate.getPhysicalLocation();
            if (cl == null) continue;
            if (cl.getWorld() == null || loc.getWorld() == null) continue;
            if (!cl.getWorld().equals(loc.getWorld())) continue;
            if (cl.getBlockX() == loc.getBlockX()
                    && cl.getBlockY() == loc.getBlockY()
                    && cl.getBlockZ() == loc.getBlockZ()) {
                return crate;
            }
        }
        return null;
    }
}