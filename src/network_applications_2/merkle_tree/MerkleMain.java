package network_applications_2.merkle_tree;

import java.util.Arrays;
import java.util.List;

public class MerkleMain {
    public static void main(String[] args) {
        System.out.println(MerkleTree.hash("1"));

        List<String> messages = Arrays.asList("0", "1", "2", "3");
        System.out.println(MerkleTree.getTopHash(messages));
    }
}
