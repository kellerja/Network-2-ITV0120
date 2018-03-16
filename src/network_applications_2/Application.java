package network_applications_2;

import com.sun.net.httpserver.HttpServer;
import network_applications_2.block.BlockHandler;
import network_applications_2.block.BlockManager;
import network_applications_2.connections.Connection;
import network_applications_2.connections.ConnectionsHandler;
import network_applications_2.connections.PingPongHandler;
import network_applications_2.message.Message;
import network_applications_2.message.MessagesHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
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
        BlockManager blockManager = new BlockManager();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test", new MyHandler()); //Maybe no need for that anymore?
        server.createContext("/test/ping", new PingPongHandler());
        server.createContext("/", new MyHandler());
        server.createContext("/messages", new MessagesHandler(this, blockManager));
        connectionsHandler = new ConnectionsHandler();
        server.createContext("/connections", connectionsHandler);
        server.createContext("/blocks", new BlockHandler());
        server.setExecutor(null);
    }

    public int getPort() {
        return port;
    }

    public ConnectionsHandler getConnectionsHandler() {
        return connectionsHandler;
    }
}
