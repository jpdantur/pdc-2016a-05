package main;

import pop3.PopHandlerBuilder;
import proxy.server.ProxyServer;

import java.io.IOException;

/**
 * Created by root on 5/27/16.
 */
public class Main {

    public static void main(String[] args) {
        try {
            ProxyServer server = new ProxyServer(7070, new PopHandlerBuilder());
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
