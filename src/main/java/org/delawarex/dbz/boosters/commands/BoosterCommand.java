package org.delawarex.dbz.boosters.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.delawarex.dbz.boosters.core.BoosterParser;
import org.delawarex.dbz.boosters.core.BoosterSettings;
import org.delawarex.dbz.boosters.managers.GlobalBoosterManager;
import org.delawarex.dbz.boosters.managers.PersonalBoosterManager;
import org.delawarex.dbz.boosters.models.GlobalBooster;
import org.delawarex.dbz.boosters.models.PersonalBooster;
import org.delawarex.dbz.boosters.storage.BoosterStorage;
import org.delawarex.service.CC;
import org.delawarex.service.commands.BaseCommand;
import org.delawarex.service.commands.Command;
import org.delawarex.service.commands.CommandArgs;

import java.io.IOException;
import java.util.List;

public class BoosterCommand extends BaseCommand {

    @Command(name = "booster", aliases = {"boosters", "boost"}, permission = "dbz.admin.booster")
    @Override
    public void onCommand(CommandArgs command) throws IOException {
        Player player = command.getPlayer();

        if (command.length() < 1) {
            sendHelp(player);
            return;
        }

        switch (command.getArgs(0).toLowerCase()) {
            case "global" -> handleGlobalCommand(command);
            case "personal" -> handlePersonalCommand(command);
            case "info" -> handleInfoCommand(command);
            case "save" -> {
                BoosterStorage.saveAllData();
                command.getSender().sendMessage(CC.translate("&aDatos guardados exitosamente"));
            }
            case "load" -> {
                BoosterStorage.loadAllData();
                command.getSender().sendMessage(CC.translate("&aDatos cargados exitosamente"));
            }
            case "help" -> sendHelp(player);
            default -> {
                command.getSender().sendMessage(CC.translate("&cSubcomando desconocido: " + command.getArgs(0)));
                sendHelp(player);
            }
        }
    }

    private void handleGlobalCommand(CommandArgs command) {
        if (command.length() < 2) {
            command.getSender().sendMessage(CC.translate("&cUso: /booster global <activate|deactivate|info>"));
            return;
        }

        switch (command.getArgs(1).toLowerCase()) {
            case "activate" -> {
                if (command.length() < 3) {
                    command.getSender().sendMessage(CC.translate("&cUso: /booster global activate <porcentaje> [tiempo]"));
                    command.getSender().sendMessage(CC.translate("&7Ejemplo: /booster global activate 50% 1h"));
                    return;
                }
                try {
                    double multiplier = BoosterParser.parsePercentageToMultiplier(command.getArgs(2));
                    long duration = BoosterSettings.getGlobalBoosterDuration();
                    if (command.length() >= 4) duration = BoosterParser.parseTimeToSeconds(command.getArgs(3));

                    GlobalBoosterManager.activateBooster(multiplier, command.getSender().getName(), duration);

                    String percentDisplay = BoosterParser.formatMultiplierAsPercentage(multiplier);
                    String timeDisplay = BoosterParser.formatSecondsToTime(duration);

                    command.getSender().sendMessage(CC.translate("&aBooster global activado:"));
                    command.getSender().sendMessage(CC.translate("  &6Bonus: &a+" + percentDisplay));
                    command.getSender().sendMessage(CC.translate("  &6Multiplicador: &ax" + String.format("%.2f", multiplier)));
                    command.getSender().sendMessage(CC.translate("  &6Duración: &a" + timeDisplay));

                    Bukkit.broadcastMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    Bukkit.broadcastMessage(CC.translate("&6&l⚡ BOOSTER GLOBAL ACTIVADO ⚡"));
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(CC.translate("  &eBono: &a+" + percentDisplay));
                    Bukkit.broadcastMessage(CC.translate("  &eDuración: &f" + timeDisplay));
                    Bukkit.broadcastMessage(CC.translate("  &eActivado por: &6" + command.getSender().getName()));
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                } catch (IllegalArgumentException e) {
                    command.getSender().sendMessage(CC.translate("&c✗ Error: " + e.getMessage()));
                }
            }
            case "deactivate" -> {
                if (GlobalBoosterManager.isBoosterActive()) {
                    GlobalBoosterManager.deactivateBooster();
                    command.getSender().sendMessage(CC.translate("&aBooster global desactivado"));
                    Bukkit.broadcastMessage(CC.translate("&e[BOOSTER] &cBooster Global Desactivado"));
                } else {
                    command.getSender().sendMessage(CC.translate("&cNo hay booster global activo"));
                }
            }
            case "info", "multiplier" -> {
                GlobalBooster booster = GlobalBoosterManager.getActiveBooster();
                if (booster != null) {
                    String percentDisplay = BoosterParser.formatMultiplierAsPercentage(booster.getMultiplier());
                    command.getSender().sendMessage(CC.translate("&6&l&m━━━━━━━━━━━━━━ Booster Global &6&l&m━━━━━━━━━━━━━━"));
                    command.getSender().sendMessage(CC.translate("&aBono: &6+" + percentDisplay));
                    command.getSender().sendMessage(CC.translate("&aMultiplicador: &6x" + String.format("%.2f", booster.getMultiplier())));
                    command.getSender().sendMessage(CC.translate("&aTiempo restante: &6" + booster.getFormattedTime()));
                    command.getSender().sendMessage(CC.translate("&aActivado por: &6" + booster.getActivatedBy()));
                } else {
                    command.getSender().sendMessage(CC.translate("&cNo hay booster global activo"));
                }
            }
            default -> command.getSender().sendMessage(CC.translate("&cAcción desconocida: " + command.getArgs(1)));
        }
    }

    private void handlePersonalCommand(CommandArgs command) {
        if (command.length() < 2) {
            command.getSender().sendMessage(CC.translate("&cUso: /booster personal <add|activate|list>"));
            return;
        }

        switch (command.getArgs(1).toLowerCase()) {
            case "add" -> {
                if (command.length() < 4) {
                    command.getSender().sendMessage(CC.translate("&cUso: /booster personal add <jugador> <nivel>"));
                    return;
                }
                Player target = Bukkit.getPlayer(command.getArgs(2));
                if (target == null) {
                    command.getSender().sendMessage(CC.translate("&cJugador no encontrado: " + command.getArgs(2)));
                    return;
                }
                try {
                    int level = Integer.parseInt(command.getArgs(3));
                    if (level < 1 || level > 5) {
                        command.getSender().sendMessage(CC.translate("&cNivel debe ser entre 1 y 5"));
                        return;
                    }
                    double mult = BoosterSettings.getPersonalBoosterMultiplier(level);
                    PersonalBooster booster = new PersonalBooster(target.getUniqueId(), level, mult);
                    PersonalBoosterManager.addBooster(booster);

                    String percentDisplay = BoosterParser.formatMultiplierAsPercentage(mult);
                    command.getSender().sendMessage(CC.translate("&aBooster nivel &6" + level + " &aañadido a &6" + target.getName()));
                    command.getSender().sendMessage(CC.translate("  &7Bonus: &a+" + percentDisplay));

                    target.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    target.sendMessage(CC.translate("&6&l⚡ BOOSTER RECIBIDO ⚡"));
                    target.sendMessage("");
                    target.sendMessage(CC.translate("  &eNivel: &6" + level));
                    target.sendMessage(CC.translate("  &eBono: &a+" + percentDisplay));
                    target.sendMessage("");
                    target.sendMessage(CC.translate("&7Usa &e/booster personal list &7para ver tus boosters"));
                    target.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                } catch (NumberFormatException e) {
                    command.getSender().sendMessage(CC.translate("&cNivel inválido"));
                }
            }
            case "activate" -> {
                if (!command.isPlayer()) {
                    command.getSender().sendMessage(CC.translate("&cDebes ser un jugador"));
                    return;
                }
                Player player = command.getPlayer();
                List<PersonalBooster> boosters = PersonalBoosterManager.getPlayerBoosters(player.getUniqueId());
                if (boosters.isEmpty()) {
                    player.sendMessage(CC.translate("&cNo tienes boosters personales"));
                    return;
                }
                if (command.length() < 3) {
                    player.sendMessage(CC.translate("&cUso: /booster personal activate <índice>"));
                    listPlayerBoosters(player, boosters);
                    return;
                }
                try {
                    int index = Integer.parseInt(command.getArgs(2));
                    if (index < 0 || index >= boosters.size()) {
                        player.sendMessage(CC.translate("&cÍndice inválido: 0-" + (boosters.size() - 1)));
                        listPlayerBoosters(player, boosters);
                        return;
                    }
                    PersonalBooster boosterToActivate = boosters.get(index);
                    PersonalBoosterManager.activateBooster(player.getUniqueId(), index);

                    String percentDisplay = BoosterParser.formatMultiplierAsPercentage(boosterToActivate.getMultiplier());
                    String timeDisplay = BoosterParser.formatSecondsToTime(BoosterSettings.getPersonalBoosterDuration());

                    player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                    player.sendMessage(CC.translate("&b&l⚡ BOOSTER ACTIVADO ⚡"));
                    player.sendMessage("");
                    player.sendMessage(CC.translate("  &eNivel: &6" + boosterToActivate.getLevelName()));
                    player.sendMessage(CC.translate("  &eBono: &a+" + percentDisplay));
                    player.sendMessage(CC.translate("  &eDuración: &f" + timeDisplay));
                    player.sendMessage("");
                    player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                } catch (NumberFormatException e) {
                    command.getPlayer().sendMessage(CC.translate("&cÍndice inválido"));
                }
            }
            case "list" -> {
                if (!command.isPlayer()) {
                    command.getSender().sendMessage(CC.translate("&cDebes ser un jugador"));
                    return;
                }
                Player player = command.getPlayer();
                List<PersonalBooster> boosters = PersonalBoosterManager.getPlayerBoosters(player.getUniqueId());
                if (boosters.isEmpty()) {
                    player.sendMessage(CC.translate("&cNo tienes boosters personales"));
                    return;
                }
                listPlayerBoosters(player, boosters);
            }
            default -> command.getSender().sendMessage(CC.translate("&cAcción desconocida: " + command.getArgs(1)));
        }
    }

    private void handleInfoCommand(CommandArgs command) {
        command.getSender().sendMessage(CC.translate("&6=== Información de Boosters ==="));
        command.getSender().sendMessage("");
        command.getSender().sendMessage(CC.translate("&aBooster Global:"));
        GlobalBooster global = GlobalBoosterManager.getActiveBooster();
        if (global != null) {
            String percentDisplay = BoosterParser.formatMultiplierAsPercentage(global.getMultiplier());
            command.getSender().sendMessage(CC.translate("  &e- Bono: &a+" + percentDisplay));
            command.getSender().sendMessage(CC.translate("  &e- Multiplicador: &ax" + String.format("%.2f", global.getMultiplier())));
            command.getSender().sendMessage(CC.translate("  &e- Tiempo: &6" + global.getFormattedTime()));
            command.getSender().sendMessage(CC.translate("  &e- Activado por: &6" + global.getActivatedBy()));
        } else {
            command.getSender().sendMessage(CC.translate("  &cInactivo"));
        }
        command.getSender().sendMessage("");
        if (command.isPlayer()) {
            Player player = command.getPlayer();
            command.getSender().sendMessage(CC.translate("&aTu Booster Personal:"));
            PersonalBooster personal = PersonalBoosterManager.getActiveBooster(player.getUniqueId());
            if (personal != null) {
                String percentDisplay = BoosterParser.formatMultiplierAsPercentage(personal.getMultiplier());
                command.getSender().sendMessage(CC.translate("  &e- Nivel: &6" + personal.getLevelName()));
                command.getSender().sendMessage(CC.translate("  &e- Bono: &a+" + percentDisplay));
                command.getSender().sendMessage(CC.translate("  &e- Multiplicador: &ax" + String.format("%.2f", personal.getMultiplier())));
            } else {
                command.getSender().sendMessage(CC.translate("  &cInactivo"));
            }
        }
        command.getSender().sendMessage("");
    }

    private void listPlayerBoosters(Player player, List<PersonalBooster> boosters) {
        player.sendMessage(CC.translate("&6=== Tus Boosters Personales ==="));
        for (int i = 0; i < boosters.size(); i++) {
            PersonalBooster booster = boosters.get(i);
            String status = booster.isActive() ? "&aActivo" : "&7Disponible";
            String percentDisplay = BoosterParser.formatMultiplierAsPercentage(booster.getMultiplier());
            player.sendMessage(CC.translate(String.format("  &6[%d] &e%s &7- &a+%s &7(%s)",
                    i, booster.getLevelName(), percentDisplay, status)));
        }
        player.sendMessage("");
        player.sendMessage(CC.translate("&7Usa &e/booster personal activate <índice> &7para activar"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(CC.translate("&7&m----------------------------------------"));
        player.sendMessage(CC.translate("&6&lBoosters"));
        player.sendMessage(CC.translate(""));

        player.sendMessage(CC.translate("&6Global"));
        player.sendMessage(CC.translate(" &7/booster global activate <porcentaje> [tiempo]"));
        player.sendMessage(CC.translate(" &7/booster global deactivate"));
        player.sendMessage(CC.translate(" &7/booster global info"));

        player.sendMessage(CC.translate(""));

        player.sendMessage(CC.translate("&6Personal"));
        player.sendMessage(CC.translate(" &7/booster personal add <jugador> <nivel>"));
        player.sendMessage(CC.translate(" &7/booster personal activate <indice>"));
        player.sendMessage(CC.translate(" &7/booster personal list"));

        player.sendMessage(CC.translate(""));

        player.sendMessage(CC.translate("&6Otros"));
        player.sendMessage(CC.translate(" &7/booster info"));
        player.sendMessage(CC.translate(" &7/booster save &8| &7/booster load"));

        player.sendMessage(CC.translate(""));

        player.sendMessage(CC.translate("&6Tiempo"));
        player.sendMessage(CC.translate(" &f1d 2h 30m 45s"));

        player.sendMessage(CC.translate("&7&m----------------------------------------"));
    }
}
