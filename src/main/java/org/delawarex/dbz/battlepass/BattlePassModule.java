package org.delawarex.dbz.battlepass;

import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.battlepass.commands.BattlePassAdminCommand;
import org.delawarex.dbz.battlepass.commands.PassCommand;
import org.delawarex.dbz.battlepass.events.BattlePassListener;
import org.delawarex.dbz.battlepass.manager.BattlePassManager;

public class BattlePassModule {

    public static void enable() {
        DbzMain plugin = DbzMain.instance;
        BattlePassManager.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new BattlePassListener(), plugin);
        new BattlePassAdminCommand();
        new PassCommand();
        plugin.getLogger().info("[BattlePass] Módulo de pase de batalla cargado.");
    }

    public static void disable() {
        BattlePassManager.reset();
        DbzMain.instance.getLogger().info("[BattlePass] Módulo de pase de batalla detenido.");
    }
}