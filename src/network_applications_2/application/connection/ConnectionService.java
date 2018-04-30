package network_applications_2.application.connection;

import com.sun.net.httpserver.HttpExchange;
import network_applications_2.application.utilities.Utilities;
import network_applications_2.connection.Connection;
import network_applications_2.connection.ConnectionFactory;

import java.io.File;
import java.io.IOException;
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
                Utilities.writeConnectionToFile(connection, new File("resources/KnownHosts.csv.csv"));
            } else {
                connections.get(connectionIndex).testConnection();
            }
        }
    }

    public String getApplicationHost() {
        return applicationHost;
    }

    public int getApplicationPort() {
        return applicationPort;
    }
}
