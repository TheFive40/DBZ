package org.delawarex.dbz;

import noppes.npcs.api.event.NpcEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.delawarex.dbz.customitems.CustomItemsModule;
import org.delawarex.dbz.fragments.FragmentsModule;
import org.delawarex.dbz.raids.events.NPCDeathListener;
import org.delawarex.dbz.tps.managers.TpManager;
import org.delawarex.service.ClassesRegistration;
import org.delawarex.service.commands.CommandFramework;

import static org.delawarex.service.dbz.General.HOLOGRAMS;

public final class DbzMain extends JavaPlugin {

    private final CommandFramework commandFramework = new CommandFramework(this);
    private final ClassesRegistration classesRegistration = new ClassesRegistration();
    public static DbzMain instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        classesRegistration.loadCommands("org.delawarex.dbz.tps.commands");
        classesRegistration.loadCommands("org.delawarex.dbz.raids.commands");

        classesRegistration.loadListeners("org.delawarex.dbz.tps.events");
        classesRegistration.loadListeners("org.delawarex.dbz.raids.events");

        new TpManager().loadAll();

        CustomItemsModule.enable();
        removeAllHolograms();
        FragmentsModule.enable();
    }

    public static void callDeath(NpcEvent.DiedEvent event) {
        NPCDeathListener deathListener = new NPCDeathListener();
        deathListener.onNpcDie(event);
    }

    @Override
    public void onDisable() {
        for (ArmorStand holo : HOLOGRAMS) {
            if (holo != null && !holo.isDead()) {
                holo.remove();
            }
        }
        HOLOGRAMS.clear();
        FragmentsModule.disable();
    }

    public CommandFramework getCommandFramework() {
        return commandFramework;
    }

    public ClassesRegistration getClassesRegistration() {
        return classesRegistration;
    }

    public void removeAllHolograms() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ArmorStand) {
                    ArmorStand as = (ArmorStand) entity;

                    if (as.isMarker() && as.getCustomName() != null) {
                        as.remove();
                    }
                }
            }
        }
    }
}