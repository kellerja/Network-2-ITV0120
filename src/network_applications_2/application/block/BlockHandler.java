package network_applications_2.application.block;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.application.chain.ChainService;
import network_applications_2.application.connection.ConnectionService;
import network_applications_2.application.utilities.Utilities;
import network_applications_2.block.Block;
import network_applications_2.block.BlockFactory;
import network_applications_2.block.BlockFormatException;
import network_applications_2.chain.Chain;
import network_applications_2.chain.ChainFactory;
import network_applications_2.chain.ChainFormatException;
import network_applications_2.connection.Connection;
import network_applications_2.message.MessageFormatException;
import network_applications_2.wallet.InsufficientFundsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlockHandler implements HttpHandler {

    private ConnectionService connectionService;
    private BlockService blockService;
    private ChainService chainService;

    public BlockHandler(ConnectionService connectionService, BlockService blockService, ChainService chainService) {
        this.connectionService = connectionService;
        this.blockService = blockService;
        this.chainService = chainService;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println(LocalDateTime.now().toString() + " " + httpExchange.getRequestURI().getPath() + " " + httpExchange.getRequestMethod() + " by " + httpExchange.getRemoteAddress().getHostString() + ":" + Utilities.getPort(httpExchange));
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
        connectionService.addIncomingConnection(httpExchange);
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
                    response = chainService.getBlocks().stream()
                            .map(Block::getStorageString).collect(Collectors.joining("\n")) + "\n";
                } else {
                    response = chainService.getBlocks(hash).stream()
                            .map(Block::getStorageString).collect(Collectors.joining("\n")) + "\n";
                }
                break;
            case "getdata":
                if (hash == null) {
                    responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
                    response = "Expected hash for /getdata/:HASH query";
                } else {
                    Block block = chainService.getBlock(hash);
                    response = block == null ? "" : block.getStorageString() + "\n";
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

    private void handlePostRequest(HttpExchange httpExchange) throws IOException {
        String[] messageBody = new String(Utilities.inputStream2ByteArray(httpExchange.getRequestBody())).split("\\R");
        StringBuilder response = new StringBuilder();
        List<Chain> chains = getChains(messageBody, response);
        int biggest = 0;
        int bigIndex = 0;
        for (int i = 0; i < chains.size(); i++) {
            Chain chain = chains.get(i);
            int temp = chainService.getBlocks(chain.getBlocks().get(0).getHash()).size();
            if (temp > biggest) {
                bigIndex = i;
                biggest = temp;
            }
        }
        if (chainService.getBlocks().size() > chains.get(bigIndex).getBlocks().size()) {
            List<Block> newBlocks = chainService.merge(chains.get(bigIndex));
            for (Block block: newBlocks) {
                blockService.floodBlock(block);
            }
        }

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.toString().getBytes());
        }
    }

    private List<Chain> getChains(String[] messageBody, StringBuilder response) {
        List<Chain> chains = new ArrayList<>();
        for (String possibleBlock : messageBody) {
            try {
                Block block = BlockFactory.parse(possibleBlock);
                response.append("Block ").append(possibleBlock).append(" saved").append("\n");
                if (block.getPreviousHash() == null || block.getPreviousHash().equals("")) {
                    Chain chain = ChainFactory.create(new ArrayList<>());
                    chain.addBlock(block);
                    chains.add(chain);
                }
                boolean added = false;
                for (Chain chain: chains) {
                    if (block.getPreviousHash().equals(chain.getBlocks().get(chain.getBlocks().size() - 1).getHash())) {
                        try {
                            added = true;
                            chain.addBlock(block);
                            break;
                        } catch (ChainFormatException ignored) {
                        }
                    }
                }
                if (!added) {
                    Chain chain = ChainFactory.create(new ArrayList<>());
                    chain.addBlock(block);
                    chains.add(chain);
                }
            } catch (InsufficientFundsException | ChainFormatException | BlockFormatException | MessageFormatException e) {
                response.append("Block ").append(possibleBlock).append(" malformed with error ").append(e.getMessage()).append("\n");
            }
        }
        return chains;
    }

    public void requestMissingBlocks() {
        for (Connection connection: connectionService.getConnections(true)) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/blocks/" + chainService.getBlocks().get(chainService.getBlocks().size() - 1));
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(connectionService.getApplicationPort()));

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = httpURLConnection.getInputStream();
                        String[] data = new String(Utilities.inputStream2ByteArray(is)).split("\\R");
                        for (String line: data) {
                            Block block = BlockFactory.parse(line);
                            try {
                                chainService.addBlock(block);
                            } catch (ChainFormatException | InsufficientFundsException ignored) {
                            }
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
