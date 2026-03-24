package org.delawarex.dbz.customitems.managers;

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

    // UUID → set of bonusIds currently applied (stats)
    private static final ConcurrentHashMap<UUID, Set<String>> applied = new ConcurrentHashMap<>();

    // UUID → slotId → task (regen effects por slot)
    private static final ConcurrentHashMap<UUID, Map<String, BukkitTask>> regenTasks = new ConcurrentHashMap<>();

    // UUID → slotId → effects snapshot (para detectar cambios)
    private static final ConcurrentHashMap<UUID, Map<String, Map<String, Double>>> activeEffects = new ConcurrentHashMap<>();

    private static BukkitRunnable bonusTask;

    /* ── Start / Stop ── */

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
        activeEffects.clear();
    }

    public static void cleanup(Player player) {
        // Quitar stats
        Set<String> ids = applied.remove(player.getUniqueId());
        if (ids != null) ids.forEach(id -> removeAllStats(player, id));

        // Cancelar tareas de regen
        Map<String, BukkitTask> tasks = regenTasks.remove(player.getUniqueId());
        if (tasks != null) tasks.values().forEach(BukkitTask::cancel);

        activeEffects.remove(player.getUniqueId());
    }

    /* ── Tick principal ── */

    private static void tickBonuses(Player player) {
        Set<String> currentBonusIds = new HashSet<>();
        // slotId → effects del slot que tiene armadura con efectos
        Map<String, Map<String, Double>> currentEffectsBySlot = new HashMap<>();

        ItemStack[] armorContents = player.getInventory().getArmorContents();
        String[] slotNames = {"feet", "legs", "chest", "head"};

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack piece = armorContents[i];
            if (piece == null) continue;
            CustomArmor armor = CustomArmorManager.getInstance().identify(piece);
            if (armor == null) continue;

            String slotId = slotNames[i];

            if (!armor.getValueByStat().isEmpty()) {
                currentBonusIds.add(armor.getId());
                applyBonus(player, armor.getId(), armor.getValueByStat(), armor.getOperation());
            }

            if (!armor.getEffects().isEmpty()) {
                currentEffectsBySlot.put(slotId, armor.getEffects());
            }
        }

        // Mano principal
        ItemStack hand = player.getInventory().getItemInMainHand();
        CustomItem ci = CustomItemManager.getInstance().identify(hand);
        if (ci != null && ci.isActive() && !ci.getValueByStat().isEmpty()) {
            currentBonusIds.add(ci.getId());
            applyBonus(player, ci.getId(), ci.getValueByStat(), ci.getOperation());
        }

        // Retirar stats de piezas que ya no están
        Set<String> prev = applied.getOrDefault(player.getUniqueId(), Collections.emptySet());
        for (String old : prev) {
            if (!currentBonusIds.contains(old)) removeAllStats(player, old);
        }
        applied.put(player.getUniqueId(), currentBonusIds);

        // Sincronizar tareas de regen
        syncRegenTasks(player, currentEffectsBySlot);
    }

    /**
     * Inicia/detiene tareas de regen según los slots activos.
     * Si el slot sigue activo con los mismos efectos, no hace nada (evita reinicios innecesarios).
     */
    private static void syncRegenTasks(Player player, Map<String, Map<String, Double>> currentEffectsBySlot) {
        Map<String, Map<String, Double>> prevEffects =
                activeEffects.getOrDefault(player.getUniqueId(), new HashMap<>());

        // Detener slots que ya no tienen efectos
        for (String slotId : new HashSet<>(prevEffects.keySet())) {
            if (!currentEffectsBySlot.containsKey(slotId)) {
                stopRegenTask(player, slotId);
            }
        }

        for (Map.Entry<String, Map<String, Double>> entry : currentEffectsBySlot.entrySet()) {
            String slotId = entry.getKey();
            Map<String, Double> effects = entry.getValue();

            boolean alreadyRunning = prevEffects.containsKey(slotId)
                    && prevEffects.get(slotId).equals(effects);

            if (!alreadyRunning) {
                stopRegenTask(player, slotId);
                startRegenTask(player, slotId, effects);
            }
        }
        activeEffects.put(player.getUniqueId(), new HashMap<>(currentEffectsBySlot));
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
                String stat  = e.getKey();
                double value = e.getValue();
                String op    = ops.getOrDefault(stat, "+");
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
                    case "HEALTHREGEN"  -> General.addHealthEffect(player,  "*", value);
                    case "KIREGEN"      -> General.addEnergyEffect(player,  "*", value);
                    case "STAMINAREGEN" -> General.addStaminaEffect(player, "*", value);
                }
            } catch (Exception ignored) {}
        });
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cleanup(event.getPlayer());
    }
}