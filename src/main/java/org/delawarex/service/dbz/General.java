package org.delawarex.service.dbz;

import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.network.S2C.StatsSyncS2C;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
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
                stats.getStats().addEnergy((int) (stats.getMaxEnergy() * value));
            } else if (operation.equals("+")) {
                stats.getStats().addEnergy((int) (stats.getMaxEnergy() + value));
            } else if (operation.equals("-")) {
                stats.getStats().addEnergy((int) (stats.getMaxEnergy() - value));
            } else {
                stats.getStats().addEnergy((int) (stats.getMaxEnergy() / value));
            }
        });
    }

    public static void addStaminaEffect(Player player, String operation, double value) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {
            if (operation.equals("*")) {
                stats.getStats().addResistance((int) (stats.getMaxStamina() * value));
            } else if (operation.equals("+")) {
                stats.getStats().addResistance((int) (stats.getMaxStamina() + value));
            } else if (operation.equals("-")) {
                stats.getStats().addResistance((int) (stats.getMaxStamina() - value));
            } else {
                stats.getStats().addResistance((int) (stats.getMaxStamina() / value));
            }
        });
    }
    public static void addHealthEffect(Player player, String operation, double value) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {
            if (operation.equals("*")) {
                stats.getStats().addVitality((int) (stats.getMaxHealth() * value));
            } else if (operation.equals("+")) {
                stats.getStats().addVitality((int) (stats.getMaxHealth() + value));
            } else if (operation.equals("-")) {
                stats.getStats().addVitality((int) (stats.getMaxHealth() - value));
            } else {
                stats.getStats().addVitality((int) (stats.getMaxHealth() / value));
            }
        });
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
        player.sendMessage(CC.translate("&8&l&m-----------------------------"));
        player.sendMessage(CC.translate("&aTotal de TPS: &6+" + ref.total));
        player.sendMessage(CC.translate("&aBooster: &6+" + 0));
        player.sendMessage(CC.translate("&aTPS: &6+" + amount));
        player.sendMessage(CC.translate("&8&l&m-----------------------------"));
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10f, 10f);
    }
}
