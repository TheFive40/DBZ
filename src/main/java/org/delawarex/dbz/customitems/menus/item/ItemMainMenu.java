package org.delawarex.dbz.customitems.menus.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.managers.CustomItemManager;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.customitems.models.CustomItem;
import org.delawarex.service.CC;

import java.util.ArrayList;

public class ItemMainMenu extends Menu {

    @Override protected String getTitle() { return "&c&lCustom Items"; }
    @Override protected int getRows()     { return 3; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();

        set(11, item(Material.EMERALD_BLOCK,
                        "&a&lCrear Item",
                        "&7Crea un item custom desde",
                        "&7el item en tu mano",
                        "", "&a[CLICK]"),
                e -> createItem(player));

        set(13, item(Material.BOOK,
                        "&b&lListar Items",
                        "&7Total: &f" + CustomItemManager.getInstance().getAll().size(),
                        "", "&a[CLICK]"),
                e -> new ItemListMenu(1).open(player));

        set(15, item(Material.PAPER,
                        "&e&lAyuda",
                        "&7/ci <subcomando>",
                        "&7Subcomandos: give, register,",
                        "&7remove, rename, list, addstat,",
                        "&7addeffect, addcmd"),
                e -> {});
    }

    private void createItem(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null || hand.getType() == Material.AIR) {
            player.sendMessage(CC.translate("&cSostén un item en la mano."));
            return;
        }

        ChatInput.await(player, "Escribe el ID del nuevo item (sin espacios):", (p, id) -> {
            id = id.toLowerCase().replace(" ", "_");

            if (CustomItemManager.getInstance().exists(id)) {
                p.sendMessage(CC.translate("&cYa existe un item con ese ID."));
                new ItemMainMenu().open(p);
                return;
            }

            // Read meta safely — vanilla items without rename may not have meta
            String displayName = "";
            java.util.List<String> lore = new ArrayList<>();

            if (hand.hasItemMeta()) {
                ItemMeta meta = hand.getItemMeta();
                if (meta != null) {
                    if (meta.hasDisplayName()) displayName = meta.getDisplayName();
                    if (meta.hasLore() && meta.getLore() != null) lore = meta.getLore();
                }
            }

            CustomItem ci = new CustomItem()
                    .setId(id)
                    .setMaterial(hand.getType().name())   // ← String: "DIAMOND_SWORD"
                    .setDisplayName(displayName)
                    .setLore(lore);

            CustomItemManager.getInstance().register(ci);
            p.sendMessage(CC.translate("&aItem &f" + id + " &acreado (&f" + hand.getType().name() + "&a)."));
            new ItemEditMenu(id).open(p);
        });
    }
}