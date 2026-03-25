package org.delawarex.dbz.fragments.service;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.fragments.model.CustomizedArmor;
import org.delawarex.service.dbz.General;

import java.util.*;

public class FragmentBonusIntegration {

    private static final Map<UUID, Set<String>> playerActiveHashes = new HashMap<>();

    public static void applyFragmentBonuses(Player player) {
        UUID playerId = player.getUniqueId();
        Set<String> currentHashes = new HashSet<>();
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (piece == null) continue;
            if (!CustomizedArmor.isCustomized(piece)) continue;
            String hash = CustomizedArmor.getHash(piece);
            Map<String, Integer> attributes = CustomizedArmor.getAttributes(piece);
            Map<String, String> operations = CustomizedArmor.getOperations(piece);
            if (hash != null && !attributes.isEmpty()) {
                currentHashes.add(hash);
                applyBonus(player, hash, attributes, operations);
            }
        }
        Set<String> previousHashes = playerActiveHashes.getOrDefault(playerId, new HashSet<>());
        for (String oldHash : previousHashes) {
            if (!currentHashes.contains(oldHash)) {
                removeBonus(player, oldHash);
            }
        }
        playerActiveHashes.put(playerId, currentHashes);
    }

    private static void applyBonus(Player player, String hash, Map<String, Integer> stats, Map<String, String> operations) {
        try {
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                String stat = entry.getKey().toUpperCase();
                int storedValue = entry.getValue();
                String operation = operations.getOrDefault(stat, "+");
                double valueToSend;
                if (operation.equals("*")) {
                    valueToSend = storedValue / 100.0;
                } else {
                    valueToSend = storedValue;
                }
                General.addBonus(player, stat, hash, operation, valueToSend);
            }
        } catch (Exception ignored) {}
    }

    private static void removeBonus(Player player, String hash) {
        try {
            String[] stats = {"STR", "SKP", "RES", "VIT", "PWR", "ENE"};
            for (String stat : stats) {
                General.removeBonus(player, stat, hash);
            }
        } catch (Exception ignored) {}
    }

    public static void clearPlayerTracking(UUID playerId) {
        playerActiveHashes.remove(playerId);
    }

    public static void clearAndRemoveBonuses(Player player) {
        UUID playerId = player.getUniqueId();
        Set<String> hashes = playerActiveHashes.remove(playerId);
        if (hashes == null) return;
        for (String hash : hashes) {
            removeBonus(player, hash);
        }
    }
}