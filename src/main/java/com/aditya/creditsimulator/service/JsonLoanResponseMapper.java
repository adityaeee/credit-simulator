package com.aditya.creditsimulator.service;

import com.aditya.creditsimulator.io.RawLoanInput;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parse manual regex, response JSON dari API pakai "loanTenure" ("tenorYears") -> jadi RawLoanInput
 * jadi sumber input ke-3 (pipeline), rute yang sama juga dengan yg lain - LoanRequestFactory -> LoanValidator -> LoanCalculator
 */
public class JsonLoanResponseMapper {

    public RawLoanInput toRawLoanInput(String json) {
        String vehicleType = extractString(json, "vehicleType");
        String vehicleCondition = extractString(json, "vehicleCondition");
        String vehicleYear = extractNumber(json, "vehicleYear");
        String totalLoanAmount = extractNumber(json, "totalLoanAmount");
        String tenorYears = extractNumber(json, "loanTenure");
        String downPayment = extractNumber(json, "downPayment");

        return new RawLoanInput(vehicleType, vehicleCondition, vehicleYear,
                totalLoanAmount, tenorYears, downPayment);
    }

    private String extractString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Field '" + key + "' tidak ditemukan di response API.");
        }
        return matcher.group(1);
    }

    private String extractNumber(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Field '" + key + "' tidak ditemukan di response API.");
        }
        return matcher.group(1);
    }
}