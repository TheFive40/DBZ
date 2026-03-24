package org.delawarex.dbz.customitems.managers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.models.CustomItem;
import org.delawarex.dbz.customitems.storage.CustomItemStorage;
import org.delawarex.service.CC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomItemManager {

    private static CustomItemManager instance;
    private final ConcurrentHashMap<String, CustomItem> items = new ConcurrentHashMap<>();
    private final CustomItemStorage storage;

    private CustomItemManager() {
        storage = new CustomItemStorage();
        items.putAll(storage.loadAll());
    }

    public static CustomItemManager getInstance() {
        if (instance == null) instance = new CustomItemManager();
        return instance;
    }


    public boolean register(CustomItem item) {
        if (items.containsKey(item.getId())) return false;
        items.put(item.getId(), item);
        storage.saveItem(item);
        return true;
    }

    public void update(CustomItem item) {
        items.put(item.getId(), item);
        storage.saveItem(item);
    }

    public boolean delete(String id) {
        if (!items.containsKey(id)) return false;
        items.remove(id);
        storage.deleteItem(id);
        return true;
    }

    public CustomItem get(String id)       { return items.get(id); }
    public boolean    exists(String id)    { return items.containsKey(id); }
    public Map<String, CustomItem> getAll(){ return Collections.unmodifiableMap(items); }

    public List<String> getSortedIds() {
        List<String> ids = new ArrayList<>(items.keySet());
        Collections.sort(ids);
        return ids;
    }


    public ItemStack buildItemStack(CustomItem item) {
        Material mat = parseMaterial(item.getMaterial(), Material.STONE);

        ItemStack stack = new ItemStack(mat, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        String display = (item.getDisplayName() != null && !item.getDisplayName().isEmpty())
                ? CC.translate(item.getDisplayName())
                : org.bukkit.ChatColor.WHITE + mat.name();
        meta.setDisplayName(display);

        if (item.getLore() != null && !item.getLore().isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String line : item.getLore())
                lore.add(CC.translate(line));
            meta.setLore(lore);
        }

        meta.setUnbreakable(item.isUnbreakable());
        stack.setItemMeta(meta);

        // Aplicar durabilidad custom si está configurada
        if (item.getMaxDurability() > 0) {
            CustomDurabilityManager.setCustomMaxDurability(stack, item.getMaxDurability());
        }

        return stack;
    }


    public CustomItem identify(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return null;

        String matName = stack.getType().name();

        String displayName = "";
        List<String> lore  = new ArrayList<>();

        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) displayName = meta.getDisplayName();
                if (meta.hasLore() && meta.getLore() != null) {
                    // Filtrar línea de durabilidad para que no rompa la identificación
                    for (String line : meta.getLore()) {
                        String clean = org.bukkit.ChatColor.stripColor(line);
                        if (clean != null && !clean.trim().matches("^\\d+/\\d+ \\(\\d+%\\)$")) {
                            lore.add(line);
                        }
                    }
                }
            }
        }

        CustomItem best     = null;
        int        bestScore = -1;

        for (CustomItem item : items.values()) {
            if (!matName.equalsIgnoreCase(item.getMaterial())) continue;

            int score = 0;

            String registeredName = (item.getDisplayName() != null)
                    ? CC.translate(item.getDisplayName()) : "";

            if (!registeredName.isEmpty()) {
                if (!registeredName.equals(displayName)) continue;
                score += 10;
            } else {
                if (!displayName.isEmpty()) continue;
            }

            List<String> regLore = item.getLore() != null ? item.getLore() : new ArrayList<>();
            List<String> translatedLore = new ArrayList<>();
            for (String l : regLore) translatedLore.add(CC.translate(l));

            if (!translatedLore.isEmpty() && translatedLore.equals(lore)) score += 5;

            if (score > bestScore) {
                bestScore = score;
                best      = item;
            }
        }

        return best;
    }

    private Material parseMaterial(String name, Material fallback) {
        if (name == null || name.isEmpty()) return fallback;
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    public CustomItemStorage getStorage() { return storage; }
}