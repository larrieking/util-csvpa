package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProvidusVerifyTransaction {

    public static void main(String[] args) throws Exception {

        Path path = Paths.get(
                ClassLoader.getSystemResource("csv/trx.csv").toURI()
        );

        List<String[]> data = readLineByLine(path);

        List<String> requests = new ArrayList<>();
        List<String[]> results = new ArrayList<>();
        // header for output CSV
        results.add(new String[]{"session_id","notification_acknowledgement_status","reason"});

        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 1) {
                String reference = row[0].trim();       // session_id
                if (!reference.isEmpty()) {
                    requests.add(reference);
                }
            }
        }

        HttpClient client = HttpClient.newHttpClient();

        for (String request : requests) {
            String url = "https://vps.providusbank.com/vps/api/PiPverifyTransaction?session_id=" + request;

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .headers("Client-Id", "RVRSQU5aQUNULUNSRUQw")
                    .header("X-Auth-Signature", "f0bee7089c390a4829d98c84d2964dc9346311a3e31920d7aab29e65b3d401e18f39e78547ffa2d9e3f19c26b21a5ce869843f23bde351a873c5db0283ef362f")
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    System.out.println(body);
                    System.out.println("✅ Verified: " + request + " → " + status);
                } else {
                    System.out.println("❌ Failed: " + request + " → " + status + " - " + body);
                }
                // Extract only notification_acknowledgement and reason
                String notif = extractNotificationAck(body);
                String reason = extractReason(body);
                String transRemarks = extractRemarks(body);
                results.add(new String[]{request, notif, reason, transRemarks});
            } catch (IOException | InterruptedException e) {
                System.err.println("❗ Error on " + request + ": " + e.getMessage());
                // On error, persist blanks for the 2 desired fields
                results.add(new String[]{request, "", ""});
            }
        }

        // Write results to CSV after verifying all
        Path outPath = Paths.get("src/main/resources/csv/Result_providus_verify.csv");
        Files.createDirectories(outPath.getParent());
        try (Writer writer = Files.newBufferedWriter(outPath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeAll(results);
            csvWriter.flush();
        }

        System.out.println("✅ Results written to: " + outPath.toAbsolutePath());
    }

    public record RefundRequest(String reference, String privateKey) {}

    // Extracts notification_acknowledgement numeric value from JSON-like response
    private static String extractNotificationAck(String json) {
        if (json == null) return "";
        Pattern p = Pattern.compile("\"notification_acknowledgement\"\\s*:\\s*\"?([^\",\\s}]+)\"?");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    // Extracts reason string value from JSON-like response
    private static String extractReason(String json) {
        if (json == null) return "";
        Pattern p = Pattern.compile("\"reason\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (m.find()) {
            // Unescape any doubled quotes from CSV-style printing
            return m.group(1).replace("\"\"", "\"");
        }
        return "";
    }

    private static String extractRemarks(String json) {
        if (json == null) return "";
        Pattern p = Pattern.compile("\"tranRemarks\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (m.find()) {
            // Unescape any doubled quotes from CSV-style printing
            return m.group(1).replace("\"\"", "\"");
        }
        return "";
    }

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
