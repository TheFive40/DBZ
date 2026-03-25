package org.delawarex.dbz.fragments.commands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.fragments.model.ArmorFragment;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;
import java.util.Arrays;

public class FragmentCommand extends BaseCommand {

    @Command(name = "fragment", permission = "dbz.admin.fragment")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length < 1) { sendHelp(player); return; }

        switch (args.getArgs(0).toLowerCase()) {
            case "create" -> {
                if (args.getArgs().length < 4) {
                    player.sendMessage(CC.translate("&cUso: /fragment create <atributo> <valor> <material>"));
                    player.sendMessage(CC.translate("&7Ej: /fragment create STR 20% EMERALD"));
                    player.sendMessage(CC.translate("&7Ej: /fragment create RES 500 DIAMOND"));
                    return;
                }
                createFragment(player, args.getArgs(1), args.getArgs(2), args.getArgs(3));
            }
            case "give" -> {
                if (args.getArgs().length < 5) {
                    player.sendMessage(CC.translate("&cUso: /fragment give <jugador> <atributo> <valor> <cantidad>"));
                    return;
                }
                giveFragment(player, args.getArgs(1), args.getArgs(2), args.getArgs(3), args.getArgs(4));
            }
            case "info" -> showFragmentInfo(player);
            default -> sendHelp(player);
        }
    }

    private void createFragment(Player player, String attribute, String value, String materialStr) {
        String[] validAttrs = {"STR", "SKP", "RES", "VIT", "PWR", "ENE"};
        if (!Arrays.asList(validAttrs).contains(attribute.toUpperCase())) {
            player.sendMessage(CC.translate("&c\u2717 Atributo inv\u00e1lido"));
            player.sendMessage(CC.translate("&7V\u00e1lidos: STR, SKP, RES, VIT, PWR, ENE"));
            return;
        }
        try { validateValue(value); } catch (Exception e) {
            player.sendMessage(CC.translate("&c\u2717 Valor inv\u00e1lido: " + e.getMessage()));
            return;
        }
        Material mat;
        try { mat = Material.valueOf(materialStr.toUpperCase()); } catch (Exception e) {
            player.sendMessage(CC.translate("&c\u2717 Material inv\u00e1lido: " + materialStr));
            return;
        }
        ArmorFragment fragment = buildFragment(attribute, value, mat);
        ItemStack fragmentItem = fragment.toItemStack();
        if (player.getInventory().firstEmpty() == -1) player.getWorld().dropItem(player.getLocation(), fragmentItem);
        else player.getInventory().addItem(fragmentItem);
        player.sendMessage(CC.translate("&a\u2713 Fragmento creado exitosamente"));
    }

    private void giveFragment(Player player, String targetName, String attribute, String value, String amountStr) {
        Player target = player.getServer().getPlayer(targetName);
        if (target == null) { player.sendMessage(CC.translate("&c\u2717 Jugador no encontrado")); return; }
        String[] validAttrs = {"STR", "SKP", "RES", "VIT", "PWR", "ENE"};
        if (!Arrays.asList(validAttrs).contains(attribute.toUpperCase())) {
            player.sendMessage(CC.translate("&c\u2717 Atributo inv\u00e1lido")); return;
        }
        try { validateValue(value); } catch (Exception e) {
            player.sendMessage(CC.translate("&c\u2717 Valor inv\u00e1lido: " + e.getMessage())); return;
        }
        int amount;
        try { amount = Integer.parseInt(amountStr); if (amount <= 0) throw new Exception(); } catch (Exception e) {
            player.sendMessage(CC.translate("&c\u2717 Cantidad inv\u00e1lida")); return;
        }
        ArmorFragment fragment = buildFragment(attribute, value, Material.EMERALD);
        ItemStack fragmentItem = fragment.toItemStack();
        fragmentItem.setAmount(amount);
        if (target.getInventory().firstEmpty() == -1) target.getWorld().dropItem(target.getLocation(), fragmentItem);
        else target.getInventory().addItem(fragmentItem);
        player.sendMessage(CC.translate("&a\u2713 Fragmento entregado a " + target.getName()));
        target.sendMessage(CC.translate("&a\u2713 Has recibido x" + amount + " fragmento(s) de " + attribute.toUpperCase()));
    }

    private ArmorFragment buildFragment(String attribute, String value, Material mat) {
        ArmorFragment fragment = new ArmorFragment(
                "FRAG_" + attribute.toUpperCase() + "_" + value.replace("%", "P").replace("-", "N"),
                attribute.toUpperCase(), value);
        fragment.setMaterial(mat);
        String operationDisplay = getOperationDisplay(value);
        String colorCode = getOperationColor(value);
        fragment.setDisplayName("&3Fragmento de " + getAttributeName(attribute.toUpperCase()));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&7Atributo: &f" + attribute.toUpperCase()));
        fragment.getLore().add(CC.translate("&7Valor: " + colorCode + operationDisplay));
        fragment.getLore().add(CC.translate("&8"));
        fragment.getLore().add(CC.translate("&e\u26a1 Clic sobre armadura &7para aplicar"));
        return fragment;
    }

    private void showFragmentInfo(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!ArmorFragment.isFragment(item)) {
            player.sendMessage(CC.translate("&c\u2717 Sost\u00e9n un fragmento en la mano")); return;
        }
        String attr = ArmorFragment.getFragmentAttribute(item);
        String op = ArmorFragment.getFragmentOperation(item);
        String valueRaw = ArmorFragment.getFragmentValueRaw(item);
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Info del Fragmento"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&7Atributo: &f" + attr));
        player.sendMessage(CC.translate("&7Operaci\u00f3n: &f" + op));
        player.sendMessage(CC.translate("&7Valor: &f" + getOperationDisplay(valueRaw)));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&3Fragmentos - Ayuda"));
        player.sendMessage(CC.translate("&8&m--------------------"));
        player.sendMessage(CC.translate("&e/fragment create <attr> <valor> <material>"));
        player.sendMessage(CC.translate("&7  Crea un fragmento"));
        player.sendMessage(CC.translate("&e/fragment give <jugador> <attr> <valor> <cant>"));
        player.sendMessage(CC.translate("&7  Da fragmentos"));
        player.sendMessage(CC.translate("&e/fragment info"));
        player.sendMessage(CC.translate("&7  Info del fragmento en mano"));
        player.sendMessage(CC.translate("&7Atributos: &fSTR, SKP, RES, VIT, PWR, ENE"));
        player.sendMessage(CC.translate("&7Valores: &f500 &7(+500) | &f-100 &7(-100) | &f20% &7(+20%) | &f-15% &7(-15%)"));
        player.sendMessage(CC.translate("&8&m--------------------"));
    }

    private void validateValue(String value) throws Exception {
        if (value.endsWith("%")) Double.parseDouble(value.substring(0, value.length() - 1));
        else Integer.parseInt(value);
    }

    private String getOperationDisplay(String value) {
        if (value.endsWith("%")) return value;
        else if (value.startsWith("-")) return value;
        else return "+" + value;
    }

    private String getOperationColor(String value) {
        if (value.endsWith("%")) return "&b";
        else if (value.startsWith("-")) return "&c";
        else return "&a";
    }

    private String getAttributeName(String attr) {
        return switch (attr) {
            case "STR" -> "Fuerza";
            case "SKP" -> "Habilidad";
            case "RES" -> "Resistencia";
            case "VIT" -> "Vitalidad";
            case "PWR" -> "Poder";
            case "ENE" -> "Energ\u00eda";
            default -> attr;
        };
    }
}