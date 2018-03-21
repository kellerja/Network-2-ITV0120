package network_applications_2.block;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Application;
import network_applications_2.Utilities;
import network_applications_2.connections.Connection;
import network_applications_2.connections.ConnectionsHandler;
import network_applications_2.message.MessageFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlockHandler implements HttpHandler {

    private Application application;

    public BlockHandler(Application application) {
        this.application = application;
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        Matcher matcher = Pattern.compile("^/(\\w+)(?:/?|/(\\w+)/?)$").matcher(path);
        String command = "blocks";
        String hash = null;
        if (matcher.matches()) {
            command = matcher.group(1);
            hash = matcher.group(2);
        }
        String response;
        int responseCode = HttpURLConnection.HTTP_OK;
        switch (command) {
            case "blocks":
                if (hash == null) {
                    response = getAllBlocks().stream()
                            .map(Block::getStorageString)
                            .collect(Collectors.joining("\n")) + "\n";
                } else {
                    response = getAllBlocksStartingFrom(hash).stream()
                            .map(Block::getStorageString)
                            .collect(Collectors.joining("\n")) + "\n";
                }
                break;
            case "getdata":
                if (hash == null) {
                    responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
                    response = "Expected hash for /getdata/:HASH query";
                } else {
                    response = getSpecificBlock(hash).stream()
                            .map(Block::getStorageString)
                            .collect(Collectors.joining("\n")) + "\n";
                }
                break;
            default:
                responseCode = HttpURLConnection.HTTP_NOT_FOUND;
                response = "Unexpected resource query " + path;
                break;
        }

        httpExchange.sendResponseHeaders(responseCode, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private List<Block> getAllBlocks() {
        return BlockManager.blocks;
    }

    private List<Block> getAllBlocksStartingFrom(String hash) {
        List<Block> blocks = new ArrayList<>();
        Block block = BlockManager.blocks.stream().filter(b -> hash.equals(b.getHash())).findFirst().orElse(null);
        if (block != null) {
            blocks = BlockManager.blocks.subList(BlockManager.blocks.indexOf(block), BlockManager.blocks.size());
        }
        return blocks;
    }

    private List<Block> getSpecificBlock(String hash) {
        List<Block> blocks = new ArrayList<>();
        BlockManager.blocks.stream().filter(b -> hash.equals(b.getHash())).findFirst().ifPresent(blocks::add);
        return blocks;
    }

    private void handlePostRequest(HttpExchange httpExchange) throws IOException {
        String[] messageBody = new String(Utilities.inputStream2ByteArray(httpExchange.getRequestBody())).split("\\R");
        StringBuilder response = new StringBuilder();
        List<Block> newBlocks = new ArrayList<>();
        for (String possibleBlock : messageBody) {
            try {
                Block block = BlockManager.parseBlock(possibleBlock);
                response.append("Block ").append(possibleBlock).append(" saved").append("\n");
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
        System.out.println(LocalDateTime.now().toString() + " " + httpExchange.getRequestURI().getPath() + " " + httpExchange.getRequestMethod() + " by " + httpExchange.getRemoteAddress().getHostString() + ":" + ConnectionsHandler.getPort(httpExchange));
        switch (httpExchange.getRequestMethod()) {
            case "GET":
                handleGetRequest(httpExchange);
                break;
            case "POST":
                handlePostRequest(httpExchange);
                break;
            default:
                String response = "Method " + httpExchange.getRequestMethod() + " not supported for resource " + httpExchange.getRequestURI().getQuery();
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, response.length());
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
        }
        application.getConnectionsHandler().addIncomingConnection(httpExchange);
    }


    public void floodBlocks(List<Block> blocks) {
        String blockBody = blocks.stream()
                .map(Block::getStorageString)
                .collect(Collectors.joining("\n")) + "\n";
        for (Connection connection : application.getConnectionsHandler().getAliveConnections()) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/blocks");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(application.getPort()));

                    httpURLConnection.setDoOutput(true);
                    try (OutputStream os = httpURLConnection.getOutputStream()) {
                        os.write(blockBody.getBytes());
                    }

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        String[] data = new String(Utilities.inputStream2ByteArray(is)).split("\\R");
                        for (String line : data) {
                            if (!line.matches("^Block .* saved$")) {
                                System.out.println("ERROR Message sent to " + connection.getUrl() + " failed: " + line);
                            }
                        }
                    }
                    httpURLConnection.disconnect();
                } catch (ConnectException e) {
                    connection.testConnection();
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
                    httpURLConnection.setRequestProperty("Port", Integer.toString(application.getPort()));

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        String[] data = new String(Utilities.inputStream2ByteArray(is)).split("\\R");
                        for (String line: data) {
                            Block block = BlockManager.parseBlock(line);
                            if (BlockManager.blocks.contains(block)) {
                                continue;
                            }
                            BlockManager.blocks.add(block);
                            BlockManager.writeToFile(block);
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
