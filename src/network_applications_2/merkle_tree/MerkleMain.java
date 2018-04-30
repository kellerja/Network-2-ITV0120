package network_applications_2.merkle_tree;

import com.sun.deploy.util.StringUtils;
import network_applications_2.message.Message;
import network_applications_2.message.data.FreeMoney;
import network_applications_2.message.data.Transaction;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MerkleMain {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        List<Message> messages = Arrays.asList(new Message<>(1, new Transaction("sfs", "yui", 42), "2469E402FF63AFFD904D2E3B5E6499257726DAE1C730A73017C77E7FC24611294D08485494AF2701867F3914CFFE1719DC25128D682CC5C9DD73288A3DD3A1E9"),
                new Message<>(3, new FreeMoney("sfd", 34243.343), "8DBC1E87782234F1B83777998B1C9E11440E73C2AA9DC9A07226F3B36779F120DC8ECA8C3817D5D8FE8A3AE4B22D31F01219083D9602ECDC04DF53A38E9AD9B3"),
                new Message<>(5, new Transaction("adas", "liukyu", 343), "66DC3CD4B74F8D79D1639ABAD90CEE1748EFCAA7336C9CAA2C837D7BBB7155AFE36740E2727B9D67350B1A071F67ED4CAD2CA85325DBDB6D8C751FFD40070BEE"));
        System.out.println(MerkleTree.getTopHash(messages));
        List<String> s = messages.stream().map(Message::getStorageString).collect(Collectors.toList());
        AltTree tree = new AltTree(s);
        printTree(tree.getRoot(), tree.getHeight());
    }

    public static void printTree(AltTree.Node node, int depth) {
        if (node == null) return;
        System.out.println(node.toString() + " " + depth);
        printTree(node.left, depth - 1);
        printTree(node.right, depth - 1);
    }
}
