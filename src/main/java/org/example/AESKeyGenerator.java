package org.example;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AESKeyGenerator {

    public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt) throws NoSuchAlgorithmException {
        // Convert the password to a byte array
        byte[] passwordBytes = new String(password).getBytes();

        // Concatenate the salt with the password
        byte[] passwordSaltBytes = new byte[passwordBytes.length + salt.length];
        System.arraycopy(passwordBytes, 0, passwordSaltBytes, 0, passwordBytes.length);
        System.arraycopy(salt, 0, passwordSaltBytes, passwordBytes.length, salt.length);

        // Use SHA-256 to hash the password and salt
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha256.digest(passwordSaltBytes);

        // Use the first 32 bytes (256 bits) of the keyBytes as the AES key
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        char[] password = "yourPassword".toCharArray();
        byte[] salt = generateSalt();

        SecretKey key = getAESKeyFromPassword(password, salt);

        System.out.println("Generated Key: " + bytesToHex(key.getEncoded()));
    }

    private static byte[] generateSalt() {
        // Generate a random salt
        byte[] salt = new byte[16]; // Salt size is 16 bytes
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
