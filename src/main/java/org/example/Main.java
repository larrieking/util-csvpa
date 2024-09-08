package org.example;

import com.opencsv.CSVReader;

import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get(
                ClassLoader.getSystemResource("csv/refs.csv").toURI()
    );

        List<String[]>data = readLineByLine(path);
        data.forEach(o -> {
            System.out.println("('" +o[0]+"'),");
        });
////
////     report service
//        data.forEach(o -> {
//            System.out.println("update transaction_settlement set `amount` = '"+o[10]+"' where trans_ref = '"+o[0]+"' and `settlement_type` = 2;");
//        });
//        System.out.println(data.size());
//
//

//
////        //payment service
//        data.forEach(o -> {
//            System.out.println("UPDATE `payment-processing-service`.`transaction` SET `service_code` = '"+o[6]+"' WHERE (`trans_ref` = '"+o[1]+"' AND `service_code` = '"+o[8]+"');");
//        });

////        //settlement service transactions
//        data.forEach(o -> {
//            System.out.println("DELETE FROM `settlement-service`.`transaction` WHERE (`trans_ref` = '"+o[1]+"' AND `service_code` = '"+o[8]+"');");
//        });


//        data.forEach(o -> {
//            System.out.println("UPDATE `settlement-service`.`transaction` SET `SERVICE_CODE` = '"+o[6]+"' WHERE (`TRANS_REF` = '"+o[1]+"' AND `SERVICE_CODE` = '"+o[8]+"');");
//        });


//       settlement service transactions settlement
//        data.forEach(o -> {
//            System.out.println("DELETE FROM `settlement-service`.`transaction_settlement` WHERE (`trans_ref` = '"+o[1]+"');");
//       });
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