package network_applications_2;

import network_applications_2.block.BlockFormatException;
import network_applications_2.block.BlockManager;
import network_applications_2.message.data.Data;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFormatException;
import network_applications_2.message.data.FreeMoney;
import network_applications_2.message.data.Transaction;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockGenerator {

    private MessageGenerator messageGenerator;

    public BlockGenerator() {
        messageGenerator = new MessageGenerator();
    }

    public void generateBlocks(int blockAmount) throws BlockFormatException, IOException, MessageFormatException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        BlockManager blockManager = new BlockManager();
        for (int i = 0; i < blockAmount; i++) {
            List<Message> messages = messageGenerator.generateMessages(10, "2000-05-02");
            blockManager.createBlock(ensureValidBlock(messages));
        }
    }

    private List<Message> ensureValidBlock(List<Message> messages) throws MessageFormatException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Map<String, Double> wallets = new HashMap<>();
        for (Message message: messages) {
            if (!(message.getData() instanceof Transaction)) {
                continue;
            }
            Transaction transaction = (Transaction) message.getData();
            wallets.put(transaction.getSender(), wallets.getOrDefault(transaction.getSender(), 0.0) - transaction.getAmount());
            wallets.put(transaction.getReceiver(), wallets.getOrDefault(transaction.getReceiver(), 0.0) + transaction.getAmount());
        }
        for (String receiver: wallets.keySet()) {
            if (wallets.get(receiver) < 0) {
                long timestamp = 2342352;
                double amount = Math.abs(wallets.get(receiver));
                Data data = new FreeMoney(receiver, amount);
                messages.add(Message.parseMessage(timestamp + ", " +
                        MessageGenerator.sign(messageGenerator.getKeys().get(receiver), Message.getStorageString(timestamp, data)) +
                        ", " + receiver + ", " + amount));
            }
        }
        return messages;
    }

    public static void main(String[] args) throws BlockFormatException, IOException, MessageFormatException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        BlockGenerator blockGenerator = new BlockGenerator();
        blockGenerator.generateBlocks(10);
    }
}
