package network_applications_2.message;

import network_applications_2.message.data.FreeMoney;
import network_applications_2.message.data.Transaction;
import network_applications_2.utilities.KeyManager;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.*;

public class MessageFactoryTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static KeyManager keyManager;
    private static String publicKey;

    @BeforeClass
    public static void initialize() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        publicKey = "30819F300D06092A864886F70D010101050003818D00308189028181009413458E7974F42C5792BE5D54451C70825C15E94CBAE89054812C85208C5E80BB680F0F67B40A55EA9BE398CC09936D0CB68716163410995E54BFD24B03355D4D6AFA77DC5EEA272C43ADA1DC3FBE1FCE129255FF7E04A97E36A13169EAA755EEEB3312E8FE93BB8F346A4509B76D5030191CAF2D04E27050C3CADD4F6ED3AD0203010001";
        keyManager = new KeyManager();
    }

    @Test
    public void testCreateFreeMoneyMessage() throws MessageFormatException {
        Message<FreeMoney> message = MessageFactory.create(15.66);
        assertNotNull(message);
        assertEquals(15.66, message.getData().getAmount(), 10e-3);
        assertEquals(keyManager.getPublicKey(), message.getData().getReceiver());
        assertNotNull( message.getHash());
        assertNotNull(message.getSignature());
    }

    @Test(expected = MessageFormatException.class)
    public void testCreateFreeMoneyNegativeAmountThrowsError() throws MessageFormatException {
        MessageFactory.create(-12.77);
    }

    @Test(expected = MessageFormatException.class)
    public void testCreateFreeMoneyZeroAmountThrowsError() throws MessageFormatException {
        MessageFactory.create(0);
    }

    @Test
    public void testCreateTransaction() throws MessageFormatException {
        Message<Transaction> message = MessageFactory.create(publicKey, 32.44);
        assertNotNull(message);
        assertEquals(32.44, message.getData().getAmount(), 10e-3);
        assertEquals(keyManager.getPublicKey(), message.getData().getSender());
        assertEquals(publicKey, message.getData().getReceiver());
        assertNotNull( message.getHash());
        assertNotNull(message.getSignature());
    }

    @Test(expected = MessageFormatException.class)
    public void testCreateTransactionNullReceiverThrowsError() throws MessageFormatException {
        MessageFactory.create((String) null, 32.44);
    }

    @Test(expected = MessageFormatException.class)
    public void testCreateTransactionNegativeAmountThrowsError() throws MessageFormatException {
        MessageFactory.create(publicKey, -44.55);
    }

    @Test(expected = MessageFormatException.class)
    public void testCreateTransactionZeroAmountThrowsError() throws MessageFormatException {
        MessageFactory.create(publicKey, 0);
    }

    @Test
    public void testParseFreeMoney() throws MessageFormatException {
        String messageString = MessageFactory.create(33.66).getStorageString();
        Message message = MessageFactory.parse(messageString);
        assertNotNull(message);
        assertTrue(message.getData() instanceof FreeMoney);
        FreeMoney freeMoney = (FreeMoney) message.getData();
        assertEquals(keyManager.getPublicKey(), freeMoney.getReceiver());
        assertEquals(33.66, freeMoney.getAmount(), 10e-3);
        assertNotEquals(0, message.getTimestamp());
        assertNotNull(message.getHash());
        assertNotNull(message.getSignature());
        assertEquals(messageString, message.getStorageString());
    }

    @Test
    public void testParseFreeMoneySignatureIncorrectThrowsError() throws MessageFormatException {
        Message<FreeMoney> baseMessage = MessageFactory.create(12);
        thrown.expect(MessageFormatException.class);
        thrown.expectMessage("Message signature {aaccdd3311} was incorrect");
        String messageString = String.format("%d,%s,%s,%s",
                baseMessage.getTimestamp(),
                baseMessage.getData().getStorageString(),
                baseMessage.getHash(),
                "aaccdd3311");
        MessageFactory.parse(messageString);
    }

    @Test
    public void testParseTransaction() throws MessageFormatException {
        String messageString = MessageFactory.create(publicKey, 12).getStorageString();
        Message message = MessageFactory.parse(messageString);
        assertNotNull(message);
        assertTrue(message.getData() instanceof Transaction);
        Transaction transaction = (Transaction) message.getData();
        assertEquals(keyManager.getPublicKey(), transaction.getSender());
        assertEquals(publicKey, transaction.getReceiver());
        assertEquals(12, transaction.getAmount(), 10e-3);
        assertNotEquals(0, message.getTimestamp());
        assertNotNull(message.getHash());
        assertNotNull(message.getSignature());
        assertEquals(messageString, message.getStorageString());
    }

    @Test
    public void testParseTransactionWrongHashThrowsError() throws MessageFormatException {
        Message<Transaction> baseMessage = MessageFactory.create(publicKey, 12);
        thrown.expect(MessageFormatException.class);
        thrown.expectMessage("Supplied hash {AA4466BB} is incorrect. Was expecting {" + baseMessage.getHash() + "}");
        String messageString = String.format("%d,%s,%s,%s",
                baseMessage.getTimestamp(),
                baseMessage.getData().getStorageString(),
                "AA4466BB",
                baseMessage.getSignature());
        MessageFactory.parse(messageString);
    }
}
