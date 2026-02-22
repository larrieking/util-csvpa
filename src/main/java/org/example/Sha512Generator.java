package org.example;

import javax.swing.*;
import java.security.MessageDigest;
import java.util.stream.IntStream;

public class Sha512Generator {
    public static void main(String[] args) {
        try {

            String json = """
{
  "billNumber": "BIL-1751739512",
  "fullName": "Ms. Charlie Fahey",
  "email": "Celestine.Dickens@gmail.com",
  "phoneNumber": "09099302323",
  "reference": "1751740923",
  "status": "paid",
  "bankBranchId": "0580000000",
  "bankId": "034",
  "batchId": "SYS058806368737",
  "channel": "Bank",
  "cbnAcct": "12343433342",
  "collectedAmount": "157.59",
  "fee": "2",
  "itemCode": "asas",
  "itemName": "asasas",
  "locationCode": "2456",
  "locationName": "ABAJI LOCAL GOVERNMENT",
  "mdaCode": "0220008001000",
  "mdaName": "FEDERAL INLAND REVENUE SERVICE",
  "narrationDesc": "Rancellteller#amt:3157.5MDAFEDERALINLANDREVENUESERVICE(FIRS)-0220008001000",
  "feedDate": "2025-07-05T18:56:21.627Z",
  "payColDate": "1751740923",
  "remittedAmount": "78.09",
  "meta": {},
  "gifmisCode": "122w",
  "settlementRef": "3356767453",
  "sysDate": "2025-07-05T18:56:21.627Z",
  "tsaPcCodeName": "string",
  "res3": "4NQO356SS7U6EFJOACTA",
  "whoPays": "",
  "hasPassedTsq": true,
  "isSettled": true
}
""";

            String secret = "dev_test_sk_18f5e43f9dec41ee9843779f2f37693c";
            String pk = "dev_test_pk_56b237cde73b4ef4a88d7fcc8160951b";
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] data = md.digest((json + "").getBytes());
            StringBuilder sb = new StringBuilder();
            IntStream.rangeClosed(0, data.length-1).forEach(x ->{
                sb.append(Integer.toString((data[x] & 0xff) + 0x100, 16).substring(1));
            });

            System.out.println(sb.toString());

        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }
}
