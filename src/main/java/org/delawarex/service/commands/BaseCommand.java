package org.delawarex.service.commands;
import org.delawarex.dbz.DbzMain;

import java.io.IOException;

public abstract class BaseCommand {

    public DbzMain main = DbzMain.instance;

    public BaseCommand() {
        main.getCommandFramework().registerCommands(this);
    }

    public abstract void onCommand(CommandArgs command) throws IOException;

}
