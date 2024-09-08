package org.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Another {

    public static void main(String[] args) throws Exception {
        // Inputs
        String ciphertextHex = "50C5C6AB65CE0C6B725F2F850B1ADCF0D7492B9978BA4CDDED7337737933A40A478DDCCB2A5770A97605667E014A73BD332B70D24E201E1A4F1471CCACB2759E8307070C32B09D8A42431CD84486C91521CC60E8C08C3B9FF441B5BF3ED2D652BEFAE8DFCC450934765C3A26163DEC2742754B96892C47ABC5198A3CD95E7A06CA104A2774E40E89059FED16F88D78FA4C7B3BF5C4C55513EB5AB0420288825B70B7C5EC52AF7CFA3DD79911D5D18683F8DFB060E1822EB168BB62A925A523EBD4E936EDCF84D2567313D732DAE59DECEC8A7FEF72F3BAA7CD683F2D2A88F49B591F1D03DFB09A7E1861C4EB85E9B35290D9C1E72BA40FFE59125244F0FC90346602E3BF22AECF3C78751236DCA4A1341FD1140206DC759BD0B013FB934697B5C6DB5FBF59394BF804C23FB5944F5BFDF4A0FC6CEF396FA51C921F9507BED65FAA7BF68E448B013A4B91D4E0C6CA85B6F3FE272A1D190BAAACA70A18269F6CCE8DF081F09FD04C6E3F2C118753705A931126D6E94FAD13BBCCB0632F7E96F0675405AF8236229FCF77DC0E37D67089A388421806ED296997AC675B363335FDD7D8CC4BC370219F3B7B07D982FCB134A5C8E3AB6488AF1832568B6AA19706A31AD236B0B070FFDC6CA9C29468C0397779C0A788DF477F41744E1F7DB353C9906FF41C91E1D8E2A45A059A6D6011C307691A36766B39517D9E7CAF862F315693910824F4ED69CBBDED80C0A6BF9B415C3B464CE6761269DF16A6DEB72DCB41A9734D7A4C409768EB0935AE3EFEC8B30343CCCF5A153B922BE5187060EF07625E25F71F6667EF79246D4F0C7BBB4653AFC46AA3A11C779414FE660118403A8F60DE3AF45F2084BCB1F888069FE506E9865E7B03553999D061A741A9398AC25111BA4FF5C7673D8C945A07030D8FB9CE249E493B1FC4F32E28C4ED337F0F9C471023AA14806FA3297CD8A3CCB06007";
        String passwordHex = "E0CB97592A026980C0C9966FEFAD5211";
        String saltHex = "1ea9566250aadd285cc374326db731c41125DAFEF6699C0C089620A29579BC0E";
        String ivHex = "4c137bd623473cc582ddaa0526659ae1";

        // Convert hex strings to byte arrays
        byte[] ciphertext = hexStringToByteArray(ciphertextHex);
        byte[] password = hexStringToByteArray(passwordHex);
        byte[] salt = hexStringToByteArray(saltHex);
        byte[] iv = hexStringToByteArray(ivHex);

        // Derive AES key using password and salt
        SecretKey secretKey = deriveAESKey(password, salt);

        // Decrypt using AES-GCM
        byte[] decryptedText = decryptAESGCM(ciphertext, secretKey, iv);

        // Print decrypted text (assuming UTF-8 encoding)
        System.out.println("Decrypted text: " + new String(decryptedText, StandardCharsets.UTF_8));
    }

    public static SecretKey deriveAESKey(byte[] password, byte[] salt) throws Exception {
        // Implement key derivation here (e.g., using PBKDF2, Argon2, etc.)
        // For simplicity, we can use PBKDF2 as an example (replace with your key derivation method)
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(Arrays.toString(password).toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    public static byte[] decryptAESGCM(byte[] ciphertext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv); // IV length for AES-GCM is 12 bytes (96 bits)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
        return cipher.doFinal(ciphertext);
    }

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
