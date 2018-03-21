package network_applications_2;

import network_applications_2.block.BlockManager;
import network_applications_2.message.Message;

import java.util.List;

public class BlockGenerator {

    public void generateBlocks(int blockAmount) {
        MessageGenerator messageGenerator = new MessageGenerator();
        BlockManager blockManager = new BlockManager();
        for (int i = 0; i < blockAmount; i++) {
            List<Message> messages = messageGenerator.generateMessages(10, "2000-05-02");
            blockManager.createBlock(messages);
        }
    }

    public static void main(String[] args) {
        BlockGenerator blockGenerator = new BlockGenerator();
        blockGenerator.generateBlocks(10);
    }
}
