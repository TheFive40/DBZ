package org.delawarex.dbz.advancedcrates.managers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.service.CC;
import org.delawarex.dbz.advancedcrates.models.Crate;
import org.delawarex.dbz.advancedcrates.storage.CrateStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CrateManager {

    private final Map<String, Crate> crates = new ConcurrentHashMap<>();
    private final CrateStorage storage;
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public CrateManager(CrateStorage storage) {
        this.storage = storage;
    }

    public void loadAll() {
        crates.clear();
        Map<String, Crate> loaded = storage.loadAll();
        crates.putAll(loaded);
        loaded.keySet().stream()
                .filter(k -> k.matches("crate_\\d+"))
                .mapToInt(k -> Integer.parseInt(k.replace("crate_", "")))
                .max()
                .ifPresent(idCounter::set);
    }

    public void saveAll() {
        crates.values().forEach(storage::saveCrate);
    }

    public Crate createCrate(String id) {
        Crate crate = new Crate(id);
        crates.put(id, crate);
        storage.saveCrate(crate);
        return crate;
    }

    public Crate createCrate() {
        String id = "crate_" + idCounter.incrementAndGet();
        return createCrate(id);
    }

    public void saveCrate(Crate crate) {
        crates.put(crate.getId(), crate);
        storage.saveCrate(crate);
    }

    public void deleteCrate(String id) {
        crates.remove(id);
        storage.deleteCrate(id);
    }

    public Crate getCrate(String id) { return crates.get(id); }
    public boolean exists(String id) { return crates.containsKey(id); }
    public Collection<Crate> getAll() { return new ArrayList<>(crates.values()); }
    public int getTotal() { return crates.size(); }

    public void reload() {
        crates.clear();
        storage.reload();
        loadAll();
    }

    public boolean hasKey(Player player, Crate crate) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isKey(item, crate.getKeyId())) return true;
        }
        return false;
    }

    public boolean consumeKey(Player player, Crate crate) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (isKey(item, crate.getKeyId())) {
                if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                else player.getInventory().setItem(i, null);
                player.updateInventory();
                return true;
            }
        }
        return false;
    }

    private boolean isKey(ItemStack item, String keyId) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore() || meta.getLore() == null) return false;
        String tag = ChatColor.DARK_GRAY + "[KEY:" + keyId + "]";
        return meta.getLore().stream().anyMatch(line -> line.equals(tag));
    }

    public ItemStack buildKeyItem(Crate crate) {
        Material mat;
        try { mat = Material.valueOf(crate.getKeyMaterial().toUpperCase()); }
        catch (Exception e) { mat = Material.TRIPWIRE_HOOK; }

        ItemStack key = new ItemStack(mat);
        ItemMeta meta = key.getItemMeta();
        meta.setDisplayName(CC.translate(crate.getKeyDisplayName()));
        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7Crate: &f" + CC.strip(crate.getDisplayName())));
        lore.add(CC.translate("&7Rareza: " + crate.getRarity().getDisplay()));
        lore.add("");
        lore.add(CC.translate("&e\u25BA Clic derecho en la crate para usarla"));
        lore.add(CC.translate("&e\u25BA O desde el menú &7/crates"));
        lore.add(ChatColor.DARK_GRAY + "[KEY:" + crate.getKeyId() + "]");
        meta.setLore(lore);
        key.setItemMeta(meta);
        return key;
    }

    public ItemStack buildCrateDisplayItem(Crate crate) {
        ItemStack base;
        if (crate.getVisualItem() != null) {
            base = crate.getVisualItem().clone();
        } else {
            Material mat;
            try { mat = Material.valueOf(crate.getMaterial().toUpperCase()); }
            catch (Exception e) { mat = Material.CHEST; }
            base = new ItemStack(mat);
        }

        ItemMeta meta = base.getItemMeta();
        if (meta == null) return base;
        meta.setDisplayName(CC.translate(crate.getDisplayName()));

        List<String> lore = new ArrayList<>();
        if (crate.getLore() != null && !crate.getLore().isEmpty()) {
            crate.getLore().forEach(l -> lore.add(CC.translate(l)));
            lore.add("");
        }
        lore.add(CC.translate("&7Rareza: " + crate.getRarity().getDisplay()));
        lore.add(CC.translate("&7Recompensas: &f" + crate.getRewards().size()));
        lore.add("");
        lore.add(CC.translate("&a\u25BA Clic para abrir"));
        lore.add(CC.translate("&e\u25BA Shift+Clic para previsualizar"));
        meta.setLore(lore);
        base.setItemMeta(meta);
        return base;
    }
}
