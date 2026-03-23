package org.delawarex.dbz.tps.commands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.tps.managers.TpManager;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;
import java.util.List;

public class RegisterTpCommand extends BaseCommand {
    @Override
    @Command(name = "dbtps", permission = "dbz.admin.dbtps")
    public void onCommand(CommandArgs command) throws IOException {
        String base = command.getArgs(0).toLowerCase();
        Player player = command.getPlayer();
        TpManager tpManager = new TpManager();

        switch (base) {

            case "give" -> {
                try {
                    player = DbzMain.instance.getServer().getPlayer(command.getArgs(1));
                    int value = Integer.parseInt(command.getArgs(2));
                    int amount = Integer.parseInt(command.getArgs(3));

                    if (tpManager.give(value) == null) {
                        player.sendMessage(CC.translate("&8[&c&l!&8] &cEl valor del TP no existe."));
                        player.playSound(player, Sound.ENTITY_CAT_HURT, 1.0f, 1.0f);
                        break;
                    }

                    for (int i = 0; i < amount; i += 64) {
                        ItemStack stack = tpManager.give(value).clone();
                        stack.setAmount(Math.min(64, amount - i));
                        player.getInventory().addItem(stack);
                    }
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                } catch (Exception ignored) {
                    player.sendMessage(CC.translate("&8[&c&l!&8] &cUso: /dbtps give <player> <valor> <cantidad>"));
                    player.playSound(player, Sound.ENTITY_CAT_HURT, 1.0f, 1.0f);
                }
            }

            case "register" -> {
                try {
                    int amount = Integer.parseInt(command.getArgs(1));
                    ItemStack item = player.getInventory().getItemInMainHand();
                    ItemMeta meta = item.getItemMeta();

                    meta.setDisplayName(CC.translate("&a+" + amount + " TPS"));
                    meta.setLore(List.of(
                            CC.translate("&eAl presionar clic derecho al aire."),
                            CC.translate("&eConsumiras &6+" + amount + " Tps"),
                            CC.translate("&f"),
                            CC.translate("&C&l㊙ ITEM CONSUMIBLE")
                    ));
                    item.setItemMeta(meta);

                    tpManager.add(amount, item);
                    player.sendMessage(CC.translate("&8[&a&l✔&8] &aTP de &6+" + amount + " &aregistrado correctamente."));
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                } catch (Exception ignored) {
                    player.sendMessage(CC.translate("&8[&c&l!&8] &cUso: /dbtps register <cantidad>"));
                    player.playSound(player, Sound.ENTITY_CAT_HURT, 1.0f, 1.0f);
                }
            }

            case "remove" -> {
                try {
                    int amount = Integer.parseInt(command.getArgs(1));

                    if (tpManager.give(amount) == null) {
                        player.sendMessage(CC.translate("&8[&c&l!&8] &cNo existe un TP con ese valor."));
                        player.playSound(player, Sound.ENTITY_CAT_HURT, 1.0f, 1.0f);
                        break;
                    }

                    tpManager.remove(amount);
                    player.sendMessage(CC.translate("&8[&a&l✔&8] &aTP de &6+" + amount + " &aeliminado correctamente."));
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                } catch (Exception ignored) {
                    player.sendMessage(CC.translate("&8[&c&l!&8] &cUso: /dbtps remove <valor>"));
                    player.playSound(player, Sound.ENTITY_CAT_HURT, 1.0f, 1.0f);
                }
            }

            case "rename" -> {
                try {
                    int amount = Integer.parseInt(command.getArgs(1));

                    if (tpManager.give(amount) == null) {
                        player.sendMessage(CC.translate("&8[&c&l!&8] &cNo existe un TP con ese valor."));
                        player.playSound(player, Sound.ENTITY_CAT_HURT, 1.0f, 1.0f);
                        break;
                    }

                    StringBuilder nameBuilder = new StringBuilder();
                    for (int i = 2; i < command.getArgs().length; i++) {
                        if (i > 2) nameBuilder.append(" ");
                        nameBuilder.append(command.getArgs(i));
                    }
                    String newName = CC.translate(nameBuilder.toString());

                    ItemStack item = tpManager.give(amount).clone();
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(newName);
                    item.setItemMeta(meta);

                    tpManager.add(amount, item);
                    player.sendMessage(CC.translate("&8[&a&l✔&8] &aNombre del TP &6+" + amount + " &aactualizado."));
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                } catch (Exception ignored) {
                    player.sendMessage(CC.translate("&8[&c&l!&8] &cUso: /dbtps rename <valor> <nombre>"));
                    player.playSound(player, Sound.ENTITY_CAT_HURT, 1.0f, 1.0f);
                }
            }
            case "list" -> {
                try {
                    int page = command.getArgs().length > 1
                            ? Math.max(1, Integer.parseInt(command.getArgs(1)))
                            : 1;

                    int pageSize = 8;
                    List<Integer> keys = tpManager.getKeys();

                    if (keys.isEmpty()) {
                        player.sendMessage(CC.translate("&8[&c&l!&8] &cNo hay TPs registrados."));
                        break;
                    }

                    int totalPages = (int) Math.ceil((double) keys.size() / pageSize);
                    page = Math.min(page, totalPages);

                    int from = (page - 1) * pageSize;
                    int to = Math.min(from + pageSize, keys.size());
                    List<Integer> pageKeys = keys.subList(from, to);

                    player.sendMessage(CC.translate("&8&m----&r &6&lTPs Registrados &8&m-----------------"));
                    player.sendMessage(CC.translate("&7Página &e" + page + "&7/&e" + totalPages));
                    player.sendMessage("");

                    for (Integer key : pageKeys) {
                        ItemStack tp = tpManager.give(key);
                        String displayName = (tp.hasItemMeta() && tp.getItemMeta().hasDisplayName())
                                ? tp.getItemMeta().getDisplayName()
                                : "&f+" + key + " TPS";
                        player.sendMessage(CC.translate(
                                "&8● &6+" + key + " TPS &8| &7Nombre: " + displayName +
                                        " &8| &7Item: &f" + tp.getType().name()
                        ));
                    }

                    player.sendMessage("");

                    if (totalPages > 1) {
                        String prev = page > 1
                                ? "&a◀ /dbtps list " + (page - 1)
                                : "&8◀";
                        String next = page < totalPages
                                ? "&a/dbtps list " + (page + 1) + " ▶"
                                : "&8▶";
                        player.sendMessage(CC.translate("   " + prev + "  &8|  " + next));
                    }

                    player.sendMessage(CC.translate("&8&m-----------------------------------"));

                } catch (Exception ignored) {
                    player.sendMessage(CC.translate("&8[&c&l!&8] &cUso: /dbtps list [página]"));
                }
            }
            default -> {
                player.sendMessage(CC.translate("&8[&e&l?&8] &eComandos disponibles:"));
                player.sendMessage(CC.translate("&7/dbtps give <player> <valor> <cantidad> "));
                player.sendMessage(CC.translate("&7/dbtps register <cantidad>"));
                player.sendMessage(CC.translate("&7/dbtps list [página]"));
                player.sendMessage(CC.translate("&7/dbtps remove <valor>"));
                player.sendMessage(CC.translate("&7/dbtps rename <valor> <nombre>"));
            }
        }
    }
}