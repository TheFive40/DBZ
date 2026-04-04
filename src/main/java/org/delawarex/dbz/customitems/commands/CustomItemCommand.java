package org.delawarex.dbz.customitems.commands;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.customitems.managers.CustomItemManager;
import org.delawarex.dbz.customitems.menus.item.ItemMainMenu;
import org.delawarex.dbz.customitems.menus.item.ItemStatMenu;
import org.delawarex.dbz.customitems.models.CustomItem;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomItemCommand extends BaseCommand {

    @Command(name = "ci", permission = "dbz.admin.ci", aliases = {"customitem", "customitems"})
    @Override
    public void onCommand(CommandArgs args) throws IOException {
        if (!args.isPlayer()) return;
        Player player = args.getPlayer();

        if (args.getArgs().length == 0) { new ItemMainMenu().open(player); return; }

        switch (args.getArgs(0).toLowerCase()) {
            case "menu"     -> new ItemMainMenu().open(player);
            case "register" -> { if (args.getArgs().length < 2) { usage(player); return; } registerItem(player, args.getArgs(1)); }
            case "give"     -> { if (args.getArgs().length < 3) { usage(player); return; } giveItem(player, args.getArgs(1), args.getArgs(2), args.getArgs().length > 3 ? args.getArgs(3) : "1"); }
            case "remove"   -> { if (args.getArgs().length < 2) { usage(player); return; } removeItem(player, args.getArgs(1)); }
            case "rename"   -> {
                if (args.getArgs().length < 3) { usage(player); return; }
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.getArgs().length; i++) { if (i > 2) sb.append(" "); sb.append(args.getArgs(i)); }
                renameItem(player, args.getArgs(1), CC.translate(sb.toString()));
            }
            case "list" -> { int p = 1; if (args.getArgs().length > 1) { try { p = Integer.parseInt(args.getArgs(1)); } catch (Exception ignored) {} } listItems(player, p); }
            case "addstat" -> {
                if (args.getArgs().length < 5) { player.sendMessage(CC.translate("&cUso: /ci addstat <id> <stat> <op> <valor>")); return; }
                addStat(player, args.getArgs(1), args.getArgs(2), args.getArgs(3), args.getArgs(4));
            }
            case "addeffect" -> {
                if (args.getArgs().length < 4) { player.sendMessage(CC.translate("&cUso: /ci addeffect <id> <tipo> <valor>")); return; }
                addEffect(player, args.getArgs(1), args.getArgs(2), args.getArgs(3));
            }
            case "addcmd" -> {
                if (args.getArgs().length < 3) { player.sendMessage(CC.translate("&cUso: /ci addcmd <id> <comando>")); return; }
                StringBuilder cmd = new StringBuilder();
                for (int i = 2; i < args.getArgs().length; i++) { if (i > 2) cmd.append(" "); cmd.append(args.getArgs(i)); }
                addCommand(player, args.getArgs(1), cmd.toString());
            }
            case "info" -> { if (args.getArgs().length < 2) { usage(player); return; } infoItem(player, args.getArgs(1)); }
            default     -> usage(player);
        }
    }

    private void registerItem(Player player, String id) {
        id = id.toLowerCase();
        if (CustomItemManager.getInstance().exists(id)) { player.sendMessage(CC.translate("&8[&c&l!&8] &cYa existe un item con ese ID: &f" + id)); return; }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) { player.sendMessage(CC.translate("&8[&c&l!&8] &cSostén un item en la mano.")); return; }
        String displayName = ""; List<String> lore = new ArrayList<>();
        if (hand.hasItemMeta()) { ItemMeta m = hand.getItemMeta(); if (m != null) { if (m.hasDisplayName()) displayName = m.getDisplayName(); if (m.hasLore() && m.getLore() != null) lore = m.getLore(); } }
        CustomItem ci = new CustomItem().setId(id).setMaterial(hand.getType().name()).setDisplayName(displayName).setLore(lore);
        CustomItemManager.getInstance().register(ci);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aItem &f" + id + " &aregistrado (&f" + hand.getType().name() + "&a)."));
    }

    private void giveItem(Player player, String id, String targetName, String amountStr) {
        CustomItem ci = CustomItemManager.getInstance().get(id);
        if (ci == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cItem no encontrado: &f" + id)); return; }
        Player target = DbzMain.instance.getServer().getPlayer(targetName);
        if (target == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cJugador no encontrado.")); return; }
        int amount = 1; try { amount = Math.max(1, Integer.parseInt(amountStr)); } catch (Exception ignored) {}
        ItemStack stack = CustomItemManager.getInstance().buildItemStack(ci); stack.setAmount(amount);
        target.getInventory().addItem(stack);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aDado &f" + amount + "x " + id + " &aa &f" + target.getName()));
        target.sendMessage(CC.translate("&8[&a&l✔&8] &aRecibiste &f" + amount + "x " + ci.getDisplayName()));
    }

    private void removeItem(Player player, String id) {
        if (!CustomItemManager.getInstance().delete(id)) player.sendMessage(CC.translate("&8[&c&l!&8] &cItem no encontrado: &f" + id));
        else player.sendMessage(CC.translate("&8[&a&l✔&8] &aItem &f" + id + " &aeliminado."));
    }

    private void renameItem(Player player, String id, String newName) {
        CustomItem ci = CustomItemManager.getInstance().get(id);
        if (ci == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cItem no encontrado: &f" + id)); return; }
        ci.setDisplayName(newName); CustomItemManager.getInstance().update(ci);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aNombre: &f" + newName));
    }

    private void listItems(Player player, int page) {
        List<String> ids = CustomItemManager.getInstance().getSortedIds();
        int pageSize = 8, total = Math.max(1, (int) Math.ceil((double) ids.size() / pageSize));
        page = Math.min(Math.max(1, page), total);
        int from = (page-1)*pageSize, to = Math.min(from+pageSize, ids.size());
        player.sendMessage(CC.translate("&8&m----&r &c&lItems Custom &8&m-----------------"));
        player.sendMessage(CC.translate("&7Página &e" + page + "&7/&e" + total)); player.sendMessage("");
        for (String listId : ids.subList(from, to)) {
            CustomItem ci = CustomItemManager.getInstance().get(listId);
            player.sendMessage(CC.translate("&8● &c" + listId + " &8| &7" + (ci!=null?ci.getMaterial():"") + " &8| &f" + (ci!=null?ci.getDisplayName():"")));
        }
        player.sendMessage("");
        if (total > 1) player.sendMessage(CC.translate("   " + (page>1?"&a◀ /ci list "+(page-1):"&8◀") + "  &8|  " + (page<total?"&a/ci list "+(page+1)+" ▶":"&8▶")));
        player.sendMessage(CC.translate("&8&m-----------------------------------"));
    }

    private void addStat(Player player, String id, String stat, String op, String valueStr) {
        CustomItem ci = CustomItemManager.getInstance().get(id);
        if (ci == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cItem no encontrado: &f" + id)); return; }
        stat = stat.toUpperCase();
        if (!ItemStatMenu.STATS.contains(stat)) {
            player.sendMessage(CC.translate("&8[&c&l!&8] &cStat inválida. Usa: " + String.join(", ", ItemStatMenu.STATS))); return;
        }
        if (!ItemStatMenu.OPS.contains(op)) { player.sendMessage(CC.translate("&8[&c&l!&8] &cOperación inválida. Usa: + - *")); return; }
        double value; try { value = Double.parseDouble(valueStr); } catch (NumberFormatException e) { player.sendMessage(CC.translate("&8[&c&l!&8] &cValor inválido.")); return; }
        ci.setStat(stat, op, value); CustomItemManager.getInstance().update(ci);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aStat: &f" + stat + " " + op + value));
    }

    private void addEffect(Player player, String id, String type, String valueStr) {
        CustomItem ci = CustomItemManager.getInstance().get(id);
        if (ci == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cItem no encontrado: &f" + id)); return; }
        String typeUpper = type.toUpperCase();
        if (!List.of("HEALTHREGEN","KIREGEN","STAMINAREGEN").contains(typeUpper)) { player.sendMessage(CC.translate("&8[&c&l!&8] &cTipo inválido. Usa: HEALTHREGEN, KIREGEN, STAMINAREGEN")); return; }
        double value; try { value = Double.parseDouble(valueStr); } catch (NumberFormatException e) { player.sendMessage(CC.translate("&8[&c&l!&8] &cValor inválido (0.0-1.0).")); return; }
        ci.getEffects().put(typeUpper, value); CustomItemManager.getInstance().update(ci);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aEfecto: &f" + typeUpper + " = " + (value*100) + "%"));
    }

    private void addCommand(Player player, String id, String cmd) {
        CustomItem ci = CustomItemManager.getInstance().get(id);
        if (ci == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cItem no encontrado: &f" + id)); return; }
        String cleaned = cmd.startsWith("/") ? cmd.substring(1) : cmd;
        ci.getCommands().add(cleaned); CustomItemManager.getInstance().update(ci);
        player.sendMessage(CC.translate("&8[&a&l✔&8] &aComando: &f/" + cleaned));
    }

    private void infoItem(Player player, String id) {
        CustomItem ci = CustomItemManager.getInstance().get(id);
        if (ci == null) { player.sendMessage(CC.translate("&8[&c&l!&8] &cItem no encontrado: &f" + id)); return; }
        player.sendMessage(CC.translate("&8&m----&r &c&lInfo Item &8&m-----------"));
        player.sendMessage(CC.translate("&7ID: &f"+ci.getId()+" &7| Mat: &f"+ci.getMaterial()));
        player.sendMessage(CC.translate("&7Nombre: &f"+ci.getDisplayName()));
        player.sendMessage(CC.translate("&7Consumible: &f"+ci.isConsumable()+" &7| Irrompible: &f"+ci.isUnbreakable()+" &7| TP: &f"+ci.getTpValue()));
        player.sendMessage(CC.translate("&7Cmds: &f"+ci.getCommands().size()+" &7| Stats: &f"+ci.getValueByStat().size()+" &7| Efectos: &f"+ci.getEffects().size()));
        ci.getValueByStat().forEach((s,v)->player.sendMessage(CC.translate("  &8● &f"+s+" "+ci.getOperation().getOrDefault(s,"+")+v)));
        ci.getEffects().forEach((e,v)->player.sendMessage(CC.translate("  &8● &f"+e+" "+(v*100)+"%")));
        player.sendMessage(CC.translate("&8&m------------------------------"));
    }

    private void usage(Player player) {
        player.sendMessage(CC.translate("&8[&e&l?&8] &e/ci: register | give | remove | rename | list | addstat | addeffect | addcmd | info | menu"));
    }
}