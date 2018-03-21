package network_applications_2;

import network_applications_2.connections.Connection;

import java.io.*;

public class Main {

    private static int DEFAULT_PORT = 8000;

    private static int parsePort(String[] args) {
        int port;
        if (args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                port = DEFAULT_PORT;
            }
        }
        return port;
    }

    public static void main(String[] args) throws IOException {
        int port = parsePort(args);
        System.out.println("Starting application on port " + port);
        Application application = new Application(port);
        System.out.println("Application started");
        System.out.println("Num of connections: " + application.getConnectionsHandler().getConnections().size());
        System.out.println("Num of connections alive: " + application.getConnectionsHandler().getConnections().stream().filter(Connection::isAlive).count());
        System.out.println();
    }
}
