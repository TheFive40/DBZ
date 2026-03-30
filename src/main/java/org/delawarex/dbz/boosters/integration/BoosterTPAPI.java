package org.delawarex.dbz.boosters.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.boosters.managers.GlobalBoosterManager;
import org.delawarex.dbz.boosters.managers.PersonalBoosterManager;

public class BoosterTPAPI {

    public static void giveTPsWithBooster(Player player, int baseTPs) {
        giveTPsWithBooster(player, baseTPs, true);
    }

    public static void giveTPsWithBooster(Player player, int baseTPs, boolean showMessage) {
        if (player == null || baseTPs <= 0) return;
        int totalTPs = calculateBoostedTPs(player, baseTPs);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addtp " + player.getName() + " " + totalTPs);
    }

    public static int calculateBoostedTPs(Player player, int baseTPs) {
        if (player == null || baseTPs <= 0) return baseTPs;
        double globalMultiplier = GlobalBoosterManager.getCurrentMultiplier();
        double personalMultiplier = PersonalBoosterManager.getActiveMultiplier(player.getUniqueId());
        double combinedMultiplier = globalMultiplier * personalMultiplier;
        return (int) Math.round(baseTPs * combinedMultiplier);
    }

    public static double getCombinedMultiplier(Player player) {
        if (player == null) return 1.0;
        double globalMultiplier = GlobalBoosterManager.getCurrentMultiplier();
        double personalMultiplier = PersonalBoosterManager.getActiveMultiplier(player.getUniqueId());
        return globalMultiplier * personalMultiplier;
    }

    public static boolean hasActiveBooster(Player player) {
        if (player == null) return false;
        boolean hasGlobal = GlobalBoosterManager.isBoosterActive();
        boolean hasPersonal = PersonalBoosterManager.getActiveBooster(player.getUniqueId()) != null;
        return hasGlobal || hasPersonal;
    }

    public static void giveTPsSilent(Player player, int baseTPs) {
        giveTPsWithBooster(player, baseTPs, false);
    }

    public static int getBonusTPs(Player player, int baseTPs) {
        if (player == null || baseTPs <= 0) return 0;
        return calculateBoostedTPs(player, baseTPs) - baseTPs;
    }
}
