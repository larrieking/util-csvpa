package org.example;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AesGcmEncryptionDecryptionExample {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;

    public static void main(String[] args) {
        try {
            // Provided key, IV and encrypted data
            String keyHex = "E0CB97592A026980C0C9966FEFAD52114c137bd623473cc582ddaa0526659ae1";
            String ivHex = "1ea9566250aadd285cc374326db731c41125DAFEF6699C0C089620A29579BC0E";
            String encryptedHex = "50C5C6AB65CE0C6B725F2F850B1ADCF0D7492B9978BA4CDDED7337737933A40A478DDC"
                    + "CB2A5770A97605667E014A73BD332B70D24E201E1A4F1471CCACB2759E8307070C32B09D"
                    + "8A42431CD84486C91521CC60E8C08C3B9FF441B5BF3ED2D652BEFAE8DFCC450934765C3"
                    + "A26163DEC2742754B96892C47ABC5198A3CD95E7A06CA104A2774E40E89059FED16F88D"
                    + "78FA4C7B3BF5C4C55513EB5AB0420288825B70B7C5EC52AF7CFA3DD79911D5D18683F8D"
                    + "FB060E1822EB168BB62A925A523EBD4E936EDCF84D2567313D732DAE59DECEC8A7FEF7"
                    + "2F3BAA7CD683F2D2A88F49B591F1D03DFB09A7E1861C4EB85E9B35290D9C1E72BA40FFE"
                    + "59125244F0FC90346602E3BF22AECF3C78751236DCA4A1341FD1140206DC759BD0B013FB"
                    + "934697B5C6DB5FBF59394BF804C23FB5944F5BFDF4A0FC6CEF396FA51C921F9507BED65"
                    + "FAA7BF68E448B013A4B91D4E0C6CA85B6F3FE272A1D190BAAACA70A18269F6CCE8DF08"
                    + "1F09FD04C6E3F2C118753705A931126D6E94FAD13BBCCB0632F7E96F0675405AF8236229"
                    + "FCF77DC0E37D67089A388421806ED296997AC675B363335FDD7D8CC4BC370219F3B7B07"
                    + "D982FCB134A5C8E3AB6488AF1832568B6AA19706A31AD236B0B070FFDC6CA9C29468C03"
                    + "97779C0A788DF477F41744E1F7DB353C9906FF41C91E1D8E2A45A059A6D6011C307691A3"
                    + "6766B39517D9E7CAF862F315693910824F4ED69CBBDED80C0A6BF9B415C3B464CE67612"
                    + "69DF16A6DEB72DCB41A9734D7A4C409768EB0935AE3EFEC8B30343CCCF5A153B922BE5"
                    + "187060EF07625E25F71F6667EF79246D4F0C7BBB4653AFC46AA3A11C779414FE660118403"
                    + "A8F60DE3AF45F2084BCB1F888069FE506E9865E7B03553999D061A741A9398AC25111BA4"
                    + "FF5C7673D8C945A07030D8FB9CE249E493B1FC4F32E28C4ED337F0F9C471023AA14806F"
                    + "A3297CD8A3CCB06007";

            // Convert the hex strings to byte arrays
            byte[] key = Hex.decodeHex(keyHex);
            byte[] iv = Hex.decodeHex(ivHex);
            byte[] encryptedData = Hex.decodeHex(encryptedHex);

            // Decrypt the data
            String decryptedText = decrypt(encryptedData, key, iv);
            System.out.println("Decrypted Text: " + decryptedText);

            // Sample plain text for encryption
            String plainText = "Hello, AES-GCM Encryption and Decryption!";
            byte[] encryptedText = encrypt(plainText, key, iv);
            System.out.println("Encrypted Text: " + Hex.encodeHexString(encryptedText));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(String plainText, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec);
        return cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    }

    public static String decrypt(byte[] cipherText, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, parameterSpec);
        cipher.updateAAD("E0CB97592A026980C0C9966FEFAD52114c137bd623473cc582ddaa0526659ae1".getBytes());
        byte[] decryptedText = cipher.doFinal(cipherText);
        return new String(decryptedText, StandardCharsets.UTF_8);
    }


}
