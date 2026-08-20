package com.aditya.creditsimulator.vehicle;

import java.time.Year;

/**
 *   - tahun harus 4 digit numeric - jaga2
 *   - kalau kondisi BARU, tahun kendaraan tidak boleh kurang dari (currentYear - 1)
 */

public class VehicleFactory {

    public Vehicle create(String rawType, String rawCondition, int year) {
        VehicleType type = VehicleType.fromString(rawType);
        VehicleCondition condition = VehicleCondition.fromString(rawCondition);

        validateYear(year, condition);

        return new Vehicle(type, condition, year);
    }

    private void validateYear(int year, VehicleCondition condition) {
        if (String.valueOf(Math.abs(year)).length() != 4) {
            throw new IllegalArgumentException("Tahun kendaraan harus 4 digit angka: " + year);
        }

        int currentYear = Year.now().getValue();

        if (condition == VehicleCondition.BARU && year < currentYear - 1) {
            throw new IllegalArgumentException(
                    "Kendaraan dengan kondisi BARU tidak boleh tahun kurang dari "
                            + (currentYear - 1) + ". Input tahun: " + year);
        }

        if (year > currentYear) {
            throw new IllegalArgumentException(
                    "Tahun kendaraan tidak boleh lebih dari tahun sekarang (" + currentYear + ").");
        }
    }
}
