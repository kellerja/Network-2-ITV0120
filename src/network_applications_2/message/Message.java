package network_applications_2.message;

import network_applications_2.message.data.Data;

public class Message<T extends Data> implements Comparable<Message> {

    private long timestamp;
    private T data;
    private String hash;
    private String signature;

    Message(long timestamp, T data, String hash, String signature) {
        this.timestamp = timestamp;
        this.data = data;
        this.hash = hash;
        this.signature = signature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public T getData() {
        return data;
    }

    public String getHash() {
        return hash;
    }

    public String getSignature() {
        return signature;
    }

    public String getStorageString() {
        return String.format("%d,%s,%s,%s", timestamp, data.getStorageString(), hash, signature);
    }

    @Override
    public int compareTo(Message o) {
        int compare = Long.compare(timestamp, o.getTimestamp());
        if (compare == 0) {
            compare = hash.compareTo(o.getHash());
        }
        return compare;
    }
}
