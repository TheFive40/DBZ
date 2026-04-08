package org.delawarex.dbz.battlepass.manager;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delawarex.dbz.battlepass.models.BattlePass;
import org.delawarex.dbz.battlepass.models.BattlePassLevel;
import org.delawarex.dbz.battlepass.models.BattlePassPlayer;
import org.delawarex.dbz.battlepass.storage.BattlePassPlayerStorage;
import org.delawarex.dbz.battlepass.storage.BattlePassStorage;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BattlePassManager {

    private static BattlePassManager instance;

    private final Map<String, BattlePass> passes = new ConcurrentHashMap<>();
    private final BattlePassStorage storage;
    private final BattlePassPlayerStorage playerStorage;
    private final AtomicInteger idCounter = new AtomicInteger(0);

    private BattlePassManager() {
        this.storage = new BattlePassStorage();
        this.playerStorage = new BattlePassPlayerStorage();
        loadAll();
    }

    public static BattlePassManager getInstance() {
        if (instance == null) instance = new BattlePassManager();
        return instance;
    }

    public static void reset() { instance = null; }

    private void loadAll() {
        passes.clear();
        passes.putAll(storage.loadAll());
        passes.keySet().stream()
                .filter(k -> k.matches("pass_\\d+"))
                .mapToInt(k -> Integer.parseInt(k.replace("pass_", "")))
                .max().ifPresent(idCounter::set);
    }

    public void reload() {
        passes.clear();
        storage.reload();
        passes.putAll(storage.loadAll());
    }

    public BattlePass createPass(String displayName) {
        String id = "pass_" + idCounter.incrementAndGet();
        BattlePass pass = new BattlePass(id);
        pass.setDisplayName(displayName);
        passes.put(id, pass);
        storage.savePass(pass);
        return pass;
    }

    public void savePass(BattlePass pass) {
        passes.put(pass.getId(), pass);
        storage.savePass(pass);
    }

    public void deletePass(String id) {
        passes.remove(id);
        storage.deletePass(id);
    }

    public BattlePass getPass(String id) { return passes.get(id); }
    public Collection<BattlePass> getAllPasses() { return new ArrayList<>(passes.values()); }
    public boolean exists(String id) { return passes.containsKey(id); }
    public int getTotal() { return passes.size(); }

    public BattlePassPlayer getOrCreatePlayer(UUID uuid, String playerName) {
        return playerStorage.loadOrCreate(uuid, playerName);
    }

    public BattlePassPlayer getPlayer(UUID uuid) {
        BattlePassPlayer p = playerStorage.getCached(uuid);
        if (p == null) p = playerStorage.load(uuid);
        return p;
    }

    public void savePlayer(BattlePassPlayer player) {
        playerStorage.save(player);
    }

    public void addPoints(Player player, String passId, int amount) {
        BattlePass pass = getPass(passId);
        if (pass == null) return;
        BattlePassPlayer bpPlayer = getOrCreatePlayer(player.getUniqueId(), player.getName());
        int oldPoints = bpPlayer.getPoints(passId);
        int oldLevel = pass.getLevelForPoints(oldPoints);
        bpPlayer.addPoints(passId, amount);
        int newLevel = pass.getLevelForPoints(bpPlayer.getPoints(passId));
        savePlayer(bpPlayer);
        if (newLevel > oldLevel) {
            notifyLevelUp(player, pass, oldLevel, newLevel);
        }
    }

    public void setPoints(Player player, String passId, int amount) {
        BattlePass pass = getPass(passId);
        if (pass == null) return;
        BattlePassPlayer bpPlayer = getOrCreatePlayer(player.getUniqueId(), player.getName());
        int oldPoints = bpPlayer.getPoints(passId);
        int oldLevel = pass.getLevelForPoints(oldPoints);
        bpPlayer.setPoints(passId, amount);
        int newLevel = pass.getLevelForPoints(bpPlayer.getPoints(passId));
        savePlayer(bpPlayer);
        if (newLevel > oldLevel) {
            notifyLevelUp(player, pass, oldLevel, newLevel);
        }
    }

    public void takePoints(Player player, String passId, int amount) {
        BattlePass pass = getPass(passId);
        if (pass == null) return;
        BattlePassPlayer bpPlayer = getOrCreatePlayer(player.getUniqueId(), player.getName());
        bpPlayer.addPoints(passId, -amount);
        savePlayer(bpPlayer);
    }

    private void notifyLevelUp(Player player, BattlePass pass, int oldLevel, int newLevel) {
        for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
            BattlePassLevel level = pass.getLevelByNumber(lvl);
            String levelName = level != null ? CC.strip(level.getDisplayName()) : "Nivel " + lvl;
            player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            player.sendMessage(CC.translate("&6&l    ⭐ PASE DE BATALLA: NIVEL UP ⭐"));
            player.sendMessage(CC.translate("&7  Pase: &f" + CC.strip(pass.getDisplayName())));
            player.sendMessage(CC.translate("&7  Nuevo nivel: &e&l" + levelName));
            player.sendMessage(CC.translate("&7  Usa &f/pass &7para reclamar tus recompensas."));
            player.sendMessage(CC.translate("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        }
        BattlePassLevel topLevel = pass.getLevelByNumber(newLevel);
        String topName = topLevel != null ? CC.strip(topLevel.getDisplayName()) : "Nivel " + newLevel;
        player.sendTitle(
                CC.translate("&6&l⭐ NIVEL UP ⭐"),
                CC.translate("&e" + CC.strip(pass.getDisplayName()) + " → " + topName),
                10, 80, 20
        );
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
        player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 80, 0.6, 1.0, 0.6, 0.2);
        player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation().add(0, 2, 0), 50, 0.8, 0.8, 0.8, 0.15);
    }

    public boolean claimLevel(Player player, BattlePass pass, BattlePassLevel level) {
        BattlePassPlayer bpPlayer = getOrCreatePlayer(player.getUniqueId(), player.getName());
        int points = bpPlayer.getPoints(pass.getId());
        if (points < level.getRequiredPoints()) return false;
        if (bpPlayer.hasClaimed(pass.getId(), level.getLevelNumber())) return false;
        bpPlayer.addClaimed(pass.getId(), level.getLevelNumber());
        savePlayer(bpPlayer);
        for (org.bukkit.inventory.ItemStack item : level.getItems()) {
            if (item == null) continue;
            if (player.getInventory().firstEmpty() == -1)
                player.getWorld().dropItem(player.getLocation(), item.clone());
            else
                player.getInventory().addItem(item.clone());
        }
        for (String cmd : level.getCommands()) {
            String finalCmd = cmd.replace("@p", player.getName()).replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        }
        player.sendMessage(CC.translate("&a✓ Recompensa reclamada: &f" + CC.strip(level.getDisplayName())));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        return true;
    }

    public BattlePassPlayerStorage getPlayerStorage() { return playerStorage; }
}