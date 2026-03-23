package org.delawarex.dbz.customitems.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CustomArmor {

    private String id;
    private String material = "IRON_CHESTPLATE"; // Material enum name
    private String displayName = "";
    private List<String> lore = new ArrayList<>();
    private HashMap<String, Double> valueByStat = new HashMap<>();
    private HashMap<String, String> operation   = new HashMap<>();
    private HashMap<String, Double> effects     = new HashMap<>();
    private int     maxDurability = -1;
    private boolean unbreakable   = false;

    public CustomArmor() {}

    /* ── Getters ── */
    public String  getId()          { return id; }
    public String  getMaterial()    { return material; }
    public String  getDisplayName() { return displayName; }
    public List<String> getLore()   { return lore; }
    public HashMap<String, Double> getValueByStat() { return valueByStat; }
    public HashMap<String, String> getOperation()   { return operation; }
    public HashMap<String, Double> getEffects()     { return effects; }
    public int     getMaxDurability() { return maxDurability; }
    public boolean isUnbreakable()    { return unbreakable; }

    /* ── Setters (fluent) ── */
    public CustomArmor setId(String id)                          { this.id = id; return this; }
    public CustomArmor setMaterial(String material)              { this.material = material != null ? material.toUpperCase() : "IRON_CHESTPLATE"; return this; }
    public CustomArmor setDisplayName(String displayName)        { this.displayName = displayName != null ? displayName : ""; return this; }
    public CustomArmor setLore(List<String> lore)                { this.lore = lore != null ? lore : new ArrayList<>(); return this; }
    public CustomArmor setValueByStat(HashMap<String, Double> v) { this.valueByStat = v; return this; }
    public CustomArmor setOperation(HashMap<String, String> o)   { this.operation = o; return this; }
    public CustomArmor setEffects(HashMap<String, Double> e)     { this.effects = e; return this; }
    public CustomArmor setMaxDurability(int d)                   { this.maxDurability = d; return this; }
    public CustomArmor setUnbreakable(boolean u)                 { this.unbreakable = u; return this; }

    public CustomArmor setStat(String stat, String op, double value) {
        valueByStat.put(stat, value);
        operation.put(stat, op);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CustomArmor other)) return false;
        return Objects.equals(material, other.material)
                && Objects.equals(displayName, other.displayName)
                && Objects.equals(lore, other.lore);
    }

    @Override
    public int hashCode() { return Objects.hash(material, displayName, lore); }
}