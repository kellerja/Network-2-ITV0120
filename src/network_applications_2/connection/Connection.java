package network_applications_2.connection;

import java.io.IOException;
import java.net.*;

public class Connection {
    private String url;
    private boolean alive;

    Connection(String url) {
        this.url = url;
        this.alive = testConnection();
    }

    public boolean testConnection() {
        try {
            URL url = new URL(this.url + "/test/ping");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(100);
            connection.setRequestMethod("GET");
            connection.disconnect();
            alive = connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            alive = false;
        }
        return alive;
    }

    public String getUrl() {
        return url;
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Connection && ((Connection) o).getUrl().equals(url);
    }

}
