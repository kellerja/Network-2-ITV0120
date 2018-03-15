package network_applications_2.connections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class PingPongHandler implements HttpHandler {

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        String query = httpExchange.getRequestURI().toASCIIString();
        if (!query.equals("/test/ping")) throw new IOException("Not correct test"); //Might need a better solution.

        String response = "pong";
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        handleGetRequest(httpExchange);
    }
}
