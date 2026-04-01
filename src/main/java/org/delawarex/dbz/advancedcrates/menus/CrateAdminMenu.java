package org.delawarex.dbz.advancedcrates.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.DbzMain;
import org.delawarex.service.CC;
import org.delawarex.dbz.advancedcrates.managers.ChatInputManager;
import org.delawarex.dbz.advancedcrates.managers.CrateManager;
import org.delawarex.dbz.advancedcrates.models.Crate;

import java.util.ArrayList;
import java.util.List;

public class CrateAdminMenu extends Menu {

    private static final int PAGE_SIZE = 21;
    private final int page;

    public CrateAdminMenu(int page) { this.page = page; }

    @Override protected String getTitle() { return "&c&l\u2699 Admin Crates"; }
    @Override protected int getRows()     { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder(7);

        CrateManager mgr = DbzMain.get().getCrateManager();
        List<Crate> all   = new ArrayList<>(mgr.getAll());
        int total  = all.size();
        int pages  = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start  = (page - 1) * PAGE_SIZE;
        int end    = Math.min(start + PAGE_SIZE, total);
        int[] slots = buildInnerSlots();

        for (int i = start; i < end; i++) {
            Crate crate  = all.get(i);
            int slot     = slots[i - start];
            ItemStack di = buildAdminDisplay(crate);
            set(slot, di, e -> new CrateEditMenu(crate.getId()).open(player));
        }

        set(36, page > 1 ? navBtn("\u25C4 Anterior", true) : pane(7),
                page > 1 ? e -> new CrateAdminMenu(page - 1).open(player) : null);

        set(40, item(Material.EMERALD_BLOCK,
                "&a&lCrear Crate",
                "&7Crea una nueva crate vacía",
                "", "&a[CLICK]"), e -> {
            ChatInputManager.await(player, "ID para la nueva crate (sin espacios):", (p, id) -> {
                id = id.toLowerCase().replace(" ", "_");
                if (mgr.exists(id)) {
                    p.sendMessage(CC.translate("&c\u2717 Ya existe una crate con ese ID: &f" + id));
                    new CrateAdminMenu(1).open(p);
                    return;
                }
                Crate crate = mgr.createCrate(id);
                p.sendMessage(CC.translate("&a\u2713 Crate &f" + id + " &acreada."));
                new CrateEditMenu(crate.getId()).open(p);
            });
        });

        set(44, page < pages ? navBtn("Siguiente \u25BA", true) : pane(7),
                page < pages ? e -> new CrateAdminMenu(page + 1).open(player) : null);

        set(39, item(Material.BOOK, "&ePágina &f" + page + "&7/&f" + pages,
                "&7Total: &f" + total + " crates"));

        set(41, item(Material.CLOCK, "&bRecargar", "&7Recarga todas las crates del disco", "", "&b[CLICK]"),
                e -> {
                    mgr.reload();
                    player.sendMessage(CC.translate("&a\u2713 Crates recargadas."));
                    new CrateAdminMenu(1).open(player);
                });
    }

    private ItemStack buildAdminDisplay(Crate crate) {
        ItemStack base;
        if (crate.getVisualItem() != null) base = crate.getVisualItem().clone();
        else {
            Material mat;
            try { mat = Material.valueOf(crate.getMaterial().toUpperCase()); }
            catch (Exception e) { mat = Material.CHEST; }
            base = new ItemStack(mat);
        }

        ItemMeta meta = base.getItemMeta();
        if (meta == null) return base;
        String enabledColor = crate.isEnabled() ? "&a" : "&c";
        meta.setDisplayName(CC.translate(enabledColor + "\u25CF &f" + CC.strip(crate.getDisplayName())));

        List<String> lore = new ArrayList<>();
        lore.add(CC.translate("&7ID: &f" + crate.getId()));
        lore.add(CC.translate("&7Rareza: " + crate.getRarity().getDisplay()));
        lore.add(CC.translate("&7Recompensas: &f" + crate.getRewards().size()));
        lore.add(CC.translate("&7Estado: " + (crate.isEnabled() ? "&aActiva" : "&cDesactivada")));
        lore.add(CC.translate("&7Loc. física: " + (crate.getPhysicalLocation() != null ? "&aSí" : "&cNo")));
        lore.add("");
        lore.add(CC.translate("&e[CLICK] Editar"));
        meta.setLore(lore);
        base.setItemMeta(meta);
        return base;
    }

    private int[] buildInnerSlots() {
        int[] slots = new int[PAGE_SIZE];
        int idx = 0;
        for (int row = 1; row <= 3; row++)
            for (int col = 1; col <= 7; col++)
                slots[idx++] = row * 9 + col;
        return slots;
    }
}
