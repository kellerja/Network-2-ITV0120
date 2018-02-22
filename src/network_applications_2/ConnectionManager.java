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
        URL url = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(defaultHosts))) {
            String line;
            while ((line = reader.readLine()) != null) {
                url = new URL(line + "/test");
                if (!tryConnection(url)) {
                    url = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return url;
    }

    private URL findFromKnownHosts() {
        URL url = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(knownHosts))) {
            String line;
            while ((line = reader.readLine()) != null) {
                url = new URL(line + "/test");
                if (!tryConnection(url)) {
                    url = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return url;
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
