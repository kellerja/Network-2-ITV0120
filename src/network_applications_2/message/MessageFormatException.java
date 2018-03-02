package network_applications_2.message;

public class MessageFormatException extends Exception {

    public MessageFormatException(String s, Throwable e) {
        super(s, e);
    }

    public MessageFormatException(String s) {
        super(s);
    }
}
