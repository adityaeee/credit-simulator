package com.aditya.creditsimulator.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Baca input dari file .txt. Format file file_inputs.txt:
 *   Mobil
 *   Baru
 *   2025
 *   100000000
 *   3
 *   25000000
 */

public class FileInputReader implements InputReader {

    private static final int EXPECTED_LINE_COUNT = 6;

    private final Path filePath;

    public FileInputReader(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public RawLoanInput readLoanInput() {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Gagal membaca file input: " + filePath + ". Pesan error: " + e.getMessage(), e);
        }

        if (lines.size() < EXPECTED_LINE_COUNT) {
            throw new IllegalArgumentException(
                    "File input '" + filePath + "' harus punya " + EXPECTED_LINE_COUNT
                            + " baris (Jenis Kendaraan, Kondisi, Tahun, Jumlah Pinjaman, Tenor, DP). "
                            + "Ditemukan cuman " + lines.size() + " baris.");
        }

        return new RawLoanInput(
                lines.get(0).trim(),
                lines.get(1).trim(),
                lines.get(2).trim(),
                lines.get(3).trim(),
                lines.get(4).trim(),
                lines.get(5).trim()
        );
    }
}