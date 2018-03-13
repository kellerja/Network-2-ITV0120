package network_applications_2.connections;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connection {

    private String url;
    private boolean alive;

    public Connection(String url) {
        this.url = url;
        alive = testConnection();
    }

    public boolean testConnection() {
        try {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(100);
            // connection.setReadTimeout(0); - Might be useful at some point
            connection.setRequestMethod("HEAD");
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

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAlive() {
        return alive;
    }

}
