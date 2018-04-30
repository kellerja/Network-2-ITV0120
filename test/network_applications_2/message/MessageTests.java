package network_applications_2.message;

import network_applications_2.message.data.Transaction;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class MessageTests {

    @Test
    public void testMessageOrderByTimestampIsCorrectUsingTreeSet() {
        Message<Transaction> message1 = new Message<>(2, new Transaction("acc1", "acc2", 12), "hash1", "acc1Sig");
        Message<Transaction> message2 = new Message<>(1, new Transaction("acc2", "acc1", 13), "hash2", "acc2Sig");
        Set<Message> messages = new TreeSet<>();
        messages.add(message1);
        messages.add(message2);
        int i = 0;
        for (Message message: messages) {
            if (i == 0) {
                assertEquals(message2, message);
            } else if (i == 1) {
                assertEquals(message1, message);
            }
            i++;
        }
    }

    @Test
    public void testMessageOrderByHashIsCorrectUsingTreeSet() {
        Message<Transaction> message1 = new Message<>(1, new Transaction("acc1", "acc2", 12), "hash1", "acc1Sig");
        Message<Transaction> message2 = new Message<>(1, new Transaction("acc2", "acc1", 13), "ahash2", "acc2Sig");
        Set<Message> messages = new TreeSet<>();
        messages.add(message1);
        messages.add(message2);
        int i = 0;
        for (Message message: messages) {
            if (i == 0) {
                assertEquals(message2, message);
            } else if (i == 1) {
                assertEquals(message1, message);
            }
            i++;
        }
    }

    @Test
    public void testMessageOrderByTimestampIsCorrect() {
        Message<Transaction> message1 = new Message<>(2, new Transaction("acc1", "acc2", 12), "hash1", "acc1Sig");
        Message<Transaction> message2 = new Message<>(1, new Transaction("acc2", "acc1", 13), "hash2", "acc2Sig");
        if (message1.compareTo(message2) <= 0) {
            fail("Order was wrong");
        }
    }

    @Test
    public void testMessageOrderByHashIsCorrect() {
        Message<Transaction> message1 = new Message<>(1, new Transaction("acc1", "acc2", 12), "hash1", "acc1Sig");
        Message<Transaction> message2 = new Message<>(1, new Transaction("acc2", "acc1", 13), "ahash2", "acc2Sig");
        if (message1.compareTo(message2) <= 0) {
            fail("Order was wrong");
        }
    }

    @Test
    public void testMessagesAreEqual() {
        Message<Transaction> message1 = new Message<>(1, new Transaction("acc1", "acc2", 12), "hash1", "acc1Sig");
        Message<Transaction> message2 = new Message<>(1, new Transaction("acc2", "acc1", 13), "hash1", "acc2Sig");
        assertEquals(0, message1.compareTo(message2));
    }
}
