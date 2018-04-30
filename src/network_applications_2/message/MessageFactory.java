package network_applications_2.message;

import network_applications_2.message.data.FreeMoney;
import network_applications_2.message.data.Transaction;
import network_applications_2.utilities.Hasher;
import network_applications_2.utilities.KeyManager;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageFactory {
    private static final Pattern FREE_MONEY_PATTERN = Pattern.compile("^(?<time>[\\d]+), ?(?<receiver>[\\w]+), ?(?<amount>[\\d]+(.[\\d]+)?), ?(?<hash>[\\w]+), ?(?<signature>[\\w]+)$");
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("^(?<time>[\\d]+), ?(?<sender>[\\w]+), ?(?<receiver>[\\w]+), ?(?<amount>[\\d]+(.[\\d]+)?), ?(?<hash>[\\w]+), ?(?<signature>[\\w]+)$");
    private static KeyManager keyManager;

    static {
        try {
            keyManager = new KeyManager();
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public static Message parse(String possibleMessage) throws MessageFormatException {
        Matcher matcher = TRANSACTION_PATTERN.matcher(possibleMessage);
        if (matcher.matches()) {
            return parseTransaction(matcher);
        }
        matcher = FREE_MONEY_PATTERN.matcher(possibleMessage);
        if (matcher.matches()) {
            return parseFreeMoney(matcher);
        }
        throw new MessageFormatException("Message did not match any expected format");
    }

    private static Message<FreeMoney> parseFreeMoney(Matcher freeMoneyMatcher) throws MessageFormatException {
        String timestampString = freeMoneyMatcher.group("time");
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampString);
        } catch (NumberFormatException e) {
            throw new MessageFormatException(String.format("Timestamp {%s} is incorrect", timestampString), e);
        }
        String receiver = freeMoneyMatcher.group("receiver");
        String amountString = freeMoneyMatcher.group("amount");
        double amount;
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            throw new MessageFormatException(String.format("Amount {%s} is incorrect", amountString), e);
        }
        String hash = freeMoneyMatcher.group("hash");
        String signature = freeMoneyMatcher.group("signature");
        FreeMoney freeMoney = new FreeMoney(receiver, amount);
        String hashMessage = MessageUtils.hashMessage(timestamp, freeMoney);
        String validateHash = Hasher.hash(hashMessage);
        if (!validateHash.equals(hash)) {
            throw new MessageFormatException(String.format("Supplied hash {%s} is incorrect. Was expecting {%s}", hash, validateHash));
        }
        if (!KeyManager.validate(hashMessage, signature, receiver)) {
            throw new MessageFormatException(String.format("Message signature {%s} was incorrect", signature));
        }
        return new Message<>(timestamp, freeMoney, hash, signature);
    }

    private static Message<Transaction> parseTransaction(Matcher transactionMatcher) throws MessageFormatException {
        String timestampString = transactionMatcher.group("time");
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampString);
        } catch (NumberFormatException e) {
            throw new MessageFormatException(String.format("Timestamp {%s} is incorrect", timestampString), e);
        }
        String sender = transactionMatcher.group("sender");
        String receiver = transactionMatcher.group("receiver");
        String amountString = transactionMatcher.group("amount");
        double amount;
        try {
            amount = Double.parseDouble(amountString);
        } catch (NumberFormatException e) {
            throw new MessageFormatException(String.format("Amount {%s} is incorrect", amountString), e);
        }
        String hash = transactionMatcher.group("hash");
        String signature = transactionMatcher.group("signature");
        Transaction transaction = new Transaction(sender, receiver, amount);
        String hashMessage = MessageUtils.hashMessage(timestamp, transaction);
        String validateHash = Hasher.hash(hashMessage);
        if (!validateHash.equals(hash)) {
            throw new MessageFormatException(String.format("Supplied hash {%s} is incorrect. Was expecting {%s}", hash, validateHash));
        }
        if (!KeyManager.validate(hashMessage, signature, sender)) {
            throw new MessageFormatException(String.format("Message signature {%s} was incorrect", signature));
        }
        return new Message<>(timestamp, transaction, hash, signature);
    }

    public static Message<Transaction> create(String receiver, double amount) throws MessageFormatException {
        return create(keyManager.getKeys(), receiver, amount);
    }

    public static Message<FreeMoney> create(double amount) throws MessageFormatException {
        return create(keyManager.getKeys(), amount);
    }

    public static Message<Transaction> create(KeyPair sender, String receiver, double amount) throws MessageFormatException {
        if (receiver == null) {
            throw new MessageFormatException("Receiver must not be null");
        }
        if (amount <= 0) {
            throw new MessageFormatException("Amount must be positive");
        }
        try {
            long timestamp = Instant.now().toEpochMilli();
            Transaction data = new Transaction(KeyManager.convertToString(sender.getPublic()), receiver, amount);
            String hashMessage = MessageUtils.hashMessage(timestamp, data);
            String hash = Hasher.hash(hashMessage);
            String signature = KeyManager.sign(sender.getPrivate(), hashMessage);
            return new Message<>(timestamp, data, hash, signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new MessageFormatException("Message creation failed", e);
        }
    }

    public static Message<FreeMoney> create(KeyPair receiver, double amount) throws MessageFormatException {
        if (amount <= 0) {
            throw new MessageFormatException("Amount must be positive");
        }
        try {
            long timestamp = Instant.now().toEpochMilli();
            FreeMoney data = new FreeMoney(KeyManager.convertToString(receiver.getPublic()), amount);
            String hashMessage = MessageUtils.hashMessage(timestamp, data);
            String hash = Hasher.hash(hashMessage);
            String signature = KeyManager.sign(receiver.getPrivate(), hashMessage);
            return new Message<>(timestamp, data, hash, signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new MessageFormatException("Message creation failed");
        }
    }
}
