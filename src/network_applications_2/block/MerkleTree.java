package network_applications_2.block;

import network_applications_2.message.Message;
import network_applications_2.utilities.Hasher;

import java.util.*;

public class MerkleTree {

    public static String getTopHash(NavigableSet<Message> messages) {
        return getTopHash(new ArrayList<>(messages));
    }

    static String getTopHash(List<Message> messages) {
        int size = messages.size();
        if (size < 1) throw new IllegalArgumentException("Message list cannot be empty.");
        if (size < 2) {
            String message = messages.get(0).getStorageString();
            return Hasher.hash(message);
        }
        int middleIndex = size / 2;
        String message1 = getTopHash(messages.subList(0, middleIndex));
        String message2 = getTopHash(messages.subList(middleIndex, size));
        return Hasher.hash(message1 + message2);
    }
}
