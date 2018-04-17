package network_applications_2.message;

import network_applications_2.message.data.Data;
import network_applications_2.message.data.FreeMoney;
import network_applications_2.message.data.Transaction;
import network_applications_2.validation.KeyManager;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message<T extends Data> implements Comparable<Message>, Serializable {

    private final long timestamp;
    private final T data;
    private final String signature;
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("(?<time>[\\d]+), ?(?<signature>[\\w]+), ?(?<sender>[\\w]+), ?(?<receiver>[\\w]+), ?(?<amount>[\\d]+.[\\d]+)");
    private static final Pattern FREE_MONEY_PATTERN = Pattern.compile("(?<time>[\\d]+), ?(?<signature>[\\w]+), ?(?<receiver>[\\w]+), ?(?<amount>[\\d]+[.,][\\d]+)");

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

    public static Message parseMessage(String possibleMessage) throws MessageFormatException {
        Matcher transactionMatcher = TRANSACTION_PATTERN.matcher(possibleMessage);
        Matcher freeMoneyMatcher = FREE_MONEY_PATTERN.matcher(possibleMessage);
        Message message;
        if (transactionMatcher.matches()) {
            message = parseTransactionMessage(possibleMessage, transactionMatcher);
        } else if (freeMoneyMatcher.matches()) {
            message = parseFreeMoneyMessage(possibleMessage, freeMoneyMatcher);
        } else {
            throw new MessageFormatException("Message invalid " + possibleMessage);
        }
        return message;
    }

    private static Message parseFreeMoneyMessage(String possibleMessage, Matcher freeMoneyMatcher) throws MessageFormatException {
        Message message;
        if (freeMoneyMatcher.groupCount() != 4) {
            throw new MessageFormatException("Free money query must have timestamp, receiver and amount");
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(freeMoneyMatcher.group("time"));
        } catch (NumberFormatException e) {
            throw new MessageFormatException("Transaction first parameter must be a correct unix timestamp " + possibleMessage, e);
        }
        String receiver = freeMoneyMatcher.group("receiver");
        double amount;
        try {
            amount = Double.parseDouble(freeMoneyMatcher.group("amount"));
        } catch (NumberFormatException e) {
            throw new MessageFormatException("Transaction amount format must be valid " + possibleMessage, e);
        }
        String signature = freeMoneyMatcher.group("signature");
        message = new Message<>(timestamp, new FreeMoney(receiver, amount), signature);
        return message;
    }

    private static Message parseTransactionMessage(String possibleMessage, Matcher transactionMatcher) throws MessageFormatException {
        Message message;
        if (transactionMatcher.groupCount() != 5) {
            throw new MessageFormatException("Transaction must have timestamp, sender, receiver and amount");
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(transactionMatcher.group("time"));
        } catch (NumberFormatException e) {
            throw new MessageFormatException("Transaction first parameter must be a correct unix timestamp " + possibleMessage, e);
        }
        String sender = transactionMatcher.group("sender");
        double amount;
        try {
            amount = Double.parseDouble(transactionMatcher.group("amount"));
        } catch (NumberFormatException e) {
            throw new MessageFormatException("Transaction amount format must be valid " + possibleMessage, e);
        }
        String receiver = transactionMatcher.group("receiver");
        String signature = transactionMatcher.group("signature");
        message = new Message<>(timestamp, new Transaction(sender, receiver, amount), signature);
        return message;
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
