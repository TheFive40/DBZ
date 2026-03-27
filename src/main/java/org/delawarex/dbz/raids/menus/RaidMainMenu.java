package org.delawarex.dbz.raids.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.raids.managers.PartyManager;
import org.delawarex.dbz.raids.managers.RaidChatInputManager;
import org.delawarex.dbz.raids.managers.RaidManager;
import org.delawarex.dbz.raids.managers.RaidSessionManager;
import org.delawarex.service.CC;

public class RaidMainMenu extends Menu {

    @Override
    protected String getTitle() { return "&6&lSistema de Raids"; }

    @Override
    protected int getRows() { return 3; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();

        set(11, item(Material.EMERALD_BLOCK,
                        "&a&lCrear Raid",
                        "&7Crea una nueva raid desde cero",
                        "", "&a[CLICK]"),
                e -> RaidChatInputManager.startCreateRaidInput(player));

        set(13, item(Material.BOOK,
                        "&e&lListar Raids",
                        "&7Total: &f" + RaidManager.getInstance().getTotal(),
                        "", "&e[CLICK]"),
                e -> new RaidListMenu(1).open(player));

        set(15, item(Material.NETHER_STAR,
                "&b&lEstadísticas",
                "&7Raids totales: &f" + RaidManager.getInstance().getTotal(),
                "&7Sesiones activas: &f" + RaidSessionManager.getTotalActive(),
                "&7Parties activas: &f" + PartyManager.getTotalParties()));
    }
}