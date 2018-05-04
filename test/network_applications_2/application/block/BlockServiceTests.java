package network_applications_2.application.block;

import network_applications_2.application.chain.ChainService;
import network_applications_2.application.connection.ConnectionService;
import network_applications_2.application.message.MessageService;
import network_applications_2.block.Block;
import network_applications_2.block.BlockFormatException;
import network_applications_2.block.BlockUtils;
import network_applications_2.chain.ChainFormatException;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFactory;
import network_applications_2.message.MessageFormatException;
import network_applications_2.wallet.InsufficientFundsException;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BlockServiceTests {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private ChainService chainService;

    @Mock
    private ConnectionService connectionService;

    private BlockService blockService;

    private static SortedSet<Message> messages;

    private static int HASH_VALIDATION_ZEROES_COUNT_MEMORY;

    @BeforeClass
    public static void initialize() throws MessageFormatException {
        HASH_VALIDATION_ZEROES_COUNT_MEMORY = BlockUtils.HASH_VALIDATION_ZEROES_COUNT;
        BlockUtils.HASH_VALIDATION_ZEROES_COUNT = 1;
        messages = new TreeSet<>();
        messages.add(MessageFactory.create(100));
        for (int i = 1; i < MessageService.MINIMUM_MESSAGES_PER_BLOCK; i++) {
            messages.add(MessageFactory.create("account", Math.random()));
        }
    }

    @AfterClass
    public static void teardown() {
        BlockUtils.HASH_VALIDATION_ZEROES_COUNT = HASH_VALIDATION_ZEROES_COUNT_MEMORY;
    }

    @Before
    public void setup() {
        when(connectionService.getConnections()).thenReturn(new ArrayList<>());
        blockService = new BlockService(connectionService, chainService);
    }

    @Test
    public void testAddMessagesSendsBlockToChain() throws BlockFormatException, ChainFormatException, InsufficientFundsException {
        blockService.addMessages(messages);
        verify(chainService).addBlock(any(Block.class));
    }

    @Test
    public void testAddMessagesTooManyFreeMoneyMessagesThrowsError() throws MessageFormatException, BlockFormatException, ChainFormatException, InsufficientFundsException {
        SortedSet<Message> messages = new TreeSet<>();
        for (int i = 0; i <= BlockService.MAXIMUM_FREE_MONEY_MESSAGES_PER_BLOCK; i++) {
            messages.add(MessageFactory.create(100 + i));
        }
        for (int i = 1; i < MessageService.MINIMUM_MESSAGES_PER_BLOCK; i++) {
            messages.add(MessageFactory.create("account", Math.random()));
        }
        exception.expect(BlockFormatException.class);
        blockService.addMessages(messages);
    }

}
