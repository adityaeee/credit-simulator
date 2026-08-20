package com.aditya.creditsimulator.calculator;

import java.math.BigDecimal;

/**
 * menghitung rate bunga yang naik tiap tahun kepemilikan pinjaman

 *   "Interest Year On Year increase 0.5% every 2 year"
 *   "Interest Year On Year increase 0.1% every 1 year"

 *   tahun 1: 8.0%
 *   tahun 2: 8.1%   (+0.1%)
 *   tahun 3: 8.6%   (+0.5%)

 *   - transisi tahun ganjil (1->2, 3->4, 5->6, ...) naik 0.1%
 *   - transisi tahun genap  (2->3, 4->5, ...)       naik 0.5%

 * baseRate  rate dasar dari VehicleType (8% Mobil / 9% Motor)
 * yearIndex tahun ke berapa
 */
public class InterestRateCalculator {

    private static final BigDecimal YEARLY_INCREMENT = new BigDecimal("0.001");   // 0.1%
    private static final BigDecimal BIENNIAL_INCREMENT = new BigDecimal("0.005"); // 0.5%

    public BigDecimal calculateRateForYear(BigDecimal baseRate, int yearIndex) {
        // tahun pertama langsung papek baseRate tanpa kenaikan
        if (yearIndex < 1) {
            throw new IllegalArgumentException("yearIndex harus mulai dari 1, diterima: " + yearIndex);
        }

        BigDecimal rate = baseRate;

        // elapsed = jumlah transisi tahun yang sudah dilewati sebelum yearIndex ini
        for (int elapsed = 1; elapsed < yearIndex; elapsed++) {
            if (elapsed % 2 == 0) {
                rate = rate.add(BIENNIAL_INCREMENT);
            } else {
                rate = rate.add(YEARLY_INCREMENT);
            }
        }

        return rate;
    }
}