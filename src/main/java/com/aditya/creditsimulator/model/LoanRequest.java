package com.aditya.creditsimulator.model;

import com.aditya.creditsimulator.vehicle.Vehicle;

import java.math.BigDecimal;

/**
 * nampung semua input yang dibutuhin buat 1x kalkulasi
 */
public final class LoanRequest {

    private final Vehicle vehicle;
    private final BigDecimal totalLoanAmount;   // Jumlah Pinjaman Total
    private final int tenorYears;               // Tenor Pinjaman (1-6 thn)
    private final BigDecimal downPayment;       // Jumlah DP

    public LoanRequest(Vehicle vehicle, BigDecimal totalLoanAmount, int tenorYears, BigDecimal downPayment) {
        this.vehicle = vehicle;
        this.totalLoanAmount = totalLoanAmount;
        this.tenorYears = tenorYears;
        this.downPayment = downPayment;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public BigDecimal getTotalLoanAmount() {
        return totalLoanAmount;
    }

    public int getTenorYears() {
        return tenorYears;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    /**
     * Pokok pinjaman awal = Total Pinjaman - DP.
     * Ini "Pokok Pinjaman" tahun 1 di Rumus.xlsx.
     */

    public BigDecimal getPrincipal() {
        return totalLoanAmount.subtract(downPayment); // dikurang
    }

    @Override
    public String toString() {
        return "LoanRequest{" +
                "vehicle=" + vehicle +
                ", totalLoanAmount=" + totalLoanAmount +
                ", tenorYears=" + tenorYears +
                ", downPayment=" + downPayment +
                '}';
    }
}