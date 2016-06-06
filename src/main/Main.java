package main;

import manager.Manager;
import pop3.PopHandlerBuilder;
import proxy.server.ProxyServer;
import proxy.utils.Configuration;

import java.io.IOException;

/**
 * Created by root on 5/27/16.
 */
public class Main {
    private  static Configuration config;

    public static void main(String[] args) {
        try {
            
            config = Configuration.getInstance();

            Runnable manager = new Manager(config);
            new Thread(manager).start();

            ProxyServer server = new ProxyServer(config, new PopHandlerBuilder());
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
