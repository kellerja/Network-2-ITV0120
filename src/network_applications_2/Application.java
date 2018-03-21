package network_applications_2;

import com.sun.net.httpserver.HttpServer;
import network_applications_2.block.BlockHandler;
import network_applications_2.block.BlockManager;
import network_applications_2.connections.Connection;
import network_applications_2.connections.ConnectionsHandler;
import network_applications_2.connections.PingPongHandler;
import network_applications_2.message.MessagesHandler;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Application {

    private String host;
    private int port;
    private ConnectionsHandler connectionsHandler;
    private MessagesHandler messagesHandler;
    private BlockHandler blockHandler;
    private BlockManager blockManager;
    private HttpServer server;

    public Application(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        setUpServer();
        server.start();
        connectionsHandler.updateConnections();
        connectionsHandler.requestConnections(true, -1);
        blockHandler.requestMissingBlocks();
        messagesHandler.requestCurrentMessages();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            for (Connection connection: connectionsHandler.getConnections()) {
                connection.testConnection();
            }
        }, 5, 5, TimeUnit.MINUTES);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            connectionsHandler.requestConnections(true, -1);
        }, 20, 20, TimeUnit.MINUTES);
    }

    private void setUpServer() throws IOException {
        blockManager = new BlockManager();
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/test", new MyHandler()); //Maybe no need for that anymore?
        server.createContext("/test/ping", new PingPongHandler(this));
        server.createContext("/", new MyHandler());
        messagesHandler = new MessagesHandler(this, blockManager);
        server.createContext("/messages", messagesHandler);
        connectionsHandler = new ConnectionsHandler(this);
        server.createContext("/connections", connectionsHandler);
        blockHandler = new BlockHandler(this);
        server.createContext("/blocks", blockHandler);
        server.createContext("/getblocks", blockHandler);
        server.createContext("/getdata", blockHandler);
        server.setExecutor(Executors.newCachedThreadPool());
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public ConnectionsHandler getConnectionsHandler() {
        return connectionsHandler;
    }
}
