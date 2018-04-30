package network_applications_2.chain;

import network_applications_2.block.Block;

import java.util.ArrayList;
import java.util.List;

public class ChainFactory {

    public static Chain create(List<Block> blocks) throws ChainFormatException {
        if (blocks == null) {
            throw new ChainFormatException("Blocks must not be null");
        }
        if (blocks.isEmpty()) {
            return new Chain(new ArrayList<>());
        }
        String prevHash = blocks.get(0).getHash();
        for (int i = 1; i < blocks.size(); i++) {
            if (!blocks.get(i).getPreviousHash().equals(prevHash)) {
                throw new ChainFormatException(String.format("Parent block hash must be child block's parent block. Parent idx = {%s}, child idx = {%s}, parent = {%s}, child = {%s}", i-1, i, blocks.get(i - 1).getStorageString(), blocks.get(i).getStorageString()));
            }
            prevHash = blocks.get(i).getHash();
        }
        return new Chain(new ArrayList<>(blocks));
    }
}
