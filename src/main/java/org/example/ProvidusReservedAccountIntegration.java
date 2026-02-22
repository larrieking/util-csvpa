package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.CSVWriter;
import org.springframework.web.client.RestClient;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class ProvidusReservedAccountIntegration {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        int numberOfAccountsToCreate = 1; // Configurable loop count
        if (args.length > 0) {
            try {
                numberOfAccountsToCreate = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid argument for count, using default 1");
            }
        }

        List<VirtualAccount> virtualAccounts = new ArrayList<>();
        RestClient restClient = RestClient.create();

        for (int i = 0; i < numberOfAccountsToCreate; i++) {
            try {
                String transRef = generateRandomAlphanumeric(20);
                String requestTag = generateRandomAlphanumeric(20);

                CreateReservedAccountRequest body = new CreateReservedAccountRequest(
                        transRef,
                        "Lasaa Collection",
                        requestTag
                );

                System.out.println("Calling Providus API (" + (i + 1) + "/" + numberOfAccountsToCreate + ")...");
                
                ReservedAccountNumberResponse resObj = restClient.post()
                        .uri("https://vps.providusbank.com/vps/api/PiPCreateReservedAccountNumber")
                        .header("Content-Type", "application/json")
                        .header("X-Trans-Ref", transRef)
                        .header("X-Request-tag", requestTag)
                        .header("X-Credo-nonce", UUID.randomUUID().toString())
                        .header("Client-Id", "RVRSQU5aQUNULUNSRUQw")
                        .header("X-Auth-Signature", "f0bee7089c390a4829d98c84d2964dc9346311a3e31920d7aab29e65b3d401e18f39e78547ffa2d9e3f19c26b21a5ce869843f23bde351a873c5db0283ef362f")
                        .body(body)
                        .retrieve()
                        .body(ReservedAccountNumberResponse.class);

                if (resObj != null && resObj.getAccountNumber() != null && !resObj.getAccountNumber().isEmpty()) {
                    VirtualAccount va = buildVirtualAccount(resObj);
                    virtualAccounts.add(va);
                    System.out.println("✅ Successfully created account: " + va.getAccountNumber());
                } else {
                    System.err.println("❌ API returned failure or no account number: " + resObj);
                }

            } catch (Exception e) {
                System.err.println("❗ Error during iteration " + i + ": " + e.getMessage());
            }
        }

        if (!virtualAccounts.isEmpty()) {
            writeToCsv(virtualAccounts, "src/main/resources/csv/ProvidusReservedAccounts.csv");
        } else {
            System.out.println("No accounts were created, CSV not generated.");
        }
    }

    private static String generateRandomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private static VirtualAccount buildVirtualAccount(ReservedAccountNumberResponse response) {
        Date accountExpiresIn = Date.from(
                LocalDateTime.now()
                        .plusHours(48)
                        .atZone(ZoneId.of("Africa/Lagos"))
                        .toInstant());

        return VirtualAccount.builder()
                .accountNumber(response.getAccountNumber())
                .accountName(response.getAccountName())
                .assignedCount(0)
                .assigned(0)
                .assignedSuccessCount(0)
                .processorCode("101CRDSETL")
                .expiryDate(accountExpiresIn)
                .assignedDate(new Date())
                .businessCode("700607002620017")
                .createdBy("System")
                .createdDate(new Date())
                .lastModifiedBy("System")
                .lastModifiedDate(new Date())
                .businessId(6530L)
                .build();
    }

    private static void writeToCsv(List<VirtualAccount> accounts, String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            String[] header = {
                    "accountNumber", "accountName", "assignedCount", "assigned",
                    "assignedSuccessCount", "processorCode", "expiryDate", "assignedDate",
                    "businessCode", "createdBy", "createdDate", "lastModifiedBy",
                    "lastModifiedDate", "businessId"
            };
            writer.writeNext(header);

            for (VirtualAccount va : accounts) {
                writer.writeNext(new String[]{
                        va.getAccountNumber(),
                        va.getAccountName(),
                        String.valueOf(va.getAssignedCount()),
                        String.valueOf(va.getAssigned()),
                        String.valueOf(va.getAssignedSuccessCount()),
                        va.getProcessorCode(),
                        va.getExpiryDate().toString(),
                        va.getAssignedDate().toString(),
                        va.getBusinessCode(),
                        va.getCreatedBy(),
                        va.getCreatedDate().toString(),
                        va.getLastModifiedBy(),
                        va.getLastModifiedDate().toString(),
                        String.valueOf(va.getBusinessId())
                });
            }
            System.out.println("✅ Written " + accounts.size() + " records to " + filePath);
        } catch (IOException e) {
            System.err.println("❗ Failed to write CSV: " + e.getMessage());
        }
    }

    // Response DTO
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReservedAccountNumberResponse {
        @JsonProperty("account_number")
        private String accountNumber;
        @JsonProperty("account_name")
        private String accountName;
        private String responseMessage;
        private String responseCode;

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }
        public String getResponseMessage() { return responseMessage; }
        public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
        public String getResponseCode() { return responseCode; }
        public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

        @Override
        public String toString() {
            return "ReservedAccountNumberResponse{" +
                    "accountNumber='" + accountNumber + '\'' +
                    ", accountName='" + accountName + '\'' +
                    ", responseMessage='" + responseMessage + '\'' +
                    ", responseCode='" + responseCode + '\'' +
                    '}';
        }
    }

    // VirtualAccount model with Builder
    public static class VirtualAccount {
        private String accountNumber;
        private String accountName;
        private int assignedCount;
        private int assigned;
        private int assignedSuccessCount;
        private String processorCode;
        private Date expiryDate;
        private Date assignedDate;
        private String businessCode;
        private String createdBy;
        private Date createdDate;
        private String lastModifiedBy;
        private Date lastModifiedDate;
        private Long businessId;

        private VirtualAccount() {}

        public String getAccountNumber() { return accountNumber; }
        public String getAccountName() { return accountName; }
        public int getAssignedCount() { return assignedCount; }
        public int getAssigned() { return assigned; }
        public int getAssignedSuccessCount() { return assignedSuccessCount; }
        public String getProcessorCode() { return processorCode; }
        public Date getExpiryDate() { return expiryDate; }
        public Date getAssignedDate() { return assignedDate; }
        public String getBusinessCode() { return businessCode; }
        public String getCreatedBy() { return createdBy; }
        public Date getCreatedDate() { return createdDate; }
        public String getLastModifiedBy() { return lastModifiedBy; }
        public Date getLastModifiedDate() { return lastModifiedDate; }
        public Long getBusinessId() { return businessId; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private VirtualAccount va = new VirtualAccount();

            public Builder accountNumber(String val) { va.accountNumber = val; return this; }
            public Builder accountName(String val) { va.accountName = val; return this; }
            public Builder assignedCount(int val) { va.assignedCount = val; return this; }
            public Builder assigned(int val) { va.assigned = val; return this; }
            public Builder assignedSuccessCount(int val) { va.assignedSuccessCount = val; return this; }
            public Builder processorCode(String val) { va.processorCode = val; return this; }
            public Builder expiryDate(Date val) { va.expiryDate = val; return this; }
            public Builder assignedDate(Date val) { va.assignedDate = val; return this; }
            public Builder businessCode(String val) { va.businessCode = val; return this; }
            public Builder createdBy(String val) { va.createdBy = val; return this; }
            public Builder createdDate(Date val) { va.createdDate = val; return this; }
            public Builder lastModifiedBy(String val) { va.lastModifiedBy = val; return this; }
            public Builder lastModifiedDate(Date val) { va.lastModifiedDate = val; return this; }
            public Builder businessId(Long val) { va.businessId = val; return this; }

            public VirtualAccount build() {
                return va;
            }
        }
    }

    public record CreateReservedAccountRequest(String transRef, String accountName, String requestTag) {
    }

}
