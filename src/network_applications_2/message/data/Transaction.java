package network_applications_2.message.data;

public class Transaction extends Data {

    private final String sender;

    public Transaction(String sender, String receiver, double amount) {
        super(receiver, amount);
        this.sender = sender;
    }

    @Override
    public int compareTo(Data o) {
        if (!(o instanceof Transaction)) {
            return super.compareTo(o);
        }
        Transaction otherData = (Transaction) o;
        return getSender() == null || getSender().equals(otherData.getSender()) ?
                super.compareTo(o) :
                getSender().compareTo(otherData.getSender());
    }

    public String getSender() {
        return sender;
    }

    @Override
    public String getStorageString() {
        return sender + ", " + getReceiver() + ", " + getAmount();
    }

}
