package org.delawarex.dbz.raids.menus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.raids.managers.RaidManager;
import org.delawarex.dbz.raids.models.Raid;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class RaidListMenu extends Menu {

    private static final int PAGE_SIZE = 21;
    private final int page;

    public RaidListMenu(int page) { this.page = page; }

    @Override
    protected String getTitle() { return "&6&lRaids — Pág. " + page; }

    @Override
    protected int getRows() { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();

        List<Raid> raids = RaidManager.getInstance().getAll();
        int total = raids.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);

        int[] slots = buildInnerSlots();
        for (int i = start; i < end; i++) {
            Raid raid = raids.get(i);
            Material mat = raid.isEnabled() ? Material.EMERALD : Material.REDSTONE;

            ItemStack it = new ItemStack(mat);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate("&6&l" + raid.getRaidName()));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7ID: &f" + raid.getRaidId()));
                lore.add(CC.translate("&7Desc: &f" + (raid.getDescription().isEmpty() ? "N/A" : raid.getDescription())));
                lore.add(CC.translate("&7Oleadas: &f" + raid.getTotalWaves()));
                lore.add(CC.translate("&7Jugadores: &f" + raid.getMinPlayers() + "-" + raid.getMaxPlayers()));
                lore.add(CC.translate("&7Cooldown: &f" + (raid.getCooldownSeconds() / 60) + " min"));
                lore.add(CC.translate("&7Estado: " + (raid.isEnabled() ? "&a✓ Activa" : "&c✗ Desactivada")));
                lore.add(CC.translate("&7Config: " + (raid.isConfigured() ? "&a✓ Lista" : "&c✗ Incompleta")));
                lore.add("");
                lore.add(ChatColor.GREEN + "[CLICK para editar]");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }

            String raidId = raid.getRaidId();
            set(slots[i - start], it, e -> new RaidConfigMenu(raidId).open(player));
        }

        if (page > 1) set(45, navButton("◀ Anterior", true), e -> new RaidListMenu(page - 1).open(player));
        set(49, item(Material.BOOK, "&fPágina &e" + page + "&7/&e" + pages));
        if (page < pages) set(53, navButton("Siguiente ▶", true), e -> new RaidListMenu(page + 1).open(player));
        set(48, back(), e -> new RaidMainMenu().open(player));
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