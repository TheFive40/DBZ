package org.delawarex.dbz.raids.managers;

import org.delawarex.dbz.raids.models.Raid;
import org.delawarex.dbz.raids.storage.RaidStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RaidManager {

    private static RaidManager instance;
    private final Map<String, Raid> raids = new ConcurrentHashMap<>();
    private final RaidStorage storage;
    private final AtomicInteger counter = new AtomicInteger(0);

    private RaidManager() {
        storage = new RaidStorage();
        raids.putAll(storage.loadAll());
        int maxId = raids.keySet().stream()
                .filter(k -> k.startsWith("raid_"))
                .mapToInt(k -> {
                    try { return Integer.parseInt(k.replace("raid_", "")); } catch (Exception e) { return 0; }
                })
                .max().orElse(0);
        counter.set(maxId);
    }

    public static RaidManager getInstance() {
        if (instance == null) instance = new RaidManager();
        return instance;
    }

    public Raid createRaid(String name) {
        String id = "raid_" + counter.incrementAndGet();
        Raid raid = new Raid(id, name);
        raids.put(id, raid);
        storage.saveRaid(raid);
        return raid;
    }

    public void saveRaid(Raid raid) {
        raids.put(raid.getRaidId(), raid);
        storage.saveRaid(raid);
    }

    public void deleteRaid(String id) {
        raids.remove(id);
        storage.deleteRaid(id);
    }

    public Raid getById(String id) { return raids.get(id); }

    public Raid getByName(String name) {
        return raids.values().stream()
                .filter(r -> r.getRaidName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public List<Raid> getAll() { return new ArrayList<>(raids.values()); }
    public boolean exists(String id) { return raids.containsKey(id); }
    public int getTotal() { return raids.size(); }
    public RaidStorage getStorage() { return storage; }

    public void reload() {
        raids.clear();
        storage.reload();
        raids.putAll(storage.loadAll());
    }
}