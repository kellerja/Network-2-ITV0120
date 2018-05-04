package network_applications_2.message.data;

import java.math.BigDecimal;

public class FreeMoney extends Data {

    private final String receiver;
    private final BigDecimal amount;

    public FreeMoney(String receiver, BigDecimal amount) {
        this.receiver = receiver;
        this.amount = amount;
    }

    public FreeMoney(String receiver, double amount) {
        this(receiver, BigDecimal.valueOf(amount));
    }

    public String getReceiver() {
        return receiver;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String getStorageString() {
        return String.format("%s,%s", receiver, amount.toString());
    }
}
