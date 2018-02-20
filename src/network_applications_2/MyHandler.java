package network_applications_2;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class MyHandler implements HttpHandler {

    private void handleRequest(HttpExchange t) throws IOException {
        System.out.println(t.getRequestURI().toASCIIString());
        System.out.println(t.getRequestMethod());
        System.out.println(t.getRemoteAddress().getAddress().getHostAddress());
        System.out.println(t.getRemoteAddress().getPort());
        for (String header: t.getRequestHeaders().keySet()) {
            System.out.println(header + ": " + t.getRequestHeaders().get(header));
        }
        InputStream is = t.getRequestBody();
        String data = new String(Utilities.inputStream2ByteArray(is));
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
