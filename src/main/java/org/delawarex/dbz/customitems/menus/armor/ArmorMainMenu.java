package org.delawarex.dbz.customitems.menus.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.managers.CustomArmorManager;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.customitems.models.CustomArmor;
import org.delawarex.service.CC;

import java.util.ArrayList;

public class ArmorMainMenu extends Menu {

    @Override protected String getTitle() { return "&b&lCustom Armaduras"; }
    @Override protected int getRows()     { return 3; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();

        set(11, item(Material.IRON_CHESTPLATE,
                        "&a&lCrear Armadura",
                        "&7Crea una armadura custom desde",
                        "&7el item en tu mano",
                        "", "&a[CLICK]"),
                e -> createArmor(player));

        set(13, item(Material.BOOK,
                        "&b&lListar Armaduras",
                        "&7Total: &f" + CustomArmorManager.getInstance().getAll().size(),
                        "", "&a[CLICK]"),
                e -> new ArmorListMenu(1).open(player));

        set(15, item(Material.PAPER,
                        "&e&lAyuda",
                        "&7/ca <subcomando>",
                        "&7Subcomandos: give, register,",
                        "&7remove, rename, list,",
                        "&7addstat, addeffect"),
                e -> {});
    }

    private void createArmor(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null || hand.getType() == Material.AIR) {
            player.sendMessage(CC.translate("&cSostén una armadura en la mano."));
            return;
        }

        ChatInput.await(player, "ID para la nueva armadura (sin espacios):", (p, id) -> {
            id = id.toLowerCase().replace(" ", "_");

            if (CustomArmorManager.getInstance().exists(id)) {
                p.sendMessage(CC.translate("&cYa existe una armadura con ese ID."));
                new ArmorMainMenu().open(p);
                return;
            }

            String displayName = "";
            java.util.List<String> lore = new ArrayList<>();

            if (hand.hasItemMeta()) {
                ItemMeta meta = hand.getItemMeta();
                if (meta != null) {
                    if (meta.hasDisplayName()) displayName = meta.getDisplayName();
                    if (meta.hasLore() && meta.getLore() != null) lore = meta.getLore();
                }
            }

            CustomArmor armor = new CustomArmor()
                    .setId(id)
                    .setMaterial(hand.getType().name())   // ← String: "DIAMOND_CHESTPLATE"
                    .setDisplayName(displayName)
                    .setLore(lore);

            CustomArmorManager.getInstance().register(armor);
            p.sendMessage(CC.translate("&aArmadura &f" + id + " &acreada (&f" + hand.getType().name() + "&a)."));
            new ArmorEditMenu(id).open(p);
        });
    }
}