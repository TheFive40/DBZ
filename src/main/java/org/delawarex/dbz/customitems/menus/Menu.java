package org.delawarex.dbz.customitems.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Lightweight inventory-menu framework compatible with Arclight 1.20.1.
 * Uses the legacy String-based createInventory to avoid Adventure API issues.
 */
public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> handlers = new HashMap<>();

    protected abstract String getTitle();
    protected abstract int getRows();
    protected abstract void buildContents(Player player);

    /* ── Open ── */

    public final void open(Player player) {
        handlers.clear();
        // Use legacy String title - works on Arclight and all Bukkit/Spigot/Paper variants
        String title = ChatColor.translateAlternateColorCodes('&', getTitle());
        inventory = Bukkit.createInventory(this, getRows() * 9, title);
        buildContents(player);
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    /* ── Click dispatch ── */

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Consumer<InventoryClickEvent> handler = handlers.get(event.getSlot());
        if (handler != null) handler.accept(event);
    }

    /* ── Slot setters ── */

    protected void set(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    protected void set(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        inventory.setItem(slot, item);
        handlers.put(slot, onClick);
    }

    /* ── Item builders ── */

    protected ItemStack item(Material mat, String name, String... loreLines) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (loreLines.length > 0) {
            List<String> lore = new ArrayList<>();
            for (String l : loreLines)
                lore.add(ChatColor.translateAlternateColorCodes('&', l));
            meta.setLore(lore);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    protected ItemStack glass() {
        ItemStack g = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = g.getItemMeta();
        if (m != null) { m.setDisplayName(" "); g.setItemMeta(m); }
        return g;
    }

    protected void fillBorder() {
        ItemStack pane = glass();
        int rows = getRows();
        for (int i = 0; i < 9; i++) inventory.setItem(i, pane);
        for (int i = (rows - 1) * 9; i < rows * 9; i++) inventory.setItem(i, pane);
        for (int i = 1; i < rows - 1; i++) {
            inventory.setItem(i * 9,     pane);
            inventory.setItem(i * 9 + 8, pane);
        }
    }

    protected ItemStack back() {
        return item(Material.ARROW, "&b← &fAtrás");
    }

    protected ItemStack navButton(String label, boolean active) {
        return item(active ? Material.ARROW : Material.BARRIER,
                active ? "&e" + label : "&7" + label);
    }
}