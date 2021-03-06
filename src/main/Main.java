package main;

import administrator.Administrator;
import pop3.PopHandlerBuilder;
import proxy.server.ProxyServer;
import java.io.IOException;

/**
 * Created by root on 5/27/16.
 */
public class Main {
    public static void main(String[] args) {
        try {
            Runnable manager = new Administrator();
            new Thread(manager).start();

            ProxyServer server = new ProxyServer(new PopHandlerBuilder());
            server.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
