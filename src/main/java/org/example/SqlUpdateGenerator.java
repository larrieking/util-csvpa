package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads a CSV file and generates SQL UPDATE statements based on specified columns.
 */
public class SqlUpdateGenerator {

    /**
     * Generates SQL UPDATE statements.
     *
     * @param filePath     Path to the CSV file.
     * @param tableName    Target SQL table name.
     * @param setColumns   Array of column names to be updated (SET clause).
     * @param whereColumns Array of column names for the filter (WHERE clause).
     */
    public void generateUpdateStatements(String filePath, String tableName, String[] setColumns, String[] whereColumns) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                System.out.println("CSV file is empty or missing headers.");
                return;
            }

            // Map header names to their column index
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i], i);
            }

            // Verify all specified columns exist in the CSV
            for (String col : setColumns) {
                if (!headerMap.containsKey(col)) {
                    System.err.println("Column not found in CSV: " + col);
                    return;
                }
            }
            for (String col : whereColumns) {
                if (!headerMap.containsKey(col)) {
                    System.err.println("Column not found in CSV: " + col);
                    return;
                }
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                StringBuilder sql = new StringBuilder("UPDATE ");
                sql.append(tableName).append(" SET ");

                // Build SET clause
                for (int i = 0; i < setColumns.length; i++) {
                    String colName = setColumns[i];
                    String value = row[headerMap.get(colName)];
                    sql.append(colName).append(" = '").append(escapeSql(value)).append("'");
                    if (i < setColumns.length - 1) {
                        sql.append(", ");
                    }
                }

                sql.append(" WHERE ");

                // Build WHERE clause
                for (int i = 0; i < whereColumns.length; i++) {
                    String colName = whereColumns[i];
                    String value = row[headerMap.get(colName)];
                    sql.append(colName).append(" = '").append(escapeSql(value)).append("'");
                    if (i < whereColumns.length - 1) {
                        sql.append(" AND ");
                    }
                }

                sql.append(";");
                System.out.println(sql.toString());
            }

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    private String escapeSql(String value) {
        if (value == null) return "";
        return value.replace("'", "''");
    }

    public static void main(String[] args) {
        // Example usage based on mdas_to_update.csv
        String filePath = "src/main/resources/csv/mdas_to_update.csv";
        String tableName = "mda_collections";
        
        // Columns to update
        String[] setColumns = {"cbn_internal_account_number", "nuban"};
        
        // Columns for the WHERE clause
        String[] whereColumns = {"mda_code", "cbn_collection_code"};

        SqlUpdateGenerator generator = new SqlUpdateGenerator();
        generator.generateUpdateStatements(filePath, tableName, setColumns, whereColumns);
    }
}
