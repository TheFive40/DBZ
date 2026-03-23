package org.delawarex.dbz.customitems.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CustomItem {

    private String id;
    private String material = "STONE";   // Material enum name, e.g. "DIAMOND_SWORD"
    private String displayName = "";
    private List<String> lore = new ArrayList<>();
    private HashMap<String, Double> valueByStat = new HashMap<>();
    private HashMap<String, String> operation   = new HashMap<>();
    private HashMap<String, Double> effects     = new HashMap<>();
    private int     maxDurability  = -1;
    private boolean unbreakable    = false;
    private boolean consumable     = false;
    private List<String> commands  = new ArrayList<>();
    private int     tpValue        = 0;
    private boolean tpConsumeStack = false;
    private boolean active         = true;

    public CustomItem() {}

    /* ── Getters ── */
    public String  getId()            { return id; }
    public String  getMaterial()      { return material; }
    public String  getDisplayName()   { return displayName; }
    public List<String> getLore()     { return lore; }
    public HashMap<String, Double> getValueByStat() { return valueByStat; }
    public HashMap<String, String> getOperation()   { return operation; }
    public HashMap<String, Double> getEffects()     { return effects; }
    public int     getMaxDurability() { return maxDurability; }
    public boolean isUnbreakable()    { return unbreakable; }
    public boolean isConsumable()     { return consumable; }
    public List<String> getCommands() { return commands; }
    public int     getTpValue()       { return tpValue; }
    public boolean isTpConsumeStack() { return tpConsumeStack; }
    public boolean isActive()         { return active; }

    /* ── Setters (fluent) ── */
    public CustomItem setId(String id)                          { this.id = id; return this; }
    public CustomItem setMaterial(String material)              { this.material = material != null ? material.toUpperCase() : "STONE"; return this; }
    public CustomItem setDisplayName(String displayName)        { this.displayName = displayName != null ? displayName : ""; return this; }
    public CustomItem setLore(List<String> lore)                { this.lore = lore != null ? lore : new ArrayList<>(); return this; }
    public CustomItem setValueByStat(HashMap<String, Double> v) { this.valueByStat = v; return this; }
    public CustomItem setOperation(HashMap<String, String> o)   { this.operation = o; return this; }
    public CustomItem setEffects(HashMap<String, Double> e)     { this.effects = e; return this; }
    public CustomItem setMaxDurability(int d)                   { this.maxDurability = d; return this; }
    public CustomItem setUnbreakable(boolean u)                 { this.unbreakable = u; return this; }
    public CustomItem setConsumable(boolean c)                  { this.consumable = c; return this; }
    public CustomItem setCommands(List<String> commands)        { this.commands = commands != null ? commands : new ArrayList<>(); return this; }
    public CustomItem setTpValue(int tpValue)                   { this.tpValue = tpValue; return this; }
    public CustomItem setTpConsumeStack(boolean b)              { this.tpConsumeStack = b; return this; }
    public CustomItem setActive(boolean active)                 { this.active = active; return this; }

    public CustomItem setStat(String stat, String op, double value) {
        valueByStat.put(stat, value);
        operation.put(stat, op);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CustomItem other)) return false;
        return Objects.equals(material, other.material)
                && Objects.equals(displayName, other.displayName)
                && Objects.equals(lore, other.lore);
    }

    @Override
    public int hashCode() { return Objects.hash(material, displayName, lore); }
}