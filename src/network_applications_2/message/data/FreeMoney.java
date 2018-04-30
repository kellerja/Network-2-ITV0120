package network_applications_2.message.data;

public class FreeMoney extends Data {

    private final String receiver;
    private final double amount;

    public FreeMoney(String receiver, double amount) {
        this.receiver = receiver;
        this.amount = amount;
    }

    public String getReceiver() {
        return receiver;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String getStorageString() {
        return String.format("%s,%f", receiver, amount);
    }
}
