package org.delawarex.dbz.battlepass.menus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.battlepass.manager.BattlePassManager;
import org.delawarex.dbz.battlepass.models.BattlePass;
import org.delawarex.dbz.battlepass.models.BattlePassLevel;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class BattlePassLevelListMenu extends Menu {

    private static final int PAGE_SIZE = 21;
    private final String passId;
    private final int page;

    public BattlePassLevelListMenu(String passId, int page) {
        this.passId = passId;
        this.page = page;
    }

    @Override
    protected String getTitle() { return "&6&l⭐ Niveles del Pase"; }

    @Override
    protected int getRows() { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        BattlePassManager mgr = BattlePassManager.getInstance();
        BattlePass pass = mgr.getPass(passId);
        if (pass == null) { player.closeInventory(); return; }

        List<BattlePassLevel> levels = pass.getLevels();
        int total = levels.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        int[] slots = buildInnerSlots();

        for (int i = start; i < end; i++) {
            BattlePassLevel level = levels.get(i);
            final int idx = i;
            boolean hasRewards = level.hasRewards();

            ItemStack it = new ItemStack(hasRewards ? Material.GOLD_INGOT : Material.IRON_INGOT);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate("&e&lNivel " + level.getLevelNumber()
                        + ": &f" + CC.strip(level.getDisplayName())));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7Puntos requeridos: &f" + level.getRequiredPoints()));
                lore.add(CC.translate("&7Ítems de recompensa: &f" + level.getItems().size()));
                lore.add(CC.translate("&7Comandos de recompensa: &f" + level.getCommands().size()));
                lore.add("");
                lore.add(ChatColor.YELLOW + "[CLICK] Editar");
                lore.add(ChatColor.RED + "[SHIFT+CLICK] Eliminar");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }
            set(slots[i - start], it, e -> {
                if (e.isShiftClick()) {
                    pass.getLevels().remove(idx);
                    mgr.savePass(pass);
                    player.sendMessage(CC.translate("&c✗ Nivel eliminado."));
                    int maxPage = Math.max(1, (int) Math.ceil((double) pass.getLevels().size() / PAGE_SIZE));
                    new BattlePassLevelListMenu(passId, Math.min(page, maxPage)).open(player);
                } else {
                    new BattlePassLevelEditMenu(passId, idx).open(player);
                }
            });
        }

        if (page > 1) set(36, navButton("◀ Anterior", true), e -> new BattlePassLevelListMenu(passId, page - 1).open(player));
        else set(36, glass());

        set(40, item(Material.EMERALD_BLOCK,
                        "&a&lAgregar Nivel",
                        "&7Nuevo nivel #" + (pass.getLevels().size() + 1),
                        "", "&a[CLICK]"),
                e -> {
                    int nextNum = pass.getLevels().size() + 1;
                    int suggestedPts = nextNum <= 1 ? 0 : nextNum * 100;
                    BattlePassLevel newLevel = new BattlePassLevel(nextNum, suggestedPts);
                    pass.getLevels().add(newLevel);
                    mgr.savePass(pass);
                    player.sendMessage(CC.translate("&a✓ Nivel &f" + nextNum + " &acreado."));
                    new BattlePassLevelEditMenu(passId, pass.getLevels().size() - 1).open(player);
                });

        if (page < pages) set(44, navButton("Siguiente ▶", true), e -> new BattlePassLevelListMenu(passId, page + 1).open(player));
        else set(44, glass());

        set(39, item(Material.PAPER, "&7Página &e" + page + "&7/&e" + pages,
                "&7Total niveles: &f" + total));

        set(37, back(), e -> new BattlePassEditMenu(passId).open(player));
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