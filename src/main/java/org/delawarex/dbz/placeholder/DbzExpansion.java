package org.delawarex.dbz.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.delawarex.dbz.boosters.managers.GlobalBoosterManager;
import org.delawarex.dbz.boosters.models.GlobalBooster;
import org.jetbrains.annotations.NotNull;

public class DbzExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "dmztp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "dbz";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        return switch (identifier.toLowerCase()) {
            case "multiplier":
                yield "x" + GlobalBoosterManager.getCurrentMultiplier();

            case "timeleft": {
                GlobalBooster booster = GlobalBoosterManager.getActiveBooster();

                if (booster == null) {
                    yield "00:00";
                }

                yield booster.getFormattedTime();
            }

            default:
                yield "";
        };
    }
}
