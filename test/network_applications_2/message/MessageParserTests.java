package network_applications_2.message;

import network_applications_2.message.data.Transaction;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageParserTests {

    @Test
    public void parseFreeMoneyTest() throws MessageFormatException {
        String possibleMessage = "123456, signature1, account1, 12.66";
        Message message = MessageParser.parseMessage(possibleMessage);
        assertEquals(123456, message.getTimestamp());
        assertEquals("signature1", message.getSignature());
        assertEquals("account1", message.getData().getReceiver());
        assertEquals(12.66, message.getData().getAmount(), 10e-3);
    }

    @Test
    public void parseTransactionTest() throws MessageFormatException {
        String possibleMessage = "123456, signature1, account1, account2, 12.66";
        Message message = MessageParser.parseMessage(possibleMessage);
        assertEquals(123456, message.getTimestamp());
        assertEquals("signature1", message.getSignature());
        assertEquals("account2", message.getData().getReceiver());
        assertEquals(12.66, message.getData().getAmount(), 10e-3);
        assertEquals("account1", ((Transaction) message.getData()).getSender());
    }

    @Test(expected = MessageFormatException.class)
    public void paresIncorrectMessageTest() throws MessageFormatException {
        String possibleMessage = "123456, account2, 12.66";
        MessageParser.parseMessage(possibleMessage);
    }
}
