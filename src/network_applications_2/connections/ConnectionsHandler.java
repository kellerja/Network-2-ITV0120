package network_applications_2.connections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectionsHandler implements HttpHandler {

    private List<Connection> connections;

    public List<Connection> getConnections() {
        return connections;
    }

    private String getConnectionsString(boolean isAlive, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        if (count == 0 || count > connections.size()) count = connections.size();
        for (int i = 0; i < count; i++) {
            if (!isAlive || connections.get(i).isAlive()) {
                stringBuilder.append(connections.get(i).getUrl()).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public ConnectionsHandler() {

    }

    public List<Connection> getAliveConnections() {
        return getConnections().stream().filter(Connection::isAlive).collect(Collectors.toList());
    }

    public void updateConnections() {
        try {
            connections = Utilities.getConnectionsFromFiles(Arrays.asList(
                    new File("resources/DefaultHosts.csv"),
                    new File("resources/KnownHosts.csv"))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        String query = httpExchange.getRequestURI().getQuery();
        String[] body = new String[0];
        if (query != null) {
            body = httpExchange.getRequestURI().getQuery().split("&");
        }
        boolean isAlive = false;
        int count = 0;

        for (String line : body) {
            String[] element = line.split("=");
            if (element.length != 2) return;

            if (element[0].equals("state")) {
                String state = element[1];
                if (state.equals("alive")) isAlive = true;
            }
            else if (element[0].equals("count")) {
                String value = element[1];
                if (value.equals("infinity")) count = 0; // Maybe it should not take in strings at all.
                else {
                    try {
                        count = Math.max(Integer.parseInt(value), 0);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        String response = getConnectionsString(isAlive, count);
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (httpExchange.getRequestMethod().equals("GET")) {
            handleGetRequest(httpExchange);
        }
    }

    public void requestConnections(boolean isAlive, int count) {
        String state = isAlive ? "alive" : "all";
        String countValue = count <= 0 ? "infinity" : Integer.toString(count);
        for (Connection connection: connections) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/connections");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                } catch (ConnectException e) {
                    connection.testConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
