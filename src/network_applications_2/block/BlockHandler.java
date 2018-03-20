package network_applications_2.block;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Application;
import network_applications_2.Utilities;
import network_applications_2.connections.Connection;
import network_applications_2.message.Message;
import network_applications_2.message.MessageFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

public class BlockHandler implements HttpHandler {

    private Application application;

    public BlockHandler(Application application) {
        this.application = application;
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        System.out.println(httpExchange.getRequestURI().getPath());
        StringBuilder response = new StringBuilder();
        for (Block block : BlockManager.blocks) {
            response.append(block.getHash()).append("|").append(block.getPreviousHash()).append("|").append(block.getTimestamp()).append("|");
            for (int i = 0; i < block.getMessages().size(); i++) {
                Message message = block.getMessages().get(i);
                response.append(message.getTimestamp()).append(",").append(message.getData());
                if (i != block.getMessages().size() - 1) {
                    response.append(";");
                }
            }
            response.append("\n");
        }
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.toString().getBytes());
        }
    }

    private void handlePostRequest(HttpExchange httpExchange) throws IOException {
        String[] messageBody = new String(Utilities.inputStream2ByteArray(httpExchange.getRequestBody())).split("\\R");
        StringBuilder response = new StringBuilder();
        List<Block> newBlocks = new ArrayList<>();
        for (String possibleBlock : messageBody) {
            try {
                Block block = BlockManager.parseBlock(possibleBlock);
                response.append("Block ").append(possibleBlock).append(" saved\n");
                if (BlockManager.blocks.contains(block)) {
                    continue;
                }
                BlockManager.blocks.add(block);
                newBlocks.add(block);
            } catch (BlockFormatException | MessageFormatException e) {
                response.append("Block ").append(possibleBlock).append(" malformed with error ").append(e.getMessage()).append("\n");
            }
        }
        if (newBlocks.size() > 0) {
            floodBlocks(newBlocks);
        }

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.toString().getBytes());
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        switch (httpExchange.getRequestMethod()) {
            case "GET":
                handleGetRequest(httpExchange);
                break;
            case "POST":
                handlePostRequest(httpExchange);
                break;
        }
        application.getConnectionsHandler().addIncomingConnection(httpExchange);
    }


    public void floodBlocks(List<Block> blocks) {
        StringBuilder blockBody = new StringBuilder();
        for (Block block : blocks) {
            blockBody.append(block.getHash()).append("|").append(block.getPreviousHash()).append("|").append(block.getTimestamp()).append("|");
            for (int i = 0; i < block.getMessages().size(); i++) {
                Message message = block.getMessages().get(i);
                blockBody.append(message.getTimestamp()).append(",").append(message.getData());
                if (i != block.getMessages().size() - 1) {
                    blockBody.append(";");
                }
            }
            blockBody.append("\n");
        }
        System.out.println(blockBody.toString());
        for (Connection connection : application.getConnectionsHandler().getAliveConnections()) {
            if (connection.getUrl().equals("http://" + application.getHost() + ":" + application.getPort())) {
                continue;
            }
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/blocks");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(application.getPort()));

                    httpURLConnection.setDoOutput(true);
                    OutputStream os = httpURLConnection.getOutputStream();
                    os.write(blockBody.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        byte[] dataBytes = Utilities.inputStream2ByteArray(is);
                        String[] data = new String(dataBytes).split("\\R");
                        for (String line : data) {
                            if (!line.matches("^Block .*,.* saved$")) {
                                System.out.println("ERROR Message sent to " + connection.getUrl() + " failed: " + line);
                            }
                        }
                    }
                    httpURLConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void requestMissingBlocks() {
        /* TODO: get last block and request everything after that.
            Currently since blocks are not preserved I will request all blocks */
        for (Connection connection: application.getConnectionsHandler().getAliveConnections()) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/blocks");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        byte[] dataBytes = Utilities.inputStream2ByteArray(is);
                        String[] data = new String(dataBytes).split("\\R");
                        for (String line: data) {
                            if ("".equals(line)) continue;
                            Block block = BlockManager.parseBlock(line);
                            if (BlockManager.blocks.contains(block)) {
                                continue;
                            }
                            BlockManager.blocks.add(block);
                        }
                    }
                    httpURLConnection.disconnect();
                } catch (ConnectException e) {
                    connection.testConnection();
                } catch (IOException | MessageFormatException | BlockFormatException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
