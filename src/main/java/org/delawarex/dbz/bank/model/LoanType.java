package org.delawarex.dbz.bank.model;

public enum LoanType {
    TPS, ZENIS;

    public String display() {
        return this == TPS ? "TPS" : "Zenis";
    }
}
