package network_applications_2.message;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Application;
import network_applications_2.Utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.*;

public class MessagesHandler implements HttpHandler {

    private static Set<Message> messages = Collections.synchronizedSet(new TreeSet<>());
    private Application application;
    private MessagesFullEvent messagesFullEvent;

    public MessagesHandler(Application application, MessagesFullEvent event) {
        this.application = application;
        this.messagesFullEvent = event;
    }

    private void handleGetRequest(HttpExchange httpExchange) {
    }

    private void handlePostRequest(HttpExchange httpExchange) throws IOException {
        System.out.println(httpExchange.getRequestURI());
        String[] messageBody = new String(Utilities.inputStream2ByteArray(httpExchange.getRequestBody())).split("\n");
        StringBuilder response = new StringBuilder();
        List<Message> newMessages = new ArrayList<>();
        for (String possibleMessage: messageBody) {
            try {
                Message message = Message.parseMessage(possibleMessage);
                response.append("Message ").append(possibleMessage).append(" saved\n");
                if(messages.contains(message)) {
                    continue;
                }
                messages.add(message);
                newMessages.add(message);
            } catch (MessageFormatException e) {
                response.append("Message ").append(possibleMessage).append(" malformed with error ").append(e.getMessage()).append("\n");
            }
        }

        if (newMessages.size() > 0) {
            application.floodMessage(newMessages);
        }

        if (messagesFullEvent != null && messages.size() > 5) {
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
        switch (httpExchange.getRequestMethod()) {
            case "GET":
                handleGetRequest(httpExchange);
                break;
            case "POST":
                handlePostRequest(httpExchange);
                break;
        }
    }
}
