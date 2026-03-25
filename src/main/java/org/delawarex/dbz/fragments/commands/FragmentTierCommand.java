package org.delawarex.dbz.fragments.commands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.fragments.manager.FragmentManager;
import org.delawarex.dbz.fragments.model.TierFragment;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;
import java.util.Map;

public class FragmentTierCommand extends BaseCommand {

    @Command(name = "fragmenttier", permission = "dbz.admin.fragment")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length < 1) { sendHelp(player); return; }

        switch (args.getArgs(0).toLowerCase()) {
            case "create" -> {
                if (args.getArgs().length < 3) {
                    player.sendMessage(CC.translate("&cUso: /fragmenttier create <tier> <material>"));
                    player.sendMessage(CC.translate("&7Ej: /fragmenttier create TIER_2 DIAMOND"));
                    return;
                }
                createTierFragment(player, args.getArgs(1), args.getArgs(2));
            }
            case "give" -> {
                if (args.getArgs().length < 4) {
                    player.sendMessage(CC.translate("&cUso: /fragmenttier give <jugador> <tier> <cantidad>")); return;
                }
                giveTierFragment(player, args.getArgs(1), args.getArgs(2), args.getArgs(3));
            }
            case "info" -> showFragmentInfo(player);
            case "list" -> listAvailableTiers(player);
            default -> sendHelp(player);
        }
    }

    private void createTierFragment(Player player, String targetTier, String materialStr) {
        targetTier = targetTier.toUpperCase();
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance().getTierConfig().getAllTiers();
        if (!tiers.containsKey(targetTier)) {
            player.sendMessage(CC.translate("&c\u2717 Tier inv\u00e1lido: " + targetTier));
            player.sendMessage(CC.translate("&7Disponibles: " + String.join(", ", tiers.keySet())));
            return;
        }
        Material mat;
        try { mat = Material.valueOf(materialStr.toUpperCase()); } catch (Exception e) {
            player.sendMessage(CC.translate("&c\u2717 Material inv\u00e1lido: " + materialStr));
            return;
        }
        TierFragment fragment = buildTierFragment(targetTier, mat);
        ItemStack fragmentItem = fragment.toItemStack();
        if (player.getInventory().firstEmpty() == -1) player.getWorld().dropItem(player.getLocation(), fragmentItem);
        else player.getInventory().addItem(fragmentItem);
        player.sendMessage(CC.translate("&a\u2713 Fragmento de tier creado"));
        player.sendMessage(CC.translate("&7Tier objetivo: &f" + targetTier));
    }

    private void giveTierFragment(Player player, String targetName, String targetTier, String amountStr) {
        Player target = player.getServer().getPlayer(targetName);
        if (target == null) { player.sendMessage(CC.translate("&c\u2717 Jugador no encontrado")); return; }
        targetTier = targetTier.toUpperCase();
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance().getTierConfig().getAllTiers();
        if (!tiers.containsKey(targetTier)) { player.sendMessage(CC.translate("&c\u2717 Tier inv\u00e1lido")); return; }
        int amount;
        try { amount = Integer.parseInt(amountStr); if (amount <= 0) throw new Exception(); } catch (Exception e) {
            player.sendMessage(CC.translate("&c\u2717 Cantidad inv\u00e1lida")); return;
        }
        TierFragment fragment = buildTierFragment(targetTier, Material.EMERALD);
        ItemStack fragmentItem = fragment.toItemStack();
        fragmentItem.setAmount(amount);
        if (target.getInventory().firstEmpty() == -1) target.getWorld().dropItem(target.getLocation(), fragmentItem);
        else target.getInventory().addItem(fragmentItem);
        player.sendMessage(CC.translate("&a\u2713 Fragmento de tier entregado a " + target.getName()));
        target.sendMessage(CC.translate("&a\u2713 Has recibido x" + amount + " fragmento(s) de upgrade a " + targetTier));
    }

    private TierFragment buildTierFragment(String targetTier, Material mat) {
        TierFragment fragment = new TierFragment("TIER_FRAG_" + targetTier, targetTier);
        fragment.setMaterial(mat);
        String tierColor = getTierColor(targetTier);
        int tierNum = TierFragment.getTierNumber(targetTier);
        String tierDisplay = tierNum != 999 ? "Tier " + tierNum : targetTier;
        fragment.setDisplayName(tierColor + "\u2b06 Fragmento de Upgrade - " + tierDisplay);
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&7Upgradea armadura a:"));
        fragment.getLore().add(CC.translate("&f  " + tierColor + "\u27a4 " + tierDisplay));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&7\u26a0 Requiere tier previo"));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&e\u26a1 Clic sobre armadura &7para aplicar"));
        return fragment;
    }

    private void showFragmentInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!TierFragment.isTierFragment(item)) {
            player.sendMessage(CC.translate("&c\u2717 Sost\u00e9n un fragmento de tier en la mano")); return;
        }
        String targetTier = TierFragment.getTargetTier(item);
        int tierNum = TierFragment.getTierNumber(targetTier);
        String tierDisplay = tierNum != 999 ? "Tier " + tierNum : targetTier;
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Info del Fragmento de Tier"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&7Tier objetivo: " + getTierColor(targetTier) + tierDisplay));
        player.sendMessage(CC.translate("&7Uso: &eClic &7sobre armadura"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void listAvailableTiers(Player player) {
        Map<String, Map<String, Integer>> tiers = FragmentManager.getInstance().getTierConfig().getAllTiers();
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Tiers Disponibles"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        for (String tierName : tiers.keySet()) {
            int tierNum = TierFragment.getTierNumber(tierName);
            String tierColor = getTierColor(tierName);
            String display = tierNum != 999 ? "Tier " + tierNum : tierName;
            player.sendMessage(CC.translate(tierColor + "  \u2022 " + display));
        }
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Usa: &e/fragmenttier create <tier> <material>"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Fragmentos de Tier - Ayuda"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&e/fragmenttier create <tier> <material>"));
        player.sendMessage(CC.translate("&e/fragmenttier give <jugador> <tier> <cant>"));
        player.sendMessage(CC.translate("&e/fragmenttier info"));
        player.sendMessage(CC.translate("&e/fragmenttier list"));
        player.sendMessage(CC.translate("&7Upgrades son secuenciales (TIER_1 -> TIER_2 -> ...)"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private String getTierColor(String tier) {
        int tierNum = TierFragment.getTierNumber(tier);
        return switch (tierNum) {
            case 1 -> "&7";
            case 2 -> "&a";
            case 3 -> "&b";
            case 4 -> "&d";
            case 5 -> "&6";
            case 999 -> "&5";
            default -> "&f";
        };
    }
}