package network_applications_2.message.data;

import java.math.BigDecimal;

public class Transaction extends Data {

    private final String sender;
    private final String receiver;
    private final BigDecimal amount;

    public Transaction(String sender, String receiver, BigDecimal amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    public Transaction(String sender, String receiver, double amount) {
        this(sender, receiver, BigDecimal.valueOf(amount));
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String getStorageString() {
        return String.format("%s,%s,%s", sender, receiver, amount.toString());
    }
}
