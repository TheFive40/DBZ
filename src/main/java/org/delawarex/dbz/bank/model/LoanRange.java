package org.delawarex.dbz.bank.model;

public class LoanRange {

    private int    minLevel;
    private int    maxLevel;
    private long   maxTPS;
    private double maxZenis;
    private double interestRate;
    private int    installmentCount;
    private int    installmentIntervalHours;
    private double penaltyRate;

    public LoanRange() {}

    public LoanRange(int minLevel, int maxLevel, long maxTPS, double maxZenis,
                     double interestRate, int installmentCount, int installmentIntervalHours, double penaltyRate) {
        this.minLevel               = minLevel;
        this.maxLevel               = maxLevel;
        this.maxTPS                 = maxTPS;
        this.maxZenis               = maxZenis;
        this.interestRate           = interestRate;
        this.installmentCount       = installmentCount;
        this.installmentIntervalHours = installmentIntervalHours;
        this.penaltyRate            = penaltyRate;
    }

    public boolean contains(int level) {
        return level >= minLevel && level <= maxLevel;
    }

    public int    getMinLevel()                { return minLevel; }
    public void   setMinLevel(int v)           { this.minLevel = v; }
    public int    getMaxLevel()                { return maxLevel; }
    public void   setMaxLevel(int v)           { this.maxLevel = v; }
    public long   getMaxTPS()                  { return maxTPS; }
    public void   setMaxTPS(long v)            { this.maxTPS = v; }
    public double getMaxZenis()                { return maxZenis; }
    public void   setMaxZenis(double v)        { this.maxZenis = v; }
    public double getInterestRate()            { return interestRate; }
    public void   setInterestRate(double v)    { this.interestRate = v; }
    public int    getInstallmentCount()        { return installmentCount; }
    public void   setInstallmentCount(int v)   { this.installmentCount = v; }
    public int    getInstallmentIntervalHours(){ return installmentIntervalHours; }
    public void   setInstallmentIntervalHours(int v) { this.installmentIntervalHours = v; }
    public double getPenaltyRate()             { return penaltyRate; }
    public void   setPenaltyRate(double v)     { this.penaltyRate = v; }
}
