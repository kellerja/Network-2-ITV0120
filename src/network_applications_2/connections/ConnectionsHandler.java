package network_applications_2.connections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Application;
import network_applications_2.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectionsHandler implements HttpHandler {

    private Application application;
    private List<Connection> connections;

    public ConnectionsHandler(Application application) {
        this.application = application;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void addIncomingConnection(HttpExchange httpExchange) throws IOException {
        String port = getPort(httpExchange);
        if (port.equals("") || (httpExchange.getLocalAddress().getHostString().equals(httpExchange.getRemoteAddress().getHostString()) && port.equals(Integer.toString(application.getPort())))) {
            return;
        }
        Connection connection = new Connection("http://" + httpExchange.getRemoteAddress().getHostString() + ":" + port);
        int connectionIndex = connections.indexOf(connection);
        if (connectionIndex == -1) {
            connections.add(connection);
            Utilities.writeConnectionToFile(connection, new File("resources/KnownHosts.csv"));
        } else {
            connections.get(connectionIndex).testConnection();
        }
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

    public List<Connection> getAliveConnections() {
        return getConnections().stream().filter(Connection::isAlive).collect(Collectors.toList());
    }

    public void updateConnections() {
        try {
            connections = Collections.synchronizedList(Utilities.getConnectionsFromFiles(Arrays.asList(
                    new File("resources/DefaultHosts.csv"),
                    new File("resources/KnownHosts.csv"))
            ));
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
        System.out.println(LocalDateTime.now().toString() + " " + httpExchange.getRequestURI().getPath() + " " + httpExchange.getRequestMethod() + " by " + httpExchange.getRemoteAddress().getHostString() + ":" + ConnectionsHandler.getPort(httpExchange));
        if (httpExchange.getRequestMethod().equals("GET")) {
            handleGetRequest(httpExchange);
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
                            if (line.trim().equals("")) continue;
                            Connection newConnection = new Connection(line);
                            if (connections.contains(newConnection)) {
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
