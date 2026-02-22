package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvToSqlGenerator {

    public static void main(String[] args) {
        String csvFileName = "csv/pending_mda_inserts.csv";
        String tableName = "mda_collections";

        try {
            generateInsertQueries(csvFileName, tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generateInsertQueries(String resourcePath, String tableName) throws IOException, CsvValidationException {
        InputStream inputStream = CsvToSqlGenerator.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + resourcePath);
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                System.out.println("CSV file is empty.");
                return;
            }

            String columns = Stream.of(headers)
                    .collect(Collectors.joining(", "));

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                StringBuilder values = new StringBuilder();
                for (int i = 0; i < nextLine.length; i++) {
                    String header = (i < headers.length) ? headers[i] : "";
                    if (header.equalsIgnoreCase("created_date") || header.equalsIgnoreCase("last_modified_date")) {
                        values.append("now()");
                    } else {
                        values.append(escapeSqlValue(nextLine[i]));
                    }
                    if (i < nextLine.length - 1) {
                        values.append(", ");
                    }
                }

                String sql = String.format("INSERT INTO %s (%s) VALUES (%s);", tableName, columns, values.toString());
                System.out.println(sql);
            }
        }
    }

    private static String escapeSqlValue(String value) {
        if (value == null || value.equalsIgnoreCase("null")) {
            return "NULL";
        }
        // Basic escaping: replace ' with '' and wrap in '
        return "'" + value.replace("'", "''") + "'";
    }
}
