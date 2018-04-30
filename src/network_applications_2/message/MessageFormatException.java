package network_applications_2.message;

public class MessageFormatException extends Exception {
    public MessageFormatException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MessageFormatException(String message) {
        super(message);
    }
}
