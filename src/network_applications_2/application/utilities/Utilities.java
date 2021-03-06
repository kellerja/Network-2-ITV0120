package network_applications_2.application.utilities;

import com.sun.net.httpserver.HttpExchange;
import network_applications_2.connection.Connection;
import network_applications_2.connection.ConnectionFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public static String getPort(HttpExchange httpExchange) {
        List<String> headers = httpExchange.getRequestHeaders().get("Port");
        String port = "";
        if (headers != null && headers.size() > 0) port = headers.get(0);
        return port;
    }

    public static byte[] inputStream2ByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    public static List<Connection> getConnectionsFromFiles(List<File> files, String host, int port) throws IOException {
        List<Connection> connections = new ArrayList<>();
        for (File file: files) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                Connection connection = ConnectionFactory.create(line, host, port);
                if (connection == null) continue;
                connections.add(connection);
            }
            reader.close();
        }
        return connections;
    }

    public static void writeConnectionToFile(Connection connection, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(connection.getUrl() + "\n");
        }
    }

}
