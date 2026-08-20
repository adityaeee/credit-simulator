package com.aditya.creditsimulator.io;

/**
 * Main.java cuma kenal interface ini,tanpa tau implementasinya dari console atau file, mudah switch antara :
 *   $ ./credit_simulator                 -> pakai ConsoleInputReader
 *   $ ./credit_simulator file_inputs.txt  -> pakai FileInputReader
 * tanpa Main.java perlu tau detail perbedaannya
 */
public interface InputReader {
    RawLoanInput readLoanInput();
}