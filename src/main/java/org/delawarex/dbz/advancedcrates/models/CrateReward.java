package org.delawarex.dbz.advancedcrates.models;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CrateReward {

    private String id;
    private ItemStack item;
    private double chance;
    private List<String> commands;

    public CrateReward() {
        this.commands = new ArrayList<>();
    }

    public CrateReward(String id, ItemStack item, double chance) {
        this.id = id;
        this.item = item != null ? item.clone() : null;
        this.chance = chance;
        this.commands = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ItemStack getItem() { return item; }
    public void setItem(ItemStack item) { this.item = item != null ? item.clone() : null; }

    public double getChance() { return chance; }
    public void setChance(double chance) { this.chance = Math.max(0.01, chance); }

    public List<String> getCommands() { return commands; }
    public void setCommands(List<String> commands) { this.commands = commands != null ? commands : new ArrayList<>(); }
}
