package org.delawarex.dbz.customitems.menus.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.managers.CustomItemManager;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.customitems.models.CustomItem;
import org.delawarex.service.CC;

import java.util.List;

public class ItemCommandMenu extends Menu {

    private final String itemId;

    public ItemCommandMenu(String itemId) { this.itemId = itemId; }

    @Override protected String getTitle() { return "&e&lComandos " ; }
    @Override protected int getRows()     { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        CustomItem item = CustomItemManager.getInstance().get(itemId);
        if (item == null) return;

        // List commands
        List<String> cmds = item.getCommands();
        for (int i = 0; i < cmds.size() && i < 21; i++) {
            final int idx = i;
            final String cmd = cmds.get(i);
            set(10 + i, item(Material.PAPER, "&f" + cmd,
                            "&7Comando #" + (i + 1), "", "&c[CLICK para eliminar]"),
                    e -> {
                        item.getCommands().remove(idx);
                        CustomItemManager.getInstance().update(item);
                        new ItemCommandMenu(itemId).open(player);
                    });
        }

        // Add command
        set(47, item(Material.EMERALD_BLOCK, "&a&lAgregar Comando",
                        "&7Sin incluir /",
                        "&7@dp = quien usa, @p = objetivo",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Escribe el comando (sin /):", (p, text) -> {
                    String cmd = text.startsWith("/") ? text.substring(1) : text;
                    item.getCommands().add(cmd);
                    CustomItemManager.getInstance().update(item);
                    p.sendMessage(CC.translate("&aComando agregado: &f/" + cmd));
                    new ItemCommandMenu(itemId).open(p);
                }));

        set(45, back(), e -> new ItemEditMenu(itemId).open(player));
    }
}