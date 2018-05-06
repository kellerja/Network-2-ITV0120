package network_applications_2.application.message;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.application.connection.ConnectionService;
import network_applications_2.application.utilities.Utilities;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFactory;
import network_applications_2.message.MessageFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

public class FreeMoneyHandler implements HttpHandler {

    private ConnectionService connectionService;

    public FreeMoneyHandler(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println(LocalDateTime.now().toString() + " " + httpExchange.getRequestURI().getPath() + " " + httpExchange.getRequestMethod() + " by " + httpExchange.getRemoteAddress().getHostString() + ":" + Utilities.getPort(httpExchange));
        switch (httpExchange.getRequestMethod()) {
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

    private void handlePostRequest(HttpExchange httpExchange) throws IOException {
        String messageBody = new String(Utilities.inputStream2ByteArray(httpExchange.getRequestBody()));

        StringBuilder response = new StringBuilder();
        URL url = new URL(connectionService.getSafeApplicationHost() + ":" + connectionService.getApplicationPort() + "/messages");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        OutputStream outputStream;
        InputStream inputStream;
        try {
            Message message = MessageFactory.create(Double.parseDouble(messageBody));
            String messageString = message.getStorageString();
            outputStream = connection.getOutputStream();
            outputStream.write(messageString.getBytes());
            inputStream = connection.getInputStream();
            response.append(new String(Utilities.inputStream2ByteArray(inputStream))).append("\n");
        } catch (MessageFormatException | NumberFormatException e) {
            response.append(messageBody).append(" failed with exception {").append(e).append("}\n");
        }
        connection.disconnect();

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.toString().getBytes());
        }
    }

}
