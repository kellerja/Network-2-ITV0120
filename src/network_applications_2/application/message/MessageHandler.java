package network_applications_2.application.message;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.application.connection.ConnectionService;
import network_applications_2.application.utilities.Utilities;
import network_applications_2.block.BlockFormatException;
import network_applications_2.chain.ChainFormatException;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFactory;
import network_applications_2.message.MessageFormatException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;

public class MessageHandler implements HttpHandler {

    private ConnectionService connectionService;
    private MessageService messageService;

    public MessageHandler(ConnectionService connectionService, MessageService messageService) {
        this.connectionService = connectionService;
        this.messageService = messageService;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println(LocalDateTime.now().toString() + " " + httpExchange.getRequestURI().getPath() + " " + httpExchange.getRequestMethod() + " by " + httpExchange.getRemoteAddress().getHostString() + ":" + Utilities.getPort(httpExchange));
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
        connectionService.addIncomingConnection(httpExchange);
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        String response = messageService.getMessages().stream().map(Message::getStorageString)
                .reduce("", (accumulator, current) -> current + "\n" + accumulator);
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void handlePostRequest(HttpExchange httpExchange) throws IOException {
        String[] messageBody = new String(Utilities.inputStream2ByteArray(httpExchange.getRequestBody())).split("\\R");

        StringBuilder response = new StringBuilder();
        for (String possibleMessage: messageBody) {
            try {
                Message message = MessageFactory.parse(possibleMessage);
                messageService.addMessage(message);
                response.append(message.getStorageString()).append("\n");
            } catch (BlockFormatException | ChainFormatException | MessageFormatException | MessageDeniedException e) {
                response.append(possibleMessage).append(" failed with exception {").append(e).append("}\n");
            }
        }

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.toString().getBytes());
        }
    }

}
