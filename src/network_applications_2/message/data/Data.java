package network_applications_2.message.data;

import java.io.Serializable;

public class Data implements Comparable<Data>, Serializable {

    private final String receiver;
    private final double amount;

    public Data(String receiver, double amount) {
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
    public int compareTo(Data o) {
        return getReceiver() != null && getReceiver().equals(o.getReceiver()) ?
                        getReceiver().compareTo(o.getReceiver()) :
                        Double.compare(getAmount(), o.getAmount());
    }

    public String getStorageString() {
        return receiver + ", " + amount;
    }
}
