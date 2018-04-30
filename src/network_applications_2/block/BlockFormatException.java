package network_applications_2.block;

public class BlockFormatException extends Exception {
    public BlockFormatException(String message) {
        super(message);
    }

    public BlockFormatException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
