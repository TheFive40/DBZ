package org.delawarex.dbz.market;

import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.market.commands.ShopAdminCommand;
import org.delawarex.dbz.market.commands.ShopCommand;
import org.delawarex.dbz.market.commands.BalanceCommand;
import org.delawarex.dbz.market.commands.PayCommand;

public class ShopModule {

    public static void enable() {
        ShopManager.getInstance();
        new ShopCommand();
        new ShopAdminCommand();
        new BalanceCommand();
        new PayCommand();
        DbzMain.instance.getLogger().info("[Market] Módulo de mercado cargado correctamente.");
    }

    public static void disable() {
        ShopManager.resetInstance();
        DbzMain.instance.getLogger().info("[Market] Módulo de mercado detenido.");
    }
}
