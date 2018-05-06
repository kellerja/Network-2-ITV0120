package network_applications_2.chain;

import network_applications_2.block.Block;
import network_applications_2.block.BlockFactory;
import network_applications_2.block.BlockFormatException;
import network_applications_2.message.MessageFormatException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ChainUtils {

    public static Chain readChainFile(File file) throws ChainFormatException {
        if (file == null) {
            return ChainFactory.create(new ArrayList<>());
        }
        List<Block> blocks = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                blocks.add(BlockFactory.parse(line));
            }
        } catch (IOException | BlockFormatException | MessageFormatException e) {
            return ChainFactory.create(new ArrayList<>());
        }
        return ChainFactory.create(blocks);
    }

    public static void appendChainFile(File file, Block block) throws IOException {
        if (file == null || block == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(block.getStorageString() + "\n");
        }
    }

    public static void rewriteChainFile(File file, Chain chain) throws IOException {
        if (file == null || chain == null) {
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Block block: chain.getBlocks()) {
                writer.write(block.getStorageString() + "\n");
            }
        }
    }
}
