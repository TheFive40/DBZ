package org.delawarex.dbz.battlepass.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.battlepass.models.BattlePassPlayer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BattlePassPlayerStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File folder;
    private final ConcurrentHashMap<UUID, BattlePassPlayer> cache = new ConcurrentHashMap<>();

    public BattlePassPlayerStorage() {
        folder = new File(DbzMain.instance.getDataFolder(), "battlepass/players");
        folder.mkdirs();
    }

    public BattlePassPlayer load(UUID uuid) {
        if (cache.containsKey(uuid)) return cache.get(uuid);
        File file = new File(folder, uuid + ".json");
        if (!file.exists()) return null;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            BattlePassPlayer p = GSON.fromJson(reader, BattlePassPlayer.class);
            if (p != null) cache.put(uuid, p);
            return p;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BattlePassPlayer loadOrCreate(UUID uuid, String playerName) {
        BattlePassPlayer p = load(uuid);
        if (p == null) {
            p = new BattlePassPlayer(uuid.toString(), playerName);
            cache.put(uuid, p);
            save(p);
        }
        return p;
    }

    public void save(BattlePassPlayer p) {
        File file = new File(folder, p.getUuid() + ".json");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(p, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try { cache.put(UUID.fromString(p.getUuid()), p); } catch (Exception ignored) {}
    }

    public BattlePassPlayer getCached(UUID uuid) { return cache.get(uuid); }
    public void evict(UUID uuid) { cache.remove(uuid); }
    public Collection<BattlePassPlayer> getAllCached() { return cache.values(); }
}