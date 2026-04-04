package org.delawarex.service.dbz;
import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.network.S2C.StatsSyncS2C;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.server.level.ServerPlayer;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.service.CC;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class General {
    public static final Set<ArmorStand> HOLOGRAMS = new HashSet<>();

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

    public static void spawnNpc(double x, double y, double z, int tab, String name, Player player) {
        IWorld world = null;
        for (IWorld iWorld : NpcAPI.Instance().getIWorlds()) {
            if (iWorld.getName().equals(player.getWorld().getName())) {
                world = iWorld;
                break;
            }
        }
        NpcAPI.Instance().getClones().spawn(x, y, z, tab, name,
                world);
    }

    public static int getLVL(Player player) {
        AtomicInteger lvl = new AtomicInteger();
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {
            lvl.set(stats.getLevel());
        });
        return lvl.get();
    }

    public static void addEnergyEffect(Player player, String operation, double value) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {
            int current = stats.getResources().getCurrentEnergy();
            int max = stats.getMaxEnergy();
            int newEnergy;

            switch (operation) {
                case "*" -> newEnergy = (int) Math.min(max, current + (max * value));
                case "+" -> newEnergy = (int) Math.min(max, current + value);
                case "-" -> newEnergy = (int) Math.max(0, current - value);
                default -> newEnergy = current;
            }

            stats.getResources().setCurrentEnergy(newEnergy);

            Location loc = player.getLocation().clone().add(0, 2.2, 0);
            spawnHologram(loc, CC.translate("&9⚡ +" + (int) (max * value)), 25);
        });
    }


    public static void addStaminaEffect(Player player, String operation, double value) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {
            int current = stats.getResources().getCurrentStamina();
            int max = stats.getMaxStamina();
            int newStamina;

            switch (operation) {
                case "*" -> newStamina = (int) Math.min(max, current + (max * value));
                case "+" -> newStamina = (int) Math.min(max, current + value);
                case "-" -> newStamina = (int) Math.max(0, current - value);
                default -> newStamina = current;
            }

            stats.getResources().setCurrentStamina(newStamina);

            Location loc = player.getLocation().clone().add(0, 1.8, 0);
            spawnHologram(loc, CC.translate("&e❃ +" + (int) (max * value)), 25);
        });
    }

    public static void addHealthEffect(Player player, String operation, double value) {
        StatsProvider.get(StatsCapability.INSTANCE, ((CraftPlayer) player).getHandle()).ifPresent(stats -> {
            double current = player.getHealth();
            double maxHealth = player.getMaxHealth();
            double newHealth;

            switch (operation) {
                case "*" -> newHealth = Math.min(maxHealth, current + (maxHealth * value));
                case "+" -> newHealth = Math.min(maxHealth, current + value);
                case "-" -> newHealth = Math.max(0, current - value);
                default -> newHealth = current;
            }

            player.setHealth(newHealth);

            Location loc = player.getLocation().clone().add(0, 2.0, 0);
            spawnHologram(loc, CC.translate("&c❤ +" + (int) (maxHealth * value)), 25);
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

        HOLOGRAMS.add(hologram);

        Bukkit.getScheduler().runTaskLater(DbzMain.instance, () -> {
            if (!hologram.isDead()) {
                hologram.remove();
            }
            HOLOGRAMS.remove(hologram);
        }, durationTicks);
    }

    public static void cleanupHolograms(World world) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof ArmorStand) {
                ArmorStand as = (ArmorStand) entity;
                if (as.isMarker() && as.getCustomName() != null) {
                    as.remove();
                }
            }
        }
    }

    public static void sendMessageBooster(int bonusTPs, int totalTPs, int baseTPs,
                                          double globalMult, double personalMult, String senderName,
                                          CommandSender target) {
        target.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        target.sendMessage(CC.translate("&6&l⚡ TPS RECIBIDOS ⚡"));
        target.sendMessage("");
        target.sendMessage(CC.translate("  &eTPs Base: &a+" + baseTPs));
        target.sendMessage(CC.translate("  &eBonus: &6+" + bonusTPs + " TPs"));
        target.sendMessage(CC.translate("  &eTotal: &b+" + totalTPs + " TPs"));
        target.sendMessage("");
        if (globalMult > 1.0) {
            String globalPercent = String.format("%.0f%%", (globalMult - 1.0) * 100);
            target.sendMessage(CC.translate("  &6⚡ Booster Global: &a+" + globalPercent));
        }
        if (personalMult > 1.0) {
            String personalPercent = String.format("%.0f%%", (personalMult - 1.0) * 100);
            target.sendMessage(CC.translate("  &b⚡ Booster Personal: &a+" + personalPercent));
        }
        target.sendMessage(CC.translate("  &7Otorgado por: &6" + senderName));
        target.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
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
        player.sendMessage(CC.translate("&8[&a&l✓&9] &eHas recibido &6+" + amount + " TPs&e."));
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10f, 10f);
    }

    public static int getTP(Player player) {
        ServerPlayer entity = ((CraftPlayer) player).getHandle();
        AtomicInteger trainingPoints = new AtomicInteger();
        StatsProvider.get(StatsCapability.INSTANCE, entity).ifPresent((statsData -> {
            trainingPoints.set(statsData.getResources().getTrainingPoints());
        }));
        return trainingPoints.get();
    }
}