package org.delawarex.service.dbz;

import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.network.S2C.StatsSyncS2C;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.service.CC;

public class General {
    public static void addBonus(Player player, String stat, String bonusName, String operation, double value) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {
            stats.getBonusStats().addBonus(stat, bonusName, operation, value);
        });
    }

    public static void removeBonus(Player player, String stat, String bonusName) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {
            stats.getBonusStats().removeBonus(stat, bonusName);
        });
    }

    public static void addEnergyEffect(Player player, String operation, double value) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {

            if (operation.equals("*")) {
                int current = stats.getResources().getCurrentEnergy();
                int max = stats.getMaxEnergy();
                int newEnergy = (int) Math.min(max, current + (current * (value - 1)));
                stats.getResources().setCurrentEnergy(newEnergy);
            } else if (operation.equals("+")) {
                int current = stats.getResources().getCurrentEnergy();
                int max = stats.getMaxEnergy();
                int newEnergy = (int) Math.min(max, current + value);
                stats.getResources().setCurrentEnergy(newEnergy);
            } else if (operation.equals("-")) {
                int current = stats.getResources().getCurrentEnergy();
                int max = stats.getMaxEnergy();
                int newEnergy = (int) Math.min(max, current - value);
                stats.getResources().setCurrentEnergy(newEnergy);
            } else {
                int current = stats.getResources().getCurrentEnergy();
                int max = stats.getMaxEnergy();
                int newEnergy = (int) Math.min(max, current / value);
                stats.getResources().setCurrentEnergy(newEnergy);
            }
            Location location = player.getLocation();
            spawnHologram(location, CC.translate("&9⚡"), 25);
        });
    }

    public static void addStaminaEffect(Player player, String operation, double value) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {

            if (operation.equals("*")) {
                int current = stats.getResources().getCurrentStamina();
                int max = stats.getMaxStamina();
                int newStamina = (int) Math.min(max, current + (current * (value - 1)));
                stats.getResources().setCurrentStamina(newStamina);
            } else if (operation.equals("+")) {
                int current = stats.getResources().getCurrentStamina();
                int max = stats.getMaxStamina();
                int newStamina = (int) Math.min(max, current + value);
                stats.getResources().setCurrentStamina(newStamina);
            } else if (operation.equals("-")) {
                int current = stats.getResources().getCurrentStamina();
                int max = stats.getMaxStamina();
                int newStamina = (int) Math.min(max, current - value);
                stats.getResources().setCurrentStamina(newStamina);
            } else {
                int current = stats.getResources().getCurrentStamina();
                int max = stats.getMaxStamina();
                int newStamina = (int) Math.min(max, current / value);
                stats.getResources().setCurrentStamina(newStamina);
            }
            Location location = player.getLocation();
            location.add(location.getX(), 0.4, 0.2);
            spawnHologram(location, CC.translate("&e❃"), 25);
        });
    }

    public static void addHealthEffect(Player player, String operation, double value) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {
            if (operation.equals("*")) {
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + (player.getHealth() * (value - 1))));
            } else if (operation.equals("+")) {
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + value));
            } else if (operation.equals("-")) {
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() - value));
            } else {
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() / value));
            }
            Location location = player.getLocation();
            location.add(location.getX(), 0.1, 0.5);
            spawnHologram(location, CC.translate("&c❤"), 25);
        });
    }

    public static void spawnHologram(Location location, String text, int durationTicks) {
        World world = location.getWorld();
        if (world == null) return;
        ArmorStand hologram = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setCanPickupItems(false);
        hologram.setCustomName(text);
        hologram.setCustomNameVisible(true);
        hologram.setMarker(true);
        Bukkit.getScheduler().runTaskLater(DbzMain.instance, hologram::remove, durationTicks);
    }

    public static void addTp(String username, int amount) {
        var ref = new Object() {
            int total = 0;
        };
        Player player = DbzMain.instance.getServer().getPlayer(username);
        ServerPlayer entity = ((CraftPlayer) player).getHandle();
        ServerPlayer serverPlayer = entity;
        StatsProvider.get(StatsCapability.INSTANCE, entity).ifPresent((statsData -> {
            statsData.getResources().addTrainingPoints(amount);
            ref.total = statsData.getResources().getTrainingPoints();
            NetworkHandler.sendToTrackingEntityAndSelf(new StatsSyncS2C(serverPlayer), serverPlayer);
        }));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(CC.translate("&6&l⚡ TPS OBTENIDOS ⚡"));
        player.sendMessage("");

        player.sendMessage(CC.translate("  &eTPs Base: &a+" + amount));
        player.sendMessage(CC.translate("  &eBonus: &6+" + 0)); // aquí luego metes booster real
        player.sendMessage(CC.translate("  &eTotal: &b+" + ref.total + " TPs"));
        player.sendMessage("");
        double booster = 0;

        if (booster > 1.0) {
            String percent = String.format("%.0f%%", (booster - 1.0) * 100);
            player.sendMessage(CC.translate("  &6⚡ Booster: &a+" + percent));
        }
        double mult = 1.0;
        String totalPercent = String.format("%.0f%%", (mult - 1.0) * 100);
        player.sendMessage(CC.translate("  &a⚡ Multiplicador Total: &6x" + String.format("%.2f", mult) + " &7(+" + totalPercent + ")"));
        player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10f, 10f);
    }
}
