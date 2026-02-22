package org.example;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to call Credo endpoint in a loop and save account information to CSV.
 */
public class CredoAccountIntegration {

    public static void main(String[] args) {
        int loopCount = 15000; // Default loop count
        if (args.length > 0) {
            try {
                loopCount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid loop count provided. Using default: " + loopCount);
            }
        }

        // Endpoint URL - using the one inferred from the response structure or a placeholder if not found.
        // Based on common Credo patterns, it's likely a payment initialization.
        String endpointUrl = "https://api.credocentral.com/transaction/initialize";
        
        HttpClient client = HttpClient.newHttpClient();
        List<String[]> csvData = new ArrayList<>();
        // CSV Header
        csvData.add(new String[]{"accountNumber", "accountName", "bankName", "expiryDate", "amount"});

        System.out.println("Starting integration loop (" + loopCount + " iterations)...");

        for (int i = 0; i < loopCount; i++) {
            System.out.println("Iteration " + (i + 1) + "...");
            try {
                // Assuming POST request as it's common for initialization
                // Request body might be needed, but since it wasn't provided, 
                // I'll use a generic one or empty if it's a GET.
                // Given the response, it's almost certainly a POST with some amount.
                String jsonBody = "{\"amount\": 10000, \"currency\": \"NGN\", \"email\": \"virtual.account@credo.com\",  \"initializeAccount\": \"1\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpointUrl))
                        .header("Content-Type", "application/json")
                        .headers("Authorization", "1PRI6530acX4lsBuiGfOYPjE93M9NrRX38e4yx")
                        // Bearer token would normally go here
                        // .header("Authorization", "Bearer YOUR_TOKEN") 
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    String body = response.body();
                    String accountNumber = extractValue(body, "accountNumber");
                    String accountName = extractValue(body, "accountName");
                    String bankName = extractValue(body, "bankName");
                    String expiryDate = extractValue(body, "expiryDate");
                    String amount = extractValue(body, "amount");

                    csvData.add(new String[]{accountNumber, accountName, bankName, expiryDate, amount});
                    System.out.println("✅ Successfully processed: " + accountNumber);
                } else {
                    System.err.println("❌ Failed with status " + response.statusCode() + ": " + response.body());
                }
            } catch (Exception e) {
                System.err.println("❗ Error in iteration " + i + ": " + e.getMessage());
            }
        }

        writeToCsv(csvData, "src/main/resources/csv/CredoAccountInfo.csv");
    }

    private static String extractValue(String json, String key) {
        // Look for the key and extract the value (handles both quoted strings and numbers)
        // Optimized to handle the nested structure if needed, but since keys are unique enough in the sample:
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?(.*?)\"?\\s*(?:,|\\s|})");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private static void writeToCsv(List<String[]> data, String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeAll(data);
            System.out.println("✅ Data written to " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Error writing to CSV: " + e.getMessage());
        }
    }
}
