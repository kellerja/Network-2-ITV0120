package network_applications_2.application.block;

import network_applications_2.application.chain.ChainService;
import network_applications_2.application.connection.ConnectionService;
import network_applications_2.application.utilities.Utilities;
import network_applications_2.block.Block;
import network_applications_2.block.BlockFactory;
import network_applications_2.block.BlockFormatException;
import network_applications_2.chain.ChainFormatException;
import network_applications_2.connection.Connection;
import network_applications_2.message.Message;
import network_applications_2.message.data.FreeMoney;
import network_applications_2.wallet.InsufficientFundsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.SortedSet;

public class BlockService {

    public static final int MAXIMUM_FREE_MONEY_MESSAGES_PER_BLOCK = 1;

    private ConnectionService connectionService;
    private ChainService chainService;

    public BlockService(ConnectionService connectionService, ChainService chainService) {
        this.connectionService = connectionService;
        this.chainService = chainService;
    }

    public void addMessages(SortedSet<Message> messages) throws BlockFormatException, ChainFormatException, InsufficientFundsException {
        Block block = BlockFactory.create(chainService.getLatestHash(), messages);
        long freeMoneyMessageCount = block.getMessages().stream().filter(m -> m.getData() instanceof FreeMoney).count();
        if (freeMoneyMessageCount > MAXIMUM_FREE_MONEY_MESSAGES_PER_BLOCK) {
            throw new BlockFormatException(String.format("Too many FreeMoney messages. Was %d but limit is %d", freeMoneyMessageCount, MAXIMUM_FREE_MONEY_MESSAGES_PER_BLOCK));
        }
        chainService.addBlock(block);
        floodBlock(block);
    }

    public void floodBlock(Block block) {
        String blockBody = block.getStorageString() + "\n";
        for (Connection connection : connectionService.getConnections(true)) {
            new Thread(() -> {
                try {
                    URL url = new URL(connection.getUrl() + "/blocks");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Port", Integer.toString(connectionService.getApplicationPort()));

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
}
