package com.aditya.creditsimulator.io;

import java.util.Scanner;

public class ConsoleInputReader implements InputReader {

    private final Scanner scanner;

    public ConsoleInputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public RawLoanInput readLoanInput() {
        System.out.print("Jenis Kendaraan (Motor/Mobil): ");
        String vehicleType = scanner.nextLine().trim();

        System.out.print("Kondisi Kendaraan (Bekas/Baru): ");
        String vehicleCondition = scanner.nextLine().trim();

        System.out.print("Tahun Kendaraan: ");
        String vehicleYear = scanner.nextLine().trim();

        System.out.print("Jumlah Pinjaman Total: ");
        String totalLoanAmount = scanner.nextLine().trim();

        System.out.print("Tenor Pinjaman (1-6 tahun): ");
        String tenorYears = scanner.nextLine().trim();

        System.out.print("Jumlah DP: ");
        String downPayment = scanner.nextLine().trim();

        return new RawLoanInput(vehicleType, vehicleCondition, vehicleYear,
                totalLoanAmount, tenorYears, downPayment);
    }
}