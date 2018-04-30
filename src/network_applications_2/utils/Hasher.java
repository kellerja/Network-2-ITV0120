package network_applications_2.utils;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {

    public static String hash(String plain) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] buf = plain.getBytes();
        byte[] sha256ByteArr = digest != null ? digest.digest(buf) : new byte[0];

        return DatatypeConverter.printHexBinary(sha256ByteArr);
    }
}
