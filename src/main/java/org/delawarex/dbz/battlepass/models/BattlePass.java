package org.delawarex.dbz.battlepass.models;

import java.util.ArrayList;
import java.util.List;

public class BattlePass {

    private String id;
    private String displayName;
    private String description;
    private String permission;
    private boolean enabled;
    private String material;
    private List<BattlePassLevel> levels;

    public BattlePass() {
        this.levels = new ArrayList<>();
        this.enabled = true;
        this.permission = "";
        this.description = "";
        this.material = "BOOK";
    }

    public BattlePass(String id) {
        this();
        this.id = id;
        this.displayName = "&e" + id;
    }

    public int getLevelForPoints(int points) {
        int current = 0;
        for (BattlePassLevel l : levels) {
            if (points >= l.getRequiredPoints()) {
                current = Math.max(current, l.getLevelNumber());
            }
        }
        return current;
    }

    public BattlePassLevel getLevelByNumber(int number) {
        return levels.stream().filter(l -> l.getLevelNumber() == number).findFirst().orElse(null);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisplayName() { return displayName != null ? displayName : "&e" + id; }
    public void setDisplayName(String v) { this.displayName = v; }
    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String v) { this.description = v; }
    public String getPermission() { return permission != null ? permission : ""; }
    public void setPermission(String v) { this.permission = v; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }
    public String getMaterial() { return material != null ? material : "BOOK"; }
    public void setMaterial(String v) { this.material = v; }
    public List<BattlePassLevel> getLevels() { return levels; }
    public void setLevels(List<BattlePassLevel> v) { this.levels = v != null ? v : new ArrayList<>(); }
}