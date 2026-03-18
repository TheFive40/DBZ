package org.delawarex.dbz.tps.managers;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TpManager {
    private static final ConcurrentHashMap<Integer, ItemStack> tps = new ConcurrentHashMap<>();

    // Patrón que coincide con "+5 Tps", "+100 Tps", etc. (ignora códigos de color §x)
    private static final Pattern TP_PATTERN = Pattern.compile("\\+(\\d+)\\s+Tps");

    public ItemStack give(int amount) {
        return tps.get(amount);
    }

    public boolean add(int amount, ItemStack item) {
        tps.put(amount, item);
        SerializeTpsManager.saveItem(amount, item);
        return true;
    }

    public boolean remove(int amount) {
        if (tps.remove(amount) != null) {
            SerializeTpsManager.removeItem(amount);
            return true;
        }
        return false;
    }

    /**
     * Verifica si el ItemStack es un TP registrado
     * comparando su lore con el patrón "+X Tps"
     */
    public boolean isTp(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;

        return meta.getLore().stream()
                .anyMatch(line -> TP_PATTERN.matcher(stripColor(line)).find());
    }

    /**
     * Devuelve las claves ordenadas de mayor a menor
     */
    public List<Integer> getKeys() {
        return tps.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .collect(java.util.stream.Collectors.toList());
    }
    /**
     * Obtiene el valor de TPs del lore del item.
     * Devuelve -1 si no es un TP válido.
     */
    public int getValue(ItemStack item) {
        if (!isTp(item)) return -1;

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            Matcher matcher = TP_PATTERN.matcher(stripColor(line));
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return -1;
    }

    /**
     * Carga todos los items del config.
     * Extrae el valor clave del lore ("+X Tps") y lo guarda en el mapa.
     */
    public void loadAll() {
        ConcurrentHashMap<Integer, ItemStack> loaded = SerializeTpsManager.loadAll();
        loaded.forEach((key, item) -> {
            // Verificar que el lore tenga el patrón correcto
            int valueFromLore = new TpManager().getValue(item);
            if (valueFromLore > 0) {
                tps.put(valueFromLore, item);
            } else {
                // Fallback: usar la clave guardada en config
                tps.put(key, item);
            }
        });
    }

    public void saveAll() {
        tps.forEach(SerializeTpsManager::saveItem);
    }

    // Elimina códigos de color Bukkit (§x) para parsear texto limpio
    private String stripColor(String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}