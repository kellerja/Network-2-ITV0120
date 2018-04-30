package network_applications_2.chain;

import network_applications_2.block.Block;
import network_applications_2.block.BlockFactory;
import network_applications_2.block.BlockFormatException;
import network_applications_2.block.BlockUtils;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFactory;
import network_applications_2.message.MessageFormatException;
import network_applications_2.utilities.KeyGenerator;
import network_applications_2.utilities.KeyManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class ChainFactoryTests {

    private static List<Block> blocks;
    private static List<KeyPair> users;
    private static int HASH_VALIDATION_ZEROES_COUNT_MEMORY;

    @BeforeClass
    public static void initialize() throws MessageFormatException, BlockFormatException {
        HASH_VALIDATION_ZEROES_COUNT_MEMORY = BlockUtils.HASH_VALIDATION_ZEROES_COUNT;
        BlockUtils.HASH_VALIDATION_ZEROES_COUNT = 1;
        // TODO: load data from file instead
        blocks = new ArrayList<>();
        users = new ArrayList<>();
        String prevHash = null;
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 10; i++) {
                users.add(KeyGenerator.generate());
            }
            SortedSet<Message> messages = new TreeSet<>();
            messages.add(MessageFactory.create(users.get(0), 1000));
            for (int i = 1; i < 10; i++) {
                messages.add(MessageFactory.create(users.get(j + i - 1),
                        KeyManager.convertToString(users.get(j + i).getPublic()),
                        1000 - 100 * i));
            }
            Block block = BlockFactory.create(prevHash, messages);
            blocks.add(block);
            prevHash = block.getHash();
        }
    }

    @AfterClass
    public static void teardown() {
        BlockUtils.HASH_VALIDATION_ZEROES_COUNT = HASH_VALIDATION_ZEROES_COUNT_MEMORY;
    }

    @Test
    public void testCreateChain() throws ChainFormatException {
        Chain chain = ChainFactory.create(blocks);
        assertNotNull(chain);
        assertNotNull(chain.getBlocks());
    }

    @Test
    public void testCreateChainNewBlocksReference() throws ChainFormatException {
        Chain chain = ChainFactory.create(blocks);
        assertNotSame(blocks, chain.getBlocks());
    }
}
