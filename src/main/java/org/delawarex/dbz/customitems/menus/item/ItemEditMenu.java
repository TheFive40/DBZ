package org.delawarex.dbz.customitems.menus.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.managers.CustomItemManager;
import org.delawarex.dbz.customitems.managers.CustomDurabilityManager;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.customitems.models.CustomItem;
import org.delawarex.dbz.customitems.utils.PastebinReader;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class ItemEditMenu extends Menu {

    private final String itemId;

    public ItemEditMenu(String itemId) { this.itemId = itemId; }

    @Override protected String getTitle() { return "&c&lEditar Item: &f"; }
    @Override protected int getRows()     { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        CustomItem item = CustomItemManager.getInstance().get(itemId);
        if (item == null) { player.closeInventory(); return; }

        // ── Fila 1 ─ Info ───────────────────────────────────────────────────

        set(4, item(Material.PAPER,
                "&7ID: &f" + item.getId(),
                "&7Material: &f" + item.getMaterial(),
                "&7Nombre: &f" + item.getDisplayName(),
                "&7Lore: &f" + (item.getLore() != null ? item.getLore().size() : 0) + " líneas",
                "&7Cmds: &f" + item.getCommands().size(),
                "&7TP: &f" + item.getTpValue(),
                "&7Durabilidad: &f" + (item.getMaxDurability() > 0 ? item.getMaxDurability() : "Sin límite"),
                "&7Activo: &f" + item.isActive()));

        // ── Fila 2 ─ Edición básica ──────────────────────────────────────────

        // Renombrar
        set(10, item(Material.NAME_TAG,
                        "&eRenombrar",
                        "&7Actual: &f" + item.getDisplayName(),
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Nuevo nombre (soporta &colores):", (p, text) -> {
                    item.setDisplayName(CC.translate(text));
                    CustomItemManager.getInstance().update(item);
                    p.sendMessage(CC.translate("&aNombre actualizado."));
                    new ItemEditMenu(itemId).open(p);
                }));

        // Lore desde Pastebin
        set(11, item(Material.BOOK,
                        "&dLore desde Pastebin",
                        "&7Líneas actuales: &f" + (item.getLore() != null ? item.getLore().size() : 0),
                        "",
                        "&7Pega la URL de Pastebin",
                        "&7Ej: pastebin.com/xxxxxxxx",
                        "", "&a[CLICK]"),
                e -> pastebin(player, item));

        // Limpiar lore
        set(12, item(Material.WRITABLE_BOOK,
                        "&cLimpiar Lore",
                        "&7Elimina todas las líneas de lore",
                        "", "&c[CLICK]"),
                e -> {
                    item.setLore(new ArrayList<>());
                    CustomItemManager.getInstance().update(item);
                    player.sendMessage(CC.translate("&aLore limpiado."));
                    new ItemEditMenu(itemId).open(player);
                });

        // Stats
        List<String> statLines = new ArrayList<>();
        if (item.getValueByStat().isEmpty()) {
            statLines.add("&7Sin estadísticas");
        } else {
            item.getValueByStat().forEach((s, v) ->
                    statLines.add("&7" + s + ": &f" + item.getOperation().getOrDefault(s, "+") + v));
        }
        statLines.add(""); statLines.add("&a[CLICK para gestionar]");
        set(13, item(Material.REDSTONE, "&aEstadísticas", statLines.toArray(new String[0])),
                e -> new ItemStatMenu(itemId).open(player));

        // Efectos
        List<String> effLines = new ArrayList<>();
        if (item.getEffects().isEmpty()) {
            effLines.add("&7Sin efectos");
        } else {
            item.getEffects().forEach((eff, val) ->
                    effLines.add("&7" + eff + ": &f" + (val * 100) + "%"));
        }
        effLines.add(""); effLines.add("&a[CLICK para gestionar]");
        set(14, item(Material.BLAZE_POWDER, "&6Efectos", effLines.toArray(new String[0])),
                e -> new ItemEffectMenu(itemId).open(player));

        // ── Fila 3 ─ Toggles y configuraciones ──────────────────────────────

        // Consumible
        set(19, item(Material.APPLE,
                        "&a&lConsumible",
                        "&7El item se consume al usarlo.",
                        "",
                        item.isConsumable()
                                ? "&a✔ ACTIVADO &8[CLICK desactivar]"
                                : "&c✘ DESACTIVADO &8[CLICK activar]"),
                e -> {
                    item.setConsumable(!item.isConsumable());
                    CustomItemManager.getInstance().update(item);
                    new ItemEditMenu(itemId).open(player);
                });

        // Irrompible
        set(20, item(Material.BEDROCK,
                        "&b&lIrrompible",
                        "",
                        item.isUnbreakable()
                                ? "&a✔ ACTIVADO &8[CLICK desactivar]"
                                : "&c✘ DESACTIVADO &8[CLICK activar]"),
                e -> {
                    item.setUnbreakable(!item.isUnbreakable());
                    CustomItemManager.getInstance().update(item);
                    new ItemEditMenu(itemId).open(player);
                });

        // TP Value
        set(21, item(Material.EXPERIENCE_BOTTLE,
                        "&b&lTP: &f" + item.getTpValue(),
                        "&7TPs al consumir el item.",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Valor de TP (0 para desactivar):", (p, text) -> {
                    try {
                        item.setTpValue(Math.max(0, Integer.parseInt(text.trim())));
                        CustomItemManager.getInstance().update(item);
                        p.sendMessage(CC.translate("&aTP: &f" + item.getTpValue()));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cNúmero inválido."));
                    }
                    new ItemEditMenu(itemId).open(p);
                }));

        // Comandos
        set(22, item(Material.COMMAND_BLOCK,
                        "&e&lComandos: &f" + item.getCommands().size(),
                        "&7Ejecutados al consumir el item.",
                        "&7@dp = quien usa, @p = objetivo",
                        "", "&a[CLICK gestionar]"),
                e -> new ItemCommandMenu(itemId).open(player));

        // Durabilidad personalizada
        int currentMaxDur = item.getMaxDurability();
        set(23, item(Material.ANVIL,
                        "&d&lDurabilidad Personalizada",
                        "&7Bindea usos custom a la barra vanilla.",
                        "&7Al agotarse el item se destruye.",
                        "",
                        "&7Max actual: &f" + (currentMaxDur > 0 ? currentMaxDur + " usos" : "Sin límite"),
                        "",
                        "&a[CLICK para configurar]",
                        "&7(Escribe 0 para desactivar)"),
                e -> ChatInput.await(player,
                        "Durabilidad máxima (0 = desactivar):",
                        (p, text) -> {
                            try {
                                int max = Integer.parseInt(text.trim());
                                if (max < 0) throw new NumberFormatException();
                                item.setMaxDurability(max > 0 ? max : -1);
                                CustomItemManager.getInstance().update(item);
                                p.sendMessage(CC.translate(max > 0
                                        ? "&aDurabilidad: &f" + max + " usos."
                                        : "&aDurabilidad custom desactivada."));
                            } catch (NumberFormatException ex) {
                                p.sendMessage(CC.translate("&cNúmero inválido."));
                            }
                            new ItemEditMenu(itemId).open(p);
                        }));

        // Dar item
        set(25, item(Material.CHEST,
                        "&a&lDar a mí",
                        "", "&a[CLICK]"),
                e -> {
                    player.getInventory().addItem(CustomItemManager.getInstance().buildItemStack(item));
                    player.sendMessage(CC.translate("&aItem entregado."));
                });

        // ── Fila 4 ─ Estado activo / inactivo ───────────────────────────────

        // Activo (stats de mano aplican o no)
        set(28, item(item.isActive() ? Material.LIME_DYE : Material.GRAY_DYE,
                        "&f&lEstado del item",
                        "&7Cuando está inactivo las stats",
                        "&7de mano NO se aplican.",
                        "",
                        item.isActive()
                                ? "&a✔ ACTIVO &8[CLICK desactivar]"
                                : "&c✘ INACTIVO &8[CLICK activar]"),
                e -> {
                    item.setActive(!item.isActive());
                    CustomItemManager.getInstance().update(item);
                    new ItemEditMenu(itemId).open(player);
                });

        // ── Fila 5 ─ Eliminar ────────────────────────────────────────────────

        set(43, item(Material.TNT,
                        "&c&lEliminar",
                        "&cIrreversible",
                        "", "&c[CLICK]"),
                e -> {
                    CustomItemManager.getInstance().delete(itemId);
                    player.sendMessage(CC.translate("&cItem &f" + itemId + " &celiminado."));
                    new ItemListMenu(1).open(player);
                });

        // ── Fila 6 ─ Navegación ──────────────────────────────────────────────

        set(45, back(), e -> new ItemListMenu(1).open(player));
    }

    /* ── Pastebin lore ── */

    private void pastebin(Player player, CustomItem item) {
        ChatInput.await(player,
                "Pega la URL de Pastebin (ej: pastebin.com/xxxxxxxx):",
                (p, url) -> {
                    p.sendMessage(CC.translate("&7Descargando desde Pastebin..."));

                    Bukkit.getScheduler().runTaskAsynchronously(DbzMain.instance, () -> {
                        List<String> lines = PastebinReader.download(url);

                        Bukkit.getScheduler().runTask(DbzMain.instance, () -> {
                            if (lines == null || lines.isEmpty()) {
                                p.sendMessage(CC.translate("&c✗ No se pudo descargar el contenido."));
                                p.sendMessage(CC.translate("&7Verifica que la URL sea correcta y pública."));
                                new ItemEditMenu(itemId).open(p);
                                return;
                            }

                            List<String> translated = new ArrayList<>();
                            for (String line : lines)
                                translated.add(CC.translate(line));

                            item.setLore(translated);
                            CustomItemManager.getInstance().update(item);

                            p.sendMessage(CC.translate("&a✔ Lore cargado: &f" + translated.size() + " &alíneas."));
                            new ItemEditMenu(itemId).open(p);
                        });
                    });
                });
    }
}