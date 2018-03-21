package network_applications_2;

import com.sun.net.httpserver.HttpServer;
import network_applications_2.block.BlockHandler;
import network_applications_2.block.BlockManager;
import network_applications_2.connections.Connection;
import network_applications_2.connections.ConnectionsHandler;
import network_applications_2.connections.PingPongHandler;
import network_applications_2.error.ErrorHandler;
import network_applications_2.message.MessagesHandler;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Application {

    private ConnectionsHandler connectionsHandler;
    private MessagesHandler messagesHandler;
    private BlockHandler blockHandler;
    private BlockManager blockManager;
    private HttpServer server;

    public Application(int port) throws IOException {
        setUpServer(port);
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

    private void setUpServer(int port) throws IOException {
        blockManager = new BlockManager();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test/ping", new PingPongHandler(this));
        messagesHandler = new MessagesHandler(this, blockManager);
        server.createContext("/messages", messagesHandler);
        connectionsHandler = new ConnectionsHandler(this);
        server.createContext("/connections", connectionsHandler);
        blockHandler = new BlockHandler(this);
        server.createContext("/blocks", blockHandler);
        server.createContext("/", new ErrorHandler(this));
        server.setExecutor(Executors.newCachedThreadPool());
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public String getHost() {
        return server.getAddress().getHostName().matches(".*(0:)+.*") ? "127.0.0.1" : server.getAddress().getHostName();
    }

    public ConnectionsHandler getConnectionsHandler() {
        return connectionsHandler;
    }
}
