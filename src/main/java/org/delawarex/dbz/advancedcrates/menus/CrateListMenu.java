package org.delawarex.dbz.advancedcrates.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.DbzMain;
import org.delawarex.service.CC;
import org.delawarex.dbz.advancedcrates.managers.CrateManager;
import org.delawarex.dbz.advancedcrates.models.Crate;
import org.delawarex.dbz.advancedcrates.models.CrateReward;

import java.util.ArrayList;
import java.util.List;

public class CrateListMenu extends Menu {

    private static final int PAGE_SIZE = 21;
    private final int page;

    public CrateListMenu(int page) { this.page = page; }

    @Override protected String getTitle() { return "&6&l\u2756 Crates \u2756"; }
    @Override protected int getRows()     { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder(0);

        CrateManager mgr = DbzMain.get().getCrateManager();
        List<Crate> all   = new ArrayList<>(mgr.getAll().stream().filter(Crate::isEnabled).toList());
        int total  = all.size();
        int pages  = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start  = (page - 1) * PAGE_SIZE;
        int end    = Math.min(start + PAGE_SIZE, total);
        int[] slots = buildInnerSlots();

        for (int i = start; i < end; i++) {
            Crate crate   = all.get(i);
            ItemStack disp = mgr.buildCrateDisplayItem(crate);
            int slot       = slots[i - start];

            set(slot, disp, e -> {
                if (e.isShiftClick()) {
                    new CratePreviewMenu(crate.getId(), 1).open(player);
                    return;
                }
                if (!mgr.hasKey(player, crate)) {
                    player.sendMessage(CC.translate("&c\u2717 Necesitas la llave: &f"
                            + CC.strip(crate.getKeyDisplayName())));
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }
                if (!mgr.consumeKey(player, crate)) return;
                CrateReward reward = crate.selectReward();
                if (reward == null) {
                    player.sendMessage(CC.translate("&c\u2717 Esta crate no tiene recompensas configuradas."));
                    return;
                }
                player.closeInventory();
                new CrateOpenAnimation(crate, reward, player).start();
            });
        }

        set(36, page > 1 ? navBtn("\u25C4 Anterior", true) : pane(0),
                page > 1 ? e -> new CrateListMenu(page - 1).open(player) : null);

        set(40, item(Material.BOOK, "&f&lPágina &e" + page + "&7/&e" + pages,
                "&7Total de crates: &f" + total));

        set(44, page < pages ? navBtn("Siguiente \u25BA", true) : pane(0),
                page < pages ? e -> new CrateListMenu(page + 1).open(player) : null);
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
