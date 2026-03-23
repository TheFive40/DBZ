package org.delawarex.dbz.customitems.menus.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.managers.CustomItemManager;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.customitems.models.CustomItem;
import org.delawarex.service.CC;

public class ItemEffectMenu extends Menu {

    private static final String[] EFFECTS = {"HEALTHREGEN","KIREGEN","STAMINAREGEN"};

    private final String itemId;

    public ItemEffectMenu(String itemId) { this.itemId = itemId; }

    @Override protected String getTitle() { return "&6&lEfectos - " ; }
    @Override protected int getRows()     { return 3; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        CustomItem item = CustomItemManager.getInstance().get(itemId);
        if (item == null) return;

        int[] slots = {10, 13, 16};
        Material[] mats = {Material.REDSTONE, Material.LAPIS_LAZULI, Material.GOLD_NUGGET};
        String[] labels = {"&c❤ HEALTHREGEN", "&9⚡ KIREGEN", "&e❃ STAMINAREGEN"};

        for (int i = 0; i < EFFECTS.length; i++) {
            final String eff = EFFECTS[i];
            double current = item.getEffects().getOrDefault(eff, 0.0);
            set(slots[i], item(mats[i], labels[i],
                            "&7Valor actual: &f" + (current * 100) + "%",
                            "&7Rango: 0.0 - 1.0 (0% - 100%)",
                            "", "&a[CLICK para cambiar]"),
                    e -> ChatInput.await(player, "Valor para " + eff + " (0.0 - 1.0):", (p, text) -> {
                        try {
                            double val = Double.parseDouble(text);
                            if (val < 0 || val > 1) throw new NumberFormatException();
                            item.getEffects().put(eff, val);
                            CustomItemManager.getInstance().update(item);
                            p.sendMessage(CC.translate("&aEfecto actualizado: &f" + eff + " = " + (val*100) + "%"));
                        } catch (NumberFormatException ex) {
                            p.sendMessage(CC.translate("&cValor inválido. Usa 0.0 - 1.0"));
                        }
                        new ItemEffectMenu(itemId).open(p);
                    }));
        }

        set(18, back(), e -> new ItemEditMenu(itemId).open(player));
    }
}