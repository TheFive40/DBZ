package org.delawarex.dbz.customitems.menus.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.managers.CustomItemManager;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.customitems.models.CustomItem;
import org.delawarex.service.CC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemStatMenu extends Menu {

    public static final List<String> STATS = List.of("STR","SKP","RES","VIT","PWR","ENE");
    public static final List<String> OPS   = List.of("+","-","*");

    private final String itemId;

    public ItemStatMenu(String itemId) { this.itemId = itemId; }

    @Override protected String getTitle() { return "&a&lStats - "; }
    @Override protected int getRows()     { return 4; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        CustomItem item = CustomItemManager.getInstance().get(itemId);
        if (item == null) return;

        // Show existing stats (row 1, cols 1-7)
        int col = 1;
        for (Map.Entry<String, Double> entry : item.getValueByStat().entrySet()) {
            final String stat = entry.getKey();
            double val  = entry.getValue();
            String op   = item.getOperation().getOrDefault(stat, "+");

            set(col, item(Material.REDSTONE,
                            "&f" + stat + " " + op + val,
                            "&7Stat: &f" + stat,
                            "&7Op: &f" + op,
                            "&7Valor: &f" + val,
                            "", "&c[CLICK para eliminar]"),
                    e -> {
                        item.getValueByStat().remove(stat);
                        item.getOperation().remove(stat);
                        CustomItemManager.getInstance().update(item);
                        new ItemStatMenu(itemId).open(player);
                    });
            if (++col >= 8) break;
        }

        // Add stat button
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
                e -> askStat(player, item));

        // Clear all
        set(25, item(Material.TNT, "&cLimpiar todo", "", "&c[CLICK]"),
                e -> {
                    item.setValueByStat(new HashMap<>());
                    item.setOperation(new HashMap<>());
                    CustomItemManager.getInstance().update(item);
                    new ItemStatMenu(itemId).open(player);
                });

        set(27, back(), e -> new ItemEditMenu(itemId).open(player));
    }

    private void askStat(Player player, CustomItem item) {
        ChatInput.await(player,
                "Escribe: <stat> <op> <valor>   Ej: STR + 50   |   PWR * 1.1",
                (p, text) -> {
                    String[] parts = text.trim().split("\\s+");
                    if (parts.length < 3) {
                        p.sendMessage(CC.translate("&cFormato inválido. Ej: STR + 50"));
                        new ItemStatMenu(itemId).open(p);
                        return;
                    }
                    String stat = parts[0].toUpperCase();
                    String op   = parts[1];
                    double val;
                    try { val = Double.parseDouble(parts[2]); }
                    catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cNúmero inválido."));
                        new ItemStatMenu(itemId).open(p);
                        return;
                    }
                    if (!STATS.contains(stat)) {
                        p.sendMessage(CC.translate("&cStat inválida. Usa: " + String.join(", ", STATS)));
                        new ItemStatMenu(itemId).open(p);
                        return;
                    }
                    if (!OPS.contains(op)) {
                        p.sendMessage(CC.translate("&cOperación inválida. Usa: + - *"));
                        new ItemStatMenu(itemId).open(p);
                        return;
                    }
                    item.setStat(stat, op, val);
                    CustomItemManager.getInstance().update(item);
                    p.sendMessage(CC.translate("&aStat agregada: &f" + stat + " " + op + val));
                    new ItemStatMenu(itemId).open(p);
                });
    }
}