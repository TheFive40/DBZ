package org.delawarex.dbz.boosters.storage;

import com.google.gson.*;
import org.delawarex.dbz.boosters.managers.GlobalBoosterManager;
import org.delawarex.dbz.boosters.managers.PersonalBoosterManager;
import org.delawarex.dbz.boosters.models.GlobalBooster;
import org.delawarex.dbz.boosters.models.PersonalBooster;
import org.delawarex.dbz.DbzMain;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BoosterStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String BASE_PATH;
    private static final String GLOBAL_FILE = "global.json";
    private static final String PERSONAL_DIR = "personal";

    static {
        BASE_PATH = DbzMain.instance.getDataFolder().getAbsolutePath() + File.separator + "boosters";
        try {
            Files.createDirectories(Paths.get(BASE_PATH));
            Files.createDirectories(Paths.get(BASE_PATH, PERSONAL_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveGlobalBooster(GlobalBooster booster) {
        try {
            Path filePath = Paths.get(BASE_PATH, GLOBAL_FILE);
            Files.write(filePath, GSON.toJson(booster).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GlobalBooster loadGlobalBooster() {
        try {
            Path filePath = Paths.get(BASE_PATH, GLOBAL_FILE);
            if (Files.exists(filePath)) {
                String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                return GSON.fromJson(json, GlobalBooster.class);
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void savePersonalBoosters(UUID playerId, List<PersonalBooster> boosters) {
        try {
            Path playerDir = Paths.get(BASE_PATH, PERSONAL_DIR, playerId.toString());
            Files.createDirectories(playerDir);
            Path filePath = playerDir.resolve("boosters.json");
            Files.write(filePath, GSON.toJson(boosters).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<PersonalBooster> loadPersonalBoosters(UUID playerId) {
        try {
            Path filePath = Paths.get(BASE_PATH, PERSONAL_DIR, playerId.toString(), "boosters.json");
            if (Files.exists(filePath)) {
                String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                PersonalBooster[] boosters = GSON.fromJson(json, PersonalBooster[].class);
                return Arrays.asList(boosters);
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static void loadAllData() {
        GlobalBooster global = loadGlobalBooster();
        if (global != null && !global.hasExpired()) {
            GlobalBoosterManager.restoreBooster(global);
        }
    }

    public static void saveAllData() {
        GlobalBooster active = GlobalBoosterManager.getActiveBooster();
        if (active != null) saveGlobalBooster(active);
    }

    public static void deletePersonalBoosters(UUID playerId) {
        try {
            Path playerDir = Paths.get(BASE_PATH, PERSONAL_DIR, playerId.toString());
            if (Files.exists(playerDir)) {
                Files.walk(playerDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try { Files.delete(path); }
                            catch (IOException e) { e.printStackTrace(); }
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
