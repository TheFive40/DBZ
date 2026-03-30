package org.delawarex.dbz.boosters.managers;

import org.delawarex.dbz.boosters.core.BoosterSettings;
import org.delawarex.dbz.boosters.models.GlobalBooster;

import java.time.Instant;

public class GlobalBoosterManager {

    private static volatile GlobalBooster activeBooster = null;

    public static void activateBooster(double multiplier, String activatedBy) {
        activateBooster(multiplier, activatedBy, BoosterSettings.getGlobalBoosterDuration());
    }

    public static void activateBooster(double multiplier, String activatedBy, long durationSeconds) {
        Instant now = Instant.now();
        Instant end = now.plusSeconds(durationSeconds);
        activeBooster = new GlobalBooster(multiplier, now, end, activatedBy);
    }

    public static void deactivateBooster() {
        if (activeBooster != null) {
            activeBooster.deactivate();
            activeBooster = null;
        }
    }

    public static GlobalBooster getActiveBooster() {
        if (activeBooster != null && activeBooster.hasExpired()) activeBooster = null;
        return activeBooster;
    }

    public static boolean isBoosterActive() {
        GlobalBooster booster = getActiveBooster();
        return booster != null && booster.isActive();
    }

    public static double getCurrentMultiplier() {
        GlobalBooster booster = getActiveBooster();
        return (booster != null && booster.isActive()) ? booster.getMultiplier() : 1.0;
    }

    public static long getRemainingSeconds() {
        GlobalBooster booster = getActiveBooster();
        return booster != null ? booster.getRemainingSeconds() : 0;
    }

    public static void updateDuration(long seconds) {
        BoosterSettings.setGlobalBoosterDuration(seconds);
    }

    public static void updateMultiplier(double multiplier) {
        if (activeBooster != null && activeBooster.isActive()) activeBooster.setMultiplier(multiplier);
    }

    public static void checkExpiration() {
        if (activeBooster != null && activeBooster.hasExpired()) deactivateBooster();
    }

    public static void restoreBooster(GlobalBooster savedBooster) {
        if (savedBooster == null || savedBooster.hasExpired()) return;
        activeBooster = savedBooster;
    }
}
