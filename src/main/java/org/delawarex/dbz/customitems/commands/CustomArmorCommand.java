package org.delawarex.dbz.customitems.commands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.customitems.managers.CustomArmorManager;
import org.delawarex.dbz.customitems.menus.armor.ArmorMainMenu;
import org.delawarex.dbz.customitems.menus.armor.ArmorStatMenu;
import org.delawarex.dbz.customitems.models.CustomArmor;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomArmorCommand extends BaseCommand {

    @Command(name = "ca", permission = "dbz.admin.ca")
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length == 0) { new ArmorMainMenu().open(player); return; }

        switch (args.getArgs(0).toLowerCase()) {
            case "menu"     -> new ArmorMainMenu().open(player);
            case "register" -> { if (args.getArgs().length < 2) { usage(player); return; } registerArmor(player, args.getArgs(1)); }
            case "give"     -> { if (args.getArgs().length < 3) { usage(player); return; } giveArmor(player, args.getArgs(1), args.getArgs(2), args.getArgs().length > 3 ? args.getArgs(3) : "1"); }
            case "remove"   -> { if (args.getArgs().length < 2) { usage(player); return; } removeArmor(player, args.getArgs(1)); }
            case "rename"   -> {
                if (args.getArgs().length < 3) { usage(player); return; }
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.getArgs().length; i++) { if (i > 2) sb.append(" "); sb.append(args.getArgs(i)); }
                renameArmor(player, args.getArgs(1), CC.translate(sb.toString()));
            }
            case "list" -> { int p = 1; if (args.getArgs().length > 1) { try { p = Integer.parseInt(args.getArgs(1)); } catch (Exception ignored) {} } listArmors(player, p); }
            case "addstat" -> {
                if (args.getArgs().length < 5) { player.sendMessage(CC.translate("&cUso: /ca addstat <id> <stat> <op> <valor>")); return; }
                addStat(player, args.getArgs(1), args.getArgs(2), args.getArgs(3), args.getArgs(4));
            }
            case "addeffect" -> {
                if (args.getArgs().length < 4) { player.sendMessage(CC.translate("&cUso: /ca addeffect <id> <tipo> <valor>")); return; }
                addEffect(player, args.getArgs(1), args.getArgs(2), args.getArgs(3));
            }
            case "info" -> { if (args.getArgs().length < 2) { usage(player); return; } infoArmor(player, args.getArgs(1)); }
            default     -> usage(player);
        }
    }

    private void registerArmor(Player player, String id) {
        id = id.toLowerCase();
        if (CustomArmorManager.getInstance().exists(id)) { player.sendMessage(CC.translate("&8[&c&l!&8] &cYa existe una armadura con ese ID: &f" + id)); return; }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) { player.sendMessage(CC.translate("&8[&c&l!&8] &cSostén la armadura en la mano.")); return; }
        String displayName = ""; List<String> lore = new ArrayList<>();
        if (hand.hasItemMeta()) { ItemMeta m = hand.getItemMeta(); if (m != null) { if (m.hasDisplayName()) displayName = m.getDisplayName(); if (m.hasLore() && m.getLore() != null) lore = m.getLore(); } }
        CustomArmor armor = new CustomArmor().setId(id).setMaterial(hand.getType().name()).setDisplayName(displayName).setLore(lore);
        CustomArmorManager.getInstance().register(armor);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aArmadura &f" + id + " &aregistrada (&f" + hand.getType().name() + "&a)."));
    }

    private void giveArmor(Player player, String id, String targetName, String amountStr) {
        CustomArmor armor = CustomArmorManager.getInstance().get(id);
        if (armor == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cArmadura no encontrada: &f" + id)); return; }
        Player target = DbzMain.instance.getServer().getPlayer(targetName);
        if (target == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cJugador no encontrado.")); return; }
        int amount = 1; try { amount = Math.max(1, Integer.parseInt(amountStr)); } catch (Exception ignored) {}
        ItemStack stack = CustomArmorManager.getInstance().buildItemStack(armor); stack.setAmount(amount);
        target.getInventory().addItem(stack);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aDado &f" + amount + "x " + id + " &aa &f" + target.getName()));
        target.sendMessage(CC.translate("&8[&a&l✔&8] &aRecibiste &f" + amount + "x " + armor.getDisplayName()));
    }

    private void removeArmor(Player player, String id) {
        if (!CustomArmorManager.getInstance().delete(id)) player.sendMessage(CC.translate("&8[&c&l!&8] &cArmadura no encontrada: &f" + id));
        else player.sendMessage(CC.translate("&8[&a&l✔&8] &aArmadura &f" + id + " &aeliminada."));
    }

    private void renameArmor(Player player, String id, String newName) {
        CustomArmor armor = CustomArmorManager.getInstance().get(id);
        if (armor == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cArmadura no encontrada: &f" + id)); return; }
        armor.setDisplayName(newName); CustomArmorManager.getInstance().update(armor);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aNombre: &f" + newName));
    }

    private void listArmors(Player player, int page) {
        List<String> ids = CustomArmorManager.getInstance().getSortedIds();
        int pageSize = 8, total = Math.max(1, (int) Math.ceil((double) ids.size() / pageSize));
        page = Math.min(Math.max(1, page), total);
        int from = (page-1)*pageSize, to = Math.min(from+pageSize, ids.size());
        player.sendMessage(CC.translate("&8&m----&r &b&lArmaduras Custom &8&m-----------"));
        player.sendMessage(CC.translate("&7Página &e" + page + "&7/&e" + total)); player.sendMessage("");
        for (String listId : ids.subList(from, to)) {
            CustomArmor a = CustomArmorManager.getInstance().get(listId);
            player.sendMessage(CC.translate("&8● &b" + listId + " &8| &7" + (a!=null?a.getMaterial():"") + " &8| &f" + (a!=null?a.getDisplayName():"")));
        }
        player.sendMessage("");
        if (total > 1) player.sendMessage(CC.translate("   " + (page>1?"&a◀ /ca list "+(page-1):"&8◀") + "  &8|  " + (page<total?"&a/ca list "+(page+1)+" ▶":"&8▶")));
        player.sendMessage(CC.translate("&8&m-----------------------------------"));
    }

    private void addStat(Player player, String id, String stat, String op, String valueStr) {
        CustomArmor armor = CustomArmorManager.getInstance().get(id);
        if (armor == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cArmadura no encontrada: &f" + id)); return; }
        stat = stat.toUpperCase();
        if (!ArmorStatMenu.STATS.contains(stat)) {
            player.sendMessage(CC.translate("&8[&c&l!&8] &cStat inválida. Usa: " + String.join(", ", ArmorStatMenu.STATS))); return;
        }
        if (!ArmorStatMenu.OPS.contains(op)) { player.sendMessage(CC.translate("&8[&c&l!&8] &cOperación inválida. Usa: + - *")); return; }
        double value; try { value = Double.parseDouble(valueStr); } catch (NumberFormatException e) { player.sendMessage(CC.translate("&8[&c&l!&8] &cValor inválido.")); return; }
        armor.setStat(stat, op, value); CustomArmorManager.getInstance().update(armor);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aStat: &f" + stat + " " + op + value));
    }

    private void addEffect(Player player, String id, String type, String valueStr) {
        CustomArmor armor = CustomArmorManager.getInstance().get(id);
        if (armor == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cArmadura no encontrada: &f" + id)); return; }
        String typeUpper = type.toUpperCase();
        if (!List.of("HEALTHREGEN","KIREGEN","STAMINAREGEN").contains(typeUpper)) { player.sendMessage(CC.translate("&8[&c&l!&8] &cTipo inválido. Usa: HEALTHREGEN, KIREGEN, STAMINAREGEN")); return; }
        double value; try { value = Double.parseDouble(valueStr); } catch (NumberFormatException e) { player.sendMessage(CC.translate("&8[&c&l!&8] &cValor inválido (0.0-1.0).")); return; }
        armor.getEffects().put(typeUpper, value); CustomArmorManager.getInstance().update(armor);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aEfecto: &f" + typeUpper + " = " + (value*100) + "%"));
    }

    private void infoArmor(Player player, String id) {
        CustomArmor armor = CustomArmorManager.getInstance().get(id);
        if (armor == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cArmadura no encontrada: &f" + id)); return; }
        player.sendMessage(CC.translate("&8&m----&r &b&lInfo Armadura &8&m--------"));
        player.sendMessage(CC.translate("&7ID: &f"+armor.getId()+" &7| Mat: &f"+armor.getMaterial()));
        player.sendMessage(CC.translate("&7Nombre: &f"+armor.getDisplayName()));
        player.sendMessage(CC.translate("&7Irrompible: &f"+armor.isUnbreakable()+" &7| Stats: &f"+armor.getValueByStat().size()+" &7| Efectos: &f"+armor.getEffects().size()));
        armor.getValueByStat().forEach((s,v)->player.sendMessage(CC.translate("  &8● &f"+s+" "+armor.getOperation().getOrDefault(s,"+")+v)));
        armor.getEffects().forEach((e,v)->player.sendMessage(CC.translate("  &8● &f"+e+" "+(v*100)+"%")));
        player.sendMessage(CC.translate("&8&m------------------------------"));
    }

    private void usage(Player player) {
        player.sendMessage(CC.translate("&8[&e&l?&8] &e/ca: register | give | remove | rename | list | addstat | addeffect | info | menu"));
    }
}