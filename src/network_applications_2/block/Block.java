package network_applications_2.block;

import network_applications_2.message.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Block implements Comparable<Block>, Serializable {

    private long timestamp;
    private String previousHash = "";
    private List<Message> messages = new ArrayList<>();
    private String newHash = "";

    public Block(String previousHash) {
        this.previousHash = previousHash;
        //timestamp = System.currentTimeMillis();
    }

    public Block(String previousHash, List<Message> messages) {
        this.previousHash = previousHash;
        this.messages = messages;
        Collections.sort(messages);
        timestamp = messages.get(messages.size()-1).getTimestamp();
    }

    public Block(long timestamp, String previousHash, List<Message> messages, String newHash) {
        this.timestamp = timestamp;
        this.previousHash = previousHash;
        this.messages = messages;
        this.newHash = newHash;
    }

    public void addMessage(Message message) {
        if (!messages.contains(message)) {
            messages.add(message);
            Collections.sort(messages);
        }
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setHash(String hash) {
        newHash = hash;
    }

    public String getHash() {
        return newHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getStorageString() {
        return getHash() + "|" + getPreviousHash() + "|" + getTimestamp() + "|" +
                getMessages().stream().map(Message::getStorageString).collect(Collectors.joining(";"));
    }


    @Override
    public int compareTo(Block o) {
        if (o == null) return -1;
        int compare = Long.compare(this.timestamp, o.getTimestamp());
        if (this.newHash.equals(o.getHash())) {
            compare = 0;
        }
        return compare;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Block && compareTo((Block) o) == 0;
    }
}
