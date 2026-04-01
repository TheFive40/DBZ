package org.delawarex.dbz.bank.menus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.dbz.bank.model.LoanRange;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class LoanAdminGUI extends Menu {

    private static final int PAGE_SIZE = 21;
    private final int page;

    public LoanAdminGUI(int page) { this.page = page; }

    @Override protected String getTitle() { return "&c&lAdmin Rangos Préstamo — Pág. " + page; }
    @Override protected int getRows()     { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        List<LoanRange> ranges = BankManager.getInstance().getRangeManager().getRanges();
        int total  = ranges.size();
        int pages  = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start  = (page - 1) * PAGE_SIZE;
        int end    = Math.min(start + PAGE_SIZE, total);
        int[] slots = buildInnerSlots();

        for (int i = start; i < end; i++) {
            LoanRange r = ranges.get(i);
            final int idx = i;

            ItemStack it = new ItemStack(Material.BOOK);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate("&b&lNivel " + r.getMinLevel() + " - " + r.getMaxLevel()));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7Máx TPS: &f" + r.getMaxTPS()));
                lore.add(CC.translate("&7Máx Zenis: &f" + String.format("%.0f", r.getMaxZenis())));
                lore.add(CC.translate("&7Interés: &f" + (int)(r.getInterestRate()*100) + "%"));
                lore.add(CC.translate("&7Cuotas: &f" + r.getInstallmentCount() + " c/" + r.getInstallmentIntervalHours() + "h"));
                lore.add(CC.translate("&7Penalización: &c" + (int)(r.getPenaltyRate()*100) + "%"));
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click izq → Editar");
                lore.add(ChatColor.RED    + "Click der → Eliminar");
                meta.setLore(lore);
                it.setItemMeta(meta);
            }

            set(slots[i - start], it, e -> {
                if (e.isRightClick()) {
                    BankManager.getInstance().getRangeManager().removeRange(idx);
                    player.sendMessage(CC.translate("&c✗ Rango eliminado."));
                    new LoanAdminGUI(1).open(player);
                } else {
                    LoanEditorHandler.startEdit(player, r, idx);
                }
            });
        }

        if (page > 1)   set(45, navButton("◀ Anterior", true), e -> new LoanAdminGUI(page - 1).open(player));
        set(49, item(Material.BOOK, "&fPágina &e" + page + "&7/&e" + pages));
        if (page < pages) set(53, navButton("Siguiente ▶", true), e -> new LoanAdminGUI(page + 1).open(player));

        set(48, item(Material.EMERALD_BLOCK,
                        "&a&lNuevo Rango",
                        "&7Shift+Click para crear.",
                        "", "&a[SHIFT+CLICK]"),
                e -> {
                    if (e.isShiftClick()) {
                        LoanRange newRange = new LoanRange(1, 10, 10000, 50000, 0.05, 5, 24, 0.30);
                        LoanEditorHandler.startEdit(player, newRange, -1);
                    }
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
