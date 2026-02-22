package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionIdExtractor {

    public static void main(String[] args) {
        String inputFilePath = "src/main/resources/csv/log_data-5.csv";
        String outputFilePath = "src/main/resources/csv/session_ids.csv";

        // Regex to find the session_id between ' ' and ' for key 'providus_transaction_credit.session_id'
        // or just 'Duplicate entry '([^']+)'
        Pattern pattern = Pattern.compile("Duplicate entry '([^']+)'");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            writer.write("session_id");
            writer.newLine();

            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String sessionId = matcher.group(1);
                    writer.write(sessionId);
                    writer.newLine();
                }
            }
            System.out.println("Extraction completed. Results written to: " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
