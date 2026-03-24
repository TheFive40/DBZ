package org.delawarex.dbz.customitems.managers;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.service.CC;
import java.util.ArrayList;
import java.util.List;

public class CustomDurabilityManager {

    private static final String DURABILITY_REGEX = "^\\d+/\\d+ \\(\\d+%\\)$";

    public static void setCustomMaxDurability(ItemStack item, int max) {
        setCustomDurability(item, max, max);
    }

    public static void setCustomDurability(ItemStack item, int current, int max) {
        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        current = Math.max(0, Math.min(current, max));
        List<String> lore = (meta.hasLore() && meta.getLore() != null)
                ? new ArrayList<>(meta.getLore())
                : new ArrayList<>();
        lore.removeIf(CustomDurabilityManager::isDurabilityLine);
        double pct = max > 0 ? (double) current / max * 100.0 : 0.0;
        String durLine = CC.translate(getDurabilityColor(pct)
                + current + "/" + max
                + " (" + String.format("%.0f", pct) + "%)");
        lore.add(0, durLine);

        meta.setLore(lore);
        item.setItemMeta(meta);

        syncVisualBar(item, current, max);
    }

    public static boolean hasCustomDurability(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore() || meta.getLore() == null) return false;
        return meta.getLore().stream().anyMatch(CustomDurabilityManager::isDurabilityLine);
    }

    public static int getCustomDurability(ItemStack item) {
        return readFromLore(item, 0);
    }

    public static int getCustomMaxDurability(ItemStack item) {
        return readFromLore(item, 1);
    }

    public static boolean damageItem(ItemStack item, int damage) {
        if (!hasCustomDurability(item)) return false;

        int current = getCustomDurability(item);
        int max     = getCustomMaxDurability(item);
        current -= damage;

        if (current <= 0) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.isUnbreakable()) {
                setCustomDurability(item, 1, max);
                return false;
            }
            return true; // debe romperse
        }

        setCustomDurability(item, current, max);
        return false;
    }

    public static void repairItem(ItemStack item, int amount) {
        if (!hasCustomDurability(item)) return;
        int current = getCustomDurability(item);
        int max     = getCustomMaxDurability(item);
        setCustomDurability(item, Math.min(current + amount, max), max);
    }

    public static void repairItemFull(ItemStack item) {
        if (!hasCustomDurability(item)) return;
        setCustomDurability(item, getCustomMaxDurability(item), getCustomMaxDurability(item));
    }

    public static void removeDurabilityFromLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore() || meta.getLore() == null) return;
        List<String> lore = new ArrayList<>(meta.getLore());
        lore.removeIf(CustomDurabilityManager::isDurabilityLine);
        meta.setLore(lore);
        item.setItemMeta(meta);
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(0);
            item.setItemMeta(meta);
        }
    }

    private static void syncVisualBar(ItemStack item, int current, int max) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable)) return;

        double pct      = max > 0 ? (double) current / max : 0.0;
        short vanillaMax = (short) item.getType().getMaxDurability();

        if (vanillaMax > 0) {
            int damage = (int) (vanillaMax * (1.0 - pct));
            damage = Math.max(0, Math.min(damage, vanillaMax - 1));
            ((Damageable) meta).setDamage(damage);
        }

        item.setItemMeta(meta);
    }
    private static int readFromLore(ItemStack item, int part) {
        if (item == null || item.getType() == Material.AIR) return 0;
        if (!item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore() || meta.getLore() == null) return 0;

        for (String line : meta.getLore()) {
            if (!isDurabilityLine(line)) continue;
            String clean = org.bukkit.ChatColor.stripColor(line);
            if (clean == null) continue;
            try {
                String[] parts = clean.split("/");
                if (part == 0) return Integer.parseInt(parts[0].trim());
                return Integer.parseInt(parts[1].split(" ")[0].trim());
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private static boolean isDurabilityLine(String line) {
        if (line == null) return false;
        String clean = org.bukkit.ChatColor.stripColor(line);
        return clean != null && clean.trim().matches(DURABILITY_REGEX);
    }
    private static String getDurabilityColor(double pct) {
        if (pct > 60) return "&a";
        if (pct > 30) return "&e";
        return "&c";
    }
}