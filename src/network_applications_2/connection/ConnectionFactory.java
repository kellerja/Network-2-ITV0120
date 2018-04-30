package network_applications_2.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

public class ConnectionFactory {

    public static Connection create(String urlString, String host, int port) {
        Connection connection = null;
        try {
            URL url = new URL(urlString + "/test/ping");
            if (url.getHost().equals("") || isSelfConnection(host, Integer.toString(port), url.getHost(), Integer.toString(url.getPort()))) {
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
