package network_applications_2.application.chain;

import network_applications_2.block.Block;
import network_applications_2.block.BlockFactory;
import network_applications_2.block.BlockFormatException;
import network_applications_2.block.BlockUtils;
import network_applications_2.chain.Chain;
import network_applications_2.chain.ChainFactory;
import network_applications_2.chain.ChainFormatException;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFactory;
import network_applications_2.message.MessageFormatException;
import network_applications_2.message.data.Transaction;
import network_applications_2.utilities.KeyGenerator;
import network_applications_2.utilities.KeyManager;
import network_applications_2.wallet.InsufficientFundsException;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.security.KeyPair;
import java.util.*;

import static org.junit.Assert.*;

public class ChainServiceTests {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static int HASH_VALIDATION_ZEROES_COUNT_MEMORY;
    private static ArrayList<Block> blocks;
    private static ArrayList<KeyPair> users;

    private ChainService chainService;

    @BeforeClass
    public static void initialize() throws BlockFormatException, MessageFormatException, ChainFormatException {
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
            NavigableSet<Message> messages = generateMessages(j);
            Block block = BlockFactory.create(prevHash, messages);
            blocks.add(block);
            prevHash = block.getHash();
        }
    }

    private static NavigableSet<Message> generateMessages(int j) throws MessageFormatException {
        NavigableSet<Message> messages = new TreeSet<>();
        messages.add(MessageFactory.create(users.get(j), 1000));
        for (int i = 1; i < 10; i++) {
            messages.add(MessageFactory.create(users.get(j + i - 1),
                    KeyManager.convertToString(users.get(j + i).getPublic()),
                    1000 - 100 * i));
        }
        return messages;
    }

    @AfterClass
    public static void teardown() {
        BlockUtils.HASH_VALIDATION_ZEROES_COUNT = HASH_VALIDATION_ZEROES_COUNT_MEMORY;
    }

    @Before
    public void setup() throws ChainFormatException {
        chainService = new ChainService(null);
    }

    @Test
    public void testAddBlock() throws ChainFormatException, InsufficientFundsException {
        assertEquals(0, chainService.getBlocks().size());
        chainService.addBlock(blocks.get(0));
        assertEquals(1, chainService.getBlocks().size());
    }

    @Test
    public void testGetBlocksFromHash() throws ChainFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        Block middle = blocks.get(blocks.size() / 2);
        List<Block> resultBlocks = chainService.getBlocks(middle.getHash());
        assertEquals(blocks.size() / 2, resultBlocks.size());
    }

    @Test
    public void testGetBlockByHash() throws ChainFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        Block target = blocks.get(blocks.size() / 2);
        Block result = chainService.getBlock(target.getHash());
        assertEquals(target, result);
    }

    @Test
    public void testGetBlockByHashNonexistentHashReturnsNull() throws ChainFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        Block result = chainService.getBlock("AAAAA");
        assertNull(result);
    }

    @Test
    public void testGetBlocksFromHashNonexistentHashReturnsEmptyList() throws ChainFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        List<Block> blocks = chainService.getBlocks("AAAAA");
        assertNotNull(blocks);
        assertTrue(blocks.isEmpty());
    }

    @Test
    public void testAddNewBlockUpdatesLatestHash() throws ChainFormatException, InsufficientFundsException {
        assertNull(chainService.getLatestHash());
        chainService.addBlock(blocks.get(0));
        assertEquals(blocks.get(0).getHash(), chainService.getLatestHash());
    }

    @Test
    public void testMerge() throws ChainFormatException, MessageFormatException, BlockFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        Chain chain = ChainFactory.create(blocks.subList(blocks.size() - 3, blocks.size()));
        List<Block> newBlocks = new ArrayList<>();
        String prevHash = chain.getBlocks().get(chain.getBlocks().size() - 1).getHash();
        for (int i = 0; i < 2; i++) {
            Block block = BlockFactory.create(prevHash, generateMessages(0));
            newBlocks.add(block);
            chain.addBlock(block);
            prevHash = block.getHash();
        }
        List<Block> targetNewBlocks = chainService.merge(chain);
        assertEquals(newBlocks.size(), targetNewBlocks.size());
        for (int i = 0; i < newBlocks.size(); i++) {
            assertEquals(newBlocks.get(i), targetNewBlocks.get(i));
        }
        assertEquals(newBlocks.get(newBlocks.size() - 1).getHash(), chainService.getLatestHash());
    }

    @Test
    public void testMergeNotConnectedChainsReturnsEmptyList() throws ChainFormatException, MessageFormatException, BlockFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        List<Block> newBlocks = new ArrayList<>();
        String prevHash = "asdagew";
        for (int i = 0; i < 2; i++) {
            Block block = BlockFactory.create(prevHash, generateMessages(0));
            newBlocks.add(block);
            prevHash = block.getHash();
        }
        Chain chain = ChainFactory.create(newBlocks);
        List<Block> targetNewBlocks = chainService.merge(chain);
        assertTrue(targetNewBlocks.isEmpty());
        assertEquals(blocks.size(), chainService.getBlocks().size());
    }

    @Test
    public void testMergeSameChainReturnsEmptyList() throws ChainFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        Chain chain = ChainFactory.create(blocks);
        List<Block> targetNewBlocks = chainService.merge(chain);
        assertTrue(targetNewBlocks.isEmpty());
        assertEquals(blocks.size(), chainService.getBlocks().size());
    }

    @Test
    public void testMergeLongerChainWins() throws ChainFormatException, MessageFormatException, BlockFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        List<Block> newBlocks = new ArrayList<>();
        String prevHash = null;
        for (int i = 0; i < blocks.size() + 1; i++) {
            Block block = BlockFactory.create(prevHash, generateMessages(0));
            newBlocks.add(block);
            prevHash = block.getHash();
        }
        Chain chain = ChainFactory.create(newBlocks);
        List<Block> targetNewBlocks = chainService.merge(chain);
        assertEquals(newBlocks.size(), targetNewBlocks.size());
        assertEquals(newBlocks.size(), chainService.getBlocks().size());
        for (int i = 0 ; i < newBlocks.size(); i++) {
            assertEquals(newBlocks.get(i), targetNewBlocks.get(i));
            assertEquals(newBlocks.get(i), chainService.getBlocks().get(i));
        }
        assertEquals(newBlocks.get(newBlocks.size() - 1).getHash(), chainService.getLatestHash());
    }

    @Test
    public void testMergeSameChainWithLongerTailWins() throws ChainFormatException, MessageFormatException, BlockFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        Chain chain = ChainFactory.create(blocks);
        Block extraBlock = BlockFactory.create(chain.getBlocks().get(chain.getBlocks().size() - 1).getHash(), generateMessages(0));
        chain.addBlock(extraBlock);
        List<Block> targetNewBlocks = chainService.merge(chain);
        assertEquals(1, targetNewBlocks.size());
        assertEquals(extraBlock, targetNewBlocks.get(0));
        assertEquals(extraBlock.getHash(), chainService.getLatestHash());
    }

    @Test
    public void testMergeChainContinuation() throws ChainFormatException, MessageFormatException, BlockFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        List<Block> newBlocks = new ArrayList<>();
        String prevHash = blocks.get(blocks.size() - 1).getHash();
        for (int i = 0; i < 5; i++) {
            Block block = BlockFactory.create(prevHash, generateMessages(0));
            newBlocks.add(block);
            prevHash = block.getHash();
        }
        Chain chain = ChainFactory.create(newBlocks);
        List<Block> targetNewBlocks = chainService.merge(chain);
        assertEquals(newBlocks.size(), targetNewBlocks.size());
        for (int i = 0; i < newBlocks.size(); i++) {
            assertEquals(newBlocks.get(i), targetNewBlocks.get(i));
        }
        assertEquals(newBlocks.get(newBlocks.size() - 1).getHash(), chainService.getLatestHash());
    }

    @Test
    public void testMergeSubChainReturnsEmptyList() throws ChainFormatException, InsufficientFundsException {
        for (Block block: blocks) {
            chainService.addBlock(block);
        }
        Chain chain = ChainFactory.create(blocks.subList(2, blocks.size() - 2));
        List<Block> target = chainService.merge(chain);
        assertTrue(target.isEmpty());
        assertEquals(blocks.size(), chainService.getBlocks().size());
    }
}
