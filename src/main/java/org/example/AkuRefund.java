package org.example;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AkuRefund {

    public static void main(String[] args) throws Exception {

        Path path = Paths.get(
                ClassLoader.getSystemResource("csv/Result_9.csv").toURI()
        );

        List<String[]> data = readLineByLine(path);

        List<RefundRequest> requests = new ArrayList<>();

        // Assumes header row is present
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 4) {
                String reference = row[0].trim();       // merchant_ref
                String privateKey = row[3].trim();      // live_private_key
                requests.add(new RefundRequest(reference, privateKey));
            }
        }

        HttpClient client = HttpClient.newHttpClient();

        for (RefundRequest request : requests) {
            String url = "https://api.akupay.africa/transaction/refund/" + request.reference();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", request.privateKey())
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            try {
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.out.println("✅ Refunded: " + request.reference() + " → " + response.statusCode());
                } else {
                    System.out.println("❌ Failed: " + request.reference() + " → " + response.statusCode() + " - " + response.body());
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("❗ Error on " + request.reference() + ": " + e.getMessage());
            }
        }
    }

    public record RefundRequest(String reference, String privateKey) {}


    public static List<String[]> readLineByLine(Path filePath) throws Exception {
        List<String[]> list = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(filePath)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    list.add(line);
                }
            }
        }
        return list;
    }
}
