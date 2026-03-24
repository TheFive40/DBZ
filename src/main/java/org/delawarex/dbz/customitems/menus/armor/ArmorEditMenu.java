package org.delawarex.dbz.customitems.menus.armor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.managers.CustomArmorManager;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.customitems.models.CustomArmor;
import org.delawarex.dbz.customitems.utils.PastebinReader;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class ArmorEditMenu extends Menu {

    private final String armorId;

    public ArmorEditMenu(String armorId) { this.armorId = armorId; }

    @Override protected String getTitle() { return "&b&lEditar Armadura: &f"; }
    @Override protected int getRows()     { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        CustomArmor armor = CustomArmorManager.getInstance().get(armorId);
        if (armor == null) { player.closeInventory(); return; }
        set(4, item(Material.PAPER,
                "&7ID: &f" + armorId,
                "&7Material: &f" + armor.getMaterial(),
                "&7Nombre: &f" + armor.getDisplayName(),
                "&7Lore: &f" + (armor.getLore() != null ? armor.getLore().size() : 0) + " líneas",
                "&7Stats: &f" + armor.getValueByStat().size(),
                "&7Efectos: &f" + armor.getEffects().size(),
                "&7Durabilidad: &f" + (armor.getMaxDurability() > 0 ? armor.getMaxDurability() : "Sin límite")));


        set(10, item(Material.NAME_TAG,
                        "&eRenombrar",
                        "&7Actual: &f" + armor.getDisplayName(),
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Nuevo nombre (soporta &colores):", (p, text) -> {
                    armor.setDisplayName(CC.translate(text));
                    CustomArmorManager.getInstance().update(armor);
                    p.sendMessage(CC.translate("&aNombre actualizado."));
                    new ArmorEditMenu(armorId).open(p);
                }));

        set(11, item(Material.BOOK,
                        "&dLore desde Pastebin",
                        "&7Líneas actuales: &f" + (armor.getLore() != null ? armor.getLore().size() : 0),
                        "",
                        "&7Pega la URL de Pastebin",
                        "&7Ej: pastebin.com/xxxxxxxx",
                        "&7Soporta códigos &ccol&a o r e s",
                        "", "&a[CLICK]"),
                e -> pastebin(player, armor));

        set(12, item(Material.WRITABLE_BOOK,
                        "&cLimpiar Lore",
                        "&7Elimina todas las líneas de lore",
                        "", "&c[CLICK]"),
                e -> {
                    armor.setLore(new ArrayList<>());
                    CustomArmorManager.getInstance().update(armor);
                    player.sendMessage(CC.translate("&aLore limpiado."));
                    new ArmorEditMenu(armorId).open(player);
                });

        List<String> statLines = new ArrayList<>();
        if (armor.getValueByStat().isEmpty()) {
            statLines.add("&7Sin estadísticas");
        } else {
            armor.getValueByStat().forEach((s, v) ->
                    statLines.add("&7" + s + ": &f" + armor.getOperation().getOrDefault(s, "+") + v));
        }
        statLines.add(""); statLines.add("&a[CLICK para gestionar]");
        set(13, item(Material.REDSTONE, "&aEstadísticas", statLines.toArray(new String[0])),
                e -> new ArmorStatMenu(armorId).open(player));

        // Efectos
        List<String> effLines = new ArrayList<>();
        if (armor.getEffects().isEmpty()) {
            effLines.add("&7Sin efectos");
        } else {
            armor.getEffects().forEach((eff, val) ->
                    effLines.add("&7" + eff + ": &f" + (val * 100) + "%"));
        }
        effLines.add(""); effLines.add("&a[CLICK para gestionar]");
        set(14, item(Material.BLAZE_POWDER, "&6Efectos", effLines.toArray(new String[0])),
                e -> new ArmorEffectMenu(armorId).open(player));


        set(19, item(Material.BEDROCK,
                        "&b&lIrrompible",
                        "",
                        armor.isUnbreakable()
                                ? "&a✔ ACTIVADO &8[CLICK desactivar]"
                                : "&c✘ DESACTIVADO &8[CLICK activar]"),
                e -> {
                    armor.setUnbreakable(!armor.isUnbreakable());
                    CustomArmorManager.getInstance().update(armor);
                    new ArmorEditMenu(armorId).open(player);
                });

        int currentMaxDur = armor.getMaxDurability();
        set(20, item(Material.ANVIL,
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
                                armor.setMaxDurability(max > 0 ? max : -1);
                                CustomArmorManager.getInstance().update(armor);
                                p.sendMessage(CC.translate(max > 0
                                        ? "&aDurabilidad: &f" + max + " usos."
                                        : "&aDurabilidad custom desactivada."));
                            } catch (NumberFormatException ex) {
                                p.sendMessage(CC.translate("&cNúmero inválido."));
                            }
                            new ArmorEditMenu(armorId).open(p);
                        }));

        set(24, item(Material.CHEST,
                        "&a&lDar a mí",
                        "", "&a[CLICK]"),
                e -> {
                    player.getInventory().addItem(CustomArmorManager.getInstance().buildItemStack(armor));
                    player.sendMessage(CC.translate("&aArmadura entregada."));
                });


        set(34, item(Material.TNT,
                        "&c&lEliminar",
                        "&cIrreversible",
                        "", "&c[CLICK]"),
                e -> {
                    CustomArmorManager.getInstance().delete(armorId);
                    player.sendMessage(CC.translate("&cArmadura &f" + armorId + " &celiminada."));
                    new ArmorListMenu(1).open(player);
                });


        set(36, back(), e -> new ArmorListMenu(1).open(player));
    }

    private void pastebin(Player player, CustomArmor armor) {
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
                                new ArmorEditMenu(armorId).open(p);
                                return;
                            }

                            List<String> translated = new ArrayList<>();
                            for (String line : lines)
                                translated.add(CC.translate(line));

                            armor.setLore(translated);
                            CustomArmorManager.getInstance().update(armor);

                            p.sendMessage(CC.translate("&a✔ Lore cargado: &f" + translated.size() + " &alíneas."));
                            new ArmorEditMenu(armorId).open(p);
                        });
                    });
                });
    }
}