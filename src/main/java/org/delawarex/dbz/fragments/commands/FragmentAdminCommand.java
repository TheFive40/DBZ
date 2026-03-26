package org.delawarex.dbz.fragments.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.fragments.model.CustomizedArmor;
import org.delawarex.dbz.fragments.manager.FragmentManager;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;
import java.util.Map;

public class FragmentAdminCommand extends BaseCommand {

    @Command(name = "fragmentadmin", permission = "dbz.admin.fragmentadmin")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length < 1) { sendHelp(player); return; }

        switch (args.getArgs(0).toLowerCase()) {
            case "info" -> showArmorInfo(player);
            case "settier" -> {
                if (args.getArgs().length < 2) {
                    player.sendMessage(CC.translate("&cUso: /fragmentadmin settier <tier>")); return;
                }
                setArmorTier(player, args.getArgs(1));
            }
            case "reset" -> resetArmor(player);
            case "stats" -> showStats(player);
            case "reload" -> reload(player);
            case "limits" -> showLimits(player);
            case "setlimit" -> {
                if (args.getArgs().length < 4) {
                    player.sendMessage(CC.translate("&cUso: /fragmentadmin setlimit <tier> <atributo> <valor>")); return;
                }
                setTierLimit(player, args.getArgs(1), args.getArgs(2), args.getArgs(3));
            }
            default -> sendHelp(player);
        }
    }

    private void showArmorInfo(Player player) {
        ItemStack armor = player.getInventory().getItemInMainHand();
        if (!CustomizedArmor.isCustomized(armor)) {
            player.sendMessage(CC.translate("&c\u2717 Sost\u00e9n una armadura personalizada en la mano")); return;
        }
        CustomizedArmor customArmor = FragmentManager.getInstance().getCustomArmor(armor);
        if (customArmor == null) { player.sendMessage(CC.translate("&c\u2717 Error al cargar la armadura")); return; }
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Info de Armadura"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&7Hash: &f" + customArmor.getHash()));
        player.sendMessage(CC.translate("&7Tier: &f" + customArmor.getTier()));
        player.sendMessage(CC.translate("&7Slot: &f" + customArmor.getArmorSlot()));
        player.sendMessage("");
        if (customArmor.getAttributes().isEmpty()) {
            player.sendMessage(CC.translate("&7Sin atributos aplicados"));
        } else {
            player.sendMessage(CC.translate("&3Atributos:"));
            for (Map.Entry<String, Integer> entry : customArmor.getAttributes().entrySet()) {
                int limit = FragmentManager.getInstance().getTierConfig().getLimit(customArmor.getTier(), entry.getKey());
                int value = entry.getValue();
                String operation = customArmor.getOperations().getOrDefault(entry.getKey(), "+");
                String displayValue;
                if (operation.equals("*")) {
                    displayValue = (value >= 0 ? "+" : "") + value + "%";
                } else {
                    displayValue = (value >= 0 ? "+" : "") + value;
                }
                player.sendMessage(CC.translate("&7  " + entry.getKey() + ": &f" + displayValue + " &8(" + operation + ")&7 / " + limit));
            }
        }
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void setArmorTier(Player player, String tier) {
        ItemStack armor = player.getInventory().getItemInMainHand();
        if (!CustomizedArmor.isCustomized(armor)) {
            player.sendMessage(CC.translate("&c\u2717 Sost\u00e9n una armadura personalizada en la mano")); return;
        }
        tier = tier.toUpperCase();
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance().getTierConfig().getAllTiers();
        if (!tiers.containsKey(tier)) {
            player.sendMessage(CC.translate("&c\u2717 Tier inv\u00e1lido"));
            player.sendMessage(CC.translate("&7Disponibles: " + String.join(", ", tiers.keySet())));
            return;
        }
        CustomizedArmor customArmor = CustomizedArmor.fromItemStack(armor);
        if (customArmor == null) { player.sendMessage(CC.translate("&c\u2717 Error al cargar la armadura")); return; }
        String oldTier = customArmor.getTier();
        customArmor.setTier(tier);
        customArmor.applyToItemStack(armor);
        FragmentManager.getInstance().getArmorStorage().saveArmor(customArmor);
        player.sendMessage(CC.translate("&a\u2713 Tier actualizado"));
        player.sendMessage(CC.translate("&7Anterior: &f" + oldTier + " &7-> Nuevo: &f" + tier));
    }

    private void resetArmor(Player player) {
        ItemStack armor = player.getInventory().getItemInMainHand();
        if (!CustomizedArmor.isCustomized(armor)) {
            player.sendMessage(CC.translate("&c\u2717 Sost\u00e9n una armadura personalizada en la mano")); return;
        }
        String hash = CustomizedArmor.getHash(armor);
        FragmentManager.getInstance().getArmorStorage().deleteArmor(hash);
        if (armor.hasItemMeta() && armor.getItemMeta() != null) {
            org.bukkit.inventory.meta.ItemMeta meta = armor.getItemMeta();
            if (meta.hasLore() && meta.getLore() != null) {
                java.util.List<String> lore = new java.util.ArrayList<>(meta.getLore());
                lore.removeIf(line -> line.contains("[ID:") || line.contains("[TIER:") || line.contains("[ATTR:")
                        || line.contains("\u00a78\u00a7m--------------------") || line.contains("\u2694 Atributos:"));
                meta.setLore(lore);
            }
            armor.setItemMeta(meta);
        }
        player.sendMessage(CC.translate("&a\u2713 Armadura reseteada a estado vanilla"));
    }

    private void showStats(Player player) {
        Map<String, Integer> stats = FragmentManager.getInstance().getArmorStorage().getStats();
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Estad\u00edsticas"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&7Total: &f" + stats.getOrDefault("total_armors", 0)));
        player.sendMessage(CC.translate("&3Por Tier:"));
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            if (!entry.getKey().equals("total_armors")) {
                player.sendMessage(CC.translate("&7  " + entry.getKey() + ": &f" + entry.getValue()));
            }
        }
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void reload(Player player) {
        try {
            FragmentManager.getInstance().reload();
            player.sendMessage(CC.translate("&a\u2713 Sistema de fragmentos recargado"));
        } catch (Exception e) {
            player.sendMessage(CC.translate("&c\u2717 Error al recargar: " + e.getMessage()));
        }
    }

    private void showLimits(Player player) {
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance().getTierConfig().getAllTiers();
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3L\u00edmites por Tier"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        for (Map.Entry<String, Map<String, Integer>> tierEntry : tiers.entrySet()) {
            player.sendMessage(CC.translate("&3" + tierEntry.getKey() + ":"));
            for (Map.Entry<String, Integer> attrEntry : tierEntry.getValue().entrySet()) {
                player.sendMessage(CC.translate("&7  " + attrEntry.getKey() + ": &f" + attrEntry.getValue()));
            }
            java.util.List<String> ops = FragmentManager.getInstance().getTierConfig().getAllowedOperations(tierEntry.getKey());
            player.sendMessage(CC.translate("&7  Operaciones: &f" + String.join(", ", ops)));
            player.sendMessage("");
        }
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void setTierLimit(Player player, String tier, String attribute, String valueStr) {
        tier = tier.toUpperCase();
        attribute = attribute.toUpperCase();
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance().getTierConfig().getAllTiers();
        if (!tiers.containsKey(tier)) {
            player.sendMessage(CC.translate("&c\u2717 Tier inv\u00e1lido")); return;
        }
        String[] validAttrs = {"STR", "SKP", "RES", "VIT", "PWR", "ENE"};
        boolean valid = false;
        for (String attr : validAttrs) { if (attr.equals(attribute)) { valid = true; break; } }
        if (!valid) { player.sendMessage(CC.translate("&c\u2717 Atributo inv\u00e1lido")); return; }
        int value;
        try { value = Integer.parseInt(valueStr); if (value < 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { player.sendMessage(CC.translate("&c\u2717 Valor inv\u00e1lido")); return; }
        boolean success = FragmentManager.getInstance().getTierConfig().setLimit(tier, attribute, value);
        if (success) {
            player.sendMessage(CC.translate("&a\u2713 L\u00edmite actualizado"));
            player.sendMessage(CC.translate("&7Tier: &f" + tier + " | Atributo: &f" + attribute + " | Nuevo l\u00edmite: &f" + value));
        } else {
            player.sendMessage(CC.translate("&c\u2717 Error al actualizar el l\u00edmite"));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Admin Fragmentos"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&e/fragmentadmin info &7- Info de armadura en mano"));
        player.sendMessage(CC.translate("&e/fragmentadmin settier <tier> &7- Cambia tier"));
        player.sendMessage(CC.translate("&e/fragmentadmin reset &7- Resetea a vanilla"));
        player.sendMessage(CC.translate("&e/fragmentadmin stats &7- Estad\u00edsticas"));
        player.sendMessage(CC.translate("&e/fragmentadmin limits &7- L\u00edmites por tier"));
        player.sendMessage(CC.translate("&e/fragmentadmin setlimit <tier> <attr> <valor>"));
        player.sendMessage(CC.translate("&e/fragmentadmin reload &7- Recarga config"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }
}