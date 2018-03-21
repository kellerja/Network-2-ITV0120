package network_applications_2;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.connections.ConnectionsHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;

class MyHandler implements HttpHandler {

    private void handleRequest(HttpExchange t) throws IOException {
        /*
        System.out.println(t.getRequestURI().toASCIIString());
        System.out.println(t.getRequestURI().getQuery());
        System.out.println(t.getRequestMethod());
        System.out.println(t.getRemoteAddress().getAddress().getHostAddress());
        System.out.println(t.getRemoteAddress().getPort());
        for (String header: t.getRequestHeaders().keySet()) {
            System.out.println(header + ": " + t.getRequestHeaders().get(header));
        }
        InputStream is = t.getRequestBody();
        String data = new String(Utilities.inputStream2ByteArray(is));
        System.out.println("DATA " + data);*/
    }

    private void handleResponse(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("HEAD")) {
            t.sendResponseHeaders(200, -1);
            t.close();
            return;
        }
        String response = "This is the response\n";
        t.sendResponseHeaders(200, response.length());
        try (OutputStream os = t.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        //System.out.println(LocalDateTime.now().toString() + " " + httpExchange.getRequestURI().getPath() + " " + httpExchange.getRequestMethod() + " by " + httpExchange.getRemoteAddress().getHostString() + ":" + ConnectionsHandler.getPort(httpExchange));
        handleRequest(httpExchange);
        handleResponse(httpExchange);
    }
}
