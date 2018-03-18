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
        int commaIndex = possibleMessage.indexOf(",");
        if (commaIndex < 0) {
            throw new MessageFormatException("Message must have timestamp and data separated by a comma");
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(possibleMessage.substring(0, commaIndex));
        } catch (NumberFormatException e) {
            throw new MessageFormatException("Message first parameter must be a correct unix timestamp " + possibleMessage, e);
        }
        return new Message(timestamp, possibleMessage.substring(commaIndex + 1));
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getData() {
        return data;
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
}
