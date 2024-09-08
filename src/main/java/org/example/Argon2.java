package org.example;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.security.SecureRandom;

public class Argon2 {

    public static void main(String[] args) {
        String password = "password123";
        byte[] salt = generateSalt(); // Example: Generate a secure random salt

        // Argon2 parameters
        int memoryCost = 65536;     // Memory cost in KiB
        int iterations = 3;         // Iterations
        int parallelism = 4;        // Degree of parallelism
        int keyLength = 256;        // Key length in bits

        // Derive key using Argon2
        byte[] key = deriveKeyWithArgon2(password.toCharArray(), salt);

        // Print derived key (for demonstration)
        System.out.println("Derived Key (Hex): " + bytesToHex(key));
    }

    public static byte[] deriveKeyWithArgon2(char[] password, byte[] salt){
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withMemoryAsKB(65536)
                .withIterations(3)
                .withParallelism(4);


                //.withKeyLength(keyLength / 8);  // Convert bits to bytes

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());

        byte[] key = new byte[256 / 8];
        generator.generateBytes(password, key);

        return key;
    }

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16 bytes = 128 bits
        random.nextBytes(salt);
        return salt;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
