package org.delawarex.dbz.bank.model;

import java.util.ArrayList;
import java.util.List;

public class BankAccount {

    private String     uuid;
    private String     playerName;
    private double     zeniBalance;
    private long       tpsBalance;
    private List<Loan> loans;
    private double     zeniPenaltyRate;
    private double     tpsPenaltyRate;

    public BankAccount() {
        this.loans          = new ArrayList<>();
        this.zeniBalance    = 0;
        this.tpsBalance     = 0;
        this.zeniPenaltyRate = 0;
        this.tpsPenaltyRate  = 0;
    }

    public BankAccount(String uuid, String playerName) {
        this();
        this.uuid       = uuid;
        this.playerName = playerName;
    }

    public boolean hasZeniPenalty() { return zeniPenaltyRate > 0; }
    public boolean hasTpsPenalty()  { return tpsPenaltyRate  > 0; }

    public boolean hasOverdueLoan(LoanType type) {
        return loans.stream().anyMatch(l -> l.getType() == type && l.isOverdue());
    }

    public List<Loan> getActiveLoans() {
        return loans.stream().filter(l -> !l.isFullyPaid()).collect(java.util.stream.Collectors.toList());
    }

    public List<Loan> getOverdueLoans(LoanType type) {
        return loans.stream()
                .filter(l -> l.getType() == type && l.isOverdue())
                .collect(java.util.stream.Collectors.toList());
    }

    public void recalcPenalties() {
        tpsPenaltyRate  = loans.stream()
                .filter(l -> l.getType() == LoanType.TPS  && l.isOverdue())
                .mapToDouble(Loan::getPenaltyRate).sum();
        zeniPenaltyRate = loans.stream()
                .filter(l -> l.getType() == LoanType.ZENIS && l.isOverdue())
                .mapToDouble(Loan::getPenaltyRate).sum();
        tpsPenaltyRate  = Math.min(tpsPenaltyRate,  0.90);
        zeniPenaltyRate = Math.min(zeniPenaltyRate, 0.90);
    }

    public String     getUuid()                  { return uuid; }
    public void       setUuid(String v)           { this.uuid = v; }
    public String     getPlayerName()             { return playerName; }
    public void       setPlayerName(String v)     { this.playerName = v; }
    public double     getZeniBalance()            { return zeniBalance; }
    public void       setZeniBalance(double v)    { this.zeniBalance = Math.max(0, v); }
    public long       getTpsBalance()             { return tpsBalance; }
    public void       setTpsBalance(long v)       { this.tpsBalance = Math.max(0, v); }
    public List<Loan> getLoans()                  { return loans; }
    public void       setLoans(List<Loan> v)      { this.loans = v != null ? v : new ArrayList<>(); }
    public double     getZeniPenaltyRate()        { return zeniPenaltyRate; }
    public void       setZeniPenaltyRate(double v){ this.zeniPenaltyRate = v; }
    public double     getTpsPenaltyRate()         { return tpsPenaltyRate; }
    public void       setTpsPenaltyRate(double v) { this.tpsPenaltyRate = v; }
}
