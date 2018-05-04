package network_applications_2.wallet;

import network_applications_2.block.Block;
import network_applications_2.message.Message;
import network_applications_2.message.data.FreeMoney;
import network_applications_2.message.data.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {

    private Map<String, BigDecimal> wallets;

    public Wallet(List<Block> blocks) {
        this.wallets = new HashMap<>();
        update(blocks);
    }

    public Map<String, BigDecimal> getWallets() {
        return wallets;
    }

    public void update(Block block) throws InsufficientFundsException {
        for (Message message: block.getMessages()) {
            if (message.getData() instanceof Transaction) {
                Transaction transaction = (Transaction) message.getData();
                wallets.put(transaction.getSender(),
                        wallets.getOrDefault(transaction.getSender(), BigDecimal.ZERO).subtract(transaction.getAmount()));
                wallets.put(transaction.getReceiver(),
                        wallets.getOrDefault(transaction.getReceiver(), BigDecimal.ZERO).add(transaction.getAmount()));
            } else if (message.getData() instanceof FreeMoney) {
                FreeMoney freeMoney = (FreeMoney) message.getData();
                wallets.put(freeMoney.getReceiver(),
                        wallets.getOrDefault(freeMoney.getReceiver(), BigDecimal.ZERO).add(freeMoney.getAmount()));
            }
        }
        validate();
    }

    public List<Block> update(List<Block> blocks) {
        List<Block> updatedBlocks = new ArrayList<>();
        for (Block block: blocks) {
            try {
                update(block);
                updatedBlocks.add(block);
            } catch (InsufficientFundsException e) {
                break;
            }
        }
        return updatedBlocks;
    }

    public List<Block> remove(List<Block> blocks) {
        List<Block> updatedBlocks = new ArrayList<>();
        for (Block block: blocks) {
            try {
                remove(block);
                updatedBlocks.add(block);
            } catch (InsufficientFundsException e) {
                break;
            }
        }
        return updatedBlocks;
    }

    public void remove(Block block) throws InsufficientFundsException {
        for (Message message: block.getMessages()) {
            if (message.getData() instanceof Transaction) {
                Transaction transaction = (Transaction) message.getData();
                wallets.put(transaction.getSender(),
                        wallets.getOrDefault(transaction.getSender(), BigDecimal.ZERO).add(transaction.getAmount()));
                wallets.put(transaction.getReceiver(),
                        wallets.getOrDefault(transaction.getReceiver(), BigDecimal.ZERO).subtract(transaction.getAmount()));
            } else if (message.getData() instanceof FreeMoney) {
                FreeMoney freeMoney = (FreeMoney) message.getData();
                wallets.put(freeMoney.getReceiver(),
                        wallets.getOrDefault(freeMoney.getReceiver(), BigDecimal.ZERO).subtract(freeMoney.getAmount()));
            }
        }
        validate();
    }

    private void validate() throws InsufficientFundsException {
        for (String wallet: wallets.keySet()) {
            if (wallets.get(wallet).compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientFundsException(String.format("Wallet %s has negative funds.", wallet));
            }
        }
    }
}
