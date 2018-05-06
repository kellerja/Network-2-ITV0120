package network_applications_2.application.message;

import network_applications_2.application.block.BlockService;
import network_applications_2.application.connection.ConnectionService;
import network_applications_2.application.utilities.Utilities;
import network_applications_2.block.BlockFormatException;
import network_applications_2.chain.ChainFormatException;
import network_applications_2.connection.Connection;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFactory;
import network_applications_2.message.MessageFormatException;
import network_applications_2.wallet.InsufficientFundsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

public class MessageService {

    public static final int MINIMUM_MESSAGES_PER_BLOCK = 5;

    private final SortedSet<Message> messages;
    private final SortedSet<Message> recentlyRemovedMessages;
    private ConnectionService connectionService;
    private BlockService blockService;

    public MessageService(ConnectionService connectionService, BlockService blockService) {
        this.connectionService = connectionService;
        this.blockService = blockService;
        messages = new TreeSet<>();
        recentlyRemovedMessages = new TreeSet<>();
        requestCurrentMessages();
    }

    public NavigableSet<Message> getMessages() {
        synchronized (messages) {
            return new TreeSet<>(messages);
        }
    }

    public void addMessage(Message message) throws MessageDeniedException, BlockFormatException, ChainFormatException, InsufficientFundsException {
        synchronized (messages) {
            if (message == null) {
                throw new MessageDeniedException("Message must not be null");
            }
            if (messages.contains(message)) {
                throw new MessageDeniedException("Message was already present");
            }
            if (recentlyRemovedMessages.contains(message)) {
                throw new MessageDeniedException("Message was already denied");
            }
            messages.add(message);
            floodMessage(message);

            if (messages.size() >= MINIMUM_MESSAGES_PER_BLOCK) {
                propagateMessages();
            }
        }
    }

    private void propagateMessages() throws InsufficientFundsException, BlockFormatException, ChainFormatException {
        synchronized (messages) {
            recentlyRemovedMessages.clear();
            try {
                blockService.addMessages(getMessages());
                recentlyRemovedMessages.addAll(messages);
                messages.clear();
            } catch (BlockFormatException | ChainFormatException | InsufficientFundsException e) {
                recentlyRemovedMessages.addAll(messages);
                messages.clear();
                e.printStackTrace();
                throw e;
            }
        }
    }

    private void floodMessage(Message message) {
        String messageBody = message.getStorageString() + "\n";
        for (Connection connection : connectionService.getConnections(true)) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/messages");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(connectionService.getApplicationPort()));

                    httpURLConnection.setDoOutput(true);
                    try (OutputStream os = httpURLConnection.getOutputStream()) {
                        os.write(messageBody.getBytes());
                    }

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        String[] data = new String(Utilities.inputStream2ByteArray(is)).split("\\R");
                        for (String line: data) {
                            if (!line.matches("^Message .*,.* saved$")) {
                                System.out.println("ERROR Message sent to " + connection.getUrl() + " failed: " + line);
                            }
                        }
                    }
                    httpURLConnection.disconnect();
                } catch (ConnectException e) {
                    connection.testConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void requestCurrentMessages() {
        for (Connection connection: connectionService.getConnections(true)) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/messages");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(connectionService.getApplicationPort()));

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        String[] data = new String(Utilities.inputStream2ByteArray(is)).split("\\R");
                        for (String line: data) {
                            if (line == null || "".equals(line.trim())) continue;
                            Message message = MessageFactory.parse(line);
                            try {
                                addMessage(message);
                            } catch (BlockFormatException | ChainFormatException | InsufficientFundsException | MessageDeniedException ignored) {
                            }
                        }
                    }
                    httpURLConnection.disconnect();
                } catch (ConnectException e) {
                    connection.testConnection();
                } catch (IOException | MessageFormatException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
