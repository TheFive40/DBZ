package org.delawarex.dbz.customitems.managers;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.customitems.models.CustomArmor;
import org.delawarex.dbz.customitems.models.CustomItem;
import org.delawarex.service.dbz.General;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorBonusManager implements Listener {

    private static final List<String> ALL_STATS = List.of("STR", "SKP", "RES", "VIT", "PWR", "ENE");

    private static final ConcurrentHashMap<UUID, Set<String>> applied = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Map<String, BukkitTask>> regenTasks = new ConcurrentHashMap<>();

    private static BukkitRunnable bonusTask;


    public static void start() {
        bonusTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : DbzMain.instance.getServer().getOnlinePlayers()) {
                    tickBonuses(player);
                }
            }
        };
        bonusTask.runTaskTimer(DbzMain.instance, 20L, 20L);
    }

    public static void stop() {
        if (bonusTask != null) bonusTask.cancel();
        regenTasks.values().forEach(map -> map.values().forEach(BukkitTask::cancel));
        regenTasks.clear();
    }

    public static void cleanup(Player player) {
        Set<String> ids = applied.remove(player.getUniqueId());
        if (ids != null) ids.forEach(id -> removeAllStats(player, id));

        Map<String, BukkitTask> tasks = regenTasks.remove(player.getUniqueId());
        if (tasks != null) tasks.values().forEach(BukkitTask::cancel);
    }

    /* ── Bonus tick (stats permanentes) ── */

    private static void tickBonuses(Player player) {
        Set<String> current = new HashSet<>();

        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (piece == null) continue;
            CustomArmor armor = CustomArmorManager.getInstance().identify(piece);
            if (armor != null && !armor.getValueByStat().isEmpty()) {
                current.add(armor.getId());
                applyBonus(player, armor.getId(), armor.getValueByStat(), armor.getOperation());
            }
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        CustomItem ci = CustomItemManager.getInstance().identify(hand);
        if (ci != null && ci.isActive() && !ci.getValueByStat().isEmpty()) {
            current.add(ci.getId());
            applyBonus(player, ci.getId(), ci.getValueByStat(), ci.getOperation());
        }

        Set<String> prev = applied.getOrDefault(player.getUniqueId(), Collections.emptySet());
        for (String old : prev) {
            if (!current.contains(old)) removeAllStats(player, old);
        }

        applied.put(player.getUniqueId(), current);
    }



    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cleanup(event.getPlayer());
    }

    public static void startRegenTask(Player player, String slotId, Map<String, Double> effects) {
        stopRegenTask(player, slotId);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                applyEffects(player, effects);
            }
        }.runTaskTimer(DbzMain.instance, 20L, 20L);

        regenTasks
                .computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(slotId, task);
    }

    public static void stopRegenTask(Player player, String slotId) {
        Map<String, BukkitTask> tasks = regenTasks.get(player.getUniqueId());
        if (tasks == null) return;
        BukkitTask task = tasks.remove(slotId);
        if (task != null) task.cancel();
    }


    private static void applyBonus(Player player, String bonusId,
                                   Map<String, Double> stats,
                                   Map<String, String> ops) {
        try {
            for (Map.Entry<String, Double> e : stats.entrySet()) {
                String stat = e.getKey();
                double value = e.getValue();
                String op = ops.getOrDefault(stat, "+");
                General.addBonus(player, stat, bonusId, op, value);
            }
        } catch (Exception ignored) {}
    }

    private static void removeAllStats(Player player, String bonusId) {
        for (String stat : ALL_STATS) {
            try {
                General.removeBonus(player, stat, bonusId);
            } catch (Exception ignored) {}
        }
    }

    private static void applyEffects(Player player, Map<String, Double> effects) {
        if (effects.isEmpty()) return;
        effects.forEach((effect, value) -> {
            try {
                switch (effect.toUpperCase()) {
                    case "HEALTHREGEN" -> General.addHealthEffect(player, "*", value);
                    case "KIREGEN" -> General.addEnergyEffect(player, "*", value);
                    case "STAMINAREGEN" -> General.addStaminaEffect(player, "*", value);
                }
            } catch (Exception ignored) {}
        });
    }
}