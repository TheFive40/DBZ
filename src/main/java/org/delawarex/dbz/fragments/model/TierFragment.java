package org.delawarex.dbz.fragments.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class TierFragment {

    private String id;
    private String targetTier;
    private String displayName;
    private Material material;
    private List<String> lore;

    private static final String TIER_FRAGMENT_TAG = "\u00a78[TIER_FRAGMENT:%s]";

    public TierFragment(String id, String targetTier) {
        this.id = id;
        this.targetTier = targetTier.toUpperCase();
        this.lore = new ArrayList<>();
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(material != null ? material : Material.EMERALD, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(CC.translate(displayName != null ? displayName : "&bFragmento de Tier"));
        List<String> finalLore = new ArrayList<>();
        if (lore != null) finalLore.addAll(lore);
        finalLore.add(String.format(TIER_FRAGMENT_TAG, targetTier));
        meta.setLore(finalLore);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isTierFragment(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore() || meta.getLore() == null) return false;
        for (String line : meta.getLore()) {
            if (line.contains("[TIER_FRAGMENT:")) return true;
        }
        return false;
    }

    public static String getTargetTier(ItemStack item) {
        if (!isTierFragment(item)) return null;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("[TIER_FRAGMENT:")) {
                return line.replace("\u00a78[TIER_FRAGMENT:", "").replace("]", "");
            }
        }
        return null;
    }

    public static int getTierNumber(String tierName) {
        if (tierName == null) return -1;
        if (tierName.matches("^TIER_\\d+$")) {
            try {
                return Integer.parseInt(tierName.substring(5));
            } catch (Exception e) {
                return -1;
            }
        }
        if (tierName.equalsIgnoreCase("VIP")) return 999;
        return -1;
    }

    public static boolean canUpgrade(String currentTier, String fragmentTargetTier) {
        int currentNum = getTierNumber(currentTier);
        int targetNum = getTierNumber(fragmentTargetTier);
        if (currentNum == -1 || targetNum == -1) return false;
        return targetNum == currentNum + 1;
    }

    public String getId() { return id; }
    public String getTargetTier() { return targetTier; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public List<String> getLore() { return lore; }

    public void setId(String id) { this.id = id; }
    public void setTargetTier(String targetTier) { this.targetTier = targetTier; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setLore(List<String> lore) { this.lore = lore; }
}