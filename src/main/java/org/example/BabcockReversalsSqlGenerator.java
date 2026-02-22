package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the CSV file "csv/Babcock Reversals.csv" from resources and generates
 * SQL update statements of the form:
 *
 *   update providus_transaction_credit
 *   set STATUS = 1, reversal_message = '<Rvsl Message>'
 *   where trans_ref = '<transRef>' and session_id = '<sessionId>';
 *
 * Output file: src/main/resources/csv/BabcockReversals_update.sql
 */
public class BabcockReversalsSqlGenerator {

    public static void main(String[] args) throws Exception {
        Path csvPath = Paths.get(
                ClassLoader.getSystemResource("csv/Babcock Reversals.csv").toURI()
        );

        List<String[]> rows = readAll(csvPath);
        if (rows.isEmpty()) {
            System.out.println("No rows found in CSV: " + csvPath);
            return;
        }

        String[] header = rows.get(0);
        Map<String, Integer> idx = indexHeader(header);

        // Required columns based on provided header sample
        Integer iSession = idx.get("sessionId");
        Integer iTransRef = idx.get("transRef");
        Integer iRvslMsg = idx.get("Rvsl Message");
        Integer iAckStatus = idx.get("notification ack status");

        if (iSession == null || iTransRef == null || iRvslMsg == null || iAckStatus == null) {
            throw new IllegalStateException("Required columns missing. Needed: sessionId, transRef, Rvsl Message, notification ack status. Found: " + idx.keySet());
        }

        List<String> statements = new ArrayList<>();
        int skipped = 0;
        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            // Guard against ragged rows
            String sessionId = getCell(r, iSession);
            String transRef = getCell(r, iTransRef);
            String rvslMessage = getCell(r, iRvslMsg);
            String ackStatus = getCell(r, iAckStatus);

            // Only generate for rows where notification ack status == 5
            if (!"5".equals(ackStatus)) {
                skipped++;
                continue;
            }

            if (isBlank(sessionId) || isBlank(transRef)) {
                skipped++;
                continue;
            }

            String sql = "update providus_transaction_credit set STATUS = 1, reversal_message = "
                    + quote(rvslMessage)
                    + " where trans_ref = " + quote(transRef)
                    + " and session_id = " + quote(sessionId)
                    + ";";
            statements.add(sql);
        }

        Path outPath = Paths.get("src/main/resources/csv/BabcockReversals_update.sql");
        Files.createDirectories(outPath.getParent());
        try (Writer w = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8)) {
            for (String s : statements) {
                w.write(s);
                w.write(System.lineSeparator());
            }
        }

        System.out.println("Generated " + statements.size() + " SQL statements (skipped: " + skipped + ")");
        System.out.println("Output: " + outPath.toAbsolutePath());
    }

    private static List<String[]> readAll(Path path) throws IOException, CsvValidationException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVReader csv = new CSVReader(reader)) {
            List<String[]> list = new ArrayList<>();
            String[] line;
            while ((line = csv.readNext()) != null) {
                list.add(line);
            }
            return list;
        }
    }

    private static Map<String, Integer> indexHeader(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            map.put(header[i], i);
        }
        return map;
    }

    private static String getCell(String[] row, int idx) {
        return idx < row.length && row[idx] != null ? row[idx].trim() : "";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String quote(String val) {
        if (val == null) val = "";
        // Escape single quotes by doubling them for SQL literals
        String escaped = val.replace("'", "''");
        return "'" + escaped + "'";
    }
}
