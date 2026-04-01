package org.delawarex.dbz.market;

import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.market.commands.BalanceCommand;
import org.delawarex.dbz.market.commands.PayCommand;
import org.delawarex.dbz.market.commands.ShopAdminCommand;
import org.delawarex.dbz.market.commands.ShopCommand;
import org.delawarex.dbz.market.storage.EconomyManager;

public class ShopModule {

    public static void enable() {
        ShopManager.getInstance();

        if (!EconomyManager.isHooked()) {
            DbzMain.instance.getLogger().severe("[Market] No se pudo enlazar con Vault/Essentials.");
            DbzMain.instance.getLogger().severe("[Market] Instala Vault y EssentialsX para usar el mercado.");
            DbzMain.instance.getLogger().severe("[Market] El módulo de mercado NO se cargará.");
            return;
        }

        new ShopCommand();
        new ShopAdminCommand();
        new BalanceCommand();
        new PayCommand();

        DbzMain.instance.getLogger().info("[Market] Módulo de mercado cargado. Economy: Essentials (Vault).");
    }

    public static void disable() {
        ShopManager.resetInstance();
        DbzMain.instance.getLogger().info("[Market] Módulo de mercado detenido.");
    }
}
