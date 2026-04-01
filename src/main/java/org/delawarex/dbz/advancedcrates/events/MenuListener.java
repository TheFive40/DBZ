package org.delawarex.dbz.advancedcrates.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryHolder;
import org.delawarex.dbz.DbzMain;
import org.delawarex.service.CC;
import org.delawarex.dbz.advancedcrates.managers.ChatInputManager;
import org.delawarex.dbz.advancedcrates.menus.CrateOpenAnimation;
import org.delawarex.dbz.advancedcrates.menus.Menu;

import java.util.function.BiConsumer;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Menu menu) {
            menu.handleClick(event);
        } else if (holder instanceof CrateOpenAnimation) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof CrateOpenAnimation anim)) return;
        anim.giveReward();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!ChatInputManager.hasPending(player)) return;

        event.setCancelled(true);
        String msg = event.getMessage().trim();
        BiConsumer<Player, String> callback = ChatInputManager.consume(player);
        if (callback == null) return;

        if (msg.equalsIgnoreCase("cancelar") || msg.equalsIgnoreCase("cancel")) {
            player.sendMessage(CC.translate("&cCancelado."));
            return;
        }

        Bukkit.getScheduler().runTask(DbzMain.instance, () -> callback.accept(player, msg));
    }
}
