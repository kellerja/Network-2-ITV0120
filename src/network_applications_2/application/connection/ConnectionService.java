package network_applications_2.application.connection;

import com.sun.net.httpserver.HttpExchange;
import network_applications_2.application.utilities.Utilities;
import network_applications_2.connection.Connection;
import network_applications_2.connection.ConnectionFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectionService {

    private final String applicationHost;
    private final int applicationPort;
    private final List<Connection> connections;

    public ConnectionService(String host, int port) throws IOException {
        this.applicationHost = host;
        this.applicationPort = port;
        connections = Utilities.getConnectionsFromFiles(Arrays.asList(
                new File("resources/DefaultHosts.csv"),
                new File("resources/KnownHosts.csv")
        ), host, port);
    }

    public List<Connection> getConnections() {
        synchronized (connections) {
            return new ArrayList<>(connections);
        }
    }

    public List<Connection> getConnections(boolean alive) {
        return getConnections(alive, 0);
    }

    public List<Connection> getConnections(boolean alive, int limit) {
        if (limit <= 0) {
            limit = connections.size();
        }
        if (!alive) {
            return getConnections();
        }
        synchronized (connections) {
            return connections.stream().filter(Connection::isAlive).limit(limit).collect(Collectors.toList());
        }
    }

    public void addIncomingConnection(HttpExchange httpExchange) throws IOException {
        String port = Utilities.getPort(httpExchange);
        if (port.equals("")) {
            return;
        }
        Connection connection = ConnectionFactory.create("http://" + httpExchange.getRemoteAddress().getHostString() + ":" + port, applicationHost, applicationPort);
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

    public String getApplicationHost() {
        return applicationHost;
    }

    public String getSafeApplicationHost() {
        return applicationHost.matches("0:0:0:0:0:0:0:0") ? "http://localhost" : applicationHost;
    }

    public int getApplicationPort() {
        return applicationPort;
    }

    public void requestConnections(boolean isAlive, int count) {
        final String state = isAlive ? "alive" : "all";
        final String countValue = count <= 0 ? "infinity" : Integer.toString(count);
        for (Connection connection: getConnections(true)) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/connections?state=" + state + "&count=" + countValue);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(applicationPort));

                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        byte[] dataBytes = Utilities.inputStream2ByteArray(is);
                        String[] data = new String(dataBytes).split("\\R");
                        for (String line: data) {
                            Connection newConnection = ConnectionFactory.create(line, applicationHost, applicationPort);
                            if (newConnection == null || connections.contains(newConnection)) {
                                continue;
                            }
                            synchronized (connections) {
                                connections.add(newConnection);
                            }
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

}
