package network_applications_2.block;

import network_applications_2.message.Message;
import network_applications_2.message.MessageFormatException;
import network_applications_2.message.MessagesFullEvent;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BlockManager implements MessagesFullEvent {

    static final List<Block> blocks = Collections.synchronizedList(new ArrayList<>());

    public BlockManager() throws BlockFormatException, MessageFormatException, IOException {
        blocks.addAll(getBlocksFromFile(new File("resources/Blocks.csv")));
    }

    public void createBlock(List<Message> messages) {
        String lastHash = "";
        if (!blocks.isEmpty()) {
            Block lastBlock = blocks.get(blocks.size() - 1);
            lastHash = lastBlock.getHash();
            Block tmpBlock = new Block(lastBlock.getPreviousHash(), messages);
            if (tmpBlock.equals(lastBlock)) return;
        }
        Block block = new Block(lastHash, messages);
        block.setHash(findHash(block));
        blocks.add(block);
        writeToFile(block);
    }

    public static void writeToFile(Block block) {
        String blockString = block.getStorageString();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("resources/Blocks.csv", true))) {
            bufferedWriter.write(blockString);
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Block> getBlocksFromFile(File file) throws IOException, MessageFormatException, BlockFormatException {
        List<Block> blocks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                blocks.add(parseBlock(line.trim()));
            }
        }
        return blocks;
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

    public static Block parseBlock(String possibleBlock) throws BlockFormatException, MessageFormatException {
        String[] blockParts = possibleBlock.split("\\|");
        if (blockParts.length < 4) {
            throw new BlockFormatException("Block must have timestamp, previous hash, new hash and data separated by |");
        }
        String newHash = blockParts[0];
        String prevHash = blockParts[1];
        String timestampStr = blockParts[2];
        String data = blockParts[3];

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new BlockFormatException("Block third parameter mus be a correct unix timestamp", e);
        }

        List<Message> messagesList = new ArrayList<>();
        String[] messages = data.split(";");
        for (String i : messages) {
            messagesList.add(Message.parseMessage(i));
        }
        return new Block(timestamp, prevHash, messagesList, newHash);
    }

    public static List<Block> getBlocks() {
        return blocks;
    }

    @Override
    public void propagateMessages(Set<Message> messages) {
        createBlock(new ArrayList<>(messages));
    }
}
