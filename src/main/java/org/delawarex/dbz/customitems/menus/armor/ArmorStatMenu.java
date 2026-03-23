package org.delawarex.dbz.customitems.menus.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.managers.CustomArmorManager;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.customitems.models.CustomArmor;
import org.delawarex.service.CC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorStatMenu extends Menu {

    public static final List<String> STATS = List.of("STR","SKP","RES","VIT","PWR","ENE");
    public static final List<String> OPS   = List.of("+","-","*");

    private final String armorId;

    public ArmorStatMenu(String armorId) { this.armorId = armorId; }

    @Override protected String getTitle() { return "&a&lStats Armadura - " ; }
    @Override protected int getRows()     { return 4; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        CustomArmor armor = CustomArmorManager.getInstance().get(armorId);
        if (armor == null) return;

        // Existing stats
        int col = 1;
        for (Map.Entry<String, Double> entry : armor.getValueByStat().entrySet()) {
            final String stat = entry.getKey();
            double val  = entry.getValue();
            String op   = armor.getOperation().getOrDefault(stat, "+");

            set(col, item(Material.REDSTONE,
                            "&f" + stat + " " + op + val,
                            "&7Stat: &f" + stat,
                            "&7Op: &f" + op,
                            "&7Valor: &f" + val,
                            "", "&c[CLICK para eliminar]"),
                    e -> {
                        armor.getValueByStat().remove(stat);
                        armor.getOperation().remove(stat);
                        CustomArmorManager.getInstance().update(armor);
                        new ArmorStatMenu(armorId).open(player);
                    });
            if (++col >= 8) break;
        }

        // Add stat
        set(22, item(Material.EMERALD_BLOCK,
                        "&a&lAgregar Stat",
                        "&7Stats disponibles:",
                        "&f  STR &7- Fuerza",
                        "&f  SKP &7- Habilidad",
                        "&f  RES &7- Resistencia",
                        "&f  VIT &7- Vitalidad",
                        "&f  PWR &7- Poder",
                        "&f  ENE &7- Energía",
                        "&7Ops: + - *",
                        "", "&7Formato: &fSTR + 50",
                        "", "&a[CLICK]"),
                e -> askStat(player, armor));

        // Clear all
        set(25, item(Material.TNT, "&cLimpiar todo", "", "&c[CLICK]"),
                e -> {
                    armor.setValueByStat(new HashMap<>());
                    armor.setOperation(new HashMap<>());
                    CustomArmorManager.getInstance().update(armor);
                    new ArmorStatMenu(armorId).open(player);
                });

        set(27, back(), e -> new ArmorEditMenu(armorId).open(player));
    }

    private void askStat(Player player, CustomArmor armor) {
        ChatInput.await(player,
                "Escribe: <stat> <op> <valor>   Ej: STR + 50   |   PWR * 1.1",
                (p, text) -> {
                    String[] parts = text.trim().split("\\s+");
                    if (parts.length < 3) {
                        p.sendMessage(CC.translate("&cFormato inválido. Ej: STR + 50"));
                        new ArmorStatMenu(armorId).open(p);
                        return;
                    }
                    String stat = parts[0].toUpperCase();
                    String op   = parts[1];
                    double val;
                    try { val = Double.parseDouble(parts[2]); }
                    catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cNúmero inválido."));
                        new ArmorStatMenu(armorId).open(p);
                        return;
                    }
                    if (!STATS.contains(stat)) {
                        p.sendMessage(CC.translate("&cStat inválida. Usa: " + String.join(", ", STATS)));
                        new ArmorStatMenu(armorId).open(p);
                        return;
                    }
                    if (!OPS.contains(op)) {
                        p.sendMessage(CC.translate("&cOperación inválida. Usa: + - *"));
                        new ArmorStatMenu(armorId).open(p);
                        return;
                    }
                    armor.setStat(stat, op, val);
                    CustomArmorManager.getInstance().update(armor);
                    p.sendMessage(CC.translate("&aStat agregada: &f" + stat + " " + op + val));
                    new ArmorStatMenu(armorId).open(p);
                });
    }
}