package org.delawarex.dbz.customitems.menus.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.managers.CustomArmorManager;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.customitems.models.CustomArmor;
import org.delawarex.service.CC;

public class ArmorEffectMenu extends Menu {

    private static final String[] EFFECTS = {"HEALTHREGEN","KIREGEN","STAMINAREGEN"};

    private final String armorId;

    public ArmorEffectMenu(String armorId) { this.armorId = armorId; }

    @Override protected String getTitle() { return "&6&lEfectos Armadura - "; }
    @Override protected int getRows()     { return 3; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        CustomArmor armor = CustomArmorManager.getInstance().get(armorId);
        if (armor == null) return;

        int[] slots  = {10, 13, 16};
        Material[] mats   = {Material.REDSTONE, Material.LAPIS_LAZULI, Material.GOLD_NUGGET};
        String[]   labels = {"&c❤ HEALTHREGEN", "&9⚡ KIREGEN", "&e❃ STAMINAREGEN"};

        for (int i = 0; i < EFFECTS.length; i++) {
            final String eff = EFFECTS[i];
            double current = armor.getEffects().getOrDefault(eff, 0.0);
            set(slots[i], item(mats[i], labels[i],
                            "&7Valor: &f" + (current * 100) + "%",
                            "&7Rango: 0.0 - 1.0",
                            "", "&a[CLICK para cambiar]"),
                    e -> ChatInput.await(player, "Valor para " + eff + " (0.0 - 1.0):", (p, text) -> {
                        try {
                            double val = Double.parseDouble(text);
                            if (val < 0 || val > 1) throw new NumberFormatException();
                            armor.getEffects().put(eff, val);
                            CustomArmorManager.getInstance().update(armor);
                            p.sendMessage(CC.translate("&aEfecto: &f" + eff + " = " + (val * 100) + "%"));
                        } catch (NumberFormatException ex) {
                            p.sendMessage(CC.translate("&cValor inválido. Usa 0.0 - 1.0"));
                        }
                        new ArmorEffectMenu(armorId).open(p);
                    }));
        }

        set(18, back(), e -> new ArmorEditMenu(armorId).open(player));
    }
}