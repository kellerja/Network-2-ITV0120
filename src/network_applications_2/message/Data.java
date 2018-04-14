package network_applications_2.message;

import java.io.Serializable;

public class Data implements Comparable<Data>, Serializable {

    private final String sender;
    private final String receiver;
    private final double amount;

    public Data(String sender, String receiver, double amount) {
        this.sender = sender;
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
        return getSender() == null || getSender().equals(o.getSender()) ?
                (getReceiver() != null && getReceiver().equals(o.getReceiver()) ?
                        getReceiver().compareTo(o.getReceiver()) :
                        Double.compare(getAmount(), o.getAmount())
                ) :
                getSender().compareTo(o.getSender());
    }

    public String getSender() {
        return sender;
    }
}
