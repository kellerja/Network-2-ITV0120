package network_applications_2.validation;

import network_applications_2.block.Block;
import network_applications_2.message.Message;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private Map<String, Double> wallets;

    public Database(List<Block> blocks) {
        wallets = new HashMap<>();
        createWallets(blocks);
        System.out.println(toString());
    }

    public boolean mergeBlock(Block block) {
        if (checkBlock(block)) {
            addBlock(block);
            return true;
        } else {
            System.out.println("fail");
        }
        return false;
    }

    private boolean checkBlock(Block block) {
        Map<String, Double> tempWallets = new HashMap<>();
        for (Message message: block.getMessages()) {
            if (!checkMessage(message)) {
                return false;
            }
            switch (message.getDataType()) {
                case TRANSACTION:
                    tempWallets.put(message.getData().getReceiver(),
                            tempWallets.getOrDefault(message.getData().getReceiver(), 0.0) + message.getData().getAmount());
                    tempWallets.put(message.getSignature(),
                            tempWallets.getOrDefault(message.getSignature(), 0.0) - message.getData().getAmount());
                    break;
                case FREE:
                    tempWallets.put(message.getData().getReceiver(),
                            tempWallets.getOrDefault(message.getData().getReceiver(), 0.0) + message.getData().getAmount());
                    break;
                default:
                    throw new NotImplementedException();
            }
        }
        for (String walletSignature: tempWallets.keySet()) {
            if (wallets.getOrDefault(walletSignature, 0.0) + tempWallets.get(walletSignature) < 0) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMessage(Message message) {
        return message.getData() != null;
    }

    private void addBlock(Block block) {
        for (Message message: block.getMessages()) {
            double funds = message.getData().getAmount();
            switch (message.getDataType()) {
                case TRANSACTION:
                    wallets.put(message.getSignature(), wallets.getOrDefault(message.getSignature(), 0.0) - funds);
                    wallets.put(message.getData().getReceiver(), wallets.getOrDefault(message.getData().getReceiver(), 0.0) + funds);
                    break;
                case FREE:
                    wallets.put(message.getData().getReceiver(), wallets.getOrDefault(message.getData().getReceiver(), 0.0) + funds);
                    break;
            }
        }
    }

    private void createWallets(List<Block> blocks) {
        for (Block block: blocks) {
            addBlock(block);
        }
    }

    @Override
    public String toString() {
        StringBuilder walletString = new StringBuilder();
        for (String account: wallets.keySet()) {
            walletString.append(account).append(": ").append(wallets.get(account)).append(" TC").append("\n");
        }
        return walletString.toString();
    }
}
