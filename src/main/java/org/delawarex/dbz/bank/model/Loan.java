package org.delawarex.dbz.bank.model;

import java.util.UUID;

public class Loan {

    private String   id;
    private LoanType type;
    private double   originalAmount;
    private double   totalWithInterest;
    private double   installmentAmount;
    private double   paidAmount;
    private int      totalInstallments;
    private int      paidInstallments;
    private long     nextPaymentTime;
    private long     intervalMillis;
    private boolean  overdue;
    private double   penaltyRate;
    private boolean  notifiedOverdue;
    private double   overdueInterest;

    public Loan() {}

    public Loan(LoanType type, double originalAmount, double interestRate,
                int installmentCount, int intervalHours, double penaltyRate) {
        this.id                = UUID.randomUUID().toString();
        this.type              = type;
        this.originalAmount    = originalAmount;
        this.totalWithInterest = originalAmount * (1.0 + interestRate);
        this.totalInstallments = installmentCount;
        this.installmentAmount = this.totalWithInterest / installmentCount;
        this.paidAmount        = 0;
        this.paidInstallments  = 0;
        this.intervalMillis    = intervalHours * 3600_000L;
        this.nextPaymentTime   = System.currentTimeMillis() + this.intervalMillis;
        this.overdue           = false;
        this.penaltyRate       = penaltyRate;
        this.notifiedOverdue   = false;
        this.overdueInterest   = 0.0;
    }

    public boolean isFullyPaid() {
        return paidInstallments >= totalInstallments;
    }

    public double getRemainingAmount() {
        return Math.max(0, totalWithInterest - paidAmount);
    }

    public int getRemainingInstallments() {
        return totalInstallments - paidInstallments;
    }

    public boolean isDueNow() {
        return System.currentTimeMillis() >= nextPaymentTime && !isFullyPaid();
    }

    public double getTotalDueInstallment() {
        return installmentAmount + overdueInterest;
    }

    public void advancePayment(double amount) {
        paidAmount       += amount;
        paidInstallments += 1;
        nextPaymentTime  += intervalMillis;
        if (overdue) overdue = false;
        overdueInterest  = 0.0;
        notifiedOverdue  = false;
    }

    public String    getId()                   { return id; }
    public void      setId(String v)           { this.id = v; }
    public LoanType  getType()                 { return type; }
    public void      setType(LoanType v)       { this.type = v; }
    public double    getOriginalAmount()       { return originalAmount; }
    public void      setOriginalAmount(double v){ this.originalAmount = v; }
    public double    getTotalWithInterest()    { return totalWithInterest; }
    public void      setTotalWithInterest(double v){ this.totalWithInterest = v; }
    public double    getInstallmentAmount()    { return installmentAmount; }
    public void      setInstallmentAmount(double v){ this.installmentAmount = v; }
    public double    getPaidAmount()           { return paidAmount; }
    public void      setPaidAmount(double v)   { this.paidAmount = v; }
    public int       getTotalInstallments()    { return totalInstallments; }
    public void      setTotalInstallments(int v){ this.totalInstallments = v; }
    public int       getPaidInstallments()     { return paidInstallments; }
    public void      setPaidInstallments(int v){ this.paidInstallments = v; }
    public long      getNextPaymentTime()      { return nextPaymentTime; }
    public void      setNextPaymentTime(long v){ this.nextPaymentTime = v; }
    public long      getIntervalMillis()       { return intervalMillis; }
    public void      setIntervalMillis(long v) { this.intervalMillis = v; }
    public boolean   isOverdue()               { return overdue; }
    public void      setOverdue(boolean v)     { this.overdue = v; }
    public double    getPenaltyRate()          { return penaltyRate; }
    public void      setPenaltyRate(double v)  { this.penaltyRate = v; }
    public boolean   isNotifiedOverdue()       { return notifiedOverdue; }
    public void      setNotifiedOverdue(boolean v) { this.notifiedOverdue = v; }
    public double    getOverdueInterest()      { return overdueInterest; }
    public void      setOverdueInterest(double v)  { this.overdueInterest = Math.max(0, v); }
}
