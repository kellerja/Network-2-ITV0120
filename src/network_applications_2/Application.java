package network_applications_2;

import com.sun.net.httpserver.HttpServer;
import network_applications_2.connections.Connection;
import network_applications_2.connections.ConnectionsHandler;
import network_applications_2.message.MessagesHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class Application {

    private int port;
    private ConnectionsHandler connectionsHandler;
    private HttpServer server;

    public Application(int port) throws IOException {
        this.port = port;
        setUpServer();
        server.start();
        connectionsHandler.updateConnections();
    }

    private void setUpServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test", new MyHandler());
        server.createContext("/", new MyHandler());
        server.createContext("/messages", new MessagesHandler(null));
        connectionsHandler = new ConnectionsHandler();
        server.createContext("/connections", connectionsHandler);
        server.setExecutor(null);
    }

    public int getPort() {
        return port;
    }

    public List<Connection> getConnections() {
        return connectionsHandler.getConnections();
    }

}
