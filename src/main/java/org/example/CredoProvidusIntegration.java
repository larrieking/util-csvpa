package org.example;

import com.opencsv.CSVWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CredoProvidusIntegration {

    private static final Log log = LogFactory.getLog(CredoProvidusIntegration.class);

    public static void main(String[] args) throws Exception {
        String bearerToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0d29fZmFjdG9yX2VuYWJsZWQiOjAsInVzZXJfZW1haWwiOiJsYW5yZS55dXN1ZkBldHJhbnphY3QuY29tIiwidXNlcl9uYW1lIjoibGFucmUueXVzdWZAZXRyYW56YWN0LmNvbSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sImlzcyI6Imh0dHBzOi8vYXBpLmNyZWRvY2VudHJhbC5jb20iLCJsYXN0X25hbWUiOiJZdXN1ZiIsInR5cCI6IkJlYXJlciIsImF1dGhvcml0aWVzIjpbIjAwMCJdLCJjbGllbnRfaWQiOiJ3ZWItY2xpZW50LXNlcnZpY2UiLCJwcm9kdWN0X2FjY2VzcyI6W3sicHJvZHVjdElkIjoxLCJuYW1lIjoiT2Jpa29iZSBGYXJtcyBMaW1pdGVkIiwiYnVzaW5lc3NDb2RlIjoiNzAwNjA3MDAwMDEwMDAxIiwic3RhdHVzIjowLCJwcm9kdWN0VHlwZU5hbWUiOiJDcmVkbyBDZW50cmFsIiwicHJvZHVjdFR5cGVJZCI6MSwiZW5mb3JjZTJmYSI6MCwiYWNjZXNzVHlwZSI6MSwiYnVzaW5lc3NUeXBlSWQiOjIsInByb2R1Y3RBY2Nlc3NTdGF0dXMiOjEsInVzZXJUeXBlIjoiQURNSU4iLCJlbXBsb3llckNvZGUiOm51bGwsInJvbGVzIjpbIkFkbWluaXN0cmF0b3IiXSwicGVybWlzc2lvbnMiOlsiMjEwNiIsIjIxMDUiLCIyMTA0IiwiMjEwMyIsIjIxMDIiLCIyMTAxIiwiMDgwNiIsIjA4MDciLCIwODA0IiwiMDgwNSIsIjA4MDIiLCIwODAzIiwiMDgwMSIsIjE1MDIiLCIxMzAzIiwiMTUwMSIsIjEzMDIiLCIxMzAxIiwiMTEwMyIsIjExMDIiLCIxMTAxIiwiMTUwOCIsIjE5MDQiLCIxNTA3IiwiMTkwMyIsIjE5MDIiLCIxOTAxIiwiMjIwNyIsIjIyMDYiLCIyMjA1IiwiMjIwNCIsIjIyMDMiLCIyMjAyIiwiMjIwMSIsIjE2MDIiLCIxNDA0IiwiMTYwMSIsIjE0MDMiLCIxNDAyIiwiMTQwMSIsIjEwMDQiLCIxMjAxIiwiMTAwMyIsIjEwMDIiLCIxMDAxIiwiMTQwOSIsIjE0MDgiLCIxODAzIiwiMTQwNyIsIjE2MDQiLCIxODAyIiwiMTQwNiIsIjE2MDMiLCIxODAxIiwiMTQwNSJdfSx7InByb2R1Y3RJZCI6MCwibmFtZSI6bnVsbCwiYnVzaW5lc3NDb2RlIjpudWxsLCJzdGF0dXMiOjAsInByb2R1Y3RUeXBlTmFtZSI6IkNyZWRvIEFkbWluIiwicHJvZHVjdFR5cGVJZCI6MiwiZW5mb3JjZTJmYSI6bnVsbCwiYWNjZXNzVHlwZSI6MSwiYnVzaW5lc3NUeXBlSWQiOm51bGwsInByb2R1Y3RBY2Nlc3NTdGF0dXMiOjEsInVzZXJUeXBlIjoiQURNSU4iLCJlbXBsb3llckNvZGUiOm51bGwsInJvbGVzIjpbIlJlcG9ydGVyIl0sInBlcm1pc3Npb25zIjpbIjAzMDMiLCIwNjAxIiwiMDMwNCIsIjAyMDUiLCIwMzAxIiwiMDIwMiIsIjAzMDIiLCIwMjAzIiwiMDIwMSIsIjA2MDgiLCIwNjA2IiwiMDcwNSIsIjA2MDciLCIwNjA0IiwiMDIwOCIsIjA2MDUiLCIwMjA5IiwiMDYwMiIsIjA3MDEiLCIwNjAzIiwiMDIwNyIsIjAyMTUiLCIwMjE2IiwiMDIxMSIsIjAyMTIiLCIwMjEwIiwiMDIxOCJdfV0sImF1ZCI6WyJtZXJjaGFudC1pbnNpZ2h0cy1zZXJ2aWNlIiwiYWJjLW1pZGRsZXdhcmUtc2VydmljZSIsIm1hcnQtc2VydmljZSIsImV0YXgtbWVyY2hhbnQtc2VydmljZSIsImludm9pY2Utc2VydmljZSIsInBheW1lbnQtcHJvY2Vzc2luZy1zZXJ2aWNlIiwic2V0dGxlbWVudC1zZXJ2aWNlIiwicmVwb3J0LXNlcnZpY2UiLCJjb3JlLXNlcnZpY2UiLCJub3RpZmljYXRpb24tc2VydmljZSJdLCJ1c2VyX2lkIjozMjQ5LCJzY29wZSI6WyJyZWFkIiwid3JpdGUiLCJwcm9maWxlIl0sIm5hbWUiOiJMYW5yZSBZdXN1ZiIsImV4cCI6MTc2ODYxOTQ5NiwiZmlyc3RfbmFtZSI6IkxhbnJlIiwiYnVzaW5lc3NlcyI6W10sImp0aSI6ImMzMjFjMmE3LTNjNzYtNDc1Ny1hM2MwLTQxYjJkOTBjMmU1ZCJ9.SzlLP_qAIK-S8qdlhRPQZXaNZF00hrUSxhtj5Ix6vYTn0YABABj_pDg2WCDZmYTEpLUnDJI92YvHOT0XcCIlvjj4OclzzgeI2QT-bSdE7ctBgdqOEcuzSswms39RMJAQuKIwDhkW6ZiJG41q8VtpMpG2zrYkfd8HIbZxAcBIlUidXZGEiQyj4dxqlmUrvkt9Ys7-oZJN16_ai1fzBq83GYFnRd0rpZp0VDfeUkvh5psAfWSBcFQGC6KHYj_zyKDuZGNFJXPDBEOYuUiF3J3rBp1vNsIAa9FiB8Rd0Zz-wzUycG9Khh69hVsxf7ChkVa2QjKEecPSELf1bUYRF9JZzQ";
        // 1. Prepare Timestamps (Lagos time)
        ZoneId lagosZone = ZoneId.of("Africa/Lagos");
        // Start date: Jan 1st 2026
        long startDate = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, lagosZone).toInstant().toEpochMilli() / 1000;
        // End date: Now
        long endDate = ZonedDateTime.now(lagosZone).toInstant().toEpochMilli() / 1000;

        int page = 1;
        int size = 20000;


        System.out.println("startDate %s, endDate %s".formatted(new Date(startDate*1000), new Date(endDate*1000)));

        String credoUrl = String.format("https://api.credocentral.com/transaction/credits?status=2&startDate=%d&endDate=%d&page=%d&size=%d", startDate, endDate, page, size);
        
        HttpClient client = HttpClient.newHttpClient();

        // 2. Fetch from Credo
        System.out.println("Fetching from Credo: " + credoUrl);
        HttpRequest credoRequest = HttpRequest.newBuilder()
                .uri(URI.create(credoUrl))
                .header("Authorization", "Bearer "+bearerToken) // Token is empty as per issue description
                .GET()
                .build();

        HttpResponse<String> credoResponse = client.send(credoRequest, HttpResponse.BodyHandlers.ofString());
        String credoBody = credoResponse.body();
        System.out.println("Credo Response: " + credoBody);

        List<CredoRecord> records = eddsparseCredoResponse(credoBody);
        
        if (records.isEmpty()) {
            System.out.println("No records found from Credo.");
            return;
        }

        // 3. Process each record and call Providus
        List<String[]> csvData = new ArrayList<>();
        // Header
        csvData.add(new String[]{
            "id", "sessionId", "accountNumber", "sourceAccountNumber", "sourceAccountName", 
            "sourceBankName", "transRef", "status", "businessCode", "debitedAmount", 
            "transactionDate", "accountName", "notification_acknowledgement", "reason"
        });

        for (CredoRecord record : records) {
            String providusUrl = "https://vps.providusbank.com/vps/api/PiPverifyTransaction?session_id=" + record.sessionId();
            System.out.println("Verifying with Providus: " + providusUrl);
            
            HttpRequest providusRequest = HttpRequest.newBuilder()
                    .uri(URI.create(providusUrl))
                    .header("Content-Type", "application/json")
                    // Note: Copying headers from ProvidusVerifyTransaction.java if they are needed, 
                    // though issue description didn't specify them for this new integration.
                    // But usually these endpoints need them.
                    .header("Client-Id", "RVRSQU5aQUNULUNSRUQw")
                    .header("X-Auth-Signature", "f0bee7089c390a4829d98c84d2964dc9346311a3e31920d7aab29e65b3d401e18f39e78547ffa2d9e3f19c26b21a5ce869843f23bde351a873c5db0283ef362f")
                    .GET()
                    .build();

            try {
                HttpResponse<String> providusResponse = client.send(providusRequest, HttpResponse.BodyHandlers.ofString());
                String providusBody = providusResponse.body();
                System.out.println("Providus Response: " + providusBody);

                String notifAck = extractValue(providusBody, "notification_acknowledgement");
                String reason = extractValue(providusBody, "reason");

                csvData.add(new String[]{
                    record.id(), record.sessionId(), record.accountNumber(), record.sourceAccountNumber(),
                    record.sourceAccountName(), record.sourceBankName(), record.transRef(), record.status(),
                    record.businessCode(), record.debitedAmount(), record.transactionDate(), record.accountName(),
                    notifAck, reason
                });
            } catch (Exception e) {
                System.err.println("Error verifying session " + record.sessionId() + ": " + e.getMessage());
                csvData.add(new String[]{
                    record.id(), record.sessionId(), record.accountNumber(), record.sourceAccountNumber(),
                    record.sourceAccountName(), record.sourceBankName(), record.transRef(), record.status(),
                    record.businessCode(), record.debitedAmount(), record.transactionDate(), record.accountName(),
                    "", "Error: " + e.getMessage()
                });
            }
        }

        // 4. Write to CSV
        Path outPath = Paths.get("src/main/resources/csv/Credo_Providus_Integration_Result.csv");
        Files.createDirectories(outPath.getParent());
        try (Writer writer = Files.newBufferedWriter(outPath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeAll(csvData);
            csvWriter.flush();
        }

        System.out.println("✅ Integration completed. Results written to: " + outPath.toAbsolutePath());
    }

    private static List<CredoRecord> eddsparseCredoResponse(String json) {
        List<CredoRecord> records = new ArrayList<>();
        // Minimalist regex parsing for the provided Credo JSON structure
        Pattern objectPattern = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL);
        Matcher matcher = objectPattern.matcher(json);

        while (matcher.find()) {
            String objContent = matcher.group(1);
            records.add(new CredoRecord(
                extractValue(objContent, "id"),
                extractValue(objContent, "sessionId"),
                extractValue(objContent, "accountNumber"),
                extractValue(objContent, "sourceAccountNumber"),
                extractValue(objContent, "sourceAccountName"),
                extractValue(objContent, "sourceBankName"),
                extractValue(objContent, "transRef"),
                extractValue(objContent, "status"),
                extractValue(objContent, "businessCode"),
                extractValue(objContent, "debitedAmount"),
                extractValue(objContent, "transactionDate"),
                extractValue(objContent, "accountName")
            ));
        }
        return records;
    }

    private static String extractValue(String json, String key) {
        // Try string value
        Pattern stringPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"(.*?)\"");
        Matcher stringMatcher = stringPattern.matcher(json);
        if (stringMatcher.find()) {
            return stringMatcher.group(1).replace("\"\"", "\"");
        }

        // Try numeric value
        Pattern numericPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([^,\\s}]+)");
        Matcher numericMatcher = numericPattern.matcher(json);
        if (numericMatcher.find()) {
            return numericMatcher.group(1);
        }

        return "";
    }

    public record CredoRecord(
        String id, String sessionId, String accountNumber, String sourceAccountNumber,
        String sourceAccountName, String sourceBankName, String transRef, String status,
        String businessCode, String debitedAmount, String transactionDate, String accountName
    ) {}
}
