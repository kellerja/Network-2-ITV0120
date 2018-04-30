package network_applications_2.chain;

import network_applications_2.block.Block;

import java.util.ArrayList;
import java.util.List;

public class Chain {

    private List<Block> blocks;

    Chain(List<Block> blocks) {
        this.blocks = blocks;
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

    public void addBlock(Block block) throws ChainFormatException {
        if (block.getPreviousHash() != null && !block.getPreviousHash().equals(blocks.get(blocks.size() - 1).getHash())) {
            throw new ChainFormatException("Chain parent block hash must be child blocks prev hash");
        }
        blocks.add(block);
    }
}
