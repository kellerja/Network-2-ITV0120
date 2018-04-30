package network_applications_2.block;

import network_applications_2.message.Message;

import java.util.Set;
import java.util.SortedSet;

public class Block {

    private final long timestamp;
    private final String previousHash;
    private final SortedSet<Message> messages;
    private final String hash;
    private final String nonce;

    Block(long timestamp, String previousHash, SortedSet<Message> messages, String hash, String nonce) {
        this.timestamp = timestamp;
        this.previousHash = previousHash;
        this.messages = messages;
        this.hash = hash;
        this.nonce = nonce;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public String getHash() {
        return hash;
    }

    public String getNonce() {
        return nonce;
    }

    public String getStorageString() {
        return String.format("%d,%s,%s,%s,{%s}", timestamp, previousHash, hash, nonce, BlockUtils.messagesStorageString(messages));
    }
}
