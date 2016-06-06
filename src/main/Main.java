package main;

import manager.Manager;
import pop3.PopHandlerBuilder;
import proxy.server.ProxyServer;
import proxy.utils.Configuration;

import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * Created by root on 5/27/16.
 */
public class Main {
    private  static Configuration config;
    private final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        try {

            logger.info("Reading properties.xml.");
            config = Configuration.getInstance();

            logger.info("Manager is running.");
            Runnable manager = new Manager(config);
            new Thread(manager).start();

            logger.info("Proxy server is running.");
            ProxyServer server = new ProxyServer(config, new PopHandlerBuilder());
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
