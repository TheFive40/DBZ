package org.delawarex.dbz;

import noppes.npcs.api.event.NpcEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.delawarex.dbz.advancedcrates.managers.CrateManager;
import org.delawarex.dbz.advancedcrates.storage.CrateStorage;
import org.delawarex.dbz.bank.manager.BankConfigManager;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.dbz.customitems.CustomItemsModule;
import org.delawarex.dbz.fragments.FragmentsModule;
import org.delawarex.dbz.placeholder.PlaceHolderModule;
import org.delawarex.dbz.raids.events.NPCDeathListener;
import org.delawarex.dbz.tps.managers.TpManager;
import org.delawarex.service.ClassesRegistration;
import org.delawarex.service.commands.CommandFramework;

import static org.delawarex.service.dbz.General.HOLOGRAMS;

public final class DbzMain extends JavaPlugin {

    private final CommandFramework commandFramework = new CommandFramework(this);
    private final ClassesRegistration classesRegistration = new ClassesRegistration();
    public static DbzMain instance;
    private CrateManager crateManager;
    private static BukkitRunnable scheduler;

    @Override
    public void onEnable() {
        instance = this;
        CrateStorage storage = new CrateStorage();
        BankConfigManager.getInstance();
        BankManager.getInstance();

        saveDefaultConfig();

        classesRegistration.loadCommands("org.delawarex.dbz.tps.commands");
        classesRegistration.loadCommands("org.delawarex.dbz.raids.commands");
        classesRegistration.loadCommands("org.delawarex.dbz.boosters.commands");
        classesRegistration.loadCommands("org.delawarex.dbz.market.commands");
        classesRegistration.loadCommands("org.delawarex.dbz.advancedcrates.commands");
        classesRegistration.loadCommands("org.delawarex.dbz.bank.commands");

        classesRegistration.loadListeners("org.delawarex.dbz.tps.events");
        classesRegistration.loadListeners("org.delawarex.dbz.raids.events");
        classesRegistration.loadListeners("org.delawarex.dbz.boosters.events");
        classesRegistration.loadListeners("org.delawarex.dbz.market.events");
        classesRegistration.loadListeners("org.delawarex.dbz.advancedcrates.events");
        classesRegistration.loadListeners("org.delawarex.dbz.bank.events");

        new TpManager().loadAll();

        CustomItemsModule.enable();
        removeAllHolograms();
        FragmentsModule.enable();
        crateManager = new CrateManager(storage);
        crateManager.loadAll();
        scheduler = new BukkitRunnable() {
            @Override
            public void run() {
                BankManager.getInstance().processScheduledPayments();
            }
        };
        scheduler.runTaskTimer(DbzMain.instance, 20L * 60, 20L * 60);

        PlaceHolderModule.initialize(this);

    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public void callDeath(NpcEvent.DiedEvent event) {
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
        PlaceHolderModule.shutdown();

    }

    public CommandFramework getCommandFramework() {
        return commandFramework;
    }

    public ClassesRegistration getClassesRegistration() {
        return classesRegistration;
    }

    public static org.delawarex.dbz.DbzMain get() {
        return instance;
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
