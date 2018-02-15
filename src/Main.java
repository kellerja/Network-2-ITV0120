import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

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
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null);
        server.start();

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
            byte[] dataBytes = inputStream2ByteArray(is);
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
            byte[] dataBytes = inputStream2ByteArray(is);
            String data = new String(dataBytes);
            System.out.println("RESPONSE " + data);
        }
    }

    private static byte[] inputStream2ByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    static class MyHandler implements HttpHandler {

        private void handleRequest(HttpExchange t) throws IOException {
            System.out.println(t.getRequestURI().toASCIIString());
            System.out.println(t.getRequestMethod());
            System.out.println(t.getRemoteAddress().getAddress().getHostAddress());
            System.out.println(t.getRemoteAddress().getPort());
            for (String header: t.getRequestHeaders().keySet()) {
                System.out.println(header + ": " + t.getRequestHeaders().get(header));
            }
            InputStream is = t.getRequestBody();
            String data = new String(inputStream2ByteArray(is));
            System.out.println("DATA " + data);
        }

        private void handleResponse(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            handleRequest(t);
            handleResponse(t);
        }
    }

}
