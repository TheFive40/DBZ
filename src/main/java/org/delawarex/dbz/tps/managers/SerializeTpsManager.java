package org.delawarex.dbz.tps.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.delawarex.dbz.DbzMain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class SerializeTpsManager {

    private static final String CONFIG_PATH = "tps";

    public static void saveItem(int key, ItemStack item) {
        try {
            String serialized = itemToBase64(item);
            DbzMain.instance.getConfig().set(CONFIG_PATH + "." + key, serialized);
            DbzMain.instance.saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeItem(int key) {
        DbzMain.instance.getConfig().set(CONFIG_PATH + "." + key, null);
        DbzMain.instance.saveConfig();
    }

    public static ConcurrentHashMap<Integer, ItemStack> loadAll() {
        ConcurrentHashMap<Integer, ItemStack> map = new ConcurrentHashMap<>();
        FileConfiguration config = DbzMain.instance.getConfig();

        ConfigurationSection section = config.getConfigurationSection(CONFIG_PATH);
        if (section == null) return map;

        for (String key : section.getKeys(false)) {
            try {
                String serialized = section.getString(key);
                if (serialized == null) continue;

                ItemStack item = itemFromBase64(serialized);
                int amount = Integer.parseInt(key);
                map.put(amount, item);

            } catch (Exception e) {
                DbzMain.instance.getLogger().warning("Error cargando TP con clave: " + key);
                e.printStackTrace();
            }
        }
        return map;
    }

    public static String itemToBase64(ItemStack item) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(item);
        }
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public static ItemStack itemFromBase64(String base64) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
        try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        }
    }
}