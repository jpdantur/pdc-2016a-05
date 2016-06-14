package proxy.server;

import administrator.Configuration;
import org.apache.log4j.Logger;
import proxy.handler.HandlerBuilder;
import proxy.handler.ProxyHandler;
import proxy.utils.*;
import java.net.InetSocketAddress;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by root on 5/27/16.
 */
public class ProxyServer implements ServerTools {
    private final static Logger logger = Logger.getLogger(ProxyServer.class);

    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private static ExecutorService workerPool;
    private static Queue<KeyModifier> keyModifierQueue;
    private HandlerBuilder handlerBuilder;

    private  static Configuration config;

    private static final int WORKER_POOL = 1;
    private static final long TIMEOUT = 10;

    public ProxyServer(HandlerBuilder handlerBuilder) throws IOException {
        config = Configuration.getInstance();
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(config.getConfiguration().getServerPort()));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        workerPool = Executors.newFixedThreadPool(WORKER_POOL);
        keyModifierQueue = new ConcurrentLinkedQueue<>();

        this.handlerBuilder = handlerBuilder;
    }

    public void run(){
        logger.info("Proxy server is running.");
        try{
            while(true){
                if(selector.select(TIMEOUT) == 0){
                    updateKeys();
                    continue;
                }
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if(!key.isValid()) continue;
                    key.interestOps(0);

                    if(WORKER_POOL == 1){
                        Worker worker = new Worker(key, this);
                        worker.run();
                    }else {
                        workerPool.execute(new Worker(key, this));
                    }
                    updateKeys();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void queue(KeyModifier keyModifier) {
        keyModifierQueue.add(keyModifier);
    }

    @Override
    public ProxyHandler getNewHandler() { return handlerBuilder.build();
    }

    private void updateKeys(){
        KeyModifier keyModifier = keyModifierQueue.poll();
        while(keyModifier != null){
            keyModifier.modifyKeys();
            keyModifier = keyModifierQueue.poll();
        }
    }
}
