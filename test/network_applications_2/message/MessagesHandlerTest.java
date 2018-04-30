package network_applications_2.message;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import network_applications_2.connections.Connection;
import network_applications_2.connections.ConnectionsHandler;
import network_applications_2.message.data.FreeMoney;
import network_applications_2.message.data.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessagesHandlerTest {

    private MessagesHandler messagesHandler;

    @Mock
    private ConnectionsHandler connectionsHandler;

    @Mock
    private MessagesFullEvent messagesFullEvent;

    @Mock
    private HttpExchange httpExchange;

    private BufferedReader reader;

    @Before
    public void setUp() {
        messagesHandler = new MessagesHandler(connectionsHandler, 9000, messagesFullEvent);
    }

    @Test
    public void propagateMessagesTest() throws IOException {
        softSetup();

        when(httpExchange.getRequestMethod()).thenReturn("POST");
        String messages = "1234567, signature123, ACCOUNT_FREE_MONEY, 120\n" +
                "1233221, signature123, ACCOUNT_FREE_MONEY, ACCOUNT_2, 30.44\n" +
                "1245223, signature222, ACCOUNT_2, ACCOUNT_3, 2.000034\n" +
                "345632, signature123, ACCOUNT_FREE_MONEY, ACCOUNT_3, 55.342\n" +
                "4234653, signature333, ACCOUNT_3, ACCOUNT_2, 0.00001\n" +
                "2342623, signature222, ACCOUNT_2, ACCOUNT_4, 10.5\n";
        InputStream inputStream = new ByteArrayInputStream(messages.getBytes());
        when(httpExchange.getRequestBody()).thenReturn(inputStream);
        messagesHandler.handle(httpExchange);
        verify(messagesFullEvent).propagateMessages(anySet());
    }

    @Test
    public void floodMessagesTest() throws IOException {
        softSetup();
        List<Message> messages = Arrays.asList(
                new Message<>(12324, new Transaction("account1", "account2", 44.23), "sig1"),
                new Message<>(523421, new FreeMoney("account1", 66), "sig1")
        );
        Connection connection = Mockito.mock(Connection.class);
        when(connection.getUrl()).thenReturn("http://localhost");
        when(connectionsHandler.getAliveConnections()).thenReturn(Collections.singletonList(connection));
        messagesHandler.floodMessage(messages);
        // TODO: test does nothing
    }

    private void softSetup() throws IOException {
        when(httpExchange.getRequestURI()).thenReturn(URI.create("messages"));
        when(httpExchange.getRemoteAddress()).thenReturn(InetSocketAddress.createUnresolved("192.168.34.77", 45789));
        when(httpExchange.getRequestHeaders()).thenReturn(new Headers());
        PipedInputStream pipeInput = new PipedInputStream();
        reader = new BufferedReader(new InputStreamReader(pipeInput));
        BufferedOutputStream out = new BufferedOutputStream(new PipedOutputStream(pipeInput));
        when(httpExchange.getResponseBody()).thenReturn(out);
    }
}
