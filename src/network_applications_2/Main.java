package network_applications_2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {

    private static int DEFAULT_PORT = 8000;

    public static void main(String[] args) throws IOException {
        int port;
        if (args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                port = DEFAULT_PORT;
            }
        }
        System.out.println("Starting application");
        Application application = new Application(port);
        System.out.println("Application started");
        System.out.println("Num of connections: " + application.getConnections().size());
        System.out.println("Num of connections alive: " + application.getConnections().stream().filter(Connection::isAlive).count());
        System.out.println();

        sendGetRequest();
        sendPostRequest();
    }

    private static void sendGetRequest() throws IOException {
        URL url = new URL("http://localhost:8000/test");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = connection.getResponseCode();
        System.out.println("GET ResponseCode " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = connection.getInputStream();
            byte[] dataBytes = Utilities.inputStream2ByteArray(is);
            String data = new String(dataBytes);
            System.out.println("RESPONSE " + data);
        }
    }

    private static void sendPostRequest() throws IOException {
        URL url = new URL("http://localhost:8000/test");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        os.write("{\"data\": \"value\"}".getBytes());
        os.flush();
        os.close();

        int responseCode = connection.getResponseCode();
        System.out.println("POST ResponseCode " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = connection.getInputStream();
            byte[] dataBytes = Utilities.inputStream2ByteArray(is);
            String data = new String(dataBytes);
            System.out.println("RESPONSE " + data);
        }
    }
}
