package network_applications_2.block;

import network_applications_2.message.Message;
import network_applications_2.message.MessageFormatException;
import network_applications_2.message.MessageParser;
import network_applications_2.message.MessagesFullEvent;
import network_applications_2.utils.Hasher;
import network_applications_2.validation.Database;
import network_applications_2.validation.KeyManager;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BlockManager implements MessagesFullEvent {

    public static final int HASH_VALIDATION_ZEROES_COUNT = 4;

    static final List<Block> blocks = Collections.synchronizedList(new ArrayList<>());
    private Database wallets;

    public BlockManager() throws BlockFormatException, MessageFormatException, IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        blocks.addAll(getBlocksFromFile(new File("resources/Blocks.csv")));
        wallets = new Database(blocks, new KeyManager());
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
        String blockString = block.getHashlessString();
        String nonce, hash;
        do {
            nonce = Double.toString(Math.random()).substring(2);
            hash = Hasher.hash(blockString + "|" + nonce);
        } while (!isBlockHashCorrect(hash));
        block.setNonce(nonce);
        block.setHash(hash);
        if (wallets.mergeBlock(block, blocks)) {
            blocks.add(block);
            writeToFile(block);
        }
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

    public static Block parseBlock(String possibleBlock) throws BlockFormatException, MessageFormatException {
        String[] blockParts = possibleBlock.split("\\|");
        if (blockParts.length < 4) {
            throw new BlockFormatException("Block must have timestamp, previous hash, new hash and data separated by |");
        }
        String newHash = blockParts[0];
        String prevHash = blockParts[1];
        String timestampStr = blockParts[2];
        String data = blockParts[3];
        String nonce = blockParts[4];

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new BlockFormatException("Block third parameter must be a correct unix timestamp", e);
        }

        List<Message> messagesList = new ArrayList<>();
        String[] messages = data.split(";");
        for (String i : messages) {
            messagesList.add(MessageParser.parseMessage(i));
        }
        return new Block(timestamp, prevHash, messagesList, newHash, nonce);
    }

    public static List<Block> getBlocks() {
        return blocks;
    }

    public static boolean isBlockHashCorrect(String hash) {
        return hash.substring(0, HASH_VALIDATION_ZEROES_COUNT).matches("^0*$");
    }

    @Override
    public void propagateMessages(Set<Message> messages) {
        createBlock(new ArrayList<>(messages));
    }
}
