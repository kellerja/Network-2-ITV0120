package network_applications_2.message;

import network_applications_2.message.data.FreeMoney;
import network_applications_2.message.data.Transaction;

public class MessageUtils {

    public static String hashMessage(long timestamp, FreeMoney data) {
        return String.format("%d,%s", timestamp, data.getStorageString());
    }

    public static String hashMessage(long timestamp, Transaction transaction) {
        return String.format("%d,%s", timestamp, transaction.getStorageString());
    }
}
