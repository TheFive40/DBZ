package org.delawarex.dbz.advancedcrates.managers;

import org.bukkit.entity.Player;
import org.delawarex.service.CC;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class ChatInputManager {

    private static final Map<UUID, BiConsumer<Player, String>> pending = new ConcurrentHashMap<>();

    public static void await(Player player, String prompt, BiConsumer<Player, String> callback) {
        pending.put(player.getUniqueId(), callback);
        player.closeInventory();
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&e " + prompt));
        player.sendMessage(CC.translate("&7 Escribe &cCancelar &7para abortar."));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    public static boolean hasPending(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    public static BiConsumer<Player, String> consume(Player player) {
        return pending.remove(player.getUniqueId());
    }

    public static void cancel(Player player) {
        pending.remove(player.getUniqueId());
    }
}
