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
import java.util.stream.Collectors;

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
            response.append(message.getTimestamp()).append(",").append(message.getData()).append("\n");
        }
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.toString().getBytes());
        }
    }

    private void handlePostRequest(HttpExchange httpExchange) throws IOException {
        String[] messageBody = new String(Utilities.inputStream2ByteArray(httpExchange.getRequestBody())).split("\\R");
        StringBuilder response = new StringBuilder();
        List<Message> newMessages = new ArrayList<>();
        for (String possibleMessage: messageBody) {
            try {
                Message message = Message.parseMessage(possibleMessage);
                response.append("Message ").append(possibleMessage).append(" saved\n");
                if(messages.contains(message) ||
                        BlockManager.getBlocks().parallelStream()
                                .anyMatch(block -> block.getMessages().contains(message))) {
                    continue;
                }
                messages.add(message);
                newMessages.add(message);
            } catch (MessageFormatException e) {
                response.append("Message ").append(possibleMessage).append(" malformed with error ").append(e.getMessage()).append("\n");
            }
        }

        if (newMessages.size() > 0) {
            floodMessage(newMessages);
        }

        if (messagesFullEvent != null && messages.size() > MINIMUM_MESSAGES_PER_BLOCK) {
            messagesFullEvent.propagateMessages(messages);
            messages = Collections.synchronizedSet(new TreeSet<>());
        }

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.toString().getBytes());
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
        }
        application.getConnectionsHandler().addIncomingConnection(httpExchange);
    }

    public void floodMessage(List<Message> messages) {
        StringBuilder messageBody = new StringBuilder();
        for (Message message: messages) {
            messageBody.append(message.getTimestamp()).append(",").append(message.getData()).append("\n");
        }
        for (Connection connection : application.getConnectionsHandler().getAliveConnections()) {
            if (connection.getUrl().equals("http://" + application.getHost() + ":" + application.getPort())) {
                continue;
            }
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/messages");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(application.getPort()));

                    httpURLConnection.setDoOutput(true);
                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(messageBody.toString().getBytes());
                    os.flush();
                    os.close();

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        byte[] dataBytes = Utilities.inputStream2ByteArray(is);
                        String[] data = new String(dataBytes).split("\\R");
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
                        byte[] dataBytes = Utilities.inputStream2ByteArray(is);
                        String[] data = new String(dataBytes).split("\\R");
                        for (String line: data) {
                            if ("".equals(line)) continue;
                            Message message = Message.parseMessage(line);
                            if (messages.contains(message)) {
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
