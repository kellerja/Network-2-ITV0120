package network_applications_2.block;

import network_applications_2.message.Message;
import network_applications_2.message.MessagesFullEvent;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BlockManager implements MessagesFullEvent {

    List<Block> blocks = new ArrayList<>();

    public void createBlock(List<Message> messages) {
        String lastHash = ""; // TODO Save blocks to file instead of list
        if (!blocks.isEmpty()) {
            lastHash = blocks.get(blocks.size() - 1).getHash();
        }
        Block block = new Block(lastHash, messages);
        block.setHash(findHash(block));
        blocks.add(block);
    }

    public String findHash(Block block) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] buf = blockToByteArray(block);
        byte[] sha256ByteArr = digest != null ? digest.digest(buf) : new byte[0];

        return DatatypeConverter.printHexBinary(sha256ByteArr);
    }

    public byte[] blockToByteArray(Block block) {
        byte[] buf = new byte[0];
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(block);
            buf = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf;
    }

    public static void main(String[] args) {
        List<Message> messages = new ArrayList<>();
        List<Message> messages1 = new ArrayList<>();
        List<Message> messages2 = new ArrayList<>();
        List<Message> messages3 = new ArrayList<>();
        Message message = new Message(123465, "data1");
        Message message1 = new Message(123463, "data2");
        Message message2 = new Message(123466, "data3");
        Message message3 = new Message(123464, "data4");
        Message message4 = new Message(123462, "data5");
        Message message5 = new Message(823469, "data6");
        Message message6 = new Message(63294, "data7");
        Message message7 = new Message(274303, "data8");
        Message message8 = new Message(5623798, "data9");
        Message message9 = new Message(283749, "data10");

        messages.add(message);
        messages.add(message5);
        messages1.add(message2);
        messages1.add(message3);
        messages2.add(message9);
        messages2.add(message7);
        messages3.add(message4);
        messages3.add(message6);
        messages2.add(message8);
        messages3.add(message1);

        BlockManager blockHandler = new BlockManager();
        blockHandler.createBlock(messages);
        blockHandler.createBlock(messages1);
        blockHandler.createBlock(messages2);
        blockHandler.createBlock(messages3);

        for (int i = 0; i < blockHandler.blocks.size(); i++) {
            Block block = blockHandler.blocks.get(i);
            System.out.println("BLOCK " + i);
            System.out.println(block.getTimestamp());
            System.out.println("Previous hash: " + block.getPreviousHash());
            for (Message bMessage : block.getMessages()) {
                System.out.println(bMessage.getTimestamp() + " : " + bMessage.getData());
            }
            System.out.println("New hash: " + block.getHash());
            System.out.println();
        }
    }

    @Override
    public void propagateMessages(List<Message> messages) {
        createBlock(messages);
    }
}
