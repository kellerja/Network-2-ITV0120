package network_applications_2.validation;

import network_applications_2.block.Block;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFormatException;
import network_applications_2.message.data.FreeMoney;
import network_applications_2.message.data.Transaction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private Map<String, Double> wallets;
    private KeyManager keyManager;

    public Database(List<Block> blocks, KeyManager keyManager) throws MessageFormatException {
        this.keyManager = keyManager;
        wallets = new HashMap<>();
        createWallets(blocks);
    }

    public boolean mergeBlock(Block block, List<Block> history) {
        try {
            if (checkBlock(block, history)) {
                addBlock(block);
                return true;
            } else {
                System.out.println("fail");
            }
        } catch (MessageFormatException e) {
            return false;
        }
        return false;
    }

    private boolean checkBlock(Block block, List<Block> chain) throws MessageFormatException {
        Map<String, Double> tempWallets = new HashMap<>();
        for (Message message: block.getMessages()) {
            if (!checkMessage(message, chain)) {
                return false;
            }
            if (message.getData() instanceof Transaction) {
                Transaction transaction = (Transaction) message.getData();
                tempWallets.put(transaction.getReceiver(),
                        tempWallets.getOrDefault(transaction.getReceiver(), 0.0) + transaction.getAmount());
                tempWallets.put(transaction.getSender(),
                        tempWallets.getOrDefault(transaction.getSender(), 0.0) - transaction.getAmount());
            } else if (message.getData() instanceof FreeMoney) {
                FreeMoney freeMoney = (FreeMoney) message.getData();
                tempWallets.put(freeMoney.getReceiver(),
                        tempWallets.getOrDefault(freeMoney.getReceiver(), 0.0) + freeMoney.getAmount());
            } else {
                throw new MessageFormatException("Invalid Data type");
            }
        }
        for (String walletSignature: tempWallets.keySet()) {
            if (wallets.getOrDefault(walletSignature, 0.0) + tempWallets.get(walletSignature) < 0) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMessage(Message message, List<Block> chain) {
        String publicKey = message.getData() instanceof Transaction ? ((Transaction) message.getData()).getSender() : message.getData().getReceiver();
        return message.getData() != null &&
                keyManager.validate(Message.getStorageString(message.getTimestamp(), message.getData()), message.getSignature(), publicKey) &&
                messageNotInChain(message, chain);
    }

    private boolean messageNotInChain(Message message, List<Block> chain) {
        for (Block link: chain) {
            for (Message linkMessage: link.getMessages()) {
                if (message.equals(linkMessage)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addBlock(Block block) throws MessageFormatException {
        for (Message message: block.getMessages()) {
            double funds = message.getData().getAmount();
            if (message.getData() instanceof Transaction) {
                Transaction transaction = (Transaction) message.getData();
                wallets.put(transaction.getSender(), wallets.getOrDefault(transaction.getSender(), 0.0) - funds);
                wallets.put(transaction.getReceiver(), wallets.getOrDefault(transaction.getReceiver(), 0.0) + funds);
            } else if (message.getData() instanceof FreeMoney) {
                FreeMoney freeMoney = (FreeMoney) message.getData();
                wallets.put(freeMoney.getReceiver(), wallets.getOrDefault(freeMoney.getReceiver(), 0.0) + funds);
            } else {
                throw new MessageFormatException("Invalid Data type");
            }
        }
    }

    private void createWallets(List<Block> blocks) throws MessageFormatException {
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
