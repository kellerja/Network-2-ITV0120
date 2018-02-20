package network_applications_2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionManager {

    private String defaultHosts = "/resources/DefaultHosts.csv";
    private String knownHosts = "/resources/KnownHosts.csv";

    URL findConnection() {
        URL url = findFromDefaultHosts();
        if (url != null) return url;
        url = findFromKnownHosts();
        return url;
    }

    private URL findFromDefaultHosts() {
        try (BufferedReader reader = new BufferedReader(new FileReader(defaultHosts))) {
            String line;
            while ((line = reader.readLine()) != null) {
                URL url = new URL(line + "/test");
                if (tryConnection(url)) return url;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private URL findFromKnownHosts() {
        try (BufferedReader reader = new BufferedReader(new FileReader(knownHosts))) {
            String line;
            while ((line = reader.readLine()) != null) {
                URL url = new URL(line + "/test");
                if (tryConnection(url)) return url;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean tryConnection(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
