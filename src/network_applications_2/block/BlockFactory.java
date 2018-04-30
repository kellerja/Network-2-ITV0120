package network_applications_2.block;

import network_applications_2.message.Message;
import network_applications_2.message.MessageFactory;
import network_applications_2.message.MessageFormatException;
import network_applications_2.utilities.Hasher;

import java.time.Instant;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockFactory {

    private static final Pattern BLOCK_PATTERN = Pattern.compile("^(?<time>[\\d]+), ?(?<prevHash>[\\w]*), ?(?<hash>[\\w]+), ?(?<nonce>[\\w]+), ?\\{(?<messages>.*)}$");

    public static Block create(String previousHash, SortedSet<Message> messages) throws BlockFormatException {
        if (messages == null || messages.isEmpty()) {
            throw new BlockFormatException("Messages must be present");
        }
        long timestamp = Instant.now().toEpochMilli();
        String messagesString = BlockUtils.messagesStorageString(messages);
        String hashMessage, nonce, hash;
        do {
            nonce = Double.toString(Math.random()).substring(2);
            hashMessage = BlockUtils.hashMessage(timestamp, previousHash, messagesString, nonce);
            hash = Hasher.hash(hashMessage);
        } while (!BlockUtils.isBlockHashCorrect(hash));
        return new Block(timestamp, previousHash, messages, hash, nonce);
    }

    public static Block parse(String possibleBlock) throws BlockFormatException, MessageFormatException {
        Matcher matcher = BLOCK_PATTERN.matcher(possibleBlock);
        if (!matcher.matches()) {
            throw new BlockFormatException(String.format("String {%s} did not match block", possibleBlock));
        }
        String timestampString = matcher.group("time");
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampString);
        } catch (NumberFormatException e) {
            throw new BlockFormatException(String.format("Timestamp {%s} is incorrect", timestampString), e);
        }
        String previousHash = matcher.group("prevHash");
        if (previousHash == null || previousHash.isEmpty() || previousHash.equals("null")) {
            previousHash = null;
        }
        String hash = matcher.group("hash");
        String nonce = matcher.group("nonce");
        SortedSet<Message> messages = parseMessages(matcher.group("messages").split(BlockUtils.MESSAGES_SEPARATOR));

        if (!BlockUtils.isBlockHashCorrect(hash)) {
            throw new BlockFormatException(String.format("Block hash work is incorrect for hash {%s}", hash));
        }

        String validationHash = Hasher.hash(BlockUtils.hashMessage(timestamp, previousHash, BlockUtils.messagesStorageString(messages), nonce));

        if (!validationHash.equals(hash)) {
            throw new BlockFormatException(String.format("Hash did not match. Got {%s} expected {%s}", hash, validationHash));
        }
        return new Block(timestamp, previousHash, messages, hash, nonce);
    }

    private static SortedSet<Message> parseMessages(String[] possibleMessages) throws MessageFormatException {
        SortedSet<Message> messages = new TreeSet<>();
        for (String possibleMessage: possibleMessages) {
            messages.add(MessageFactory.parse(possibleMessage));
        }
        return messages;
    }
}
