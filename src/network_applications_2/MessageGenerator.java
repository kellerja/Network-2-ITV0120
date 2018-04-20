package network_applications_2;

import network_applications_2.message.data.Data;
import network_applications_2.message.Message;
import network_applications_2.message.data.Transaction;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MessageGenerator {

    private Map<String, KeyPair> keys;

    public MessageGenerator() {
        keys = new HashMap<>();
    }

    public List<Message> generateMessages(int messageAmount, String beginningDate) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        List<Message> msgs = new ArrayList<>();
        for (int i = 0; i < messageAmount; i++) {
            long timestamp = generateTimestamp(beginningDate);
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(512);
            KeyPair keyPair1 = keyPairGenerator.generateKeyPair();
            String name1 = DatatypeConverter.printHexBinary(keyPair1.getPublic().getEncoded());
            keys.put(name1, keyPair1);
            KeyPair keyPair2 = keyPairGenerator.generateKeyPair();
            String name2 = DatatypeConverter.printHexBinary(keyPair2.getPublic().getEncoded());
            keys.put(name2, keyPair2);
            double tambergAmount = 0.1 + Math.random() * (10000 - 0.1);
            String msg = name1 + ", " + name2 + ", " + tambergAmount;

            Data data = new Transaction(name1, name2, tambergAmount);
            msgs.add(new Message<>(timestamp, data,
                    sign(keyPair1, Message.getStorageString(timestamp, data))));
            writeToFile(timestamp + ", " + msg);
        }
        return msgs;
    }

    public static String sign(KeyPair keyPair, String data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(data.getBytes());
        return DatatypeConverter.printHexBinary(signature.sign());
    }

    private void writeToFile(String msg) {
        System.out.println(msg);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter("resources/Messages.csv", true));
            bufferedWriter.write(msg);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long generateTimestamp(String beginningDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(beginningDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long beginningTimestamp = date != null ? date.getTime()/1000 : 1420070400;
        return beginningTimestamp + Math.abs(new Random().nextLong()) % (new Date().getTime()/1000 - beginningTimestamp);
    }

    public Map<String, KeyPair> getKeys() {
        return keys;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        MessageGenerator messageGenerator = new MessageGenerator();
        messageGenerator.generateMessages(100, "2000-05-02");
    }
}
