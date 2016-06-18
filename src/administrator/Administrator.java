package administrator;

import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * Created by matias on 6/5/16.
 */
public class Administrator implements Runnable{
    private final static Logger logger = Logger.getLogger(Administrator.class);
    private static final int TIMEOUT = 10; // Wait timeout (milliseconds)
    private static Configuration config;
    private AdminHandler adminHandler;

    public Administrator() {
        config = Configuration.getInstance();
    }

    @Override
    public void run() {
        logger.info("Administrator is running.");
        try {
            Selector selector = Selector.open();
            ServerSocketChannel listnChannel = ServerSocketChannel.open();
            listnChannel.socket().bind(new InetSocketAddress(config.getConfiguration().getAdmin().get(0).getPort()));
            listnChannel.configureBlocking(false);
            listnChannel.register(selector, SelectionKey.OP_ACCEPT);
            TCPProtocol protocol = new AdministratorProtocol(config.getConfiguration().getBufferSize(),
                    config.getConfiguration().getAdmin().get(0));

            while (true) {
                if (selector.select(TIMEOUT) == 0) {
                    continue;
                }
                Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                while (keyIter.hasNext()) {
                    SelectionKey key = keyIter.next();
                    if (key.isAcceptable()) {
                        protocol.handleAccept(key, this);
                    }
                    if (key.isReadable()) {
                        protocol.handleRead(key, this);
                    }
                    if (key.isValid() && key.isWritable()) {
                        protocol.handleWrite(key, this);
                    }
                    keyIter.remove();
                }
            }
        } catch (IOException ioe) {
            logger.debug(ioe.getStackTrace());
            //do something
        }
    }

    public void setAdminHandler(AdminHandler ah) {
        this.adminHandler = ah;
    }

    public AdminHandler getAdminHandler() {
        return this.adminHandler;
    }
}