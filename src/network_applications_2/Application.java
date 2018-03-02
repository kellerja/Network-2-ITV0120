package network_applications_2;

import com.sun.net.httpserver.HttpServer;
import network_applications_2.message.MessagesHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class Application {

    private int port;
    private List<Connection> connections;
    private HttpServer server;

    public Application(int port) throws IOException {
        this.port = port;
        setUpServer();
        server.start();
        this.connections = Utilities.getConnectionsFromFiles(Arrays.asList(
                new File("resources/DefaultHosts.csv"),
                new File("resources/KnownHosts.csv"))
        );
    }

    private void setUpServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test", new MyHandler());
        server.createContext("/", new MyHandler());
        server.createContext("/messages", new MessagesHandler(null));
        server.setExecutor(null);
    }

    public int getPort() {
        return port;
    }

    public List<Connection> getConnections() {
        return connections;
    }

}
