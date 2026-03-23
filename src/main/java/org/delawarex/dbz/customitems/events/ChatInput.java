package org.delawarex.dbz.customitems.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Simple chat-based text input for menus.
 * Usage:
 *   ChatInput.await(player, "Enter value:", (p, text) -> { ... });
 */
public class ChatInput implements Listener {

    public record InputState(String prompt, BiConsumer<Player, String> callback) {}

    private static final Map<UUID, InputState> pending = new HashMap<>();

    public static void await(Player player, String prompt, BiConsumer<Player, String> callback) {
        pending.put(player.getUniqueId(), new InputState(prompt, callback));
        player.closeInventory();
        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                "&e " + prompt));
        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                "&7 Escribe &cCancelar &7para abortar."));
        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    }

    public static boolean hasPending(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        InputState state = pending.remove(player.getUniqueId());
        if (state == null) return;

        event.setCancelled(true);
        String msg = event.getMessage().trim();

        if (msg.equalsIgnoreCase("Cancelar") || msg.equalsIgnoreCase("cancel")) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCancelado."));
            return;
        }

        org.bukkit.Bukkit.getScheduler().runTask(
                org.delawarex.dbz.DbzMain.instance,
                () -> state.callback().accept(player, msg));
    }
}