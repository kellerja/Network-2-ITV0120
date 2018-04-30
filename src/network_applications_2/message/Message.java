package network_applications_2.message;

import network_applications_2.message.data.Data;
import network_applications_2.validation.KeyManager;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class Message<T extends Data> implements Comparable<Message>, Serializable {

    private final long timestamp;
    private final T data;
    private final String signature;

    public Message(long timestamp, T data, String signature) {
        this.timestamp = timestamp;
        this.data = data;
        this.signature = signature;
    }

    public Message(long timestamp, T data, KeyManager keyManager) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        this.timestamp = timestamp;
        this.data = data;
        this.signature = keyManager.sign(getStorageString(timestamp, data));
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Data getData() {
        return data;
    }

    public String getStorageString() {
        return getTimestamp() + ", " + getSignature() + ", " + data.getStorageString();
    }

    public static String getStorageString(long timestamp, Data data) {
        return timestamp + ", " + data.getStorageString();
    }

    @Override
    public int compareTo(Message o) {
        if (o == null) return -1;
        int compare = Long.compare(this.timestamp, o.getTimestamp());
        if (compare == 0) {
            compare = this.data.compareTo(o.data);
        }
        return compare;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Message && compareTo((Message) o) == 0;
    }

    public String getSignature() {
        return signature;
    }

}
