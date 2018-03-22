package network_applications_2.message;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Application;
import network_applications_2.Utilities;
import network_applications_2.block.BlockManager;
import network_applications_2.connections.Connection;
import network_applications_2.connections.ConnectionsHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class MessagesHandler implements HttpHandler {

    public static final int MINIMUM_MESSAGES_PER_BLOCK = 5;

    private static Set<Message> messages = Collections.synchronizedSet(new TreeSet<>());
    private Application application;
    private MessagesFullEvent messagesFullEvent;

    public MessagesHandler(Application application, MessagesFullEvent event) {
        this.application = application;
        this.messagesFullEvent = event;
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        StringBuilder response = new StringBuilder();
        for (Message message: messages) {
            response.append(message.getStorageString()).append("\n");
        }
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.toString().getBytes());
        }
    }

    private boolean isMessageAlreadySaved(Message message) {
        return messages.contains(message) ||
                BlockManager.getBlocks().parallelStream()
                        .anyMatch(block -> block.getMessages().contains(message));
    }

    private synchronized String buildPostResponseString(String[] messageBody, List<Message> newMessages) {
        StringBuilder response = new StringBuilder();
        for (String possibleMessage: messageBody) {
            try {
                Message message = Message.parseMessage(possibleMessage);
                response.append("Message ").append(possibleMessage).append(" saved\n");
                if(isMessageAlreadySaved(message)) {
                    continue;
                }
                messages.add(message);
                newMessages.add(message);
            } catch (MessageFormatException e) {
                response.append("Message ").append(possibleMessage).append(" malformed with error ").append(e.getMessage()).append("\n");
            }
        }
        return response.toString();
    }

    private synchronized void handlePostRequest(HttpExchange httpExchange) throws IOException {
        String[] messageBody = new String(Utilities.inputStream2ByteArray(httpExchange.getRequestBody())).split("\\R");
        List<Message> newMessages = new ArrayList<>();
        String response = buildPostResponseString(messageBody, newMessages);

        if (newMessages.size() > 0) {
            floodMessage(newMessages);
        }

        if (messagesFullEvent != null && messages.size() > MINIMUM_MESSAGES_PER_BLOCK) {
            messagesFullEvent.propagateMessages(messages);
            messages = Collections.synchronizedSet(new TreeSet<>());
        }

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println(LocalDateTime.now().toString() + " " + httpExchange.getRequestURI().getPath() + " " + httpExchange.getRequestMethod() + " by " + httpExchange.getRemoteAddress().getHostString() + ":" + ConnectionsHandler.getPort(httpExchange));
        switch (httpExchange.getRequestMethod()) {
            case "GET":
                handleGetRequest(httpExchange);
                break;
            case "POST":
                handlePostRequest(httpExchange);
                break;
            default:
                String response = "Method " + httpExchange.getRequestMethod() + " not supported for resource " + httpExchange.getRequestURI().getQuery();
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, response.length());
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
        }
        application.getConnectionsHandler().addIncomingConnection(httpExchange);
    }

    public void floodMessage(List<Message> messages) {
        StringBuilder messageBody = new StringBuilder();
        for (Message message: messages) {
            messageBody.append(message.getStorageString()).append("\n");
        }
        for (Connection connection : application.getConnectionsHandler().getAliveConnections()) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/messages");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(application.getPort()));

                    httpURLConnection.setDoOutput(true);
                    try (OutputStream os = httpURLConnection.getOutputStream()) {
                        os.write(messageBody.toString().getBytes());
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
        for (Connection connection: application.getConnectionsHandler().getAliveConnections()) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/messages");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(application.getPort()));

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        String[] data = new String(Utilities.inputStream2ByteArray(is)).split("\\R");
                        for (String line: data) {
                            if (line == null || "".equals(line.trim())) continue;
                            Message message = Message.parseMessage(line);
                            if (isMessageAlreadySaved(message)) {
                                continue;
                            }
                            messages.add(message);

                            if (messagesFullEvent != null && messages.size() > MINIMUM_MESSAGES_PER_BLOCK) {
                                messagesFullEvent.propagateMessages(messages);
                                messages = Collections.synchronizedSet(new TreeSet<>());
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
