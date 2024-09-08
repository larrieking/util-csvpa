package org.example;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class GCM {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String FACTORY_INSTANCE = "PBKDF2WithHmacSHA512";
    private static final int TAG_LENGTH = 16;
    private static final int KEY_LENGTH = 32;
    private static final int ITERATIONS = 65535;

    public static void main(String[] args) throws Exception {
        System.out.println("AES GCMC 256 String decryption with PBKDF2 derived key");

        String masterKey = "E0CB97592A026980C0C9966FEFAD52114c137bd623473cc582ddaa0526659ae1";
        String salt = "E0CB97592A026980C0C9966FEFAD52114c137bd623473cc582ddaa0526659ae1";
        String encryptedData = encrypt(masterKey, "[ {\n" +
                " \"transactionReferenceId\" : \"5211-8350\",\n" +
                " \"customerAccountNumber\" : \"5211251134\",\n" +
                " \"accountName\" : \"test encryption\",\n" +
                " \"amount\" : \"500000.00\",\n" +
                " \"checkSum\" : \"F43CACE32FBCE26E53C56BDE8E5BAF2C5319C082B188FA360DD14CBBB9EB3BFBD4A045E1175D08DC6C6F73512DC616D3B3842E2AA9BB4DC8EC9F095FEB56AEDAC301AF331D\",\n" +
                " \"checkSumId\" : \"8350\",\n" +
                " \"bankCharge\" : \"50.00\",\n" +
                " \"adminFee\" : \"0.00\",\n" +
                " \"amountNetCharges\" : \"499950.00\",\n" +
                " \"vatOnNetCharges\" : \"34880.23\",\n" +
                " \"amountNetChargesAndVat\" : \"465069.77\",\n" +
                " \"srcAcct\" : \"1013845790\",\n" +
                " \"srcAcctBankCode\" : \"00015\",\n" +
                " \"srcAcctName\" : \"BENIN CLUB\",\n" +
                " \"sessionId\" : \"000015240311151102464310031429\"\n" +
                "} ]", salt, masterKey);
        System.out.println("Encrypted: " + encryptedData);
        String decryptedText = decrypt(encryptedData, masterKey, salt, masterKey);
        System.out.println("Decrypted: " + decryptedText);
    }

    private static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH * 8);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;
    }

    public static String decrypt(String cipherContent, String password, String saltt, String ivv) throws Exception {
        byte[] decode = Base64.getDecoder().decode(cipherContent.getBytes(UTF_8));
        ByteBuffer byteBuffer = ByteBuffer.wrap(decode);

        byte[] salt = hexStringToByteArray(saltt);//new byte[SALT_LENGTH];
        byteBuffer.get(salt);

        byte[] iv = hexStringToByteArray(ivv); //new byte[IV_LENGTH];
        byteBuffer.get(iv);

        byte[] content = new byte[byteBuffer.remaining()];
        byteBuffer.get(content);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        SecretKey aesKeyFromPassword = getAESKeyFromPassword(password.toCharArray(), salt);
        cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH * 8, iv));
        byte[] plainText = cipher.doFinal(content);
        return new String(plainText, UTF_8);
    }

    public static String encrypt(String password, String plainMessage, String saltt, String ivv) throws Exception {
        byte[] salt = hexStringToByteArray(saltt);//getRandomNonce(SALT_LENGTH);
        SecretKey secretKey = getSecretKey(password, salt);

        byte[] iv = hexStringToByteArray(ivv);//getRandomNonce(IV_LENGTH);

        Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, iv);

        byte[] encryptedMessageByte = cipher.doFinal(plainMessage.getBytes(UTF_8));

        byte[] cipherByte = ByteBuffer.allocate(salt.length + iv.length + encryptedMessageByte.length)
                .put(salt)
                .put(iv)
                .put(encryptedMessageByte)
                .array();
        return Base64.getEncoder().encodeToString(cipherByte);
    }

    public static byte[] getRandomNonce(int length) {
        byte[] nonce = new byte[length];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static SecretKey getSecretKey(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH * 8);

        SecretKeyFactory factory = SecretKeyFactory.getInstance(FACTORY_INSTANCE);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private static Cipher initCipher(int mode, SecretKey secretKey, byte[] iv) throws InvalidKeyException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(mode, secretKey, new GCMParameterSpec(TAG_LENGTH * 8, iv));
        return cipher;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}