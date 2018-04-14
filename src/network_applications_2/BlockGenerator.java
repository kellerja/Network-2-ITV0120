package network_applications_2;

import network_applications_2.block.BlockFormatException;
import network_applications_2.block.BlockManager;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFormatException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockGenerator {

    public void generateBlocks(int blockAmount) throws BlockFormatException, IOException, MessageFormatException {
        MessageGenerator messageGenerator = new MessageGenerator();
        BlockManager blockManager = new BlockManager();
        for (int i = 0; i < blockAmount; i++) {
            List<Message> messages = messageGenerator.generateMessages(10, "2000-05-02");
            blockManager.createBlock(ensureValidBlock(messages));
        }
    }

    private List<Message> ensureValidBlock(List<Message> messages) {
        Map<String, Double> wallets = new HashMap<>();
        for (Message message: messages) {
            wallets.put(message.getSignature(), wallets.getOrDefault(message.getSignature(), 0.0) - message.getData().getAmount());
            wallets.put(message.getData().getReceiver(), wallets.getOrDefault(message.getData().getReceiver(), 0.0) + message.getData().getAmount());
        }
        for (String signature: wallets.keySet()) {
            if (wallets.get(signature) < 0) {
                try {
                    messages.add(Message.parseMessage("2342352, " + signature + ", " + Math.abs(wallets.get(signature)) + " TambergCoin"));
                } catch (MessageFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return messages;
    }

    public static void main(String[] args) throws BlockFormatException, IOException, MessageFormatException {
        BlockGenerator blockGenerator = new BlockGenerator();
        blockGenerator.generateBlocks(10);
    }
}
