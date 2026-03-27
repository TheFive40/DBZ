package org.delawarex.dbz.raids.models;

public class WaveReward {

    private String command;
    private int probability;

    public WaveReward(String command, int probability) {
        this.command = command;
        this.probability = Math.max(0, Math.min(100, probability));
    }

    public boolean shouldExecute() {
        return Math.random() * 100 < probability;
    }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public int getProbability() { return probability; }
    public void setProbability(int probability) { this.probability = Math.max(0, Math.min(100, probability)); }
}