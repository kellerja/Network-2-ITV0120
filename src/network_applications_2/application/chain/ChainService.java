package network_applications_2.application.chain;

import network_applications_2.block.Block;
import network_applications_2.chain.Chain;
import network_applications_2.chain.ChainFactory;
import network_applications_2.chain.ChainFormatException;

import java.util.ArrayList;
import java.util.List;

public class ChainService {

    private final Chain chain;
    private String latestHash;

    public ChainService() throws ChainFormatException {
        chain = ChainFactory.create(new ArrayList<>());
        latestHash = null;
    }

    public List<Block> getBlocks() {
        synchronized (chain) {
            return new ArrayList<>(chain.getBlocks());
        }
    }

    public List<Block> getBlocks(String startHash) {
        synchronized (chain) {
            return new ArrayList<>(chain.getBlocks(startHash));
        }
    }

    public Block getBlock(String hash) {
        synchronized (chain) {
            return chain.getBlocks().stream().filter(b -> b.getHash().equals(hash)).findFirst().orElse(null);
        }
    }

    public void addBlock(Block block) throws ChainFormatException {
        synchronized (chain) {
            chain.addBlock(block);
            latestHash = block.getHash();
        }
    }

    public List<Block> merge(Chain chainToMerge) {
        synchronized (chain) {
            Block startBlock = chainToMerge.getBlocks().get(0);
            List<Block> blocks = getBlocks(startBlock.getPreviousHash());
            if (blocks.isEmpty()) {
                return new ArrayList<>();
            }
            if (startBlock.getPreviousHash() != null) {
                blocks.remove(0);
            }
            int i;
            for (i = 0; i < Math.min(chainToMerge.getBlocks().size(), blocks.size()); i++) {
                startBlock = chainToMerge.getBlocks().get(i);
                if (startBlock.equals(blocks.get(i))) {
                    continue;
                }
                break;
            }
            if (startBlock.equals(chainToMerge.getBlocks().get(chainToMerge.getBlocks().size() - 1))) {
                return new ArrayList<>();
            }
            chain.getBlocks().removeAll(blocks.subList(i, blocks.size()));
            chain.getBlocks().addAll(chainToMerge.getBlocks().subList(i, chainToMerge.getBlocks().size()));
            latestHash = chain.getBlocks().get(chain.getBlocks().size() - 1).getHash();
            return chainToMerge.getBlocks().subList(i, chainToMerge.getBlocks().size());
        }
    }

    public String getLatestHash() {
        return latestHash;
    }
}
