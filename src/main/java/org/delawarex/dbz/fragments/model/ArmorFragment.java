package org.delawarex.dbz.fragments.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class ArmorFragment {

    private String id;
    private String attribute;
    private String value;
    private String operation;
    private double numericValue;
    private String displayName;
    private Material material;
    private List<String> lore;

    private static final String FRAGMENT_TAG = "\u00a78[FRAGMENT:%s:%s:%s]";

    public ArmorFragment(String id, String attribute, String value) {
        this.id = id;
        this.attribute = attribute.toUpperCase();
        this.value = value;
        this.lore = new ArrayList<>();
        parseValue(value);
    }

    private void parseValue(String value) {
        value = value.trim();
        if (value.endsWith("%")) {
            this.operation = "*";
            String numStr = value.substring(0, value.length() - 1);
            double percentage = Double.parseDouble(numStr);
            this.numericValue = 1.0 + (percentage / 100.0);
        } else if (value.startsWith("-")) {
            this.operation = "-";
            this.numericValue = Math.abs(Double.parseDouble(value));
        } else {
            this.operation = "+";
            this.numericValue = Double.parseDouble(value);
        }
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(material != null ? material : Material.EMERALD, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(CC.translate(displayName != null ? displayName : "&3Fragmento"));
        List<String> finalLore = new ArrayList<>();
        if (lore != null) finalLore.addAll(lore);
        finalLore.add(String.format(FRAGMENT_TAG, attribute, operation, value));
        meta.setLore(finalLore);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isFragment(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore() || meta.getLore() == null) return false;
        for (String line : meta.getLore()) {
            if (line.contains("[FRAGMENT:")) return true;
        }
        return false;
    }

    public static String getFragmentAttribute(ItemStack item) {
        if (!isFragment(item)) return null;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("[FRAGMENT:")) {
                String clean = line.replace("\u00a78[FRAGMENT:", "").replace("]", "");
                return clean.split(":")[0];
            }
        }
        return null;
    }

    public static String getFragmentOperation(ItemStack item) {
        if (!isFragment(item)) return null;
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("[FRAGMENT:")) {
                String clean = line.replace("\u00a78[FRAGMENT:", "").replace("]", "");
                String[] parts = clean.split(":");
                return parts.length > 1 ? parts[1] : "+";
            }
        }
        return "+";
    }

    public static String getFragmentValueRaw(ItemStack item) {
        if (!isFragment(item)) return "0";
        for (String line : item.getItemMeta().getLore()) {
            if (line.contains("[FRAGMENT:")) {
                String clean = line.replace("\u00a78[FRAGMENT:", "").replace("]", "");
                String[] parts = clean.split(":");
                return parts.length > 2 ? parts[2] : "0";
            }
        }
        return "0";
    }

    public static double getFragmentValue(ItemStack item) {
        String raw = getFragmentValueRaw(item);
        try {
            if (raw.endsWith("%")) {
                double percentage = Double.parseDouble(raw.substring(0, raw.length() - 1));
                return 1.0 + (percentage / 100.0);
            } else {
                return Math.abs(Double.parseDouble(raw));
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getId() { return id; }
    public String getAttribute() { return attribute; }
    public String getValue() { return value; }
    public String getOperation() { return operation; }
    public double getNumericValue() { return numericValue; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public List<String> getLore() { return lore; }

    public void setId(String id) { this.id = id; }
    public void setAttribute(String attribute) { this.attribute = attribute; }
    public void setValue(String value) { this.value = value; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setLore(List<String> lore) { this.lore = lore; }
}