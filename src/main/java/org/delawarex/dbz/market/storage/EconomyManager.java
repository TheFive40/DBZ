package org.delawarex.dbz.market.storage;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.delawarex.dbz.DbzMain;

import java.util.UUID;

public class EconomyManager {

    private static Economy vaultEconomy;

    public EconomyManager(double startingBalance) {
        if (vaultEconomy == null) {
            setupVault();
        }
    }

    private void setupVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            DbzMain.instance.getLogger().severe("[Market] Vault no encontrado. El mercado necesita Vault + Essentials.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            DbzMain.instance.getLogger().severe("[Market] No se encontró proveedor de Economy. ¿Está EssentialsX instalado?");
            return;
        }
        vaultEconomy = rsp.getProvider();
        DbzMain.instance.getLogger().info("[Market] Economy enlazada con: " + vaultEconomy.getName());
    }

    public static boolean isHooked() {
        return vaultEconomy != null;
    }

    public double getBalance(Player player) {
        if (vaultEconomy == null) return 0;
        return vaultEconomy.getBalance(player);
    }

    public double getBalance(UUID uuid) {
        if (vaultEconomy == null) return 0;
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) return vaultEconomy.getBalance(player);
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if (name == null) return 0;
        return vaultEconomy.getBalance(Bukkit.getOfflinePlayer(uuid));
    }

    public boolean withdraw(Player player, double amount) {
        if (vaultEconomy == null) return false;
        if (vaultEconomy.getBalance(player) < amount) return false;
        EconomyResponse res = vaultEconomy.withdrawPlayer(player, amount);
        return res.transactionSuccess();
    }

    public void deposit(Player player, double amount) {
        if (vaultEconomy == null) return;
        vaultEconomy.depositPlayer(player, amount);
    }

    public boolean transfer(Player from, Player to, double amount) {
        if (vaultEconomy == null) return false;
        if (vaultEconomy.getBalance(from) < amount) return false;
        EconomyResponse wd = vaultEconomy.withdrawPlayer(from, amount);
        if (!wd.transactionSuccess()) return false;
        vaultEconomy.depositPlayer(to, amount);
        return true;
    }

    public void setBalance(UUID uuid, double amount) {
        if (vaultEconomy == null) return;
        org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        double current = vaultEconomy.getBalance(op);
        if (current > amount) {
            vaultEconomy.withdrawPlayer(op, current - amount);
        } else if (current < amount) {
            vaultEconomy.depositPlayer(op, amount - current);
        }
    }

    public void giveBalance(UUID uuid, double amount) {
        if (vaultEconomy == null) return;
        vaultEconomy.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
    }

    public void reload() {}

    public String format(double amount, String symbol) {
        if (vaultEconomy != null) return vaultEconomy.format(amount);
        return String.format("%.2f %s", amount, symbol);
    }

    public String getCurrencyName() {
        if (vaultEconomy != null) return vaultEconomy.currencyNamePlural();
        return "Zeni";
    }

    public String getCurrencySymbol() {
        if (vaultEconomy != null) return vaultEconomy.currencyNameSingular();
        return "₢";
    }
}
