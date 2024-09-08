package org.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AESGCMDecryptor {

    public static void main(String[] args) throws Exception {
        String passwordHex = "E0CB97592A026980C0C9966FEFAD52114c137bd623473cc582ddaa0526659ae1";
        String saltHex = "1ea9566250aadd285cc374326db731c41125DAFEF6699C0C089620A29579BC0E";
        String encryptedDataHex = "50C5C6AB65CE0C6B725F2F850B1ADCF0D7492B9978BA4CDDED7337737933A40A478DDCCB2A5770A97605667E014A73BD332B70D24E201E1A4F1471CCACB2759E8307070C32B09D8A42431CD84486C91521CC60E8C08C3B9FF441B5BF3ED2D652BEFAE8DFCC450934765C3A26163DEC2742754B96892C47ABC5198A3CD95E7A06CA104A2774E40E89059FED16F88D78FA4C7B3BF5C4C55513EB5AB0420288825B70B7C5EC52AF7CFA3DD79911D5D18683F8DFB060E1822EB168BB62A925A523EBD4E936EDCF84D2567313D732DAE59DECEC8A7FEF72F3BAA7CD683F2D2A88F49B591F1D03DFB09A7E1861C4EB85E9B35290D9C1E72BA40FFE59125244F0FC90346602E3BF22AECF3C78751236DCA4A1341FD1140206DC759BD0B013FB934697B5C6DB5FBF59394BF804C23FB5944F5BFDF4A0FC6CEF396FA51C921F9507BED65FAA7BF68E448B013A4B91D4E0C6CA85B6F3FE272A1D190BAAACA70A18269F6CCE8DF081F09FD04C6E3F2C118753705A931126D6E94FAD13BBCCB0632F7E96F0675405AF8236229FCF77DC0E37D67089A388421806ED296997AC675B363335FDD7D8CC4BC370219F3B7B07D982FCB134A5C8E3AB6488AF1832568B6AA19706A31AD236B0B070FFDC6CA9C29468C0397779C0A788DF477F41744E1F7DB353C9906FF41C91E1D8E2A45A059A6D6011C307691A36766B39517D9E7CAF862F315693910824F4ED69CBBDED80C0A6BF9B415C3B464CE6761269DF16A6DEB72DCB41A9734D7A4C409768EB0935AE3EFEC8B30343CCCF5A153B922BE5187060EF07625E25F71F6667EF79246D4F0C7BBB4653AFC46AA3A11C779414FE660118403A8F60DE3AF45F2084BCB1F888069FE506E9865E7B03553999D061A741A9398AC25111BA4FF5C7673D8C945A07030D8FB9CE249E493B1FC4F32E28C4ED337F0F9C471023AA14806FA3297CD8A3CCB06007";

        byte[] passwordBytes = hexStringToByteArray(passwordHex);
        byte[] saltBytes = hexStringToByteArray(saltHex);
        byte[] encryptedData = hexStringToByteArray(encryptedDataHex);

        SecretKey aesKey = deriveAESKeyFromPassword(passwordBytes, saltBytes);

        // Assuming IV (Initialization Vector) is not explicitly provided, you need to extract it from the ciphertext.
        byte[] iv = extractIVFromEncryptedData(encryptedData);

        // Decrypt using AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // Assuming 128-bit tag length
        cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
        byte[] decryptedData = cipher.doFinal(encryptedData);

        // Print decrypted data as string (assuming it was originally UTF-8 encoded)
        System.out.println("Decrypted Data:");
        System.out.println(new String(decryptedData, "UTF-8"));
    }

    private static SecretKey deriveAESKeyFromPassword(byte[] password, byte[] salt) throws Exception {
        // PBKDF2 parameters
        int iterations = 65536; // Default value for PBKDF2
        int keyLength = 256;    // Key length in bits (256 bits for AES)

        // Create PBEKeySpec with password, salt, and iterations
        KeySpec spec = new PBEKeySpec(new String(password, "UTF-8").toCharArray(), salt, iterations, keyLength);

        // Use SecretKeyFactory to generate the key
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        // Use the keyBytes to create AES key
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static byte[] extractIVFromEncryptedData(byte[] encryptedData) {
        // Extract IV from encrypted data (assuming it's placed at the beginning)
        byte[] iv = new byte[12]; // Assuming 12 bytes IV for AES-GCM
        System.arraycopy(encryptedData, 0, iv, 0, 12);
        return iv;
    }

    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
}
