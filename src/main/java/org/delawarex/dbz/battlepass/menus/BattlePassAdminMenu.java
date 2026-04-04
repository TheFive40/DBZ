package org.delawarex.dbz.battlepass.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.battlepass.manager.BattlePassManager;
import org.delawarex.dbz.battlepass.models.BattlePass;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class BattlePassAdminMenu extends Menu {

    private static final int PAGE_SIZE = 21;
    private final int page;

    public BattlePassAdminMenu(int page) { this.page = page; }

    @Override
    protected String getTitle() { return "&c&l⭐ Admin — Pases de Batalla"; }

    @Override
    protected int getRows() { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        BattlePassManager mgr = BattlePassManager.getInstance();
        List<BattlePass> all = new ArrayList<>(mgr.getAllPasses());
        int total = all.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        int[] slots = buildInnerSlots();

        for (int i = start; i < end; i++) {
            BattlePass pass = all.get(i);
            Material mat;
            try { mat = Material.valueOf(pass.getMaterial().toUpperCase()); }
            catch (Exception e) { mat = Material.BOOK; }

            ItemStack it = new ItemStack(mat);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                String sc = pass.isEnabled() ? "&a" : "&c";
                meta.setDisplayName(CC.translate(sc + "● &f" + CC.strip(pass.getDisplayName())));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7ID: &f" + pass.getId()));
                lore.add(CC.translate("&7Descripción: &f" + (pass.getDescription().isEmpty() ? "N/A" : pass.getDescription())));
                lore.add(CC.translate("&7Niveles: &f" + pass.getLevels().size()));
                lore.add(CC.translate("&7Permiso: &f" + (pass.getPermission().isEmpty() ? "Ninguno" : pass.getPermission())));
                lore.add(CC.translate("&7Estado: " + (pass.isEnabled() ? "&aActivo" : "&cDesactivado")));
                lore.add("");
                lore.add(CC.translate("&e[CLICK] Editar"));
                meta.setLore(lore);
                it.setItemMeta(meta);
            }
            set(slots[i - start], it, e -> new BattlePassEditMenu(pass.getId()).open(player));
        }

        if (page > 1) set(36, navButton("◀ Anterior", true), e -> new BattlePassAdminMenu(page - 1).open(player));
        else set(36, glass());

        set(40, item(Material.EMERALD_BLOCK,
                        "&a&lCrear Pase de Batalla",
                        "&7Crea un nuevo pase personalizado",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Nombre del nuevo pase (soporta &colores):", (p, name) -> {
                    if (name.trim().isEmpty()) {
                        p.sendMessage(CC.translate("&cNombre inválido."));
                        new BattlePassAdminMenu(1).open(p);
                        return;
                    }
                    BattlePass pass = mgr.createPass(name.trim());
                    p.sendMessage(CC.translate("&a✓ Pase &f" + pass.getId() + " &acreado."));
                    new BattlePassEditMenu(pass.getId()).open(p);
                }));

        if (page < pages) set(44, navButton("Siguiente ▶", true), e -> new BattlePassAdminMenu(page + 1).open(player));
        else set(44, glass());

        set(39, item(Material.BOOK,
                "&7Página &e" + page + "&7/&e" + pages,
                "&7Total: &f" + total + " pases"));

        set(41, item(Material.CLOCK, "&bRecargar",
                        "&7Recarga todos los pases del disco", "", "&b[CLICK]"),
                e -> {
                    mgr.reload();
                    player.sendMessage(CC.translate("&a✓ Pases recargados."));
                    new BattlePassAdminMenu(1).open(player);
                });
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