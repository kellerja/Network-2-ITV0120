package network_applications_2.connections;

import network_applications_2.Application;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class Connection {

    private String url;
    private boolean alive;

    private Connection(String url) {
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
        return o != null && o instanceof Connection && ((Connection) o).getUrl().equals(url);
    }

    public static Connection parseConnection(String urlString, Application application) {
        Connection connection = null;
        try {
            URL url = new URL(urlString + "/test/ping");
            if (url.getHost().equals("") || isSelfConnection(application.getHost(), Integer.toString(application.getPort()), url.getHost(), Integer.toString(url.getPort()))) {
                return null;
            }
            connection = new Connection(urlString);
        } catch (IOException ignored) {
        }
        return connection;
    }

    private static boolean isSelfConnection(String localHost, String localPort, String remoteAddress, String remotePort) throws SocketException {
        if (!remotePort.equals(localPort)) return false;
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
        while(interfaces.hasMoreElements()) {
            NetworkInterface anInterface = (NetworkInterface) interfaces.nextElement();
            Enumeration inetAddresses = anInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress address = (InetAddress) inetAddresses.nextElement();
                if (address.getHostAddress().equals(remoteAddress)) return true;
            }
        }
        return "127.0.0.1".equals(remoteAddress) || "localhost".equals(remoteAddress) || localHost.equals(remoteAddress);
    }
}
