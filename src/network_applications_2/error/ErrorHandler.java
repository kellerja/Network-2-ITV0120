package network_applications_2.error;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import network_applications_2.Application;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class ErrorHandler implements HttpHandler {

    public ErrorHandler(Application application) {
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = getErrorResponseBody(httpExchange);
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.getBytes().length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String getErrorResponseBody(HttpExchange httpExchange) {
        return "Requested resource " + httpExchange.getRequestURI().getQuery() + " not found!\n";
    }
}
