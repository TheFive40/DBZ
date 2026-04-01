package org.delawarex.dbz.advancedcrates.storage;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class ItemSerializer {

    public static String serialize(ItemStack item) {
        if (item == null) return null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (BukkitObjectOutputStream data = new BukkitObjectOutputStream(out)) {
                data.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack deserialize(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            try (BukkitObjectInputStream data = new BukkitObjectInputStream(in)) {
                return (ItemStack) data.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
