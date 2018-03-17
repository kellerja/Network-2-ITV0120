package network_applications_2.block;

public class BlockFormatException extends Exception {
    public BlockFormatException(String s, Throwable e) {
        super(s, e);
    }

    public BlockFormatException(String s) {
        super(s);
    }
}
