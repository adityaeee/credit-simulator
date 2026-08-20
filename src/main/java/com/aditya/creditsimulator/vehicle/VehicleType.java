package com.aditya.creditsimulator.vehicle;

import java.math.BigDecimal;

/**
 * Base Interest Rate:
 *   Mobil = 8%
 *   Motor = 9%
 */

public enum VehicleType {

    MOBIL(new BigDecimal("0.08")),
    MOTOR(new BigDecimal("0.09"));

    private final BigDecimal baseRate;

    VehicleType(BigDecimal baseRate) {
        this.baseRate = baseRate;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public static VehicleType fromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Jenis kendaraan tidak boleh kosong.");
        }
        String normalized = input.trim().toUpperCase();

        for (VehicleType type : VehicleType.values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException(
                "Jenis kendaraan tidak valid: '" + input + "'. Harus 'Mobil' atau 'Motor'.");
    }
}