package network_applications_2.application.connection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.application.utilities.Utilities;
import network_applications_2.connection.Connection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ConnectionHandler implements HttpHandler {

    private ConnectionService connectionService;

    public ConnectionHandler(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println(LocalDateTime.now().toString() + " " + httpExchange.getRequestURI().getPath() + " " + httpExchange.getRequestMethod() + " by " + httpExchange.getRemoteAddress().getHostString() + ":" + Utilities.getPort(httpExchange));
        switch (httpExchange.getRequestMethod()) {
            case "GET":
                handleGetRequest(httpExchange);
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
        Map<String, String> query = parseQueryString(httpExchange.getRequestURI().getQuery());

        boolean isAlive = parseIsAlive(query, false);
        int count = parseCount(query, 0);

        String response = getConnectionsString(isAlive, count);
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String getConnectionsString(boolean isAlive, int count) {
        return connectionService.getConnections(isAlive, count).stream().map(Connection::getUrl)
                .reduce("", (accumulator, current) -> current + "\n" + accumulator);
    }

    private boolean parseIsAlive(Map<String, String> query, boolean isAliveDefault) {
        final String expectedKey = "state";
        boolean isAlive = isAliveDefault;
        if (!query.containsKey(expectedKey)) {
            isAlive = isAliveDefault;
        } else if (query.get(expectedKey).equals("alive")) {
            isAlive = true;
        }
        return isAlive;
    }

    private int parseCount(Map<String, String> query, int countDefault) {
        final String expectedKey = "count";
        int count;
        if (!query.containsKey(expectedKey)) {
            count = countDefault;
        } else if (query.get(expectedKey).equals("infinity")) {
            count = 0;
        } else {
            try {
                count = Math.max(Integer.parseInt(query.get(expectedKey)), 0);
            } catch (NumberFormatException e) {
                count = 0;
            }
        }
        return count;
    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> query = new HashMap<>();
        if (queryString == null) {
            return query;
        }
        String[] queryParts = queryString.split("&");
        for (String part: queryParts) {
            String[] keyValue = part.split("=");
            if (keyValue.length != 2 || keyValue[0].trim().equals("") || keyValue[1].trim().equals("")) continue;
            query.put(keyValue[0], keyValue[1]);
        }
        return query;
    }
}
