package com.aditya.creditsimulator.vehicle;

import java.math.BigDecimal;

/**
 *   Jumlah DP Mobil/Motor Baru  >= 35% Jumlah Pinjaman
 *   Jumlah DP Mobil/Motor Lama  >= 25% Jumlah Pinjaman
 *
 *   * "Input Kendaraan Bekas|Baru - Alphabet, Ignore Cased".
 */

public enum VehicleCondition {

    BARU(new BigDecimal("0.35")),
    BEKAS(new BigDecimal("0.25"));

    private final BigDecimal minimumDownPaymentRate;

    VehicleCondition(BigDecimal minimumDownPaymentRate) {
        this.minimumDownPaymentRate = minimumDownPaymentRate;
    }

    public BigDecimal getMinimumDownPaymentRate() {
        return minimumDownPaymentRate;
    }


    public static VehicleCondition fromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Kondisi kendaraan tidak boleh kosong.");
        }
        String normalized = input.trim().toUpperCase();
        for (VehicleCondition condition : VehicleCondition.values()) {
            if (condition.name().equals(normalized)) {
                return condition;
            }
        }
        throw new IllegalArgumentException(
                "Kondisi kendaraan tidak valid: '" + input + "'. Harus 'Baru' atau 'Bekas'.");
    }
}