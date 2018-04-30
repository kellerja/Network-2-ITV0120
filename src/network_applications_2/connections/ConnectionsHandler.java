package network_applications_2.connections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Application;
import network_applications_2.utils.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ConnectionsHandler implements HttpHandler {

    private Application application;
    private final List<Connection> connections;

    public ConnectionsHandler(Application application) throws IOException {
        this.application = application;
        connections = Collections.synchronizedList(Utilities.getConnectionsFromFiles(
                Arrays.asList(
                        new File("resources/DefaultHosts.csv"),
                        new File("resources/KnownHosts.csv")
                ), application
        ));
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void addIncomingConnection(HttpExchange httpExchange) throws IOException {
        String port = getPort(httpExchange);
        if (port.equals("")) {
            return;
        }
        Connection connection = Connection.parseConnection("http://" + httpExchange.getRemoteAddress().getHostString() + ":" + port, application.getHost(), application.getPort());
        if (connection == null) {
            return;
        }
        synchronized (connections) {
            int connectionIndex = connections.indexOf(connection);
            if (connectionIndex == -1) {
                connections.add(connection);
                Utilities.writeConnectionToFile(connection, new File("resources/KnownHosts.csv"));
            } else {
                connections.get(connectionIndex).testConnection();
            }
        }
    }

    private String getConnectionsString(boolean isAlive, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        if (count == 0 || count > connections.size()) count = connections.size();
        connections.stream()
                .filter(connection -> !isAlive || connection.isAlive())
                .limit(count)
                .forEach(connection -> stringBuilder.append(connection.getUrl()).append("\n"));
        return stringBuilder.toString();
    }

    public List<Connection> getAliveConnections() {
        return getConnections().stream().filter(Connection::isAlive).collect(Collectors.toList());
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

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println(LocalDateTime.now().toString() + " " + httpExchange.getRequestURI().getPath() + " " + httpExchange.getRequestMethod() + " by " + httpExchange.getRemoteAddress().getHostString() + ":" + ConnectionsHandler.getPort(httpExchange));
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
        addIncomingConnection(httpExchange);
    }

    public void requestConnections(boolean isAlive, int count) {
        final String state = isAlive ? "alive" : "all";
        final String countValue = count <= 0 ? "infinity" : Integer.toString(count);
        for (Connection connection: connections) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/connections?state=" + state + "&count=" + countValue);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(application.getPort()));

                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        byte[] dataBytes = Utilities.inputStream2ByteArray(is);
                        String[] data = new String(dataBytes).split("\\R");
                        for (String line: data) {
                            Connection newConnection = Connection.parseConnection(line, application.getHost(), application.getPort());
                            if (newConnection == null || connections.contains(newConnection)) {
                                continue;
                            }
                            connections.add(newConnection);
                            Utilities.writeConnectionToFile(newConnection, new File("resources/KnownHosts.csv"));
                        }
                    }
                    httpURLConnection.disconnect();
                } catch (ConnectException e) {
                    connection.testConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static String getPort(HttpExchange httpExchange) {
        List<String> headers = httpExchange.getRequestHeaders().get("Port");
        String port = "";
        if (headers != null && headers.size() > 0) port = headers.get(0);
        return port;
    }
}
