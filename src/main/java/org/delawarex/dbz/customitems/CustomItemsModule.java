package org.delawarex.dbz.customitems;

import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.events.MenuListener;
import org.delawarex.dbz.customitems.managers.ArmorBonusManager;
import org.delawarex.dbz.customitems.managers.CustomArmorManager;
import org.delawarex.dbz.customitems.managers.CustomItemManager;


/**
 * Bootstrap del sub-sistema CustomItems.
 *
 * Llama a enable() desde DbzMain.onEnable()  (después de setear instance)
 * Llama a disable() desde DbzMain.onDisable()
 */
public class CustomItemsModule {

    public static void enable() {
        DbzMain plugin = DbzMain.instance;

        CustomItemManager.getInstance();
        CustomArmorManager.getInstance();

        plugin.getClassesRegistration().loadCommands("org.delawarex.dbz.customitems.commands");
        plugin.getClassesRegistration().loadListeners("org.delawarex.dbz.customitems.events");

        ArmorBonusManager.start();

        plugin.getLogger().info("[CustomItems] Módulo cargado correctamente.");
    }

    public static void disable() {
        ArmorBonusManager.stop();
        DbzMain.instance.getLogger().info("[CustomItems] Módulo detenido.");
    }
}