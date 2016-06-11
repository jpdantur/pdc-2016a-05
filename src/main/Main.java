package main;

import administrator.Manager;
import pop3.PopHandlerBuilder;
import proxy.server.ProxyServer;

import java.io.IOException;

/**
 * Created by root on 5/27/16.
 */
public class Main {
    public static void main(String[] args) {
        try {

            Runnable manager = new Manager();
            new Thread(manager).start();

            ProxyServer server = new ProxyServer(new PopHandlerBuilder());
            server.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
