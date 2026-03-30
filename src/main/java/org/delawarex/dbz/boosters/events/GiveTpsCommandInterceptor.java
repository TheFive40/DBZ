package org.delawarex.dbz.boosters.events;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.boosters.managers.GlobalBoosterManager;
import org.delawarex.dbz.boosters.managers.PersonalBoosterManager;
import org.delawarex.service.CC;

import static org.delawarex.service.dbz.General.sendMessageBooster;

public class GiveTpsCommandInterceptor implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!message.toLowerCase().startsWith("/addtp")) return;

        String[] args = message.split(" ");
        if (args.length < 3) {
            event.getPlayer().sendMessage(CC.translate("&cUso: /addtp <jugador> <cantidad>"));
            event.setCancelled(true);
            return;
        }

        try {
            String targetName = args[1];
            int baseTPs = Integer.parseInt(args[2]);
            Player target = DbzMain.instance.getServer().getPlayer(targetName);
            if (target == null) {
                event.getPlayer().sendMessage(CC.translate("&cJugador no encontrado: " + targetName));
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            applyBoosterAndGiveTPs(event.getPlayer(), target, baseTPs);
        } catch (NumberFormatException e) {
            event.getPlayer().sendMessage(CC.translate("&cCantidad inválida de TPs"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerCommand(ServerCommandEvent event) {
        String message = event.getCommand();
        if (!message.toLowerCase().startsWith("addtp")) return;

        String[] args = message.split(" ");
        if (args.length < 3) return;

        try {
            String targetName = args[1];
            int baseTPs = Integer.parseInt(args[2]);
            Player target = DbzMain.instance.getServer().getPlayer(targetName);
            if (target == null) return;
            event.setCancelled(true);
            applyBoosterAndGiveTPs(event.getSender(), target, baseTPs);
        } catch (NumberFormatException ignored) {
        }
    }

    public static void applyBoosterAndGiveTPs(CommandSender sender, Player target, int baseTPs) {
        double globalMultiplier = GlobalBoosterManager.getCurrentMultiplier();
        double personalMultiplier = PersonalBoosterManager.getActiveMultiplier(target.getUniqueId());
        double combinedMultiplier = globalMultiplier * personalMultiplier;

        int totalTPs = (int) Math.round(baseTPs * combinedMultiplier);
        int bonusTPs = totalTPs - baseTPs;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "addtp " + target.getName() + " " + totalTPs);

        sendSuccessMessage(sender, target, baseTPs, bonusTPs, totalTPs, globalMultiplier, personalMultiplier, combinedMultiplier);
    }

    private static void sendSuccessMessage(CommandSender sender, Player target, int baseTPs, int bonusTPs, int totalTPs,
                                           double globalMult, double personalMult, double combinedMult) {
        boolean isConsole = sender instanceof ConsoleCommandSender;
        String senderName = isConsole ? "Consola" : sender.getName();

        if (bonusTPs > 0) {
            sender.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            sender.sendMessage(CC.translate("&a✓ TPs Entregados a &6" + target.getName()));
            sender.sendMessage("");
            sender.sendMessage(CC.translate("  &eTPs Base: &a+" + baseTPs));
            sender.sendMessage(CC.translate("  &eBonus de Booster: &6+" + bonusTPs));
            sender.sendMessage(CC.translate("  &eTotal Entregado: &b+" + totalTPs + " TPs"));
            sender.sendMessage("");
            if (globalMult > 1.0) {
                String globalPercent = String.format("%.0f%%", (globalMult - 1.0) * 100);
                sender.sendMessage(CC.translate("  &6⚡ Booster Global: &a+" + globalPercent));
            }
            if (personalMult > 1.0) {
                String personalPercent = String.format("%.0f%%", (personalMult - 1.0) * 100);
                sender.sendMessage(CC.translate("  &b⚡ Booster Personal: &a+" + personalPercent));
            }
            String totalPercent = String.format("%.0f%%", (combinedMult - 1.0) * 100);
            sender.sendMessage(CC.translate("  &a⚡ Multiplicador Total: &6x" + String.format("%.2f", combinedMult) + " &7(+" + totalPercent + ")"));
            sender.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            sendMessageBooster(bonusTPs, totalTPs, baseTPs, globalMult, personalMult, senderName, target);

        }
    }
}
