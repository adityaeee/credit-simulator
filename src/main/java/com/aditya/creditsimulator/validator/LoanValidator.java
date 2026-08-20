package com.aditya.creditsimulator.validator;

import com.aditya.creditsimulator.model.LoanRequest;
import com.aditya.creditsimulator.vehicle.Vehicle;

import java.math.BigDecimal;

/**

 * Rule yang di-enforce:
 *   - jumlah pinjaman Total <= 1 miliar
 *   - Tenor Pinjaman 1-6 tahun
 *   - DP Mobil/Motor Baru  >= 35% dari Jumlah Pinjaman
 *   - DP Mobil/Motor Bekas >= 25% dari Jumlah Pinjaman

 *   - Jumlah Pinjaman Total harus > 0
 *   - DP tidak boleh lebih besar atau sama dengan Jumlah Pinjaman Total
 */
public class LoanValidator {

    private static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("1000000000"); // 1 miliar
    private static final int MIN_TENOR_YEARS = 1;
    private static final int MAX_TENOR_YEARS = 6;

    public void validate(LoanRequest request) {
        validateLoanAmount(request.getTotalLoanAmount());
        validateTenor(request.getTenorYears());
        validateDownPayment(request);
    }

    private void validateLoanAmount(BigDecimal totalLoanAmount) {
        if (totalLoanAmount == null || totalLoanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Jumlah pinjaman harus lebih besar dari 0.");
        }
        if (totalLoanAmount.compareTo(MAX_LOAN_AMOUNT) > 0) {
            throw new IllegalArgumentException("Jumlah pinjaman tidak boleh lebih dari Rp " + MAX_LOAN_AMOUNT + " (1 miliar).");
        }
    }

    private void validateTenor(int tenorYears) {
        if (tenorYears < MIN_TENOR_YEARS || tenorYears > MAX_TENOR_YEARS) {
            throw new IllegalArgumentException(
                    "Tenor pinjaman harus antara " + MIN_TENOR_YEARS + "-" + MAX_TENOR_YEARS + " tahun. Input: " + tenorYears);
        }
    }

    private void validateDownPayment(LoanRequest request) {
        BigDecimal downPayment = request.getDownPayment();
        BigDecimal totalLoanAmount = request.getTotalLoanAmount();
        Vehicle vehicle = request.getVehicle();

        if (downPayment == null || downPayment.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Jumlah DP tidak boleh negatif.");
        }

        if (downPayment.compareTo(totalLoanAmount) >= 0) {
            throw new IllegalArgumentException(
                    "Jumlah DP tidak boleh lebih besar atau sama dengan jumlah pinjaman total.");
        }

        BigDecimal minimumDownPaymentRate = vehicle.getCondition().getMinimumDownPaymentRate();
        BigDecimal minimumDownPayment = totalLoanAmount.multiply(minimumDownPaymentRate);

        if (downPayment.compareTo(minimumDownPayment) < 0) {
            throw new IllegalArgumentException(
                    "DP untuk kendaraan " + vehicle.getCondition().name()
                            + " minimal " + minimumDownPaymentRate.multiply(BigDecimal.valueOf(100)) + "% "
                            + "dari jumlah pinjaman (minimal Rp " + minimumDownPayment + "). "
                            + "DP yang diinput: Rp " + downPayment);
        }
    }
}