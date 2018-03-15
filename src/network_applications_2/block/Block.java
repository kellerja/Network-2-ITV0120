package network_applications_2.block;

import network_applications_2.message.Message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Block implements Serializable {

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
        //timestamp = System.currentTimeMillis();
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
}
