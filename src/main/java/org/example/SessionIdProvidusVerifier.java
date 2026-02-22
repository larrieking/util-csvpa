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

public class SessionIdProvidusVerifier {

    public static void main(String[] args) throws Exception {

        String inputFilePath = "src/main/resources/csv/session_ids.csv";
        Path path = Paths.get(inputFilePath);

        if (!Files.exists(path)) {
            System.err.println("Input file not found: " + inputFilePath);
            return;
        }

        List<String[]> data = readLineByLine(path);

        List<String> sessionIds = new ArrayList<>();
        List<String[]> results = new ArrayList<>();
        // header for output CSV
        results.add(new String[]{"session_id", "notification_acknowledgement_status", "reason", "full_response"});

        // Assuming first line is header "session_id"
        int startIndex = 0;
        if (data.size() > 0 && data.get(0)[0].equalsIgnoreCase("session_id")) {
            startIndex = 1;
        }

        for (int i = startIndex; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length >= 1) {
                String sessionId = row[0].trim();
                if (!sessionId.isEmpty()) {
                    sessionIds.add(sessionId);
                }
            }
        }

        HttpClient client = HttpClient.newHttpClient();

        for (String sessionId : sessionIds) {
            String url = "https://vps.providusbank.com/vps/api/PiPverifyTransaction?session_id=" + sessionId;

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
                    System.out.println("✅ Verified: " + sessionId + " → " + status);
                } else {
                    System.out.println("❌ Failed: " + sessionId + " → " + status);
                }
                
                String notif = extractNotificationAck(body);
                String reason = extractReason(body);
                results.add(new String[]{sessionId, notif, reason, body});
            } catch (IOException | InterruptedException e) {
                System.err.println("❗ Error on " + sessionId + ": " + e.getMessage());
                results.add(new String[]{sessionId, "ERROR", e.getMessage(), ""});
            }
        }

        // Write results to CSV
        Path outPath = Paths.get("src/main/resources/csv/session_ids_verification_results.csv");
        Files.createDirectories(outPath.getParent());
        try (Writer writer = Files.newBufferedWriter(outPath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeAll(results);
            csvWriter.flush();
        }

        System.out.println("✅ Results written to: " + outPath.toAbsolutePath());
    }

    private static String extractNotificationAck(String json) {
        if (json == null) return "";
        // Match both quoted and unquoted digits since the requirement was "dont match only digits"
        Pattern p = Pattern.compile("\"notification_acknowledgement\"\\s*:\\s*\"?([^\",\\s}]+)\"?");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private static String extractReason(String json) {
        if (json == null) return "";
        Pattern p = Pattern.compile("\"reason\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (m.find()) {
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
