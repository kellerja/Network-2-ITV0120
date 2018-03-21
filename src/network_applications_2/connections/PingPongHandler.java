package network_applications_2.connections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Application;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;

public class PingPongHandler implements HttpHandler {

    private Application application;

    public PingPongHandler(Application application) {
        this.application = application;
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
        application.getConnectionsHandler().addIncomingConnection(httpExchange);
    }
}
