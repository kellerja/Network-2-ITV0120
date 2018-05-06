package network_applications_2.block;

import network_applications_2.message.Message;

import java.util.NavigableSet;

public class Block {

    private final long timestamp;
    private final String previousHash;
    private final NavigableSet<Message> messages;
    private final String hash;
    private final String nonce;
    private final String merkleRootHash;

    Block(long timestamp, String previousHash, NavigableSet<Message> messages, String hash, String nonce, String merkleRootHash) {
        this.timestamp = timestamp;
        this.previousHash = previousHash;
        this.messages = messages;
        this.hash = hash;
        this.nonce = nonce;
        this.merkleRootHash = merkleRootHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public NavigableSet<Message> getMessages() {
        return messages;
    }

    public String getHash() {
        return hash;
    }

    public String getNonce() {
        return nonce;
    }

    public String getMerkleRootHash() {
        return merkleRootHash;
    }

    public String getStorageString() {
        return String.format("%d,%s,%s,%s,{%s}", timestamp, previousHash, hash, nonce, BlockUtils.messagesStorageString(messages));
    }
}
