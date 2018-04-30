package network_applications_2.application;

import network_applications_2.chain.ChainFormatException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Main {

    private static final int DEFAULT_PORT = 8000;

    private static int parsePort(String[] args) {
        int port;
        if (args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                port = DEFAULT_PORT;
            }
        }
        return port;
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, ChainFormatException {
        int port = parsePort(args);
        System.out.println("Starting application on port " + port);
        new Application(port);
        System.out.println("Application started");
    }
}
