package org.example;
import com.opencsv.CSVReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Providus {

    public static void main(String[] args) throws Exception {

        Path path = Paths.get(
                ClassLoader.getSystemResource("csv/pending.csv").toURI()
        );

        List<String[]> data = readLineByLine(path);

        List<String> requests = new ArrayList<>();

        // Assumes header row is present
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 1) {
                String reference = row[0].trim();       // merchant_ref
                requests.add(reference);
            }
        }


        HttpClient client = HttpClient.newHttpClient();

        for (String request : requests) {
            String url = "https://vps.providusbank.com/vps/api/PiP_RepushTransaction_SettlementId";
            String jsonBody = """
    {
        "session_id": "%s"
    }
    """.formatted(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .headers("Client-Id", "RVRSQU5aQUNULUNSRUQw")
                    .header("X-Auth-Signature", "f0bee7089c390a4829d98c84d2964dc9346311a3e31920d7aab29e65b3d401e18f39e78547ffa2d9e3f19c26b21a5ce869843f23bde351a873c5db0283ef362f")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            try {
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.out.println("✅ Verified: " + request + " → " + response.statusCode());
                } else {
                    System.out.println("❌ Failed: " + request + " → " + response.statusCode() + " - " + response.body());
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("❗ Error on " + request + ": " + e.getMessage());
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
