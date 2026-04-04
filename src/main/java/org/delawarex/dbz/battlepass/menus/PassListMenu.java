package org.delawarex.dbz.battlepass.menus;

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
import java.util.stream.Collectors;

public class PassListMenu extends Menu {

    @Override
    protected String getTitle() { return "&6&l⭐ Pases de Batalla"; }

    @Override
    protected int getRows() { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        BattlePassManager mgr = BattlePassManager.getInstance();
        BattlePassPlayer bpPlayer = mgr.getOrCreatePlayer(player.getUniqueId(), player.getName());

        List<BattlePass> available = mgr.getAllPasses().stream()
                .filter(BattlePass::isEnabled)
                .filter(p -> p.getPermission().isEmpty() || player.hasPermission(p.getPermission()))
                .collect(Collectors.toList());

        int[] slots = buildInnerSlots();

        for (int i = 0; i < Math.min(available.size(), slots.length); i++) {
            BattlePass pass = available.get(i);
            int points = bpPlayer.getPoints(pass.getId());
            int currentLevel = pass.getLevelForPoints(points);
            int maxLevel = pass.getLevels().size();
            int pendingClaims = countClaimable(pass, bpPlayer);

            Material mat;
            try { mat = Material.valueOf(pass.getMaterial().toUpperCase()); }
            catch (Exception e) { mat = Material.BOOK; }

            ItemStack it = new ItemStack(mat);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(CC.translate(pass.getDisplayName()));
                List<String> lore = new ArrayList<>();
                if (!pass.getDescription().isEmpty())
                    lore.add(CC.translate("&7" + pass.getDescription()));
                lore.add("");
                lore.add(CC.translate("&7Puntos actuales: &e" + points));
                lore.add(CC.translate("&7Nivel actual: &e" + currentLevel + "&7/&e" + maxLevel));
                lore.add(buildProgressBar(points, pass));
                if (pendingClaims > 0) {
                    lore.add(CC.translate("&a⭐ &f" + pendingClaims + " &arecompensa(s) disponible(s)!"));
                } else {
                    lore.add(CC.translate("&7Sin recompensas pendientes"));
                }
                if (!pass.getPermission().isEmpty()) {
                    lore.add(CC.translate("&8Requiere: &7" + pass.getPermission()));
                }
                lore.add("");
                lore.add(CC.translate("&e[CLICK] Ver progreso y reclamar"));
                meta.setLore(lore);
                it.setItemMeta(meta);
            }
            set(slots[i], it, e -> new PassProgressMenu(pass.getId()).open(player));
        }

        if (available.isEmpty()) {
            set(22, item(Material.BARRIER,
                    "&cSin pases disponibles",
                    "&7No tienes acceso a ningún pase de batalla actualmente."));
        }

        set(49, item(Material.NETHER_STAR,
                "&7Pases disponibles: &f" + available.size(),
                "&7Haz clic en un pase para ver tu progreso"));
    }

    private int countClaimable(BattlePass pass, BattlePassPlayer bpPlayer) {
        int points = bpPlayer.getPoints(pass.getId());
        int count = 0;
        for (BattlePassLevel level : pass.getLevels()) {
            if (points >= level.getRequiredPoints()
                    && !bpPlayer.hasClaimed(pass.getId(), level.getLevelNumber())) {
                count++;
            }
        }
        return count;
    }

    private String buildProgressBar(int points, BattlePass pass) {
        if (pass.getLevels().isEmpty()) return CC.translate("&7Progreso: &fN/A");
        int maxPts = pass.getLevels().stream().mapToInt(BattlePassLevel::getRequiredPoints).max().orElse(1);
        if (maxPts <= 0) return CC.translate("&7Progreso: &a[██████████] 100%");
        double pct = Math.min(1.0, (double) points / maxPts);
        int filled = (int) (pct * 10);
        StringBuilder bar = new StringBuilder("&7Progreso: &a[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "&a█" : "&8█");
        }
        bar.append("&a] &f").append(String.format("%.0f", pct * 100)).append("%");
        return CC.translate(bar.toString());
    }

    private int[] buildInnerSlots() {
        int[] slots = new int[28];
        int idx = 0;
        for (int row = 1; row <= 4; row++)
            for (int col = 1; col <= 7; col++)
                slots[idx++] = row * 9 + col;
        return slots;
    }
}