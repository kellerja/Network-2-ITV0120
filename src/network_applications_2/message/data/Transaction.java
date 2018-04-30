package network_applications_2.message.data;

public class Transaction extends Data {

    private final String sender;
    private final String receiver;
    private final double amount;

    public Transaction(String sender, String receiver, double amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String getStorageString() {
        return String.format("%s,%s,%f", sender, receiver, amount);
    }
}
