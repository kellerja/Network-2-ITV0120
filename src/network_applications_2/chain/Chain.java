package network_applications_2.chain;

import network_applications_2.block.Block;
import network_applications_2.wallet.InsufficientFundsException;
import network_applications_2.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

public class Chain {

    private List<Block> blocks;
    private Wallet wallet;

    Chain(List<Block> blocks) {
        this.blocks = blocks;
        this.wallet = new Wallet(blocks);
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<Block> getBlocks(String startHash) {
        if (startHash == null) {
            return getBlocks();
        }
        List<Block> resultBlocks = new ArrayList<>();
        Block block = blocks.stream().filter(b -> startHash.equals(b.getHash())).findFirst().orElse(null);
        if (block != null) {
            resultBlocks = blocks.subList(blocks.indexOf(block), blocks.size());
        }
        return resultBlocks;
    }

    public void addBlock(Block block) throws ChainFormatException, InsufficientFundsException {
        if (block.getPreviousHash() != null && !block.getPreviousHash().equals(blocks.get(blocks.size() - 1).getHash())) {
            throw new ChainFormatException("Chain parent block hash must be child blocks prev hash");
        }
        wallet.update(block);
        blocks.add(block);
    }

    public List<Block> addBlocks(List<Block> blocks) throws ChainFormatException {
        List<Block> updatedBlocks = new ArrayList<>();
        for (Block block: blocks) {
            try {
                addBlock(block);
                updatedBlocks.add(block);
            } catch (InsufficientFundsException e) {
                break;
            }
        }
        return updatedBlocks;
    }

    public List<Block> removeBlocks(List<Block> blocksToRemove) {
        List<Block> updatedBlocks = wallet.remove(blocksToRemove);
        blocks.removeAll(updatedBlocks);
        return updatedBlocks;
    }

    public Wallet getWallet() {
        return wallet;
    }
}
