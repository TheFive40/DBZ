package org.delawarex.dbz.fragments.manager;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.fragments.model.ArmorFragment;
import org.delawarex.dbz.fragments.model.CustomizedArmor;
import org.delawarex.dbz.fragments.model.HashGenerator;
import org.delawarex.dbz.fragments.config.TierConfig;
import org.delawarex.dbz.fragments.storage.FragmentArmorStorage;
import org.delawarex.service.CC;

import java.util.Set;

public class FragmentManager {

    private static FragmentManager instance;
    private TierConfig tierConfig;
    private FragmentArmorStorage armorStorage;

    public FragmentManager() {
        this.tierConfig = new TierConfig();
        this.armorStorage = new FragmentArmorStorage();
    }

    public static FragmentManager getInstance() {
        if (instance == null) instance = new FragmentManager();
        return instance;
    }

    public boolean applyFragment(Player player, ItemStack fragment, ItemStack armor) {
        if (!ArmorFragment.isFragment(fragment)) {
            player.sendMessage(CC.translate("&c\u2717 Este no es un fragmento v\u00e1lido"));
            return false;
        }
        if (armor == null || armor.getType() == Material.AIR) {
            player.sendMessage(CC.translate("&c\u2717 No hay armadura equipada en ese slot"));
            return false;
        }

        String attribute = ArmorFragment.getFragmentAttribute(fragment);
        String operation = ArmorFragment.getFragmentOperation(fragment);
        String valueRaw = ArmorFragment.getFragmentValueRaw(fragment);
        double value = ArmorFragment.getFragmentValue(fragment);

        CustomizedArmor customArmor;
        if (CustomizedArmor.isCustomized(armor)) {
            customArmor = CustomizedArmor.fromItemStack(armor);
            if (customArmor == null) {
                player.sendMessage(CC.translate("&c\u2717 Error al cargar la armadura personalizada"));
                return false;
            }
        } else {
            customArmor = convertVanillaArmor(armor);
            player.sendMessage(CC.translate("&a\u2713 Armadura convertida a personalizada"));
        }

        if (!tierConfig.isOperationAllowed(customArmor.getTier(), operation)) {
            player.sendMessage(CC.translate("&c\u2717 Operaci\u00f3n NO permitida en " + customArmor.getTier()));
            player.sendMessage(CC.translate("&7Operaciones permitidas: &f" +
                    String.join(", ", tierConfig.getAllowedOperations(customArmor.getTier()))));
            return false;
        }

        int currentValue = customArmor.getAttributeValue(attribute);
        int newValue;

        switch (operation) {
            case "+" -> newValue = currentValue + (int) value;
            case "-" -> newValue = currentValue - (int) value;
            case "*" -> {
                int fragmentPct = (int) Math.round((value - 1.0) * 100.0);
                String currentOp = customArmor.getOperations().get(attribute);
                if (currentOp == null || !currentOp.equals("*")) {
                    newValue = fragmentPct;
                } else {
                    newValue = currentValue + fragmentPct;
                }
            }
            default -> newValue = currentValue + (int) value;
        }

        int valueToAdd = newValue - currentValue;
        if (!tierConfig.canApply(customArmor.getTier(), attribute, currentValue, valueToAdd, operation)) {
            player.sendMessage(CC.translate("&c\u2717 El valor exceder\u00eda el l\u00edmite del tier"));
            int limit = tierConfig.getLimit(customArmor.getTier(), attribute);
            String currentDisplay, newDisplay;
            if (operation.equals("*")) {
                currentDisplay = (currentValue >= 0 ? "+" : "") + currentValue + "%";
                newDisplay = (newValue >= 0 ? "+" : "") + newValue + "%";
            } else {
                currentDisplay = String.valueOf(currentValue);
                newDisplay = String.valueOf(newValue);
            }
            player.sendMessage(CC.translate("&7Actual: &f" + currentDisplay + " &7| Nuevo: &f" + newDisplay + " &7| L\u00edmite: &f" + limit));
            return false;
        }

        if (newValue < 0) {
            player.sendMessage(CC.translate("&c\u2717 El atributo no puede ser negativo"));
            return false;
        }

        customArmor.getAttributes().put(attribute, newValue);
        customArmor.getOperations().put(attribute, operation);
        customArmor.applyToItemStack(armor);
        armorStorage.saveArmor(customArmor);

        if (fragment.getAmount() > 1) fragment.setAmount(fragment.getAmount() - 1);
        else {
            ItemStack air = new ItemStack(Material.AIR);
            player.getInventory().setItemInMainHand(air);
        }

        String operationSymbol;
        if (operation.equals("*")) operationSymbol = (((int) Math.round((value - 1.0) * 100.0)) >= 0 ? "+" : "") + (int) Math.round((value - 1.0) * 100.0) + "%";
        else if (operation.equals("-")) operationSymbol = "-" + valueRaw;
        else operationSymbol = "+" + valueRaw;

        int limit = tierConfig.getLimit(customArmor.getTier(), attribute);
        player.sendMessage("");
        player.sendMessage(CC.translate("&a\u2713 Fragmento aplicado exitosamente"));
        player.sendMessage(CC.translate("&7Atributo: &f" + attribute + " &7Operaci\u00f3n: &f" + operationSymbol));
        player.sendMessage(CC.translate("&7L\u00edmite del tier: &f" + limit));
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        return true;
    }

    private CustomizedArmor convertVanillaArmor(ItemStack armor) {
        Set<String> existingHashes = armorStorage.getRegisteredHashes();
        String hash = HashGenerator.generateUniqueHash(existingHashes);
        String defaultTier = tierConfig.getDefaultTier();
        CustomizedArmor customArmor = new CustomizedArmor(hash, defaultTier);
        customArmor.setMaterialType(armor.getType().name());
        customArmor.setArmorSlot(getArmorSlotFromMaterial(armor.getType()));
        String displayName;
        if (armor.hasItemMeta() && armor.getItemMeta() != null && armor.getItemMeta().hasDisplayName()) {
            displayName = armor.getItemMeta().getDisplayName();
        } else {
            displayName = org.bukkit.ChatColor.WHITE + armor.getType().name();
        }
        customArmor.setDisplayName(displayName);
        return customArmor;
    }

    private String getArmorSlotFromMaterial(org.bukkit.Material material) {
        String name = material.name();
        if (name.contains("HELMET")) return "HELMET";
        if (name.contains("CHESTPLATE")) return "CHESTPLATE";
        if (name.contains("LEGGINGS")) return "LEGGINGS";
        if (name.contains("BOOTS")) return "BOOTS";
        return "UNKNOWN";
    }

    public CustomizedArmor getCustomArmor(ItemStack item) {
        if (!CustomizedArmor.isCustomized(item)) return null;
        String hash = CustomizedArmor.getHash(item);
        return armorStorage.loadArmor(hash);
    }

    public TierConfig getTierConfig() { return tierConfig; }
    public FragmentArmorStorage getArmorStorage() { return armorStorage; }

    public void reload() {
        tierConfig.reload();
        armorStorage.reload();
    }
}