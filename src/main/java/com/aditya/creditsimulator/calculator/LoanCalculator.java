package com.aditya.creditsimulator.calculator;

import com.aditya.creditsimulator.model.InstallmentResult;
import com.aditya.creditsimulator.model.LoanRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Metode "declining balance": tiap tahun, pokok pinjaman yang dipakai buat hitung bunga itu SISA dari tahun sebelumnya dan rate-nya naik tiap tahun sesuai InterestRateCalculator
 * Formula per tahun ke-n (n = 1..tenorYears)

 *   rate(n)              = InterestRateCalculator.calculateRateForYear(baseRate, n)
 *   totalWithInterest(n) = pokok(n) * (1 + rate(n))
 *   remainingMonths(n)   = (tenorYears - n + 1) * 12
 *   installmentMonthly(n)= totalWithInterest(n) / remainingMonths(n)
 *   installmentYearly(n) = installmentMonthly(n) * 12
 *   pokok(n+1)           = totalWithInterest(n) - installmentYearly(n)

 * dengan pokok(1) = totalLoanAmount - downPayment.

 * Contoh verifikasi (dari Rumus.xlsx, Mobil Baru, pinjaman 100jt, DP 25%, tenor 3 tahun):
 *   Tahun 1: pokok=75,000,000  rate=8.0%   total=81,000,000     bulanan=2,250,000.00
 *   Tahun 2: pokok=54,000,000  rate=8.1%   total=58,374,000     bulanan=2,432,250.00
 *   Tahun 3: pokok=29,187,000  rate=8.6%   total=31,697,082     bulanan=2,641,423.50
 */
public class LoanCalculator {

    private static final int MONTHS_PER_YEAR = 12;
    private static final int MONEY_SCALE = 2;

    private final InterestRateCalculator rateCalculator;

    public LoanCalculator(InterestRateCalculator rateCalculator) {
        this.rateCalculator = rateCalculator;
    }


     //hitung breakdown cicilan buat seluruh tenor, satu entry per tahunn
    public List<InstallmentResult> calculate(LoanRequest request) {
        List<InstallmentResult> results = new ArrayList<>();

        BigDecimal baseRate = request.getVehicle().getType().getBaseRate();
        int tenorYears = request.getTenorYears();
        BigDecimal remainingPrincipal = request.getPrincipal();

        for (int year = 1; year <= tenorYears; year++) {
            BigDecimal rate = rateCalculator.calculateRateForYear(baseRate, year);

            BigDecimal totalWithInterest = remainingPrincipal
                    .multiply(BigDecimal.ONE.add(rate))
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            int remainingMonths = (tenorYears - year + 1) * MONTHS_PER_YEAR;

            BigDecimal installmentMonthly = totalWithInterest
                    .divide(BigDecimal.valueOf(remainingMonths), MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal installmentYearly = installmentMonthly
                    .multiply(BigDecimal.valueOf(MONTHS_PER_YEAR))
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            results.add(new InstallmentResult(
                    year, remainingPrincipal, rate, totalWithInterest,
                    installmentMonthly, installmentYearly
            ));

            // Pokok tahun berikutnya = sisa setelah dikurangi cicilan tahun ini
            remainingPrincipal = totalWithInterest.subtract(installmentYearly);
        }

        return results;
    }

     // kalau cuma butuh angka cicilan bulanan tahun pertama saja
    public BigDecimal getFirstYearMonthlyInstallment(LoanRequest request) {
        List<InstallmentResult> results = calculate(request);
        return results.get(0).getInstallmentMonthly();
    }
}