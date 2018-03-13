package network_applications_2;

import com.sun.net.httpserver.HttpServer;
import network_applications_2.block.BlockHandler;
import network_applications_2.block.BlockManager;
import network_applications_2.connections.Connection;
import network_applications_2.connections.ConnectionsHandler;
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
        server.createContext("/test", new MyHandler());
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

    public List<Connection> getConnections() {
        return connectionsHandler.getConnections();
    }

    public void floodMessage(List<Message> messages) {
        StringBuilder messageBody = new StringBuilder();
        for (Message message: messages) {
            messageBody.append(message.getTimestamp()).append(",").append(message.getData()).append("\n");
        }
        for (Connection connection : connectionsHandler.getAliveConnections()) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/messages");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");

                    httpURLConnection.setDoOutput(true);
                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(messageBody.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        byte[] dataBytes = Utilities.inputStream2ByteArray(is);
                        String data = new String(dataBytes);
                        System.out.println("RESPONSE " + data); // TODO: make sure data was saved/no errors
                    }
                    httpURLConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
