package network_applications_2.merkle_tree;

import java.util.List;

public class MerkleTree {

    static String getTopHash(List<String> messages) {
        int size = messages.size();
        if (size < 1) throw new IllegalArgumentException("Message list cannot be empty.");
        if (size < 2) return hash(messages.get(0));

        int middleIndex = size / 2;
        String message1 = getTopHash(messages.subList(0, middleIndex));
        String message2 = getTopHash(messages.subList(middleIndex, size));
        return hash(message1 + message2);
    }

    static String hash(String input) {
        return " Hash(" + input +") ";
    }
}
