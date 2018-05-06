package network_applications_2.application;

import com.sun.net.httpserver.HttpServer;
import network_applications_2.application.block.BlockHandler;
import network_applications_2.application.block.BlockService;
import network_applications_2.application.chain.ChainService;
import network_applications_2.application.connection.ConnectionHandler;
import network_applications_2.application.connection.ConnectionService;
import network_applications_2.application.connection.PingPongHandler;
import network_applications_2.application.error.ErrorHandler;
import network_applications_2.application.message.FreeMoneyHandler;
import network_applications_2.application.message.MessageHandler;
import network_applications_2.application.message.MessageService;
import network_applications_2.application.message.TransactionHandler;
import network_applications_2.chain.ChainFormatException;
import network_applications_2.connection.Connection;
import network_applications_2.utilities.KeyManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Application {
    private HttpServer server;
    private KeyManager keyManager;

    private ConnectionService connectionService;
    private MessageService messageService;
    private BlockService blockService;
    private ChainService chainService;

    public Application(int port) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, ChainFormatException {
        keyManager = new KeyManager();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        setUpServices(server.getAddress().getHostName(), port);
        setUpRoutes();
        server.start();
        connectionService.requestConnections(true, -1);
        blockService.requestMissingBlocks();
        messageService.requestCurrentMessages();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            for (Connection connection: connectionService.getConnections()) {
                connection.testConnection();
            }
        }, 5, 5, TimeUnit.MINUTES);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            connectionService.requestConnections(true, -1);
        }, 10, 10, TimeUnit.MINUTES);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            blockService.requestMissingBlocks();
        }, 10, 10, TimeUnit.MINUTES);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            messageService.requestCurrentMessages();
        }, 10, 10, TimeUnit.MINUTES);
    }

    private void setUpRoutes() {
        server.createContext("/test/ping", new PingPongHandler(connectionService));
        server.createContext("/connections", new ConnectionHandler(connectionService));
        server.createContext("/messages", new MessageHandler(connectionService, messageService));
        server.createContext("/messages/transaction", new TransactionHandler(connectionService));
        server.createContext("/messages/freemoney", new FreeMoneyHandler(connectionService));
        BlockHandler blockHandler = new BlockHandler(connectionService, blockService, chainService);
        server.createContext("/blocks", blockHandler);
        server.createContext("/getdata", blockHandler);
        server.createContext("/", new ErrorHandler());
    }

    private void setUpServices(String host, int port) throws IOException, ChainFormatException {
        connectionService = new ConnectionService(host, port);
        chainService = new ChainService(new File(ChainService.CHAIN_FILE));
        blockService = new BlockService(connectionService, chainService);
        messageService = new MessageService(connectionService, blockService);
    }

}
