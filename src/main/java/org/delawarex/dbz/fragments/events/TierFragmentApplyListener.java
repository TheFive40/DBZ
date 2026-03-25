package org.delawarex.dbz.fragments.events;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.fragments.model.CustomizedArmor;
import org.delawarex.dbz.fragments.manager.FragmentManager;
import org.delawarex.dbz.fragments.model.TierFragment;
import org.delawarex.service.CC;

import java.util.Map;

public class TierFragmentApplyListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTierFragmentUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!TierFragment.isTierFragment(itemInHand)) return;

        event.setCancelled(true);

        ItemStack[] armorContents = player.getInventory().getArmorContents();
        ItemStack targetArmor = null;
        int targetSlot = -1;

        for (int i = 3; i >= 0; i--) {
            if (armorContents[i] != null && armorContents[i].getType() != Material.AIR) {
                targetArmor = armorContents[i];
                targetSlot = i;
                break;
            }
        }

        if (targetArmor == null) {
            player.sendMessage(CC.translate("&c\u2717 No tienes ninguna armadura equipada"));
            return;
        }

        if (!CustomizedArmor.isCustomized(targetArmor)) {
            player.sendMessage(CC.translate("&c\u2717 Esta armadura no est\u00e1 personalizada"));
            player.sendMessage(CC.translate("&7Solo se pueden upgradear armaduras con fragmentos aplicados"));
            return;
        }

        String targetTier = TierFragment.getTargetTier(itemInHand);
        String currentTier = CustomizedArmor.getTier(targetArmor);

        if (!TierFragment.canUpgrade(currentTier, targetTier)) {
            int currentNum = TierFragment.getTierNumber(currentTier);
            int targetNum = TierFragment.getTierNumber(targetTier);
            player.sendMessage("");
            player.sendMessage(CC.translate("&c\u2717 No se puede aplicar este fragmento"));
            player.sendMessage(CC.translate("&7Tier actual: &f" + currentTier));
            player.sendMessage(CC.translate("&7Tier del fragmento: &f" + targetTier));
            if (targetNum <= currentNum) {
                player.sendMessage(CC.translate("&7La armadura ya tiene un tier igual o superior"));
            } else {
                player.sendMessage(CC.translate("&7Los upgrades deben ser secuenciales"));
                player.sendMessage(CC.translate("&7Necesitas: &fTIER_" + (currentNum + 1)));
            }
            player.sendMessage("");
            return;
        }

        CustomizedArmor customArmor = CustomizedArmor.fromItemStack(targetArmor);
        if (customArmor == null) {
            player.sendMessage(CC.translate("&c\u2717 Error al cargar la armadura"));
            return;
        }

        boolean exceedsLimits = false;
        StringBuilder errorMsg = new StringBuilder();
        for (Map.Entry<String, Integer> entry : customArmor.getAttributes().entrySet()) {
            String attr = entry.getKey();
            int currentValue = entry.getValue();
            String operation = customArmor.getOperations().getOrDefault(attr, "+");
            if (FragmentManager.getInstance().getTierConfig().exceedsLimit(targetTier, attr, currentValue, operation)) {
                exceedsLimits = true;
                if (errorMsg.length() == 0) errorMsg.append(CC.translate("&c\u2717 Atributos exceden l\u00edmites del nuevo tier:\n&7"));
                else errorMsg.append(", ");
                int newLimit = FragmentManager.getInstance().getTierConfig().getLimit(targetTier, attr);
                String displayValue;
                if (operation.equals("*")) {
                    double percentage = (currentValue / 100.0 - 1.0) * 100.0;
                    displayValue = String.format("%+.0f%%", percentage);
                } else {
                    displayValue = (currentValue >= 0 ? "+" : "") + currentValue;
                }
                errorMsg.append(attr).append(": ").append(displayValue).append(" (l\u00edmite: ").append(newLimit).append(")");
            }
        }

        if (exceedsLimits) {
            player.sendMessage("");
            player.sendMessage(errorMsg.toString());
            player.sendMessage(CC.translate("&7Usa fragmentos negativos para reducir valores"));
            player.sendMessage("");
            return;
        }

        boolean hasInvalidOperations = false;
        StringBuilder opErrorMsg = new StringBuilder();
        for (Map.Entry<String, String> entry : customArmor.getOperations().entrySet()) {
            String attr = entry.getKey();
            String operation = entry.getValue();
            if (!FragmentManager.getInstance().getTierConfig().isOperationAllowed(targetTier, operation)) {
                hasInvalidOperations = true;
                if (opErrorMsg.length() == 0) opErrorMsg.append(CC.translate("&c\u2717 Operaciones NO permitidas en " + targetTier + ":\n&7"));
                else opErrorMsg.append(", ");
                opErrorMsg.append(attr).append(" (").append(operation).append(")");
            }
        }

        if (hasInvalidOperations) {
            player.sendMessage("");
            player.sendMessage(opErrorMsg.toString());
            player.sendMessage(CC.translate("&7Operaciones permitidas: &f" +
                    String.join(", ", FragmentManager.getInstance().getTierConfig().getAllowedOperations(targetTier))));
            player.sendMessage("");
            return;
        }

        String oldTier = customArmor.getTier();
        customArmor.setTier(targetTier);
        customArmor.applyToItemStack(targetArmor);
        FragmentManager.getInstance().getArmorStorage().saveArmor(customArmor);

        armorContents[targetSlot] = targetArmor;
        player.getInventory().setArmorContents(armorContents);
        player.updateInventory();

        if (itemInHand.getAmount() > 1) itemInHand.setAmount(itemInHand.getAmount() - 1);
        else player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

        player.sendMessage("");
        player.sendMessage(CC.translate("&a\u2713 \u00a1Armadura upgradeada exitosamente!"));
        player.sendMessage(CC.translate("&7Tier anterior: &f" + oldTier));
        player.sendMessage(CC.translate("&7Tier nuevo: &a" + targetTier));
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Todos los stats y operaciones se han mantenido"));
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }
}