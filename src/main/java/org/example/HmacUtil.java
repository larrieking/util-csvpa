package org.example;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class HmacUtil {

    public static String generateCryptoHash(String jsonBody, String secret, String algorithm) throws Exception {
        // Default to SHA-512 if algorithm not provided
        if (algorithm == null || algorithm.isBlank()) {
            algorithm = "HmacSHA512";
        } else if (!algorithm.startsWith("Hmac")) {
            algorithm = "Hmac" + algorithm.toUpperCase();
        }

        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
        mac.init(secretKey);

        byte[] hashBytes = mac.doFinal(jsonBody.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hashBytes);
    }

    public static void main(String[] args) throws Exception {
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

        String secret = "sample-secret";
        String hash = generateCryptoHash(json, secret, "sha512");

        System.out.println("hash = " + hash);
    }
}
