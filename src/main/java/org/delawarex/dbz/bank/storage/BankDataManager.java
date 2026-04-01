package org.delawarex.dbz.bank.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.bank.model.BankAccount;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BankDataManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File                            folder;
    private final ConcurrentHashMap<UUID, BankAccount> cache = new ConcurrentHashMap<>();

    public BankDataManager() {
        folder = new File(DbzMain.instance.getDataFolder(), "bank/accounts");
        folder.mkdirs();
    }

    public BankAccount load(UUID uuid) {
        if (cache.containsKey(uuid)) return cache.get(uuid);
        File file = new File(folder, uuid + ".json");
        if (!file.exists()) return null;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            BankAccount account = GSON.fromJson(reader, BankAccount.class);
            if (account != null) cache.put(uuid, account);
            return account;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BankAccount loadOrCreate(UUID uuid, String playerName) {
        BankAccount acc = load(uuid);
        if (acc == null) {
            acc = new BankAccount(uuid.toString(), playerName);
            cache.put(uuid, acc);
            save(acc);
        }
        return acc;
    }

    public void save(BankAccount account) {
        File file = new File(folder, account.getUuid() + ".json");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(account, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cache.put(UUID.fromString(account.getUuid()), account);
    }

    public void loadAll() {
        File[] files = folder.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return;
        for (File file : files) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                BankAccount account = GSON.fromJson(reader, BankAccount.class);
                if (account != null && account.getUuid() != null) {
                    cache.put(UUID.fromString(account.getUuid()), account);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Collection<BankAccount> getAllCached() { return cache.values(); }

    public BankAccount getCached(UUID uuid) { return cache.get(uuid); }

    public void evict(UUID uuid) { cache.remove(uuid); }
}
