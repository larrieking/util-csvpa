package org.example;

import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import jakarta.xml.bind.DatatypeConverter;

public class Original {


    public  static String ivkey = "4c137bd623473cc582ddaa0526659ae1";
    public static String reversedKey = "1ea9566250aadd285cc374326db731c41125DAFEF6699c0c089620a29579bc0e";
    public static String encryptedData = "50C5C6AB65CE0C6B725F2F850B1ADCF0D7492B9978BA4CDDED7337737933A40A478DDCCB2A5770A97605667E014A73BD332B70D24E201E1A4F1471CCACB2759E8307070C32B09D8A42431CD84486C91521CC60E8C08C3B9FF441B5BF3ED2D652BEFAE8DFCC450934765C3A26163DEC2742754B96892C47ABC5198A3CD95E7A06CA104A2774E40E89059FED16F88D78FA4C7B3BF5C4C55513EB5AB0420288825B70B7C5EC52AF7CFA3DD79911D5D18683F8DFB060E1822EB168BB62A925A523EBD4E936EDCF84D2567313D732DAE59DECEC8A7FEF72F3BAA7CD683F2D2A88F49B591F1D03DFB09A7E1861C4EB85E9B35290D9C1E72BA40FFE59125244F0FC90346602E3BF22AECF3C78751236DCA4A1341FD1140206DC759BD0B013FB934697B5C6DB5FBF59394BF804C23FB5944F5BFDF4A0FC6CEF396FA51C921F9507BED65FAA7BF68E448B013A4B91D4E0C6CA85B6F3FE272A1D190BAAACA70A18269F6CCE8DF081F09FD04C6E3F2C118753705A931126D6E94FAD13BBCCB0632F7E96F0675405AF8236229FCF77DC0E37D67089A388421806ED296997AC675B363335FDD7D8CC4BC370219F3B7B07D982FC";
    public static void main(String[] args) {
        decryptPayload(encryptedData, ivkey, reversedKey);
    }

    public static String decryptPayload(String encryptedMessage, String ivkey, String
            reversedKey) {

        System.out.println(ivkey);
        System.out.println(reversedKey);
        BytesEncryptor encryptor = Encryptors.stronger(ivkey, reversedKey);
        byte[] encryptedBytes =
                DatatypeConverter.parseHexBinary(encryptedMessage);
        byte[] decryptedBytes = encryptor.decrypt(encryptedBytes);
        return new String(decryptedBytes);
    }
}
