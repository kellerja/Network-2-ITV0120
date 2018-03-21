package network_applications_2.connections;

import network_applications_2.Application;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connection {

    private String url;
    private boolean alive;

    private Connection(String url, boolean alive) {
        this.url = url;
        this.alive = alive;
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
        return o != null && o instanceof Connection && ((Connection) o).getUrl().equals(url);
    }

    public static Connection parseConnection(String urlString, Application application) {
        Connection connection = null;
        try {
            URL url = new URL(urlString + "/test/ping");
            if (url.getHost().equals("") || isSelfConnection(application.getHost(), Integer.toString(application.getPort()), url.getHost(), Integer.toString(url.getPort()))) {
                return null;
            }
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(100);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.disconnect();
            boolean alive = httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
            connection = new Connection(urlString, alive);
        } catch (IOException e) {
            connection = new Connection(urlString, false);
        }
        return connection;
    }

    private static boolean isSelfConnection(String localAddress, String localHost, String remoteAddress, String remotePort) {
        return localAddress.equals(remoteAddress) && remotePort.equals(localHost);
    }
}
