package org.delawarex.dbz.advancedcrates.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.DbzMain;
import org.delawarex.service.CC;
import org.delawarex.dbz.advancedcrates.managers.CrateManager;
import org.delawarex.dbz.advancedcrates.models.Crate;
import org.delawarex.dbz.advancedcrates.models.CrateReward;

import java.util.ArrayList;
import java.util.List;

public class CratePreviewMenu extends Menu {

    private static final int PAGE_SIZE = 28;
    private final String crateId;
    private final int page;

    public CratePreviewMenu(String crateId, int page) {
        this.crateId = crateId;
        this.page    = page;
    }

    @Override protected String getTitle() { return "&b&l\u2756 Previsualizar Recompensas"; }
    @Override protected int getRows()     { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder(5);

        CrateManager mgr  = DbzMain.get().getCrateManager();
        Crate crate       = mgr.getCrate(crateId);
        if (crate == null) { player.closeInventory(); return; }

        List<CrateReward> rewards = crate.getRewards();
        double totalChance        = crate.getTotalChance();
        int total  = rewards.size();
        int pages  = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start  = (page - 1) * PAGE_SIZE;
        int end    = Math.min(start + PAGE_SIZE, total);
        int[] slots = buildInnerSlots();

        for (int i = start; i < end; i++) {
            CrateReward reward = rewards.get(i);
            ItemStack display  = buildRewardDisplay(reward, totalChance);
            set(slots[i - start], display);
        }

        set(45, page > 1 ? navBtn("\u25C4 Anterior", true) : pane(5),
                page > 1 ? e -> new CratePreviewMenu(crateId, page - 1).open(player) : null);

        set(49, item(Material.PAPER, "&f" + CC.strip(crate.getDisplayName()),
                "&7Rareza: " + crate.getRarity().getDisplay(),
                "&7Recompensas: &f" + total,
                "&7Página: &e" + page + "&7/&e" + pages));

        set(53, page < pages ? navBtn("Siguiente \u25BA", true) : pane(5),
                page < pages ? e -> new CratePreviewMenu(crateId, page + 1).open(player) : null);

        set(48, back(), e -> new CrateListMenu(1).open(player));
    }

    private ItemStack buildRewardDisplay(CrateReward reward, double totalChance) {
        ItemStack base = reward.getItem() != null ? reward.getItem().clone() : new ItemStack(Material.BARRIER);
        ItemMeta meta  = base.hasItemMeta() ? base.getItemMeta()
                : DbzMain.instance.getServer().getItemFactory().getItemMeta(base.getType());
        if (meta == null) return base;

        List<String> lore = (meta.hasLore() && meta.getLore() != null)
                ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        double pct = totalChance > 0 ? (reward.getChance() / totalChance) * 100 : 0;
        lore.add(CC.translate("&7Probabilidad: &f" + String.format("%.2f", pct) + "%"));
        if (!reward.getCommands().isEmpty())
            lore.add(CC.translate("&7Comandos: &f" + reward.getCommands().size()));
        meta.setLore(lore);
        base.setItemMeta(meta);
        return base;
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
