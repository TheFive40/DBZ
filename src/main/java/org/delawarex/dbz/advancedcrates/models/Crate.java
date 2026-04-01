package org.delawarex.dbz.advancedcrates.models;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Crate {

    private String id;
    private String displayName;
    private List<String> lore;
    private String material;
    private Rarity rarity;
    private String keyId;
    private String keyDisplayName;
    private String keyMaterial;
    private List<CrateReward> rewards;
    private Location physicalLocation;
    private ItemStack visualItem;
    private boolean enabled;

    public Crate() {
        this.rewards = new ArrayList<>();
        this.lore = new ArrayList<>();
        this.enabled = true;
    }

    public Crate(String id) {
        this.id = id;
        this.displayName = "&6" + id;
        this.lore = new ArrayList<>();
        this.material = "CHEST";
        this.rarity = Rarity.COMMON;
        this.keyId = id + "_key";
        this.keyDisplayName = "&e\uD83D\uDDDD Llave de " + id;
        this.keyMaterial = "TRIPWIRE_HOOK";
        this.rewards = new ArrayList<>();
        this.enabled = true;
    }

    public CrateReward selectReward() {
        if (rewards.isEmpty()) return null;
        double total = rewards.stream().mapToDouble(CrateReward::getChance).sum();
        double roll = Math.random() * total;
        double cumulative = 0;
        for (CrateReward reward : rewards) {
            cumulative += reward.getChance();
            if (roll <= cumulative) return reward;
        }
        return rewards.get(rewards.size() - 1);
    }

    public double getTotalChance() {
        return rewards.stream().mapToDouble(CrateReward::getChance).sum();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName != null ? displayName : "&6Crate"; }

    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore != null ? lore : new ArrayList<>(); }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material != null ? material : "CHEST"; }

    public Rarity getRarity() { return rarity != null ? rarity : Rarity.COMMON; }
    public void setRarity(Rarity rarity) { this.rarity = rarity; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }

    public String getKeyDisplayName() { return keyDisplayName; }
    public void setKeyDisplayName(String keyDisplayName) { this.keyDisplayName = keyDisplayName; }

    public String getKeyMaterial() { return keyMaterial; }
    public void setKeyMaterial(String keyMaterial) { this.keyMaterial = keyMaterial; }

    public List<CrateReward> getRewards() { return rewards; }
    public void setRewards(List<CrateReward> rewards) { this.rewards = rewards != null ? rewards : new ArrayList<>(); }

    public Location getPhysicalLocation() { return physicalLocation; }
    public void setPhysicalLocation(Location physicalLocation) { this.physicalLocation = physicalLocation; }

    public ItemStack getVisualItem() { return visualItem; }
    public void setVisualItem(ItemStack visualItem) { this.visualItem = visualItem != null ? visualItem.clone() : null; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
