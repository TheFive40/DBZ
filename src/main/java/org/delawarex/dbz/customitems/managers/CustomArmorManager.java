package org.delawarex.dbz.customitems.managers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.models.CustomArmor;
import org.delawarex.dbz.customitems.storage.CustomArmorStorage;
import org.delawarex.service.CC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomArmorManager {

    private static CustomArmorManager instance;
    private final ConcurrentHashMap<String, CustomArmor> armors = new ConcurrentHashMap<>();
    private final CustomArmorStorage storage;

    private CustomArmorManager() {
        storage = new CustomArmorStorage();
        armors.putAll(storage.loadAll());
    }

    public static CustomArmorManager getInstance() {
        if (instance == null) instance = new CustomArmorManager();
        return instance;
    }

    /* ── CRUD ── */

    public boolean register(CustomArmor armor) {
        if (armors.containsKey(armor.getId())) return false;
        armors.put(armor.getId(), armor);
        storage.saveArmor(armor);
        return true;
    }

    public void update(CustomArmor armor) {
        armors.put(armor.getId(), armor);
        storage.saveArmor(armor);
    }

    public boolean delete(String id) {
        if (!armors.containsKey(id)) return false;
        armors.remove(id);
        storage.deleteArmor(id);
        return true;
    }

    public CustomArmor get(String id)        { return armors.get(id); }
    public boolean     exists(String id)     { return armors.containsKey(id); }
    public Map<String, CustomArmor> getAll() { return Collections.unmodifiableMap(armors); }

    public List<String> getSortedIds() {
        List<String> ids = new ArrayList<>(armors.keySet());
        Collections.sort(ids);
        return ids;
    }

    /* ── ItemStack builder ── */

    public ItemStack buildItemStack(CustomArmor armor) {
        Material mat = parseMaterial(armor.getMaterial(), Material.IRON_CHESTPLATE);

        ItemStack stack = new ItemStack(mat, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        String display = (armor.getDisplayName() != null && !armor.getDisplayName().isEmpty())
                ? CC.translate(armor.getDisplayName())
                : org.bukkit.ChatColor.WHITE + mat.name();
        meta.setDisplayName(display);

        if (armor.getLore() != null && !armor.getLore().isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String line : armor.getLore())
                lore.add(CC.translate(line));
            meta.setLore(lore);
        }

        meta.setUnbreakable(armor.isUnbreakable());
        stack.setItemMeta(meta);

        // Aplicar durabilidad custom si está configurada
        if (armor.getMaxDurability() > 0) {
            CustomDurabilityManager.setCustomMaxDurability(stack, armor.getMaxDurability());
        }

        return stack;
    }

    /**
     * Identifies a CustomArmor from an ItemStack.
     */
    public CustomArmor identify(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return null;

        String matName = stack.getType().name();

        String displayName = "";
        List<String> lore  = new ArrayList<>();

        if (stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) displayName = meta.getDisplayName();
                if (meta.hasLore() && meta.getLore() != null) {
                    // Filtrar línea de durabilidad para no interferir con la identificación
                    for (String line : meta.getLore()) {
                        String clean = org.bukkit.ChatColor.stripColor(line);
                        if (clean != null && !clean.trim().matches("^\\d+/\\d+ \\(\\d+%\\)$")) {
                            lore.add(line);
                        }
                    }
                }
            }
        }

        CustomArmor best      = null;
        int         bestScore = -1;

        for (CustomArmor armor : armors.values()) {
            if (!matName.equalsIgnoreCase(armor.getMaterial())) continue;

            int score = 0;

            String registeredName = (armor.getDisplayName() != null)
                    ? CC.translate(armor.getDisplayName()) : "";

            if (!registeredName.isEmpty()) {
                if (!registeredName.equals(displayName)) continue;
                score += 10;
            } else {
                if (!displayName.isEmpty()) continue;
            }

            List<String> regLore = armor.getLore() != null ? armor.getLore() : new ArrayList<>();
            List<String> translatedLore = new ArrayList<>();
            for (String l : regLore) translatedLore.add(CC.translate(l));

            if (!translatedLore.isEmpty() && translatedLore.equals(lore)) score += 5;

            if (score > bestScore) {
                bestScore = score;
                best      = armor;
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

    public CustomArmorStorage getStorage() { return storage; }
}