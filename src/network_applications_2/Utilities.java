package network_applications_2;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class Utilities {
    static byte[] inputStream2ByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    static List<Connection> getConnectionsFromFiles(List<File> files) throws IOException {
        List<Connection> connections = new ArrayList<>();
        for (File file: files) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                connections.add(new Connection(line));
            }
            reader.close();
        }
        return connections;
    }
}
