package com.aditya.creditsimulator.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Client untk manggil endpoint loan exixting calculation, hasil raw JSON string
 * parsingnya di JsonLoanResponseMapper
 */
public class LoanApiClient {

    private final HttpClient httpClient;
    private final String endpointUrl;

    public LoanApiClient(HttpClient httpClient, String endpointUrl) {
        this.httpClient = httpClient;
        this.endpointUrl = endpointUrl;
    }

    public String fetchLoanDataJson() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Gagal mengambil data dari API. Status code: " + response.statusCode());
        }

        return response.body();
    }
}