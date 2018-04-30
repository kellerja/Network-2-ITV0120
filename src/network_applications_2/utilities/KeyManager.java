package network_applications_2.utilities;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyManager {

    private final KeyPair keyPair;

    private static final String PUBLIC_KEY_PATH = "resources/keys/key.pub";
    private static final String PRIVATE_KEY_PATH = "resources/keys/key";

    public KeyManager() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        File privateKeyFile = new File(PRIVATE_KEY_PATH);
        File publicKeyFile = new File(PUBLIC_KEY_PATH);
        KeyFactory keyFactory = getKeyFactory();
        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            byte[] privateKey = new byte[(int) privateKeyFile.length()];
            try (DataInputStream dis = new DataInputStream(new FileInputStream(privateKeyFile))) {
                dis.readFully(privateKey);
            }
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKey);
            PrivateKey privateRsaKey = keyFactory.generatePrivate(privateSpec);

            byte[] publicKey = new byte[(int) publicKeyFile.length()];
            try (DataInputStream dis = new DataInputStream(new FileInputStream(publicKeyFile))) {
                dis.readFully(publicKey);
            }
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKey);
            PublicKey publicRsaKey = keyFactory.generatePublic(publicSpec);

            keyPair = new KeyPair(publicRsaKey, privateRsaKey);
        } else {
            keyPair = KeyGenerator.generate();
            try (FileOutputStream fos = new FileOutputStream(privateKeyFile)) {
                fos.write(keyPair.getPrivate().getEncoded());
            }
            try (FileOutputStream fos = new FileOutputStream(publicKeyFile)) {
                fos.write(keyPair.getPublic().getEncoded());
            }
        }
    }

    public String getPublicKey() {
        return KeyManager.convertToString(keyPair.getPublic());
    }

    public KeyPair getKeys() {
        return keyPair;
    }

    public String sign(String data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return KeyManager.sign(keyPair.getPrivate(), data);
    }

    public static String sign(PrivateKey key, String data) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);
        signature.update(data.getBytes());
        return DatatypeConverter.printHexBinary(signature.sign());
    }

    public static boolean validate(String data, String allegedSignature, String pubKey){
        Signature signature;
        try {
            signature = Signature.getInstance("SHA256withRSA");
            byte[] publicKey = DatatypeConverter.parseHexBinary(pubKey);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKey);
            PublicKey publicRsaKey = getKeyFactory().generatePublic(publicSpec);
            signature.initVerify(publicRsaKey);
            signature.update(data.getBytes());
            return signature.verify(DatatypeConverter.parseHexBinary(allegedSignature));
        } catch (IllegalArgumentException | NoSuchAlgorithmException | SignatureException | InvalidKeyException | InvalidKeySpecException ignored) {
        }
        return false;
    }

    public static String convertToString(PublicKey publicKey) {
        return DatatypeConverter.printHexBinary(publicKey.getEncoded());
    }

    private static KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("RSA");
    }
}
