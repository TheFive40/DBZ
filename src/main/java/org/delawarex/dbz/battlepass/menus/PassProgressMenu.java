package org.delawarex.dbz.battlepass.menus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.battlepass.manager.BattlePassManager;
import org.delawarex.dbz.battlepass.models.BattlePass;
import org.delawarex.dbz.battlepass.models.BattlePassLevel;
import org.delawarex.dbz.battlepass.models.BattlePassPlayer;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class PassProgressMenu extends Menu {

    private static final int PAGE_SIZE = 21;
    private final String passId;
    private final int page;

    public PassProgressMenu(String passId) { this(passId, 1); }

    public PassProgressMenu(String passId, int page) {
        this.passId = passId;
        this.page = page;
    }

    @Override
    protected String getTitle() { return "&6&l⭐ Progreso del Pase"; }

    @Override
    protected int getRows() { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        BattlePassManager mgr = BattlePassManager.getInstance();
        BattlePass pass = mgr.getPass(passId);
        if (pass == null) { player.closeInventory(); return; }
        BattlePassPlayer bpPlayer = mgr.getOrCreatePlayer(player.getUniqueId(), player.getName());

        int points = bpPlayer.getPoints(passId);
        int currentLevel = pass.getLevelForPoints(points);
        int maxLevel = pass.getLevels().size();

        set(4, item(Material.NETHER_STAR,
                CC.strip(pass.getDisplayName()),
                "&7Puntos actuales: &e" + points,
                "&7Nivel desbloqueado: &e" + currentLevel + "&7/&e" + maxLevel,
                buildProgressBar(points, pass)));

        List<BattlePassLevel> levels = pass.getLevels();
        int total = levels.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        int[] slots = buildInnerSlots();

        long claimableCount = levels.stream().filter(l ->
                points >= l.getRequiredPoints()
                        && !bpPlayer.hasClaimed(passId, l.getLevelNumber())).count();

        if (claimableCount > 0) {
            set(8, item(Material.EMERALD,
                            "&a&lReclamar Todo (" + claimableCount + ")",
                            "&7Reclama todas las recompensas disponibles",
                            "", "&a[CLICK]"),
                    e -> {
                        int claimed = 0;
                        for (BattlePassLevel l : pass.getLevels()) {
                            if (mgr.claimLevel(player, pass, l)) claimed++;
                            else if (points >= l.getRequiredPoints()
                                    && !bpPlayer.hasClaimed(passId, l.getLevelNumber())
                                    && !l.hasRewards()) {
                                bpPlayer.addClaimed(passId, l.getLevelNumber());
                                claimed++;
                            }
                        }
                        if (claimed > 0) {
                            mgr.savePlayer(bpPlayer);
                            player.sendMessage(CC.translate("&a✓ &f" + claimed + " &anivel(es) reclamado(s)."));
                        }
                        new PassProgressMenu(passId, page).open(player);
                    });
        }

        for (int i = start; i < end; i++) {
            BattlePassLevel level = levels.get(i);
            boolean unlocked = points >= level.getRequiredPoints();
            boolean claimed = bpPlayer.hasClaimed(passId, level.getLevelNumber());

            Material dye;
            String statusColor;
            String statusText;

            if (claimed) {
                dye = Material.LIME_DYE;
                statusColor = "&a";
                statusText = "&a✔ RECLAMADO";
            } else if (unlocked) {
                dye = level.hasRewards() ? Material.ORANGE_DYE : Material.YELLOW_DYE;
                statusColor = "&6";
                statusText = level.hasRewards()
                        ? "&6⭐ DISPONIBLE — CLIC PARA RECLAMAR"
                        : "&6✔ DESBLOQUEADO";
            } else {
                dye = Material.GRAY_DYE;
                statusColor = "&8";
                int needed = level.getRequiredPoints() - points;
                statusText = "&8🔒 &7Faltan &f" + needed + " &7puntos";
            }

            ItemStack it = new ItemStack(dye);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate(statusColor + "Nivel "
                        + level.getLevelNumber() + ": &f" + CC.strip(level.getDisplayName())));
                List<String> lore = new ArrayList<>();
                lore.add(CC.translate("&7Puntos requeridos: &f" + level.getRequiredPoints()));
                if (!unlocked) {
                    lore.add(CC.translate("&7Tu progreso: &e" + points + "&7/&f" + level.getRequiredPoints()));
                }
                if (!level.getItems().isEmpty()) {
                    lore.add("");
                    lore.add(CC.translate("&7Recompensa — Ítems:"));
                    for (ItemStack reward : level.getItems()) {
                        if (reward == null) continue;
                        String rName = reward.hasItemMeta() && reward.getItemMeta().hasDisplayName()
                                ? ChatColor.stripColor(reward.getItemMeta().getDisplayName())
                                : reward.getType().name();
                        lore.add(CC.translate("  &8● &f" + rName + " x" + reward.getAmount()));
                    }
                }
                if (!level.getCommands().isEmpty()) {
                    if (level.getItems().isEmpty()) lore.add("");
                    lore.add(CC.translate("&7Recompensa — Comandos: &f" + level.getCommands().size()));
                }
                lore.add("");
                lore.add(CC.translate(statusText));
                meta.setLore(lore);
                it.setItemMeta(meta);
            }

            if (unlocked && !claimed && level.hasRewards()) {
                set(slots[i - start], it, e -> {
                    if (!mgr.claimLevel(player, pass, level)) {
                        player.sendMessage(CC.translate("&cNo puedes reclamar este nivel."));
                    }
                    new PassProgressMenu(passId, page).open(player);
                });
            } else if (unlocked && !claimed && !level.hasRewards()) {
                set(slots[i - start], it, e -> {
                    bpPlayer.addClaimed(passId, level.getLevelNumber());
                    mgr.savePlayer(bpPlayer);
                    player.sendMessage(CC.translate("&a✓ Nivel &f" + level.getLevelNumber() + " &amarcado."));
                    new PassProgressMenu(passId, page).open(player);
                });
            } else {
                set(slots[i - start], it);
            }
        }

        if (page > 1) set(36, navButton("◀ Anterior", true), e -> new PassProgressMenu(passId, page - 1).open(player));
        else set(36, glass());

        set(40, item(Material.BOOK,
                "&fPágina &e" + page + "&7/&e" + pages,
                "&7Total niveles: &f" + total,
                "&7Puntos: &e" + points));

        if (page < pages) set(44, navButton("Siguiente ▶", true), e -> new PassProgressMenu(passId, page + 1).open(player));
        else set(44, glass());

        set(39, back(), e -> new PassListMenu().open(player));
    }

    private String buildProgressBar(int points, BattlePass pass) {
        if (pass.getLevels().isEmpty()) return CC.translate("&7Progreso: &fN/A");
        int maxPts = pass.getLevels().stream()
                .mapToInt(BattlePassLevel::getRequiredPoints).max().orElse(1);
        if (maxPts <= 0) return CC.translate("&7Progreso: &a[██████████] 100%");
        double pct = Math.min(1.0, (double) points / maxPts);
        int filled = (int) (pct * 10);
        StringBuilder bar = new StringBuilder("&7Progreso: &a[");
        for (int i = 0; i < 10; i++) bar.append(i < filled ? "&a█" : "&8█");
        bar.append("&a] &f").append(String.format("%.0f", pct * 100)).append("%");
        return CC.translate(bar.toString());
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