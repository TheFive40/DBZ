package org.delawarex.dbz.fragments.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.service.CC;

import java.util.*;

public class CustomizedArmor {

    private String hash;
    private String tier;
    private Map<String, Integer> attributes;
    private Map<String, String> operations;
    private String materialType;
    private String armorSlot;
    private String displayName;

    private static final String HASH_TAG = "\u00a78[ID:%s]";
    private static final String TIER_TAG = "\u00a78[TIER:%s]";
    private static final String ATTR_TAG = "\u00a78[ATTR:%s:%s:%d]";

    public CustomizedArmor() {
        this.attributes = new HashMap<>();
        this.operations = new HashMap<>();
    }

    public CustomizedArmor(String hash, String tier) {
        this.hash = hash;
        this.tier = tier;
        this.attributes = new HashMap<>();
        this.operations = new HashMap<>();
    }

    public void addAttribute(String attribute, int value) {
        int current = attributes.getOrDefault(attribute, 0);
        attributes.put(attribute, current + value);
    }

    public int getAttributeValue(String attribute) {
        return attributes.getOrDefault(attribute, 0);
    }

    public static boolean isCustomized(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore() || meta.getLore() == null) return false;
        for (String line : meta.getLore()) {
            if (line.contains("[ID:")) return true;
        }
        return false;
    }

    public static String getHash(ItemStack item) {
        if (!item.hasItemMeta() || item.getItemMeta() == null) return null;
        if (!item.getItemMeta().hasLore() || item.getItemMeta().getLore() == null) return null;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("[ID:")) {
                return line.replace("\u00a78[ID:", "").replace("]", "");
            }
        }
        return null;
    }

    public static String getTier(ItemStack item) {
        if (!item.hasItemMeta() || item.getItemMeta() == null) return null;
        if (!item.getItemMeta().hasLore() || item.getItemMeta().getLore() == null) return null;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("[TIER:")) {
                return line.replace("\u00a78[TIER:", "").replace("]", "");
            }
        }
        return null;
    }

    public static Map<String, Integer> getAttributes(ItemStack item) {
        Map<String, Integer> attributes = new HashMap<>();
        if (!item.hasItemMeta() || item.getItemMeta() == null) return attributes;
        if (!item.getItemMeta().hasLore() || item.getItemMeta().getLore() == null) return attributes;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("[ATTR:")) {
                String clean = line.replace("\u00a78[ATTR:", "").replace("]", "");
                String[] parts = clean.split(":");
                try {
                    if (parts.length >= 3) {
                        attributes.put(parts[0], Integer.parseInt(parts[2]));
                    } else if (parts.length >= 2) {
                        attributes.put(parts[0], Integer.parseInt(parts[1]));
                    }
                } catch (Exception ignored) {}
            }
        }
        return attributes;
    }

    public static Map<String, String> getOperations(ItemStack item) {
        Map<String, String> operations = new HashMap<>();
        if (!item.hasItemMeta() || item.getItemMeta() == null) return operations;
        if (!item.getItemMeta().hasLore() || item.getItemMeta().getLore() == null) return operations;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("[ATTR:")) {
                String clean = line.replace("\u00a78[ATTR:", "").replace("]", "");
                String[] parts = clean.split(":");
                try {
                    if (parts.length >= 3) {
                        operations.put(parts[0], parts[1]);
                    } else if (parts.length >= 2) {
                        operations.put(parts[0], "+");
                    }
                } catch (Exception ignored) {}
            }
        }
        return operations;
    }

    public void applyToItemStack(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.hasLore() && meta.getLore() != null
                ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(displayName);
        }

        lore.removeIf(line -> line.contains("[ID:") || line.contains("[TIER:") || line.contains("[ATTR:")
                || line.contains("\u00a78\u00a7m--------------------")
                || line.contains("\u2694 Atributos:") || (
                org.bukkit.ChatColor.stripColor(line) != null &&
                        org.bukkit.ChatColor.stripColor(line).contains("  \u2022 ") && (
                        org.bukkit.ChatColor.stripColor(line).contains("STR:") ||
                                org.bukkit.ChatColor.stripColor(line).contains("SKP:") ||
                                org.bukkit.ChatColor.stripColor(line).contains("RES:") ||
                                org.bukkit.ChatColor.stripColor(line).contains("VIT:") ||
                                org.bukkit.ChatColor.stripColor(line).contains("PWR:") ||
                                org.bukkit.ChatColor.stripColor(line).contains("ENE:"))));

        if (!attributes.isEmpty()) {
            lore.add(CC.translate("&8&m--------------------"));
            lore.add(CC.translate("&3\u2694 Atributos:"));
            for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
                String attrName = entry.getKey();
                int storedValue = entry.getValue();
                String operation = operations.getOrDefault(entry.getKey(), "+");
                String displayValue;
                if (operation.equals("*")) {
                    displayValue = storedValue >= 0
                            ? CC.translate("&b+" + storedValue + "%")
                            : CC.translate("&b" + storedValue + "%");
                } else if (operation.equals("-")) {
                    displayValue = CC.translate("&c-" + storedValue);
                } else {
                    displayValue = CC.translate("&a+" + storedValue);
                }
                lore.add(CC.translate("&7  \u2022 " + attrName + ": ") + displayValue);
            }
            lore.add(CC.translate("&8&m--------------------"));
        }

        lore.add(String.format(HASH_TAG, hash));
        lore.add(String.format(TIER_TAG, tier));
        for (Map.Entry<String, Integer> entry : attributes.entrySet()) {
            String attr = entry.getKey();
            int value = entry.getValue();
            String op = operations.getOrDefault(attr, "+");
            lore.add(String.format(ATTR_TAG, attr, op, value));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static CustomizedArmor fromItemStack(ItemStack item) {
        String hash = getHash(item);
        String tier = getTier(item);
        if (hash == null || tier == null) return null;
        CustomizedArmor armor = new CustomizedArmor(hash, tier);
        armor.setMaterialType(item.getType().name());
        armor.setArmorSlot(getArmorSlotFromItem(item));
        armor.attributes.putAll(getAttributes(item));
        armor.operations.putAll(getOperations(item));
        return armor;
    }

    private static String getArmorSlotFromItem(ItemStack item) {
        String typeName = item.getType().name();
        if (typeName.contains("HELMET")) return "HELMET";
        if (typeName.contains("CHESTPLATE")) return "CHESTPLATE";
        if (typeName.contains("LEGGINGS")) return "LEGGINGS";
        if (typeName.contains("BOOTS")) return "BOOTS";
        return "UNKNOWN";
    }

    public String getHash() { return hash; }
    public String getTier() { return tier; }
    public Map<String, Integer> getAttributes() { return attributes; }
    public Map<String, String> getOperations() { return operations; }
    public String getMaterialType() { return materialType; }
    public String getArmorSlot() { return armorSlot; }
    public String getDisplayName() { return displayName; }

    public void setHash(String hash) { this.hash = hash; }
    public void setTier(String tier) { this.tier = tier; }
    public void setAttributes(Map<String, Integer> attributes) { this.attributes = attributes; }
    public void setOperations(Map<String, String> operations) { this.operations = operations; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public void setArmorSlot(String armorSlot) { this.armorSlot = armorSlot; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}