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
import org.delawarex.dbz.advancedcrates.models.CrateReward;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrateRewardListMenu extends Menu {

    private static final int PAGE_SIZE = 28;
    private final String crateId;
    private final int page;

    public CrateRewardListMenu(String crateId, int page) {
        this.crateId = crateId;
        this.page    = page;
    }

    @Override protected String getTitle() { return "&a&l\u2756 Recompensas"; }
    @Override protected int getRows()     { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder(6);

        CrateManager mgr  = DbzMain.get().getCrateManager();
        Crate crate       = mgr.getCrate(crateId);
        if (crate == null) { player.closeInventory(); return; }

        List<CrateReward> rewards = crate.getRewards();
        double totalChance        = crate.getTotalChance();
        int total = rewards.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start = (page - 1) * PAGE_SIZE;
        int end   = Math.min(start + PAGE_SIZE, total);
        int[] slots = buildInnerSlots();

        for (int i = start; i < end; i++) {
            CrateReward reward = rewards.get(i);
            final int idx      = i;
            ItemStack display   = buildRewardDisplay(reward, totalChance);
            set(slots[i - start], display, e -> {
                if (e.isShiftClick()) {
                    crate.getRewards().remove(idx);
                    mgr.saveCrate(crate);
                    player.sendMessage(CC.translate("&c\u2717 Recompensa eliminada."));
                    new CrateRewardListMenu(crateId, Math.min(page,
                            Math.max(1, (int) Math.ceil((double)(crate.getRewards().size()) / PAGE_SIZE)))).open(player);
                } else {
                    ChatInputManager.await(player, "Nueva probabilidad (ej: 10.5):", (p, txt) -> {
                        try {
                            double chance = Double.parseDouble(txt.trim());
                            if (chance <= 0) throw new NumberFormatException();
                            reward.setChance(chance);
                            mgr.saveCrate(crate);
                            p.sendMessage(CC.translate("&a\u2713 Probabilidad: &f" + chance));
                        } catch (NumberFormatException ex) {
                            p.sendMessage(CC.translate("&c\u2717 Valor inválido."));
                        }
                        new CrateRewardListMenu(crateId, page).open(p);
                    });
                }
            });
        }

        set(45, page > 1 ? navBtn("\u25C4 Anterior", true) : pane(6),
                page > 1 ? e -> new CrateRewardListMenu(crateId, page - 1).open(player) : null);

        set(49, item(Material.HOPPER, "&aAgregar Recompensa",
                "&7Sostén el ítem en la mano",
                "&7y haz clic para agregarlo",
                "", "&a[CLICK]"),
                e -> {
                    if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                        player.sendMessage(CC.translate("&c\u2717 Sostén un ítem en la mano."));
                        return;
                    }
                    ChatInputManager.await(player, "Probabilidad del ítem (ej: 10.0):", (p, txt) -> {
                        try {
                            double chance = Double.parseDouble(txt.trim());
                            if (chance <= 0) throw new NumberFormatException();
                            CrateReward reward = new CrateReward(
                                    UUID.randomUUID().toString().substring(0, 8),
                                    player.getInventory().getItemInMainHand(), chance);
                            crate.getRewards().add(reward);
                            mgr.saveCrate(crate);
                            p.sendMessage(CC.translate("&a\u2713 Recompensa agregada: &f"
                                    + player.getInventory().getItemInMainHand().getType().name() + " &7(" + chance + ")"));
                        } catch (NumberFormatException ex) {
                            p.sendMessage(CC.translate("&c\u2717 Probabilidad inválida."));
                        }
                        new CrateRewardListMenu(crateId, page).open(p);
                    });
                });

        set(53, page < pages ? navBtn("Siguiente \u25BA", true) : pane(6),
                page < pages ? e -> new CrateRewardListMenu(crateId, page + 1).open(player) : null);

        set(45, back(), e -> new CrateEditMenu(crateId).open(player));
    }

    private ItemStack buildRewardDisplay(CrateReward reward, double totalChance) {
        ItemStack base = reward.getItem() != null ? reward.getItem().clone()
                : new ItemStack(Material.BARRIER);
        ItemMeta meta  = base.hasItemMeta() ? base.getItemMeta()
                : DbzMain.instance.getServer().getItemFactory().getItemMeta(base.getType());
        if (meta == null) return base;

        List<String> lore = (meta.hasLore() && meta.getLore() != null)
                ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        double pct = totalChance > 0 ? (reward.getChance() / totalChance) * 100 : 0;
        lore.add(CC.translate("&7Probabilidad: &f" + String.format("%.2f", pct) + "%"));
        lore.add(CC.translate("&7Peso: &f" + reward.getChance()));
        if (!reward.getCommands().isEmpty())
            lore.add(CC.translate("&7Comandos: &f" + reward.getCommands().size()));
        lore.add("");
        lore.add(CC.translate("&e[CLICK] Cambiar probabilidad"));
        lore.add(CC.translate("&c[SHIFT] Eliminar"));
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
