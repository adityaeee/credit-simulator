package com.aditya.creditsimulator.model;

import java.math.BigDecimal;

/**
 * hasil kalkulasi buat 1 tahun. LoanCalculator bakal return
 * list<InstallmentResult>, satu entry per tahun tenor
 * breakdown tahun 1/2/3 di Rumus.xlsx.

 * Mapping rumus
 *   year              -> "tahun ke-"
 *   startingPrincipal -> "pokok Pinjaman"
 *   rate              -> "rate"
 *   totalWithInterest -> "total pinjaman" (pokok + bunga tahun ini)
 *   installmentMonthly
 *   installmentYearly
 */

public final class InstallmentResult {

    private final int year;
    private final BigDecimal startingPrincipal;
    private final BigDecimal rate;
    private final BigDecimal totalWithInterest;
    private final BigDecimal installmentMonthly;
    private final BigDecimal installmentYearly;

    public InstallmentResult(int year, BigDecimal startingPrincipal, BigDecimal rate,
                             BigDecimal totalWithInterest, BigDecimal installmentMonthly,
                             BigDecimal installmentYearly) {
        this.year = year;
        this.startingPrincipal = startingPrincipal;
        this.rate = rate;
        this.totalWithInterest = totalWithInterest;
        this.installmentMonthly = installmentMonthly;
        this.installmentYearly = installmentYearly;
    }

    public int getYear() {
        return year;
    }

    public BigDecimal getStartingPrincipal() {
        return startingPrincipal;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getTotalWithInterest() {
        return totalWithInterest;
    }

    public BigDecimal getInstallmentMonthly() {
        return installmentMonthly;
    }

    public BigDecimal getInstallmentYearly() {
        return installmentYearly;
    }

    @Override
    public String toString() {
        return "Tahun " + year +
                " | Pokok: " + startingPrincipal +
                " | Rate: " + rate +
                " | Total: " + totalWithInterest +
                " | Cicilan/bln: " + installmentMonthly;
    }
}