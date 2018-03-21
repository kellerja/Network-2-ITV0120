package network_applications_2;


import network_applications_2.message.Message;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MessageGenerator {

    public List<Message> generateMessages(int messageAmount, String beginningDate) {
        List<Message> msgs = new ArrayList<>();
        for (int i = 0; i < messageAmount; i++) {
            long timestamp = generateTimestamp(beginningDate);
            String name1 = generateName(3);
            String name2 = generateName(3);
            double tambergAmount = 0.1 + Math.random() * (10000 - 0.1);
            String msg = name1 + " -> " + name2 + " - " + tambergAmount + " TambergCoin";
            msgs.add(new Message(timestamp, msg));
            writeToFile(timestamp + ", " + msg);
        }
        return msgs;
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

    private String generateName(int nameLength) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(nameLength);
        for (int i = 0; i < nameLength; i++) {
            int randomLetter = 97 + (int)(random.nextFloat() * (122 - 97 + 1));
            stringBuilder.append((char) randomLetter);
        }
        return stringBuilder.toString();
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

    public static void main(String[] args) {
        MessageGenerator messageGenerator = new MessageGenerator();
        messageGenerator.generateMessages(100, "2000-05-02");
    }
}
