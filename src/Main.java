import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

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
    }

    static class MyHandler implements HttpHandler {

        private void handleRequest(HttpExchange t) throws IOException {
            System.out.println(t.getRequestURI().toASCIIString());
            System.out.println(t.getRequestMethod());
            for (String header: t.getRequestHeaders().keySet()) {
                System.out.println(header + ": " + t.getRequestHeaders().get(header));
            }
            InputStream is = t.getRequestBody();
            String data = new String(inputStream2ByteArray(is));
            System.out.println(data);
        }

        private byte[] inputStream2ByteArray(InputStream is) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();

            return buffer.toByteArray();
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
