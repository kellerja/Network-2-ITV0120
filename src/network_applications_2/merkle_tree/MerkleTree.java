package network_applications_2.merkle_tree;

import network_applications_2.message.Message;
import network_applications_2.utils.Hasher;

import java.util.List;

public class MerkleTree {

    static String getTopHash(List<Message> messages) {
        int size = messages.size();
        if (size < 1) throw new IllegalArgumentException("Message list cannot be empty.");
        if (size < 2) {
            String message = messages.get(0).getStorageString();
            System.out.println(Hasher.hash(message));
            return Hasher.hash(message);
        }

        int middleIndex = size / 2;
        messages.subList(0, middleIndex).forEach(m -> System.out.print(m.getStorageString() + "|"));
        System.out.print("\t\t\t");
        messages.subList(middleIndex, size).forEach(m -> System.out.print(m.getStorageString() + "|"));
        System.out.println();
        String message1 = getTopHash(messages.subList(0, middleIndex));
        String message2 = getTopHash(messages.subList(middleIndex, size));
        System.out.println(message1 + "\t\t\t" + message2);
        return Hasher.hash(message1 + message2);
    }

}
