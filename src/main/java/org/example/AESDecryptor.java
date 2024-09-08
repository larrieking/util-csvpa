package org.example;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AESDecryptor {

    public static SecretKey getAESKeyFromPassword(String password, byte[] salt) throws NoSuchAlgorithmException, DecoderException {
        // Convert the password to a byte array using a specified charset
        byte[] passwordBytes = hexStringToByteArray(password);

        // Concatenate the salt with the password
        byte[] passwordSaltBytes = new byte[passwordBytes.length + salt.length];
        System.arraycopy(passwordBytes, 0, passwordSaltBytes, 0, passwordBytes.length);
        System.arraycopy(salt, 0, passwordSaltBytes, passwordBytes.length, salt.length);

        // Use SHA-256 to hash the password and salt
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha256.digest(passwordSaltBytes);

        System.out.println(bytesToHex(keyBytes));

        // Use the first 32 bytes (256 bits) of the keyBytes as the AES key
        return new SecretKeySpec(Arrays.copyOf(keyBytes, 32), "AES");
    }

    public static void main(String[] args) {
        try {
            String encryptedDataHex = "50C5C6AB65CE0C6B725F2F850B1ADCF0D7492B9978BA4CDDED7337737933A40A478DDC"
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

            String password = "E0CB97592A026980C0C9966FEFAD5211";
            String saltHex = "1ea9566250aadd285cc374326db731c41125DAFEF6699C0C089620A29579BC0E";
            String ivHex = "4c137bd623473cc582ddaa0526659ae1";

            // Convert hex strings to byte arrays using Apache Commons Codec
            byte[] encryptedData = hexStringToByteArray(encryptedDataHex);//Hex.decodeHex(encryptedDataHex.toCharArray());
            byte[] salt = hexStringToByteArray(saltHex); //Hex.decodeHex(saltHex.toCharArray());
            byte[] iv = hexStringToByteArray(ivHex);//Hex.decodeHex(ivHex.toCharArray());

            // Derive the key from the password and salt
            //byte[] key1 = Argon2.deriveKeyWithArgon2(password.toCharArray(), salt);
            SecretKey key = getAESKeyFromPassword(password, salt);//new SecretKeySpec(key1, "AES");


            // Separate ciphertext and authentication tag
            int tagLengthInBytes = 16; // 128-bit tag length
            byte[] ciphertext = Arrays.copyOfRange(encryptedData, 0, encryptedData.length - tagLengthInBytes);
            byte[] authenticationTag = Arrays.copyOfRange(encryptedData, encryptedData.length - tagLengthInBytes, encryptedData.length);

            // Decrypt the data
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128-bit auth tag length
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            cipher.updateAAD(authenticationTag); // Set authentication tag
            byte[] decryptedData = cipher.doFinal(ciphertext);

            // Convert decrypted bytes to string
            String decryptedText = new String(decryptedData, StandardCharsets.UTF_8);
            System.out.println("Decrypted Text: " + decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] byteArray = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return byteArray;
    }


    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

