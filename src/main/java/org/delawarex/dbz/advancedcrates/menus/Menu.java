package org.delawarex.dbz.advancedcrates.menus;

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

public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> handlers = new HashMap<>();

    protected abstract String getTitle();
    protected abstract int getRows();
    protected abstract void buildContents(Player player);

    public final void open(Player player) {
        handlers.clear();
        inventory = Bukkit.createInventory(this, getRows() * 9,
                ChatColor.translateAlternateColorCodes('&', getTitle()));
        buildContents(player);
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Consumer<InventoryClickEvent> handler = handlers.get(event.getSlot());
        if (handler != null) handler.accept(event);
    }

    protected void set(int slot, ItemStack item) {
        if (slot >= 0 && slot < inventory.getSize()) inventory.setItem(slot, item);
    }

    protected void set(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        if (slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, item);
            handlers.put(slot, onClick);
        }
    }

    protected ItemStack item(Material mat, String name, String... lorelines) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (lorelines.length > 0) {
            List<String> lore = new ArrayList<>();
            for (String l : lorelines)
                lore.add(ChatColor.translateAlternateColorCodes('&', l));
            meta.setLore(lore);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    protected ItemStack pane(int colorId) {
        Material mat = switch (colorId) {
            case 0  -> Material.BLACK_STAINED_GLASS_PANE;
            case 1  -> Material.GRAY_STAINED_GLASS_PANE;
            case 2  -> Material.ORANGE_STAINED_GLASS_PANE;
            case 3  -> Material.YELLOW_STAINED_GLASS_PANE;
            case 4  -> Material.PURPLE_STAINED_GLASS_PANE;
            case 5  -> Material.BLUE_STAINED_GLASS_PANE;
            case 6  -> Material.GREEN_STAINED_GLASS_PANE;
            case 7  -> Material.RED_STAINED_GLASS_PANE;
            case 8  -> Material.WHITE_STAINED_GLASS_PANE;
            default -> Material.BLACK_STAINED_GLASS_PANE;
        };
        ItemStack g = new ItemStack(mat);
        ItemMeta m = g.getItemMeta();
        if (m != null) { m.setDisplayName(" "); g.setItemMeta(m); }
        return g;
    }

    protected void fillBorder(int colorId) {
        ItemStack p = pane(colorId);
        int rows = getRows();
        for (int i = 0; i < 9; i++) inventory.setItem(i, p);
        for (int i = (rows - 1) * 9; i < rows * 9; i++) inventory.setItem(i, p);
        for (int i = 1; i < rows - 1; i++) {
            inventory.setItem(i * 9,     p);
            inventory.setItem(i * 9 + 8, p);
        }
    }

    protected void fill(int colorId) {
        ItemStack p = pane(colorId);
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, p);
        }
    }

    protected ItemStack back() {
        return item(Material.ARROW, "&b\u2190 &fAtrás");
    }

    protected ItemStack navBtn(String label, boolean active) {
        return item(active ? Material.ARROW : Material.BARRIER,
                active ? "&e" + label : "&8" + label);
    }
}
