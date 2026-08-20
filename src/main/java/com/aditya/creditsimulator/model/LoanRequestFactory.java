package com.aditya.creditsimulator.model;

import com.aditya.creditsimulator.io.RawLoanInput;
import com.aditya.creditsimulator.vehicle.Vehicle;
import com.aditya.creditsimulator.vehicle.VehicleFactory;

import java.math.BigDecimal;

/**
 * Factory Pattern: buat convert RawLoanInput - semua masih String, dari console ATAU file
 */
public class LoanRequestFactory {

    private final VehicleFactory vehicleFactory;

    public LoanRequestFactory(VehicleFactory vehicleFactory) {
        this.vehicleFactory = vehicleFactory;
    }

    public LoanRequest create(RawLoanInput raw) {
        int vehicleYear = parseYear(raw.getVehicleYear());

        // VehicleFactory yang handle validasi jenis/kondisi/tahun kendaraan
        Vehicle vehicle = vehicleFactory.create(raw.getVehicleType(), raw.getVehicleCondition(), vehicleYear);

        BigDecimal totalLoanAmount = parseAmount(raw.getTotalLoanAmount(), "Jumlah Pinjaman Total");
        int tenorYears = parseTenor(raw.getTenorYears());
        BigDecimal downPayment = parseAmount(raw.getDownPayment(), "Jumlah DP");

        return new LoanRequest(vehicle, totalLoanAmount, tenorYears, downPayment);
    }

    private int parseYear(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Tahun kendaraan harus berupa angka 4 digit, ditemukan: '" + raw + "'");
        }
    }

    private int parseTenor(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Tenor pinjaman harus berupa angka, ditemukan: '" + raw + "'");
        }
    }

    private BigDecimal parseAmount(String raw, String fieldName) {
        try {
            BigDecimal amount = new BigDecimal(raw.trim());
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(fieldName + " tidak boleh negatif: " + raw);
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    fieldName + " harus berupa angka, ditemukan: '" + raw + "'");
        }
    }
}