package org.delawarex.dbz.advancedcrates.models;

import org.bukkit.Material;
import org.delawarex.service.CC;

public enum Rarity {
    COMMON("&7Común", "&7", Material.GRAY_DYE),
    RARE("&9Rara", "&9", Material.LAPIS_LAZULI),
    EPIC("&5Épica", "&5", Material.AMETHYST_SHARD),
    LEGENDARY("&6&lLegendaria", "&6", Material.GOLD_INGOT),
    MYTHIC("&d&lMítica", "&d", Material.NETHER_STAR);

    private final String display;
    private final String colorCode;
    private final Material icon;

    Rarity(String display, String colorCode, Material icon) {
        this.display = display;
        this.colorCode = colorCode;
        this.icon = icon;
    }

    public String getDisplay() {
        return CC.translate(display);
    }

    public String getColorCode() {
        return colorCode;
    }

    public Material getIcon() {
        return icon;
    }

    public static Rarity fromString(String name) {
        if (name == null) return COMMON;
        for (Rarity r : values()) {
            if (r.name().equalsIgnoreCase(name)) return r;
        }
        return COMMON;
    }
}
