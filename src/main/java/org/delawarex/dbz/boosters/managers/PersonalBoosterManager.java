package org.delawarex.dbz.boosters.managers;

import org.delawarex.dbz.boosters.core.BoosterSettings;
import org.delawarex.dbz.boosters.models.PersonalBooster;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PersonalBoosterManager {

    private static final Map<UUID, List<PersonalBooster>> playerBoosters = new ConcurrentHashMap<>();

    public static void addBooster(PersonalBooster booster) {
        UUID playerId = booster.getPlayerId();
        playerBoosters.computeIfAbsent(playerId, k -> new ArrayList<>()).add(booster);
    }

    public static void activateBooster(UUID playerId, int index) {
        List<PersonalBooster> boosters = playerBoosters.get(playerId);
        if (boosters != null && index >= 0 && index < boosters.size()) {
            boosters.get(index).activate(BoosterSettings.getPersonalBoosterDuration());
        }
    }

    public static PersonalBooster getActiveBooster(UUID playerId) {
        List<PersonalBooster> boosters = playerBoosters.get(playerId);
        if (boosters != null) {
            for (PersonalBooster booster : boosters) {
                if (booster.isStillActive(BoosterSettings.getPersonalBoosterDuration())) return booster;
            }
        }
        return null;
    }

    public static List<PersonalBooster> getPlayerBoosters(UUID playerId) {
        return playerBoosters.getOrDefault(playerId, Collections.emptyList());
    }

    public static void removeExpiredBoosters(UUID playerId) {
        List<PersonalBooster> boosters = playerBoosters.get(playerId);
        if (boosters != null) {
            List<PersonalBooster> active = boosters.stream()
                    .filter(b -> !b.hasExpiredFromStorage(BoosterSettings.getPersonalBoosterStorageDays()))
                    .collect(Collectors.toList());
            if (active.isEmpty()) playerBoosters.remove(playerId);
            else playerBoosters.put(playerId, active);
        }
    }

    public static void deactivateBooster(UUID playerId) {
        PersonalBooster active = getActiveBooster(playerId);
        if (active != null) active.deactivate();
    }

    public static double getActiveMultiplier(UUID playerId) {
        PersonalBooster booster = getActiveBooster(playerId);
        return booster != null ? booster.getMultiplier() : 1.0;
    }

    public static void clearBoosters(UUID playerId) {
        playerBoosters.remove(playerId);
    }

    public static void clearAllBoosters() {
        playerBoosters.clear();
    }
}
