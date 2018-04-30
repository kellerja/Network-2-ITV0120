package network_applications_2.application.connection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class PingPongHandler implements HttpHandler {

    private ConnectionService connectionService;

    public PingPongHandler(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        String query = httpExchange.getRequestURI().toASCIIString();
        String response = "pong\n";
        int responseCode = HttpURLConnection.HTTP_OK;
        if (!query.equals("/test/ping")) {
            response = "Expected '/test/ping' but received '" + httpExchange.getRequestURI().getQuery() + "'";
            responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
        }

        httpExchange.sendResponseHeaders(responseCode, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        handleGetRequest(httpExchange);
        connectionService.addIncomingConnection(httpExchange);
    }
}
