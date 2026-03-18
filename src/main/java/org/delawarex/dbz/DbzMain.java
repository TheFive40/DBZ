package org.delawarex.dbz;

import org.bukkit.plugin.java.JavaPlugin;
import org.delawarex.dbz.tps.managers.SerializeTpsManager;
import org.delawarex.dbz.tps.managers.TpManager;
import org.delawarex.service.ClassesRegistration;
import org.delawarex.service.commands.CommandFramework;

public final class DbzMain extends JavaPlugin {
    private final CommandFramework commandFramework = new CommandFramework(this);

    private final ClassesRegistration classesRegistration = new ClassesRegistration();
    public static DbzMain instance;

    @Override
    public void onEnable() {
        instance = this;
        classesRegistration.loadCommands("org.delawarex.dbz.tps.commands");
        classesRegistration.loadListeners("org.delawarex.dbz.tps.events");
        SerializeTpsManager.loadAll();
    }

    @Override
    public void onDisable() {
        TpManager manager = new TpManager();
        manager.saveAll();
    }

    public CommandFramework getCommandFramework() {
        return commandFramework;
    }

    public ClassesRegistration getClassesRegistration() {
        return classesRegistration;
    }
}
