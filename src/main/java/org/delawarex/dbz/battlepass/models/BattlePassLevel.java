package org.delawarex.dbz.battlepass.models;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class BattlePassLevel {

    private int levelNumber;
    private int requiredPoints;
    private String displayName;
    private List<String> commands;
    private List<ItemStack> items;

    public BattlePassLevel() {
        this.commands = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public BattlePassLevel(int levelNumber, int requiredPoints) {
        this();
        this.levelNumber = levelNumber;
        this.requiredPoints = requiredPoints;
        this.displayName = "Nivel " + levelNumber;
    }

    public boolean hasRewards() {
        return !commands.isEmpty() || !items.isEmpty();
    }

    public int getLevelNumber() { return levelNumber; }
    public void setLevelNumber(int v) { this.levelNumber = v; }
    public int getRequiredPoints() { return requiredPoints; }
    public void setRequiredPoints(int v) { this.requiredPoints = Math.max(0, v); }
    public String getDisplayName() { return displayName != null ? displayName : "Nivel " + levelNumber; }
    public void setDisplayName(String v) { this.displayName = v; }
    public List<String> getCommands() { return commands; }
    public void setCommands(List<String> v) { this.commands = v != null ? v : new ArrayList<>(); }
    public List<ItemStack> getItems() { return items; }
    public void setItems(List<ItemStack> v) { this.items = v != null ? v : new ArrayList<>(); }
}