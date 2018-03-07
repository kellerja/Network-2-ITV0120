package network_applications_2.message;

import java.io.Serializable;

public class Message implements Comparable<Message>, Serializable {

    private final long timestamp;
    private final String data;

    public Message(long timestamp, String data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    public static Message parseMessage(String possibleMessage) throws MessageFormatException {
        String[] possibleMessageInParts = possibleMessage.split(",");
        if (possibleMessageInParts.length != 2) {
            throw new MessageFormatException("Message length must be bigger than 2");
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(possibleMessageInParts[0]);
        } catch (NumberFormatException e) {
            throw new MessageFormatException("Message first parameter must be an unix timestamp", e);
        }
        return new Message(timestamp, possibleMessageInParts[1]);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getData() {
        return data;
    }

    @Override
    public int compareTo(Message o) {
        return Long.compare(this.timestamp, o.getTimestamp());
    }
}
