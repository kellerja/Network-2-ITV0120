package network_applications_2.message;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message implements Comparable<Message>, Serializable {

    private final long timestamp;
    private final DataType dataType;
    private final Data data;
    private final String signature;
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("(?<time>[\\d]+), ?(?<sender>[\\w]+) -> (?<receiver>[\\w]+) - (?<amount>[\\d]+.[\\d]+) TambergCoin");
    private static final Pattern FREE_MONEY_PATTERN = Pattern.compile("(?<time>[\\d]+), ?(?<receiver>[\\w]+), ?(?<amount>[\\d]+[.,][\\d]+) TambergCoin");

    public Message(long timestamp, Data data, DataType dataType, String signature) {
        this.timestamp = timestamp;
        this.data = data;
        this.dataType = dataType;
        this.signature = signature;
    }

    public static Message parseMessage(String possibleMessage) throws MessageFormatException {
        Matcher transactionMatcher = TRANSACTION_PATTERN.matcher(possibleMessage);
        Matcher freeMoneyMatcher = FREE_MONEY_PATTERN.matcher(possibleMessage);
        Message message;
        if (transactionMatcher.matches()) {
            if (transactionMatcher.groupCount() != 4) {
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
            message = new Message(timestamp, new Data(receiver, amount), DataType.TRANSACTION, sender);
        } else if (freeMoneyMatcher.matches()) {
            if (freeMoneyMatcher.groupCount() != 3) {
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
            message = new Message(timestamp, new Data(receiver, amount), DataType.FREE, null);
        } else {
            throw new MessageFormatException("Message invalid " + possibleMessage);
        }
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Data getData() {
        return data;
    }

    public String getStorageString() {
        switch (dataType) {
            case TRANSACTION:
                return getTimestamp() + ", " + getSignature() + " -> " + getData().getReceiver() + " - " + getData().getAmount() + " TambergCoin";
            case FREE:
                return getTimestamp() + ", " + getData().getReceiver() + ", " + getData().getAmount() + " TambergCoin";
        }
        return toString();
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

    public DataType getDataType() {
        return dataType;
    }
}
