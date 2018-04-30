package network_applications_2.application.message;

import network_applications_2.application.block.BlockService;
import network_applications_2.application.connection.ConnectionService;
import network_applications_2.block.BlockFormatException;
import network_applications_2.chain.ChainFormatException;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFactory;
import network_applications_2.message.MessageFormatException;
import network_applications_2.message.data.Transaction;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.SortedSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTests {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private ConnectionService connectionService;

    @Mock
    private BlockService blockService;

    private MessageService messageService;

    private static Message<Transaction> message;

    @BeforeClass
    public static void initialize() throws MessageFormatException {
        message = MessageFactory.create("account", 12);
    }

    @Before
    public void setup() {
        messageService = new MessageService(connectionService, blockService);
    }

    @Test
    public void testsAddMessage() throws MessageDeniedException, BlockFormatException, ChainFormatException {
        assertEquals(0, messageService.getMessages().size());
        messageService.addMessage(message);
        assertEquals(1, messageService.getMessages().size());
    }

    @Test
    public void testAddMessagesToCreateBlock() throws MessageFormatException, MessageDeniedException, BlockFormatException, ChainFormatException {
        SortedSet<Message> reference = messageService.getMessages();
        for (int i = 0; i < MessageService.MINIMUM_MESSAGES_PER_BLOCK; i++) {
            messageService.addMessage(MessageFactory.create(Double.toString(Math.random()), Math.random()));
        }
        verify(blockService).addMessages(any());
        assertEquals(reference, messageService.getMessages());
        assertEquals(0, messageService.getMessages().size());
    }

    @Test
    public void testAddMultipleOfSameMessageInstance() throws MessageDeniedException, BlockFormatException, ChainFormatException {
        messageService.addMessage(message);
        exception.expect(MessageDeniedException.class);
        messageService.addMessage(message);
    }
}
