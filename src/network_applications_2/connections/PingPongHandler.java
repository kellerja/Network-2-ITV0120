package network_applications_2.connections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Application;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class PingPongHandler implements HttpHandler {

    private Application application;

    public PingPongHandler(Application application) {
        this.application = application;
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        String query = httpExchange.getRequestURI().toASCIIString();
        if (!query.equals("/test/ping")) {
            httpExchange.close();
            throw new IOException("Not correct test"); //Might need a better solution.
        }

        String response = "pong";
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
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
