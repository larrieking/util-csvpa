package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SessionIdReader {

    public static void main(String[] args) {
        String inputFilePath = "src/main/resources/csv/session_ids.csv";
        String outputFilePath = "src/main/resources/csv/processed_session_ids.csv";

        List<String[]> data = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(inputFilePath))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                // Here you can add logic to filter or process session IDs
                // For now, we just read and add to the list
                data.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath))) {
            writer.writeAll(data);
            System.out.println("Successfully read from " + inputFilePath + " and wrote to " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
