package org.delawarex.dbz.customitems.menus.armor;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.managers.CustomArmorManager;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.customitems.models.CustomArmor;

import java.util.ArrayList;
import java.util.List;

public class ArmorListMenu extends Menu {

    private static final int PAGE_SIZE = 28;
    private final int page;

    public ArmorListMenu(int page) { this.page = page; }

    @Override protected String getTitle() { return "&b&lArmaduras Custom — Pág. " + page; }
    @Override protected int getRows()     { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();

        List<String> ids   = CustomArmorManager.getInstance().getSortedIds();
        int total          = ids.size();
        int pages          = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start          = (page - 1) * PAGE_SIZE;
        int end            = Math.min(start + PAGE_SIZE, total);
        int[] innerSlots   = buildInnerSlots();

        for (int i = start; i < end; i++) {
            String id     = ids.get(i);
            CustomArmor armor = CustomArmorManager.getInstance().get(id);
            if (armor == null) continue;

            // Build the real ItemStack so the GUI shows the actual armor piece with its name/lore
            ItemStack display = CustomArmorManager.getInstance().buildItemStack(armor);

            // Append click hint to lore without modifying the stored armor
            ItemStack displayClone = display.clone();
            ItemMeta meta = displayClone.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() && meta.getLore() != null
                        ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(ChatColor.DARK_GRAY + "ID: " + id);
                lore.add("");
                lore.add(ChatColor.GREEN + "[CLICK para editar]");
                meta.setLore(lore);
                displayClone.setItemMeta(meta);
            }

            int slot = innerSlots[i - start];
            set(slot, displayClone, e -> new ArmorEditMenu(id).open(player));
        }

        // Navigation
        if (page > 1)
            set(45, navButton("◀ Anterior", true), e -> new ArmorListMenu(page - 1).open(player));

        set(49, item(org.bukkit.Material.BOOK,
                "&f&lPágina " + page + "/" + Math.max(1, pages)));

        if (page < pages)
            set(53, navButton("Siguiente ▶", true), e -> new ArmorListMenu(page + 1).open(player));

        set(48, back(), e -> new ArmorMainMenu().open(player));
    }

    private int[] buildInnerSlots() {
        int[] slots = new int[PAGE_SIZE];
        int idx = 0;
        for (int row = 1; row <= 4; row++)
            for (int col = 1; col <= 7; col++)
                slots[idx++] = row * 9 + col;
        return slots;
    }
}