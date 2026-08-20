package com.aditya.creditsimulator.io;

/**
 * kumpulan data mentah dair user, masih string semua
 * parsing dan validasi dilakukan di satu tempat (LoanRequestFactory),
 * supaya ConsoleInputReader dan FileInputReader jadi nggak perlu duplikat logic parsing masing2
 */
public final class RawLoanInput {

    private final String vehicleType;
    private final String vehicleCondition;
    private final String vehicleYear;
    private final String totalLoanAmount;
    private final String tenorYears;
    private final String downPayment;

    public RawLoanInput(String vehicleType, String vehicleCondition, String vehicleYear,
                        String totalLoanAmount, String tenorYears, String downPayment) {
        this.vehicleType = vehicleType;
        this.vehicleCondition = vehicleCondition;
        this.vehicleYear = vehicleYear;
        this.totalLoanAmount = totalLoanAmount;
        this.tenorYears = tenorYears;
        this.downPayment = downPayment;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getVehicleCondition() {
        return vehicleCondition;
    }

    public String getVehicleYear() {
        return vehicleYear;
    }

    public String getTotalLoanAmount() {
        return totalLoanAmount;
    }

    public String getTenorYears() {
        return tenorYears;
    }

    public String getDownPayment() {
        return downPayment;
    }
}