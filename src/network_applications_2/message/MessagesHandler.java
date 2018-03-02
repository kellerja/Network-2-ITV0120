package network_applications_2.message;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class MessagesHandler implements HttpHandler {

    private List<Message> messages = new ArrayList<>();
    private MessagesFullEvent messagesFullEvent;

    public MessagesHandler(MessagesFullEvent event) {
        this.messagesFullEvent = event;
    }

    private void handleGetRequest(HttpExchange httpExchange) {
    }

    private void handlePostRequest(HttpExchange httpExchange) throws IOException {
        String[] messageBody = new String(Utilities.inputStream2ByteArray(httpExchange.getRequestBody())).split("\n");
        StringBuilder response = new StringBuilder();
        for (String possibleMessage: messageBody) {
            try {
                messages.add(Message.parseMessage(possibleMessage));
                response.append("Message ").append(possibleMessage).append(" saved\n");
            } catch (MessageFormatException e) {
                response.append("Message ").append(possibleMessage).append(" malformed with error ").append(e.getMessage()).append("\n");
            }
        }

        if (messagesFullEvent != null && messages.size() > 0) {
            messagesFullEvent.propagateMessages(messages);
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
