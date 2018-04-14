package network_applications_2;

import com.sun.net.httpserver.HttpServer;
import network_applications_2.block.BlockFormatException;
import network_applications_2.block.BlockHandler;
import network_applications_2.block.BlockManager;
import network_applications_2.connections.Connection;
import network_applications_2.connections.ConnectionsHandler;
import network_applications_2.connections.PingPongHandler;
import network_applications_2.error.ErrorHandler;
import network_applications_2.message.MessageFormatException;
import network_applications_2.message.MessagesHandler;
import network_applications_2.validation.KeyManager;

import java.io.IOException;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Application {

    private ConnectionsHandler connectionsHandler;
    private MessagesHandler messagesHandler;
    private BlockHandler blockHandler;
    private BlockManager blockManager;
    private HttpServer server;
    private KeyManager keyManager;

    public Application(int port) throws IOException, MessageFormatException, BlockFormatException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        keyManager = new KeyManager();
        setUpServer(port);
        server.start();
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

    private void setUpServer(int port) throws IOException, MessageFormatException, BlockFormatException, InvalidKeySpecException, NoSuchAlgorithmException {
        blockManager = new BlockManager();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test/ping", new PingPongHandler(this));
        messagesHandler = new MessagesHandler(this, blockManager);
        server.createContext("/messages", messagesHandler);
        connectionsHandler = new ConnectionsHandler(this);
        server.createContext("/connections", connectionsHandler);
        blockHandler = new BlockHandler(this);
        server.createContext("/blocks", blockHandler);
        server.createContext("/getdata", blockHandler);
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
        return server.getAddress().getHostName();
    }

    public ConnectionsHandler getConnectionsHandler() {
        return connectionsHandler;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }
}
