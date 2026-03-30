package org.delawarex.dbz.market.model;

import java.util.UUID;

public class Transaction {

    public enum Type { BUY, SELL }

    private final UUID playerId;
    private final String playerName;
    private final String itemId;
    private final Type type;
    private final int quantity;
    private final double pricePerUnit;
    private final double total;
    private final long timestamp;

    public Transaction(UUID playerId, String playerName, String itemId, Type type, int quantity, double pricePerUnit, double total) {
        this.playerId    = playerId;
        this.playerName  = playerName;
        this.itemId      = itemId;
        this.type        = type;
        this.quantity    = quantity;
        this.pricePerUnit = pricePerUnit;
        this.total       = total;
        this.timestamp   = System.currentTimeMillis();
    }

    public UUID   getPlayerId()     { return playerId; }
    public String getPlayerName()   { return playerName; }
    public String getItemId()       { return itemId; }
    public Type   getType()         { return type; }
    public int    getQuantity()     { return quantity; }
    public double getPricePerUnit() { return pricePerUnit; }
    public double getTotal()        { return total; }
    public long   getTimestamp()    { return timestamp; }
}
