package network_applications_2.block;

import network_applications_2.message.Message;

import java.util.SortedSet;

public class BlockUtils {

    public static int HASH_VALIDATION_ZEROES_COUNT = 4;
    static final String MESSAGES_SEPARATOR = ";";

    public static boolean isBlockHashCorrect(String hash) {
        return hash.substring(0, HASH_VALIDATION_ZEROES_COUNT).matches("^0*$");
    }

    public static String hashMessage(long timestamp, String previousHash, String messages, String nonce) {
        return String.format("%d,%s,%s,{%s}", timestamp, previousHash, nonce, messages);
    }

    public static String messagesStorageString(SortedSet<Message> messages) {
        return messages.stream().map(Message::getStorageString).reduce("", (accumulator, current) -> accumulator + MESSAGES_SEPARATOR + current).substring(1);
    }

}
