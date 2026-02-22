package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opencsv.CSVWriter;
import org.springframework.web.client.RestClient;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CredoAccountService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        int numberOfLoops = 1;
        if (args.length > 0) {
            try {
                numberOfLoops = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid loop count, using default 1");
            }
        }

        List<Account> accounts = new ArrayList<>();
        RestClient restClient = RestClient.create();

        for (int i = 0; i < numberOfLoops; i++) {
            try {
                System.out.println("Executing loop " + (i + 1) + " of " + numberOfLoops + "...");
                
                // Assuming POST to some endpoint that returns the specified JSON
                CredoResponse response = restClient.post()
                        .uri("https://api.credocentral.com/v4/payments/initiate") // Placeholder URL
                        .header("Content-Type", "application/json")
                        .body(Map.of(
                                "amount", 100,
                                "currency", "NGN",
                                "transRef", generateRandomAlphanumeric(20),
                                "customerName", "Jaachi Test",
                                "customerEmail", "test@example.com"
                        ))
                        .retrieve()
                        .body(CredoResponse.class);

                if (response != null && response.getData() != null && response.getData().getAccount() != null) {
                    Account account = response.getData().getAccount();
                    accounts.add(account);
                    System.out.println("✅ Received account info: " + account.getAccountNumber());
                } else {
                    System.err.println("❌ Failed to receive account info in loop " + (i + 1));
                }
            } catch (Exception e) {
                System.err.println("❗ Error in loop " + (i + 1) + ": " + e.getMessage());
            }
        }

        if (!accounts.isEmpty()) {
            writeToCsv(accounts, "src/main/resources/csv/CredoAccounts.csv");
        } else {
            System.out.println("No account data collected.");
        }
    }

    private static String generateRandomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private static void writeToCsv(List<Account> accounts, String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            writer.writeNext(new String[]{"accountNumber", "accountName", "bankName", "expiryDate", "amount"});
            for (Account acc : accounts) {
                writer.writeNext(new String[]{
                        acc.getAccountNumber(),
                        acc.getAccountName(),
                        acc.getBankName(),
                        acc.getExpiryDate(),
                        String.valueOf(acc.getAmount())
                });
            }
            System.out.println("✅ Account information written to " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Error writing CSV: " + e.getMessage());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CredoResponse {
        private int status;
        private String message;
        private Data data;

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Data getData() { return data; }
        public void setData(Data data) { this.data = data; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            private Account account;
            public Account getAccount() { return account; }
            public void setAccount(Account account) { this.account = account; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {
        private String accountNumber;
        private String accountName;
        private String bankName;
        private String expiryDate;
        private double amount;

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
}
