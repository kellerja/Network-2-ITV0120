package network_applications_2.connections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Utilities;
import network_applications_2.message.MessagesFullEvent;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
        String[] body =
                new String(Utilities.inputStream2ByteArray(httpExchange.getRequestBody())).split("\n");
        boolean isAlive = false;
        int count = 1;

        for (String line : body) {
            String[] element = line.split(",");
            if (element.length != 2) return;

            if (element[0].equals("state")) {
                String state = element[1];
                if (state.equals("alive")) isAlive = true;
            }
            else if (element[0].equals("count")) {
                String value = element[1];
                if (value.equals("infinity")) count = 0;
                else count = Math.max(Integer.parseInt(value), 0);
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
}
