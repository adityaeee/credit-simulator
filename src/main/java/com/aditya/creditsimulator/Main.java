package com.aditya.creditsimulator;

import com.aditya.creditsimulator.calculator.InterestRateCalculator;
import com.aditya.creditsimulator.calculator.LoanCalculator;
import com.aditya.creditsimulator.io.ConsoleInputReader;
import com.aditya.creditsimulator.io.FileInputReader;
import com.aditya.creditsimulator.io.RawLoanInput;
import com.aditya.creditsimulator.model.InstallmentResult;
import com.aditya.creditsimulator.model.LoanRequest;
import com.aditya.creditsimulator.model.LoanRequestFactory;
import com.aditya.creditsimulator.service.JsonLoanResponseMapper;
import com.aditya.creditsimulator.service.LoanApiClient;
import com.aditya.creditsimulator.validator.LoanValidator;
import com.aditya.creditsimulator.vehicle.VehicleFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;


// manual dependency injection

public class Main {

    private static final String LOAD_EXISTING_ENDPOINT =
            "https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e212003666c";

    // Pakai Locale.US secara eksplisit (bukan Locale default sistem) supaya
    // format angka konsisten (koma buat ribuan, titik buat desimal) di
    // mesin manapun program ini dijalankan, nggak tergantung locale OS
    // reviewer.
    private static final DecimalFormat CURRENCY_FORMAT =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));

    // --- Composition root: rakit semua dependency di sini ---
    private final VehicleFactory vehicleFactory = new VehicleFactory();
    private final LoanRequestFactory loanRequestFactory = new LoanRequestFactory(vehicleFactory);
    private final LoanValidator loanValidator = new LoanValidator();
    private final LoanCalculator loanCalculator = new LoanCalculator(new InterestRateCalculator());
    private final LoanApiClient apiClient = new LoanApiClient(HttpClient.newHttpClient(), LOAD_EXISTING_ENDPOINT);
    private final JsonLoanResponseMapper jsonMapper = new JsonLoanResponseMapper();

    public static void main(String[] args) {
        Main app = new Main();
        if (args.length > 0) {
            app.runWithFileInput(args[0]);
        } else {
            app.runInteractiveMenu();
        }
    }

    // ---------- Mode: file input (non-interaktif) ----------

    private void runWithFileInput(String filePath) {
        try {
            RawLoanInput raw = new FileInputReader(Path.of(filePath)).readLoanInput();
            processAndPrint(raw);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------- Mode: interaktif (menu) ----------

    private void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=== Credit Simulator ===");

        while (running) {
            printMenu();
            System.out.print("Pilih menu: ");
            String choice = scanner.nextLine().trim();
            System.out.println();

            switch (choice) {
                case "1":
                    handleNewCalculation(scanner);
                    break;
                case "2":
                    handleLoadExisting();
                    break;
                case "3":
                    printAvailableCommands();
                    break;
                case "4":
                    running = false;
                    System.out.println("Terima kasih sudah menggunakan Credit Simulator.");
                    break;
                default:
                    System.out.println("Pilihan tidak valid, coba lagi.\n");
            }
        }

        scanner.close();
    }

    private void printMenu() {
        System.out.println("1. New Calculation");
        System.out.println("2. Load Existing Calculation");
        System.out.println("3. Show Commands");
        System.out.println("4. Exit");
    }

    private void printAvailableCommands() {
        System.out.println("--- Available Commands ---");
        System.out.println("1 / New Calculation      : hitung cicilan pinjaman baru dari input manual");
        System.out.println("2 / Load Existing        : ambil data pinjaman dari API dan hitung otomatis");
        System.out.println("3 / Show Commands         : tampilkan daftar perintah ini");
        System.out.println("4 / Exit                  : keluar dari aplikasi");
        System.out.println();
    }

    private void handleNewCalculation(Scanner scanner) {
        try {
            RawLoanInput raw = new ConsoleInputReader(scanner).readLoanInput();
            processAndPrint(raw);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    private void handleLoadExisting() {
        try {
            String json = apiClient.fetchLoanDataJson();
            RawLoanInput raw = jsonMapper.toRawLoanInput(json);
            System.out.println("Data berhasil diambil dari API:");
            processAndPrint(raw);
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: gagal menghubungi API. " + e.getMessage() + "\n");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    // ---------- Pipeline bersama, dipakai oleh ketiga sumber input ----------

    private void processAndPrint(RawLoanInput raw) {
        LoanRequest request = loanRequestFactory.create(raw);
        loanValidator.validate(request);

        List<InstallmentResult> results = loanCalculator.calculate(request);
        printResults(request, results);
    }

    private void printResults(LoanRequest request, List<InstallmentResult> results) {
        System.out.println();
        System.out.println("--- Hasil Simulasi Kredit ---");
        System.out.println("Kendaraan     : " + request.getVehicle());
        System.out.println("Pokok Awal    : Rp " + formatCurrency(request.getPrincipal()));
        System.out.println();

        for (InstallmentResult result : results) {
            System.out.println("Tahun " + result.getYear() + ":");
            System.out.println("  Pokok Pinjaman     : Rp " + formatCurrency(result.getStartingPrincipal()));
            System.out.println("  Rate               : " + formatPercentage(result.getRate()));
            System.out.println("  Total Pinjaman     : Rp " + formatCurrency(result.getTotalWithInterest()));
            System.out.println("  Cicilan per Bulan  : Rp " + formatCurrency(result.getInstallmentMonthly()));
            System.out.println("  Cicilan per Tahun  : Rp " + formatCurrency(result.getInstallmentYearly()));
            System.out.println();
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    private String formatPercentage(BigDecimal rate) {
        return CURRENCY_FORMAT.format(rate.multiply(BigDecimal.valueOf(100))) + "%";
    }
}